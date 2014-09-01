package org.opensource.clearpool.core.hook;

import java.util.Iterator;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.PoolLatchUtil;

/**
 * This class's duty is to close the connection which is expired.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class IdleGarbageHook extends CommonHook {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(IdleGarbageHook.class);

	/**
	 * Start IdleGarbageHook
	 */
	public static Thread startHook() {
		CommonHook idleGarbageHook = new IdleGarbageHook();
		Thread thread = idleGarbageHook.startHook("IdleGarbageHook");
		return thread;
	}

	/**
	 * Hide the constructor
	 */
	private IdleGarbageHook() {
	}

	@Override
	public void run() {
		LOG.info("IdleGarbageHook running");
		// I'm running.
		PoolLatchUtil.countDownStartLatch();
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
			long period = pool.getCfgVO().getLimitIdleTime();
			while (pool.isNeedCollected()) {
				ConnectionProxy conProxy = pool.getConnectionChain()
						.removeIdle(period);
				if (conProxy == null) {
					break;
				}
				// close connection
				pool.closeConnection(conProxy);
				pool.decrementPoolSize();
			}
		}
	}
}
