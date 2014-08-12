package org.opensource.clearpool.core.hook;

import java.util.Random;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.core.chain.AtomicCircleChain;
import org.opensource.clearpool.core.chain.CommonChain;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

abstract class CommonHook implements Runnable {
	private static final PoolLog LOG = PoolLogFactory.getLog(CommonHook.class);

	CommonChain<ConnectionPoolManager> poolChain;

	/**
	 * Set and start a idleHook
	 */
	Thread startHook(ConnectionPoolManager[] poolArray, String name) {
		this.initMixPoolList(poolArray);
		Thread thread = new Thread(this);
		thread.setName(name);
		thread.setDaemon(true);
		thread.start();
		LOG.debug("start " + name);
		return thread;
	}

	/**
	 * Init poolList and mix up the order of it.
	 */
	void initMixPoolList(ConnectionPoolManager[] poolArray) {
		if (this.poolChain != null) {
			return;
		}
		this.poolChain = new AtomicCircleChain<>();
		Random random = new Random();
		// get and mix up the order of poolChain
		for (int i = 0, length = poolArray.length; i < length; i++) {
			int index = random.nextInt(length - i) + i;
			this.poolChain.add(poolArray[index]);
			poolArray[index] = poolArray[i];
		}
	}
}
