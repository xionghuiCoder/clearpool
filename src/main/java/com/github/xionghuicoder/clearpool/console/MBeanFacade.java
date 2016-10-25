package com.github.xionghuicoder.clearpool.console;

import java.util.concurrent.CountDownLatch;

import com.github.xionghuicoder.clearpool.core.ConnectionPoolManager;

/**
 * {@link CommunicatorServerHandler CommunicatorServerHandler}的门面
 *
 * <p>
 * 当{@link Console Console}为<tt>null</tt>时，且未导入jxmtools.jar；<br>
 * 此时{@link CommunicatorServerHandler CommunicatorServerHandler}编译会报错，所以需要懒加载
 * </p>
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class MBeanFacade {
  private final CommunicatorServerHandler communicatorServerHandler;

  public MBeanFacade(Console console) {
    this.communicatorServerHandler = new CommunicatorServerHandler(console);
  }

  /**
   *
   * @param mbeanFacade mbeanFacade
   * @param pool pool
   * @param mbeanName mbeanName
   * @param poolName poolName
   * @see CommunicatorServerHandler#registerMBean(ConnectionPoolManager,String,String)
   */
  public static void registerMBean(MBeanFacade mbeanFacade, ConnectionPoolManager pool,
      String mbeanName, String poolName) {
    if (mbeanFacade != null) {
      mbeanFacade.communicatorServerHandler.registerMBean(pool, mbeanName, poolName);
    }
  }

  /**
   *
   * @param mbeanFacade mbeanFacade
   * @param poolName poolName
   * @see CommunicatorServerHandler#unregisterMBean(String)
   */
  public static void unregisterMBean(MBeanFacade mbeanFacade, String poolName) {
    if (mbeanFacade != null) {
      mbeanFacade.communicatorServerHandler.unregisterMBean(poolName);
    }
  }

  public static void start(MBeanFacade mbeanFacade, CountDownLatch startLatch) {
    if (mbeanFacade == null) {
      startLatch.countDown();
    } else {
      mbeanFacade.communicatorServerHandler.start(startLatch);
    }
  }

  /**
   *
   * @param mbeanFacade mbeanFacade
   * @see CommunicatorServerHandler#stop()
   */
  public static void stop(MBeanFacade mbeanFacade) {
    if (mbeanFacade != null) {
      mbeanFacade.communicatorServerHandler.stop();
    }
  }
}
