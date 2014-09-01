package org.opensource.clearpool.core.hook;

import java.util.Iterator;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.core.chain.CommonChain;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.PoolLatchUtil;

/**
 * This class's duty is to check if the connection is valid.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class IdleCheckHook extends CommonHook {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(IdleCheckHook.class);

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
	private IdleCheckHook() {
	}

	@Override
	public void run() {
		LOG.info("IdleCheckHook running");
		// I'm running.
		PoolLatchUtil.countDownIdleCheckLatch();
		Iterator<ConnectionPoolManager> itr = poolChain.iterator();
		while (itr.hasNext()) {
			// if pool destroyed,it will interrupt this thread.
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			ConnectionPoolManager pool = itr.next();
			if (pool == null || pool.isClosed()) {
				itr.remove();
				continue;
			}
			ConfigurationVO cfgVO = pool.getCfgVO();
			long period = cfgVO.getKeepTestPeriod();
			// some pool don't need to check
			if (period == 0) {
				continue;
			}
			CommonChain<ConnectionProxy> chain = pool.getConnectionChain();
			while (true) {
				ConnectionProxy conProxy = chain.removeIdle(period);
				if (conProxy == null) {
					break;
				}
				boolean isValid = pool.checkTestTable(conProxy, false);
				if (isValid) {
					chain.add(conProxy);
					continue;
				}
				pool.decrementPoolSize();
				pool.incrementLackCount();
				pool.closeConnection(conProxy);
			}
		}
	}
}
