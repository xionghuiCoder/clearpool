package com.github.xionghuicoder.clearpool.core.hook;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.core.ConfigurationVO;
import com.github.xionghuicoder.clearpool.core.ConnectionPoolManager;
import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;
import com.github.xionghuicoder.clearpool.util.ThreadSleepUtils;

/**
 * <p>
 * 移除超时未使用的多余连接；<br>
 * 定时检测连接是否有效，关闭无效连接，再补充有效的连接；
 * </p>
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class IdleCheckHook extends CommonHook {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(IdleCheckHook.class);

  private CountDownLatch startLatch;

  public static Thread startHook(Collection<ConnectionPoolManager> poolCollection,
      CountDownLatch startLatch) {
    CommonHook idleCheckHook = new IdleCheckHook(poolCollection, startLatch);
    Thread thread = idleCheckHook.startCommonHook();
    return thread;
  }

  private IdleCheckHook(Collection<ConnectionPoolManager> poolCollection,
      CountDownLatch startLatch) {
    super(poolCollection);
    this.startLatch = startLatch;
  }

  @Override
  public void run() {
    LOGGER.info(IdleCheckHook.class.getSimpleName() + " running");
    this.startLatch.countDown();
    // help gc
    this.startLatch = null;
    Iterator<ConnectionPoolManager> itr = this.poolChain.iterator();
    while (itr.hasNext()) {
      try {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }
        ConnectionPoolManager pool = itr.next();
        if (pool == null) {
          // 休息休息
          boolean isInterrupted = ThreadSleepUtils.sleep();
          if (isInterrupted) {
            break;
          }
          continue;
        }
        if (pool.isClosed()) {
          itr.remove();
          continue;
        }
        this.dealGarbage(pool);
        this.dealIdle(pool);
      } catch (Throwable t) {
        LOGGER.error(IdleCheckHook.class.getSimpleName() + " error: ", t);
      }
    }
  }

  private void dealGarbage(ConnectionPoolManager pool) {
    long period = pool.getCfgVO().getLimitIdleTime();
    while (pool.isNeedCollected()) {
      ConnectionProxy conProxy = pool.exitPoolIdle(period);
      if (conProxy == null) {
        break;
      }
      pool.closeConnection(conProxy);
      pool.decrementPoolSize();
    }
  }

  private void dealIdle(ConnectionPoolManager pool) {
    ConfigurationVO cfgVO = pool.getCfgVO();
    long period = cfgVO.getKeepTestPeriod();
    if (period < 0) {
      return;
    }
    while (true) {
      ConnectionProxy conProxy = pool.exitPoolIdle(period);
      if (conProxy == null) {
        break;
      }
      boolean isValid = pool.testConnection(conProxy);
      if (isValid) {
        pool.entryPool(conProxy);
        continue;
      }
      pool.decrementPoolSize();
      pool.closeConnection(conProxy);
      try {
        pool.incrementOneConnection();
      } catch (ConnectionPoolException t) {
        LOGGER.error(IdleCheckHook.class.getSimpleName() + " incrementOneConnection error: ", t);
      }
    }
  }
}
