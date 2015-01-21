package org.opensource.clearpool.core.hook;

import java.util.Iterator;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.PoolLatchUtil;
import org.opensource.clearpool.util.ThreadSleepUtil;

/**
 * This class's duty is that close the connection which is expired and check if the connection is
 * valid.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class IdleCheckHook extends CommonHook {
  private static final PoolLog LOG = PoolLogFactory.getLog(IdleCheckHook.class);

  /**
   * Start IdleCheckHook
   */
  public static Thread startHook() {
    CommonHook idleCheckHook = new IdleCheckHook();
    Thread thread = idleCheckHook.startHook("IdleCheckHook");
    return thread;
  }

  /**
   * Hide the constructor
   */
  private IdleCheckHook() {}

  @Override
  public void run() {
    LOG.info("IdleCheckHook running");
    // I'm running.
    PoolLatchUtil.countDownStartLatch();
    Iterator<ConnectionPoolManager> itr = poolChain.iterator();
    while (itr.hasNext()) {
      // if pool destroyed,it will interrupt this thread.
      if (Thread.currentThread().isInterrupted()) {
        break;
      }
      ConnectionPoolManager pool = itr.next();
      if (pool == null) {
        // rest for a minute
        ThreadSleepUtil.sleep();
        continue;
      }
      if (pool.isClosed()) {
        itr.remove();
        continue;
      }
      this.dealGarbage(pool);
      this.dealIdle(pool);
    }
  }

  /**
   * Remove garbage connection if necessary.
   */
  private void dealGarbage(ConnectionPoolManager pool) {
    long period = pool.getCfgVO().getLimitIdleTime();
    while (pool.isNeedCollected()) {
      ConnectionProxy conProxy = pool.exitPool(period);
      if (conProxy == null) {
        break;
      }
      // close connection
      pool.closeConnection(conProxy);
      pool.decrementPoolSize();
    }
  }

  /**
   * Check idle connection if necessary.
   */
  private void dealIdle(ConnectionPoolManager pool) {
    ConfigurationVO cfgVO = pool.getCfgVO();
    long period = cfgVO.getKeepTestPeriod();
    // maybe some pool don't need to check
    if (period == -1) {
      return;
    }
    while (true) {
      ConnectionProxy conProxy = pool.exitPool(period);
      if (conProxy == null) {
        break;
      }
      boolean isValid = pool.checkTestTable(conProxy, false);
      if (isValid) {
        pool.entryPool(conProxy);
        continue;
      }
      pool.decrementPoolSize();
      pool.closeConnection(conProxy);
      pool.incrementOneConnection();
    }
  }
}
