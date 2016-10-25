package com.github.xionghuicoder.clearpool.core.hook;

import java.util.Collection;

import com.github.xionghuicoder.clearpool.core.ConnectionPoolManager;
import com.github.xionghuicoder.clearpool.core.chain.CommonChain;
import com.github.xionghuicoder.clearpool.core.chain.LockCircleChain;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;

/**
 * common hook
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class CommonHook implements Runnable {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(CommonHook.class);

  final CommonChain<ConnectionPoolManager> poolChain = new LockCircleChain<ConnectionPoolManager>();

  public CommonHook(Collection<ConnectionPoolManager> poolCollection) {
    for (ConnectionPoolManager manager : poolCollection) {
      this.poolChain.add(manager);
    }
  }

  Thread startCommonHook() {
    Thread thread = new Thread(this);
    String name = this.getClass().getSimpleName();
    thread.setName(name);
    thread.setDaemon(true);
    thread.start();
    LOGGER.info("start " + name);
    return thread;
  }
}
