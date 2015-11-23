package org.opensource.clearpool.core.hook;

import java.util.Iterator;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

public class ShutdownHook extends CommonHook {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ShutdownHook.class);

  /**
   * start ShutdownHook by Runtime
   */
  public static void registerHook() {
    ShutdownHook shutdownHook = new ShutdownHook();
    Thread thread = new Thread(shutdownHook);
    thread.setName("ShutdownHook");
    Runtime.getRuntime().addShutdownHook(thread);
    LOGGER.info("register ShutdownHook");
  }

  /**
   * Hide the constructor
   */
  private ShutdownHook() {}

  @Override
  public void run() {
    /**
     * Removes all connection pools. close all the connections
     */
    long begin = System.currentTimeMillis();
    Iterator<ConnectionPoolManager> itr = poolChain.iterator();
    while (itr.hasNext()) {
      ConnectionPoolManager pool = itr.next();
      // the pool chain couldn't stop in for..in,but if it return null,we
      // can see that it would be stopped.
      if (pool == null) {
        break;
      }
      pool.remove();
    }
    long cost = System.currentTimeMillis() - begin;
    LOGGER.info("ShutdownHook pool end,it cost " + cost + " ms");
  }
}
