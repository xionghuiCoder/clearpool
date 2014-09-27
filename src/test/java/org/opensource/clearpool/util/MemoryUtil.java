package org.opensource.clearpool.util;

public class MemoryUtil {

	/**
	 * Print memory
	 */
	public static void printMemoryInfo() {
		Runtime currRuntime = Runtime.getRuntime();
		int nFreeMemory = (int) (currRuntime.freeMemory() / 1024 / 1024);
		int nTotalMemory = (int) (currRuntime.totalMemory() / 1024 / 1024);
		String message = nFreeMemory + "M/" + nTotalMemory + "M(free/total)";
		System.out.println("memory:" + message);
	}
}
