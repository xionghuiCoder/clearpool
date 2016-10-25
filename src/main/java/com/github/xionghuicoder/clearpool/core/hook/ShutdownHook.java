package com.github.xionghuicoder.clearpool.core.hook;

import java.util.Collection;
import java.util.Iterator;

import com.github.xionghuicoder.clearpool.core.ConnectionPoolManager;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;

/**
 * 移除所有连接池，关闭所有连接
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ShutdownHook extends CommonHook {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ShutdownHook.class);

  public static void registerHook(Collection<ConnectionPoolManager> poolCollection) {
    ShutdownHook shutdownHook = new ShutdownHook(poolCollection);
    Thread thread = new Thread(shutdownHook);
    String name = ShutdownHook.class.getSimpleName();
    thread.setName(name);
    Runtime.getRuntime().addShutdownHook(thread);
    LOGGER.info("start " + name);
  }

  private ShutdownHook(Collection<ConnectionPoolManager> poolCollection) {
    super(poolCollection);
  }

  @Override
  public void run() {
    long begin = System.currentTimeMillis();
    Iterator<ConnectionPoolManager> itr = this.poolChain.iterator();
    while (itr.hasNext()) {
      ConnectionPoolManager pool = itr.next();
      // 带环链表中如果pool是null，则表示遍历结束。
      if (pool == null) {
        break;
      }
      pool.remove();
    }
    long cost = System.currentTimeMillis() - begin;
    LOGGER.info(ShutdownHook.class.getSimpleName() + " pool end, cost " + cost + " ms");
  }
}
