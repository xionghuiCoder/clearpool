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
	// 3 hooks: IdleGarbageHook,PoolGrowHook and HtmlAdaptorHook.
	private static CountDownLatch startLatch = new CountDownLatch(3);

	// IdleCheckHook.
	private static CountDownLatch idleCheckLatch;

	/**
	 * Count down startLatch.
	 */
	public static void countDownStartLatch() {
		if (startLatch != null) {
			startLatch.countDown();
		}
	}

	/**
	 * Init idleCheckLatch.
	 */
	public static void initIdleCheckLatch() {
		idleCheckLatch = new CountDownLatch(1);
	}

	/**
	 * Count down idleCheckLatch.
	 */
	public static void countDownIdleCheckLatch() {
		if (idleCheckLatch != null) {
			idleCheckLatch.countDown();
		}
	}

	/**
	 * Wait one time.
	 */
	public static void await() {
		try {
			if (startLatch != null) {
				startLatch.await();
				// help gc
				startLatch = null;
			}
			if (idleCheckLatch != null) {
				idleCheckLatch.await();
				// help gc
				idleCheckLatch = null;
			}
		} catch (InterruptedException e) {
			throw new ConnectionPoolException(e);
		}
	}
}
