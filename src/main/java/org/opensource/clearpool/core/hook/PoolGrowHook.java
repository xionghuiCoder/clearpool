package org.opensource.clearpool.core.hook;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.PoolLatchUtil;
import org.opensource.clearpool.util.ThreadSleepUtil;

/**
 * This class's duty is to create connection if needed.When do we need a
 * connection?OK,it's a good question,the time we create new connection is when
 * connection is invalid and it's been threw.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class PoolGrowHook implements Runnable {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(PoolGrowHook.class);

	private ConnectionPoolManager pool;

	/**
	 * start the ConnectionIncrementHook
	 */
	public static Thread startHook(ConnectionPoolManager pool) {
		PoolGrowHook connectionIncrementHook = new PoolGrowHook(pool);
		Thread thread = new Thread(connectionIncrementHook);
		thread.setName("PoolGrowHook");
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	private PoolGrowHook(ConnectionPoolManager pool) {
		this.pool = pool;
	}

	@Override
	public void run() {
		String name = this.pool.getCfgVO().getAlias();
		LOG.info("PoolGrowHook for pool " + name + " is running");
		// I'm running.
		PoolLatchUtil.countDownPoolLatch();
		while (true) {
			// if pool destroyed or closed,it will stop this thread.
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			int lackCount = this.pool.getLackCount();
			if (lackCount == 0) {
				// release CPU
				ThreadSleepUtil.sleep();
				continue;
			}
			try {
				this.pool.fillPool(lackCount);
			} catch (ConnectionPoolException e) {
				// swallow it,we don't have to log it because fillPool() already
				// did.
			}
		}
	}
}
