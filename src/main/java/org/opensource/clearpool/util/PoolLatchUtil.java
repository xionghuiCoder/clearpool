package org.opensource.clearpool.util;

import java.util.concurrent.CountDownLatch;

import org.opensource.clearpool.exception.ConnectionPoolException;

/**
 * This class's duty is to control the order of the thread running.It make sure
 * the daemon thread is running when the main thread returned.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class PoolLatchUtil {
	// 3 hooks:IdleCheckHook,IdleGarbageHook and HtmlAdaptorHook.
	private static CountDownLatch startLatch = new CountDownLatch(3);

	// this latch is init by pool size
	private static CountDownLatch poolLatch;

	/**
	 * Init pool latch.
	 */
	public static void initLatch(int size) {
		poolLatch = new CountDownLatch(size);
	}

	/**
	 * Count down poolLatch,used by hook.
	 */
	public static void countDownPoolLatch() {
		poolLatch.countDown();
	}

	/**
	 * Count down startLatch,used by hook.
	 */
	public static void countDownStartLatch() {
		if (startLatch != null) {
			startLatch.countDown();
		}
	}

	/**
	 * Wait one time.
	 */
	public static void await() {
		try {
			poolLatch.await();
			// help gc
			poolLatch = null;
			if (startLatch != null) {
				startLatch.await();
				startLatch = null;
			}
		} catch (InterruptedException e) {
			throw new ConnectionPoolException(e);
		}
	}
}
