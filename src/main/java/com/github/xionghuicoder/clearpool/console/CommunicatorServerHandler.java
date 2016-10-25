package com.github.xionghuicoder.clearpool.console;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.console.hook.HtmlAdaptorHook;
import com.github.xionghuicoder.clearpool.core.ConnectionPoolManager;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;
import com.sun.jdmk.comm.CommunicatorServer;

/**
 * MBean操作类
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
class CommunicatorServerHandler {
  private static final PoolLogger LOGGER =
      PoolLoggerFactory.getLogger(CommunicatorServerHandler.class);

  /**
   * <p>
   * 不能使用{@link ManagementFactory#getPlatformMBeanServer ManagementFactory.getPlatformMBeanServer}，
   * 因为{@link ManagementFactory#getPlatformMBeanServer ManagementFactory.getPlatformMBeanServer}
   * 会缓存{@link MBeanServer}。
   * </p>
   */
  private final MBeanServer server = MBeanServerFactory.createMBeanServer();

  private final Console console = new Console();

  private final Map<String, ObjectName> objectNameMap = new HashMap<String, ObjectName>();

  private CommunicatorServer communicatorServer;

  CommunicatorServerHandler(Console console) {
    if (console != null) {
      this.console.setPort(console.getPort());
      Map<String, String> securityMap = console.getSecurityMap();
      if (securityMap != null) {
        for (Map.Entry<String, String> entry : securityMap.entrySet()) {
          this.console.getSecurityMap().put(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  /**
   * 注册MBean
   */
  void registerMBean(ConnectionPoolManager pool, String mbeanName, String poolName) {
    ConnectionPoolMBean bean = new ConnectionPool(pool);
    try {
      ObjectName objectName = new ObjectName(mbeanName);
      this.server.registerMBean(bean, objectName);
      this.objectNameMap.put(poolName, objectName);
    } catch (Exception e) {
      throw new ConnectionPoolException(e);
    }
    LOGGER.info("register " + poolName + "'s MBean: " + mbeanName);
  }

  /**
   * 注销MBean
   */
  void unregisterMBean(String poolName) {
    ObjectName objectName = this.objectNameMap.get(poolName);
    if (objectName == null) {
      return;
    }
    if (this.server.isRegistered(objectName)) {
      try {
        this.server.unregisterMBean(objectName);
      } catch (Exception e) {
        // 如果我们在html页面上点击Unregister注销了MBean，或者之前注销过MBean，则会出现异常
        LOGGER.error("unregisterMBean error: ", e);
      }
    }
    LOGGER.info("unregister " + poolName + "'s MBean: " + objectName.getCanonicalName());
  }

  /**
   * 启动
   */
  void start(CountDownLatch startLatch) {
    if (this.communicatorServer == null) {
      checkPort(this.console.getPort());
      this.communicatorServer = HtmlAdaptorHook.startHook(this.console, this.server, startLatch);
    } else {
      startLatch.countDown();
    }
  }

  /**
   * 检查port是否被占用
   */
  private static void checkPort(int port) {
    try {
      ServerSocket server = new ServerSocket(port);
      server.close();
    } catch (IOException e) {
      throw new ConnectionPoolException(e);
    }
  }

  /**
   * 停止HtmlAdaptorServer
   */
  void stop() {
    if (this.communicatorServer != null) {
      HtmlAdaptorHook.stop(this.communicatorServer);
    }
  }
}
