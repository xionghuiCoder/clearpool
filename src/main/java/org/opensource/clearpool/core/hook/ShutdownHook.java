package org.opensource.clearpool.core.hook;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

public class ShutdownHook extends CommonHook {
	private static final PoolLog LOG = PoolLogFactory.getLog(ShutdownHook.class);

	/**
	 * start ShutdownHook by Runtime
	 */
	public static void registerHook(ConnectionPoolManager[] poolArray) {
		ShutdownHook shutdownHook = new ShutdownHook();
		shutdownHook.initMixPoolList(poolArray);
		Thread thread = new Thread(shutdownHook);
		thread.setName("ShutdownHook");
		Runtime.getRuntime().addShutdownHook(thread);
		LOG.debug("register ShutdownHook");
	}

	/**
	 * Hide the constructor
	 */
	private ShutdownHook() {
	}

	@Override
	public void run() {
		/**
		 * Removes all connection pools. close all the connections
		 */
		long begin = System.currentTimeMillis();
		for (ConnectionPoolManager pool : this.poolChain) {
			// the pool chain couldn't stop in for..in,but if it return null,we
			// can see that it would be stopped.
			if (pool == null) {
				break;
			}
			pool.remove();
		}
		long cost = System.currentTimeMillis() - begin;
		LOG.debug("ShutdownHook pool end,it cost " + cost + " ms");
	}
}
