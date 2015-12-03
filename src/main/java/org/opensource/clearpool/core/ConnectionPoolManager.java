package org.opensource.clearpool.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.core.chain.BinaryHeap;
import org.opensource.clearpool.datasource.connection.CommonConnection;
import org.opensource.clearpool.datasource.factory.DataSourceAbstractFactory;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

/**
 * This class save the connection to {@link #connectionChain},it's duty is to manage the pool.
 *
 * The pool will increment when there is no free connection in the pool's size is less than the max
 * pool size.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConnectionPoolManager {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ConnectionPoolManager.class);

  private Lock lock = new ReentrantLock();
  private Condition notEmpty = lock.newCondition();

  private final BinaryHeap connectionChain = new BinaryHeap();;

  // save all the connection to close in case we shutdown the JVM
  private volatile Set<ConnectionProxy> connectionSet = new HashSet<ConnectionProxy>();

  // this is the sign of if the pool is removed
  private volatile boolean closed;

  private ConfigurationVO cfgVO;

  private AtomicInteger poolSize = new AtomicInteger();

  // record the peak of the connection in the pool.
  private int peakPoolSize;

  ConnectionPoolManager(ConfigurationVO cfgVO) {
    this.cfgVO = cfgVO;
  }

  /**
   * Init the pool by the corePoolsize of {@link #cfgVO}
   */
  void initPool() {
    int coreSize = cfgVO.getCorePoolSize();
    this.fillPool(coreSize);
    String tableName = cfgVO.getTestTableName();
    if (tableName != null) {
      this.initTestTable();
    }
  }

  /**
   * Return connection to the pool
   */
  public void entryPool(ConnectionProxy conProxy) {
    if (conProxy == null) {
      throw new NullPointerException();
    }
    lock.lock();
    try {
      connectionChain.add(conProxy);
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Get a connection from the pool
   */
  public PooledConnection exitPool() throws SQLException {
    ConnectionProxy conProxy = null;
    for (;;) {
      lock.lock();
      try {
        do {
          conProxy = connectionChain.removeFirst();
          // if we couln't get a connection from the pool,we should get
          // new
          // connection.
          if (conProxy == null) {
            int maxIncrement = cfgVO.getMaxPoolSize() - poolSize.get();
            // if pool is full,we shouldn't grow it
            if (maxIncrement == 0) {
              if (cfgVO.getUselessConnectionException()) {
                throw new ConnectionPoolException("there is no connection left in the pool");
              } else {
                // wait for connection
                while (connectionChain.size() == 0) {
                  notEmpty.await();
                }
              }
            } else {
              this.fillPoolByAcquireIncrement();
            }
          }
        } while (conProxy == null);
      } catch (InterruptedException e) {
        LOGGER.error("exitPool error: ", e);
        throw new ConnectionPoolException(e);
      } finally {
        lock.unlock();
      }
      if (cfgVO.isTestBeforeUse()) {
        boolean isValid = checkTestTable(conProxy, false);
        if (!isValid) {
          decrementPoolSize();
          closeConnection(conProxy);
          incrementOneConnection();
          continue;
        }
      }
      break;
    }
    DataSourceAbstractFactory factory = cfgVO.getFactory();
    PooledConnection pooledConnection = factory.createPooledConnection(conProxy);
    return pooledConnection;
  }

  public ConnectionProxy exitPool(long period) {
    lock.lock();
    try {
      return connectionChain.removeIdle(period);
    } finally {
      lock.unlock();
    }
  }

  /**
   * fill the pool by acquireIncrement
   */
  private void fillPoolByAcquireIncrement() {
    int maxIncrement = cfgVO.getMaxPoolSize() - poolSize.get();
    // double check
    if (maxIncrement != 0) {
      int increment = cfgVO.getAcquireIncrement();
      if (increment > maxIncrement) {
        increment = maxIncrement;
      }
      this.fillPool(increment);
    }
  }

  /**
   * increment one connection
   */
  public void incrementOneConnection() {
    lock.lock();
    try {
      this.fillPool(1);
    } finally {
      lock.unlock();
    }
  }

  /**
   * fill the pool by poolNum
   */
  private void fillPool(int poolNum) {
    int retryTimes = cfgVO.getAcquireRetryTimes();
    for (int i = 0; i < poolNum; i++) {
      // try to get a connection
      ConnectionProxy conProxy = this.tryGetConnection(retryTimes);
      if (closed) {
        this.remove();
        return;
      }
      connectionChain.add(conProxy);
      this.handlePeakPoolSize(i + 1);
    }
    poolSize.addAndGet(poolNum);
  }

  /**
   * try retryTimes times to get a connection
   */
  private ConnectionProxy tryGetConnection(int retryTimes) {
    int count = 0;
    CommonConnection cmnCon = null;
    do {
      try {
        cmnCon = cfgVO.getDataSource().getCommonConnection();
      } catch (SQLException e) {
        LOGGER.error("try connect error(" + count++ + " time): ", e);
        if (count > retryTimes) {
          throw new ConnectionPoolException("get connection error" + e.getMessage());
        }
      }
    } while (cmnCon == null);
    ConnectionProxy conProxy = new ConnectionProxy(this, cmnCon);
    connectionSet.add(conProxy);
    return conProxy;
  }

  /**
   * Init test table by testTableName in {@link #cfgVO}
   */
  private void initTestTable() {
    int coreSize = cfgVO.getCorePoolSize();
    ConnectionProxy conProxy = null;
    if (coreSize > 0) {
      conProxy = connectionChain.removeFirst();
    } else {
      int retryTimes = cfgVO.getAcquireRetryTimes();
      conProxy = this.tryGetConnection(retryTimes);
    }
    try {
      Connection con = conProxy.getConnection();
      boolean auto = con.getAutoCommit();
      con.setAutoCommit(true);
      this.checkTestTable(conProxy, true);
      con.setAutoCommit(auto);
    } catch (SQLException e) {
      LOGGER.error("initTestTable test: ", e);
      throw new ConnectionPoolException(e);
    }
    if (coreSize > 0) {
      connectionChain.add(conProxy);
    } else {
      this.closeConnection(conProxy);
    }
  }

  public BinaryHeap getConnectionChain() {
    return connectionChain;
  }

  public Set<ConnectionProxy> getConnectionSet() {
    return connectionSet;
  }

  public ConfigurationVO getCfgVO() {
    return cfgVO;
  }

  public boolean isClosed() {
    return closed;
  }

  public boolean isNeedCollected() {
    return poolSize.get() > cfgVO.getCorePoolSize();
  }

  /**
   * Save the peak pool size
   */
  private void handlePeakPoolSize(int poolNum) {
    int size = poolSize.get() + poolNum;
    if (size > peakPoolSize) {
      peakPoolSize = size;
    }
  }

  public void decrementPoolSize() {
    poolSize.decrementAndGet();
  }

  public int getPoolSize() {
    return poolSize.get();
  }

  public int getPeakPoolSize() {
    return peakPoolSize;
  }

  /**
   * check if test table existed
   */
  public boolean checkTestTable(ConnectionProxy conProxy, boolean autoCreateTable) {
    PreparedStatement queryPreparedStatement = null;
    try {
      Connection con = conProxy.getConnection();
      queryPreparedStatement = con.prepareStatement(cfgVO.getTestQuerySql());
      queryPreparedStatement.execute();
    } catch (SQLException e) {
      LOGGER.error(cfgVO.getTestQuerySql() + " error: ", e);
      if (autoCreateTable) {
        this.createTestTable(conProxy);
      } else {
        return false;
      }
    } finally {
      if (queryPreparedStatement != null) {
        try {
          queryPreparedStatement.close();
        } catch (SQLException e) {
          LOGGER.error("close queryPreparedStatement error: ", e);
        }
      }
    }
    return true;
  }

  /**
   * create test table if test table is not existed
   */
  private void createTestTable(ConnectionProxy conProxy) {
    PreparedStatement createPreparedStatement = null;
    try {
      Connection con = conProxy.getConnection();
      createPreparedStatement = con.prepareStatement(cfgVO.getTestCreateSql());
      createPreparedStatement.execute();
    } catch (SQLException e) {
      LOGGER.error("createTestTable error: ", e);
      throw new ConnectionPoolException(e);
    } finally {
      if (createPreparedStatement != null) {
        try {
          createPreparedStatement.close();
        } catch (SQLException e) {
          LOGGER.error("close createPreparedStatement error: ", e);
        }
      }
    }
  }

  /**
   * remove the connection of the free connection. note:we shouldn't close the using connection
   * because it may cause a exception when people is using it.
   */
  public void remove() {
    closed = true;
    Set<ConnectionProxy> tempSet = connectionSet;
    // help gc
    connectionSet = new HashSet<ConnectionProxy>();
    for (ConnectionProxy conProxy : tempSet) {
      this.closeConnection(conProxy);
    }
  }

  /**
   * close pool connection
   */
  public void closeConnection(ConnectionProxy conProxy) {
    if (conProxy != null) {
      try {
        conProxy.getConnection().close();
      } catch (SQLException e) {
        LOGGER.error("it cause a exception when we close a pool connection: ", e);
      }
      connectionSet.remove(conProxy);
    }
  }
}
