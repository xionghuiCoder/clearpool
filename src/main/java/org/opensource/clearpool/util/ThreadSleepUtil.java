package org.opensource.clearpool.util;

/**
 * This class's duty is to release CPU,it sleep for a while in case the CPU is
 * full running.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ThreadSleepUtil {

	/**
	 * release CPU:if the thread don't sleep,the CPU will be took by this thread
	 * all the time.
	 */
	public static void sleep() {
		try {
			// Thread.sleep(0, 1);
			Thread.sleep(0);
		} catch (InterruptedException e) {
			// swallow the exception
		}
	}
}
