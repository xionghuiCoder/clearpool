package com.github.xionghuicoder.clearpool.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.PooledConnection;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.ConnectionPoolUselessConnectionException;
import com.github.xionghuicoder.clearpool.core.chain.BinaryHeap;
import com.github.xionghuicoder.clearpool.datasource.CommonConnection;
import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;

/**
 * 管理连接池。
 *
 * <p>
 * 当数据库连接不够用时会自动获取新连接
 * </p>
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConnectionPoolManager {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ConnectionPoolManager.class);

  private Lock lock = new ReentrantLock();
  private Condition notEmpty = this.lock.newCondition();

  private final BinaryHeap connectionChain = new BinaryHeap();;

  private volatile Map<ConnectionProxy, Boolean> connectionProxyMap =
      new IdentityHashMap<ConnectionProxy, Boolean>();

  private volatile boolean closed;

  private final ConfigurationVO cfgVO;

  private AtomicInteger poolSize = new AtomicInteger();

  // 数据库连接的最高峰值
  private int peakPoolSize;

  ConnectionPoolManager(ConfigurationVO cfgVO) {
    this.cfgVO = cfgVO;
  }

  void initPool() {
    this.fillPool(this.cfgVO.getCorePoolSize());
  }

  public void entryPool(ConnectionProxy conProxy) {
    if (conProxy == null) {
      throw new NullPointerException();
    }
    this.lock.lock();
    try {
      this.connectionChain.add(conProxy);
      this.notEmpty.signal();
    } finally {
      this.lock.unlock();
    }
  }

  public PooledConnection exitPool(long maxWait) throws SQLException {
    long nanos = TimeUnit.MILLISECONDS.toNanos(maxWait);
    ConnectionProxy conProxy = null;
    for (;;) {
      this.lock.lock();
      try {
        do {
          conProxy = this.connectionChain.removeFirst();
          if (conProxy == null) {
            int maxIncrement = this.cfgVO.getMaxPoolSize() - this.poolSize.get();
            if (maxIncrement == 0) {
              if (maxWait > 0) {
                // wait
                while (this.connectionChain.size() == 0) {
                  nanos = this.notEmpty.awaitNanos(nanos);
                  if (nanos <= 0) {
                    return null;
                  }
                }
              } else if (this.cfgVO.isUselessConnectionException()) {
                throw new ConnectionPoolUselessConnectionException(
                    "there is no connection left in the pool, the maxPoolSize is:"
                        + cfgVO.getMaxPoolSize());
              } else {
                // wait
                while (this.connectionChain.size() == 0) {
                  this.notEmpty.await();
                }
              }
            } else {
              int increment = this.cfgVO.getAcquireIncrement();
              if (increment > maxIncrement) {
                increment = maxIncrement;
              }
              this.fillPool(increment);
            }
          }
        } while (conProxy == null);
      } catch (InterruptedException e) {
        throw new ConnectionPoolException(e);
      } finally {
        this.lock.unlock();
      }
      if (this.cfgVO.isTestBeforeUse()) {
        boolean isValid = this.testConnection(conProxy);
        if (!isValid) {
          this.decrementPoolSize();
          this.closeConnection(conProxy);
          this.incrementOneConnection();
          continue;
        }
      }
      break;
    }
    PooledConnection pooledConnection =
        this.cfgVO.getAbstractDataSource().createPooledConnection(conProxy);
    return pooledConnection;
  }

  public ConnectionProxy exitPoolIdle(long period) {
    this.lock.lock();
    try {
      return this.connectionChain.removeIdle(period);
    } finally {
      this.lock.unlock();
    }
  }

  public void incrementOneConnection() {
    this.lock.lock();
    try {
      this.fillPool(1);
    } finally {
      this.lock.unlock();
    }
  }

  private void fillPool(int poolNum) {
    int retryTimes = this.cfgVO.getAcquireRetryTimes();
    for (int i = 0; i < poolNum; i++) {
      ConnectionProxy conProxy = this.tryGetConnection(retryTimes);
      if (this.closed) {
        this.remove();
        return;
      }
      this.connectionChain.add(conProxy);
      this.handlePeakPoolSize(i + 1);
    }
    this.poolSize.addAndGet(poolNum);
  }

  private ConnectionProxy tryGetConnection(int retryTimes) {
    int count = 0;
    CommonConnection cmnCon = null;
    do {
      try {
        cmnCon = this.cfgVO.getAbstractDataSource().getCommonConnection();
      } catch (SQLException e) {
        LOGGER.error("try connect error(" + count + " times): ", e);
        count++;
        if (count > retryTimes) {
          throw new ConnectionPoolException("get connection error" + e.getMessage());
        }
      }
    } while (cmnCon == null);
    ConnectionProxy conProxy = new ConnectionProxy(this, cmnCon);
    this.connectionProxyMap.put(conProxy, true);
    return conProxy;
  }

  public BinaryHeap getConnectionChain() {
    return this.connectionChain;
  }

  public ConfigurationVO getCfgVO() {
    return this.cfgVO;
  }

  public boolean isClosed() {
    return this.closed;
  }

  public boolean isNeedCollected() {
    return this.poolSize.get() > this.cfgVO.getCorePoolSize();
  }

  private void handlePeakPoolSize(int poolNum) {
    int size = this.poolSize.get() + poolNum;
    if (size > this.peakPoolSize) {
      this.peakPoolSize = size;
    }
  }

  public void decrementPoolSize() {
    this.poolSize.decrementAndGet();
  }

  public int getPoolSize() {
    return this.poolSize.get();
  }

  public int getPeakPoolSize() {
    return this.peakPoolSize;
  }

  public boolean testConnection(ConnectionProxy conProxy) {
    PreparedStatement queryPreparedStatement = null;
    try {
      PooledConnection pooledConnection =
          this.cfgVO.getAbstractDataSource().createPooledConnection(conProxy);
      Connection con = pooledConnection.getConnection();
      queryPreparedStatement = con.prepareStatement(this.cfgVO.getTestQuerySql());
      queryPreparedStatement.execute();
    } catch (SQLException e) {
      LOGGER.error(this.cfgVO.getTestQuerySql() + " error: ", e);
      return false;
    } finally {
      if (queryPreparedStatement != null) {
        try {
          queryPreparedStatement.close();
        } catch (SQLException e) {
          // swallow
        }
      }
    }
    return true;
  }

  public void remove() {
    this.closed = true;
    for (ConnectionProxy conProxy : this.connectionProxyMap.keySet()
        .toArray(new ConnectionProxy[0])) {
      this.closeConnection(conProxy);
    }
  }

  public void closeConnection(ConnectionProxy conProxy) {
    if (conProxy != null) {
      try {
        conProxy.getConnection().close();
      } catch (SQLException e) {
        LOGGER.error("close connection error: ", e);
      }
      this.connectionProxyMap.remove(conProxy);
    }
  }
}
