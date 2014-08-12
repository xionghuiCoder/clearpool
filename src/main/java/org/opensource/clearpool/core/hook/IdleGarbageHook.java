package org.opensource.clearpool.core.hook;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.core.util.PoolLatchUtil;
import org.opensource.clearpool.core.util.ThreadSleepUtil;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

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
	public static Thread startHook(ConnectionPoolManager[] poolArray) {
		CommonHook idleGarbageHook = new IdleGarbageHook();
		Thread thread = idleGarbageHook.startHook(poolArray, "IdleGarbageHook");
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
		for (ConnectionPoolManager pool : this.poolChain) {
			// if pool destroyed,it will interrupt this thread.
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			// release CPU
			ThreadSleepUtil.sleep();
			if (pool == null || pool.isClosed()) {
				this.poolChain.remove();
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
