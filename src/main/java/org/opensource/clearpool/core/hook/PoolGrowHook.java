package org.opensource.clearpool.core.hook;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.PoolLatchUtil;
import org.opensource.clearpool.util.ThreadSleepUtil;

/**
 * This class's duty is to create connection if needed.When do we need a new
 * connection? OK,it's a good question,it is when the pool connection is invalid
 * and it had been threw.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class PoolGrowHook extends CommonHook {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(PoolGrowHook.class);

	/**
	 * start the ConnectionIncrementHook
	 */
	public static Thread startHook(ConnectionPoolManager[] poolArray) {
		CommonHook idleGarbageHook = new PoolGrowHook();
		Thread thread = idleGarbageHook.startHook(poolArray, "PoolGrowHook");
		return thread;
	}

	/**
	 * Hide the constructor
	 */
	private PoolGrowHook() {
	}

	@Override
	public void run() {
		LOG.info("PoolGrowHook running");
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
			while (pool.getLackCount() != 0 && !pool.isClosed()) {
				try {
					pool.fillPool();
				} catch (ConnectionPoolException e) {
					// we don't have to log it because fillPool() already did.
				}
			}
		}
	}
}
