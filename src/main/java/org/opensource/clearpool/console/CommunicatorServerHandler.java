package org.opensource.clearpool.console;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.opensource.clearpool.configuration.console.Console;
import org.opensource.clearpool.console.hook.HtmlAdaptorHook;
import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.exception.ConnectionPoolMBeanException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

import com.sun.jdmk.comm.CommunicatorServer;

/**
 * This class is used to control MBean.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class CommunicatorServerHandler {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(CommunicatorServerHandler.class);

	private static MBeanServer server = ManagementFactory
			.getPlatformMBeanServer();

	// this map is used to unregister ObjectName while remove the pool
	private static Map<String, ObjectNameCarry> objectNameMap = new HashMap<>();

	private static CommunicatorServer communicatorServer;

	/**
	 * Register a MBean
	 */
	public static void registerMBean(ConnectionPoolManager pool,
			String mbeanName, String poolName) {
		if (communicatorServer == null) {
			return;
		}
		ConnectionPoolMBean bean = new ConnectionPool(pool);
		try {
			ObjectName objectName = new ObjectName(mbeanName);
			server.registerMBean(bean, objectName);
			ObjectNameCarry carry = new ObjectNameCarry(objectName, mbeanName);
			objectNameMap.put(poolName, carry);
		} catch (Exception e) {
			throw new ConnectionPoolMBeanException(e);
		}
		LOG.info("register " + poolName + "'s MBean:" + mbeanName);
	}

	/**
	 * Unregister a MBean
	 */
	public static void UnregisterMBean(String poolName) {
		if (communicatorServer == null) {
			return;
		}
		ObjectNameCarry carry = objectNameMap.get(poolName);
		if (carry == null) {
			return;
		}
		ObjectName objectName = carry.objectName;
		String mbeanName = carry.mbeanName;
		try {
			server.unregisterMBean(objectName);
		} catch (MBeanRegistrationException | InstanceNotFoundException e) {
			throw new ConnectionPoolMBeanException(e);
		}
		LOG.info("unregister " + poolName + "'s MBean:" + mbeanName);
	}

	/**
	 * Start communicatorServer
	 */
	public static void start() {
		if (communicatorServer != null) {
			return;
		}
		checkPort(MBeanFacade.console.getPort());
		// start it.
		communicatorServer = HtmlAdaptorHook.startHook(server);
	}

	/**
	 * check if port is valid.
	 */
	private static void checkPort(int port) {
		try {
			ServerSocket server = new ServerSocket(port);
			server.close();
		} catch (IOException e) {
			throw new ConnectionPoolMBeanException(Console.PORT + " is used");
		}
	}

	/**
	 * Stop HtmlAdaptorServer,note:never stop the thread,instead of we should
	 * set stopRequested true and close sockListen.Want to know the
	 * reason,please to see the source of CommunicatorServer and
	 * HtmlAdaptorServer.
	 */
	public static void stop() {
		if (communicatorServer != null) {
			// stop it
			HtmlAdaptorHook.stop(communicatorServer);
			// help gc
			communicatorServer = null;
			objectNameMap = new HashMap<>();
		}
	}

	/**
	 * This class used to carry mbeanName of the ObjectName,we can't log
	 * mbeanName with it while we unregister the MBean.
	 * 
	 * @author xionghui
	 * @date 26.07.2014
	 * @version 1.0
	 */
	private static class ObjectNameCarry {
		ObjectName objectName;
		String mbeanName;

		ObjectNameCarry(ObjectName objectName, String mbeanName) {
			this.objectName = objectName;
			this.mbeanName = mbeanName;
		}
	}
}
