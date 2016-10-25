package com.github.xionghuicoder.clearpool.console.hook;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.console.Console;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;
import com.sun.jdmk.comm.AuthInfo;
import com.sun.jdmk.comm.CommunicatorServer;
import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * 启动{@link HtmlAdaptorServer}
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class HtmlAdaptorHook extends HtmlAdaptorServer {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(HtmlAdaptorHook.class);

  private final Console console;
  private CountDownLatch startLatch;

  /**
   * 启动{@link HtmlAdaptorServer}
   *
   * @param console console
   * @param server server
   * @param startLatch startLatch
   * @return CommunicatorServer
   */
  public static CommunicatorServer startHook(Console console, MBeanServer server,
      CountDownLatch startLatch) {
    CommunicatorServer communicatorServer = initServer(console, server, startLatch);
    Thread serverThread = new Thread(communicatorServer);
    serverThread.setName(HtmlAdaptorHook.class.getSimpleName());
    serverThread.setDaemon(true);
    serverThread.start();
    return communicatorServer;
  }

  /**
   * 注册MBeanServer
   * 
   * @param console console
   * @param server server
   * @param startLatch startLatch
   * @return CommunicatorServer
   */
  private static CommunicatorServer initServer(Console console, MBeanServer server,
      CountDownLatch startLatch) {
    List<AuthInfo> infoList = new ArrayList<AuthInfo>();
    Map<String, String> securityMap = console.getSecurityMap();
    if (securityMap != null) {
      for (Entry<String, String> e : securityMap.entrySet()) {
        infoList.add(new AuthInfo(e.getKey(), e.getValue()));
      }
    }
    CommunicatorServer communicatorServer =
        new HtmlAdaptorHook(console, infoList.toArray(new AuthInfo[0]), startLatch);
    try {
      ObjectName adapterName = new ObjectName("com.sun.jdmk.comm:type=HtmlAdaptorServer");
      if (!server.isRegistered(adapterName)) {
        server.registerMBean(communicatorServer, adapterName);
      }
    } catch (Exception e) {
      throw new ConnectionPoolException(e);
    }
    return communicatorServer;
  }

  private HtmlAdaptorHook(Console console, AuthInfo[] infos, CountDownLatch startLatch) {
    super(console.getPort(), infos);
    this.console = console;
    this.startLatch = startLatch;
  }

  @Override
  public void run() {
    LOGGER.info(HtmlAdaptorHook.class.getSimpleName() + " running, url is http://localhost:"
        + this.console.getPort());
    this.startLatch.countDown();
    // help gc
    this.startLatch = null;
    super.run();
  }

  /**
   * 直接调用{@link HtmlAdaptorServer#stop HtmlAdaptorServer.stop}会提示空指针，需要用反射停止服务
   * 
   * @param communicatorServer communicatorServer
   */
  public static void stop(CommunicatorServer communicatorServer) {
    try {
      Field stopRequestedField = CommunicatorServer.class.getDeclaredField("stopRequested");
      stopRequestedField.setAccessible(true);
      stopRequestedField.set(communicatorServer, true);

      Field sockListenField = HtmlAdaptorServer.class.getDeclaredField("sockListen");
      sockListenField.setAccessible(true);
      ServerSocket sockListen = (ServerSocket) sockListenField.get(communicatorServer);
      sockListen.close();
    } catch (Exception e) {
      throw new ConnectionPoolException(e);
    }
  }
}
