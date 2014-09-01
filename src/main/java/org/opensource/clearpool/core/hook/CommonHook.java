package org.opensource.clearpool.core.hook;

import java.util.Collection;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.core.chain.ChainFactory;
import org.opensource.clearpool.core.chain.CommonChain;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

public abstract class CommonHook implements Runnable {
	private static final PoolLog LOG = PoolLogFactory.getLog(CommonHook.class);

	static CommonChain<ConnectionPoolManager> poolChain = ChainFactory
			.createCircleChain((ConnectionPoolManager) null);

	/**
	 * Set and start a idleHook
	 */
	Thread startHook(String name) {
		Thread thread = new Thread(this);
		thread.setName(name);
		thread.setDaemon(true);
		thread.start();
		LOG.debug("start " + name);
		return thread;
	}

	/**
	 * Init poolChain.
	 */
	public static void initPoolChain(
			Collection<ConnectionPoolManager> poolCollection) {
		/*
		 * Random random = new Random(); for (int i = 0, length =
		 * poolArray.length; i < length; i++) { int index =
		 * random.nextInt(length - i) + i; this.poolChain.add(poolArray[index]);
		 * poolArray[index] = poolArray[i]; }
		 */

		for (ConnectionPoolManager manager : poolCollection) {
			poolChain.add(manager);
		}
	}
}
