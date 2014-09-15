package org.opensource.clearpool.console.hook;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.opensource.clearpool.console.MBeanFacade;
import org.opensource.clearpool.exception.ConnectionPoolMBeanException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.PoolLatchUtil;

import com.sun.jdmk.comm.AuthInfo;
import com.sun.jdmk.comm.CommunicatorServer;
import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * This class's duty is to start HtmlAdaptorServer and count down the latch to
 * run CommonPoolContainer.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class HtmlAdaptorHook extends HtmlAdaptorServer {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(HtmlAdaptorHook.class);

	/**
	 * Start HtmlAdaptorHook
	 */
	public static CommunicatorServer startHook(MBeanServer server) {
		CommunicatorServer communicatorServer = initServer(server);
		Thread serverThread = new Thread(communicatorServer);
		serverThread.setName("HtmlAdaptorHook");
		serverThread.setDaemon(true);
		serverThread.start();
		return communicatorServer;
	}

	/**
	 * Get CommunicatorServer and register it to MBeanServer.
	 */
	private static CommunicatorServer initServer(MBeanServer server) {
		// we should know that securityMap isn't null
		Map<String, String> securityMap = MBeanFacade.console.getSecurityMap();
		List<AuthInfo> infoList = new ArrayList<AuthInfo>();
		for (Entry<String, String> e : securityMap.entrySet()) {
			infoList.add(new AuthInfo(e.getKey(), e.getValue()));
		}
		CommunicatorServer communicatorServer = new HtmlAdaptorHook(
				MBeanFacade.console.getPort(),
				infoList.toArray(new AuthInfo[0]));
		try {
			ObjectName adapterName = new ObjectName(
					"com.sun.jdmk.comm:type=HtmlAdaptorServer");
			server.registerMBean(communicatorServer, adapterName);
		} catch (Exception e) {
			throw new ConnectionPoolMBeanException(e);
		}
		return communicatorServer;
	}

	/**
	 * Hide the constructor
	 */
	private HtmlAdaptorHook(int port, AuthInfo[] infos) {
		super(port, infos);
	}

	@Override
	public void run() {
		LOG.info("HtmlAdaptorServer running:url is http://localhost:"
				+ MBeanFacade.console.getPort());
		// I'm running.
		PoolLatchUtil.countDownStartLatch();
		super.run();
	}

	/**
	 * Set fields to stop it.Note:HtmlAdaptorServer didn't provided useful
	 * interface to stop it,so we use reflection to get it.
	 */
	public static void stop(CommunicatorServer communicatorServer) {
		try {
			Field field = CommunicatorServer.class
					.getDeclaredField("stopRequested");
			field.setAccessible(true);
			field.set(communicatorServer, true);
			field = HtmlAdaptorServer.class.getDeclaredField("sockListen");
			field.setAccessible(true);
			ServerSocket sockListen = (ServerSocket) field
					.get(communicatorServer);
			sockListen.close();
		} catch (Exception e) {
			throw new ConnectionPoolMBeanException(e);
		}
	}
}
