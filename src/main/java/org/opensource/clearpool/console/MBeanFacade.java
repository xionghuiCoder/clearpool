package org.opensource.clearpool.console;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.configuration.console.Console;
import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.core.util.PoolLatchUtil;

/**
 * This class is the facade of MBean,we should use MBean by it.
 * 
 * Note:Console may be null and user may not import jxmtools.jar,in this
 * case,CommunicatorServerHandler is fail to compile,so we should hidden it.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class MBeanFacade {
	public static Console console = ConfigurationVO.getConsole();

	/**
	 * {@see
	 * CommunicatorServerHandler#registerMBean(ConnectionPoolManager,String,
	 * String)}
	 */
	public static void registerMBean(ConnectionPoolManager pool,
			String mbeanName, String poolName) {
		if (console != null) {
			CommunicatorServerHandler.registerMBean(pool, mbeanName, poolName);
		}
	}

	/**
	 * {@see CommunicatorServerHandler#UnregisterMBean(String)}
	 */
	public static void UnregisterMBean(String poolName) {
		if (console != null) {
			CommunicatorServerHandler.UnregisterMBean(poolName);
		}
	}

	/**
	 * {@see CommunicatorServerHandler#start()}
	 */
	public static void start() {
		if (console == null) {
			/**
			 * Maybe HtmlAdaptorHook don't need to start,so don't forget count
			 * down latch,otherwise CommonPoolContainer will wait forever.
			 */
			PoolLatchUtil.countDownStartLatch();
		} else {
			CommunicatorServerHandler.start();
		}
	}

	/**
	 * {@see CommunicatorServerHandler#stop()}
	 */
	public static void stop() {
		if (console != null) {
			CommunicatorServerHandler.stop();
		}
	}
}
