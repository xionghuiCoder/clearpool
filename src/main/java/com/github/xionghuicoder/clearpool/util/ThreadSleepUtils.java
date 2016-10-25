package com.github.xionghuicoder.clearpool.util;

/**
 * 休眠{@link #MINUTE MINUTE}(ms)，防止cpu占用过高。
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ThreadSleepUtils {
  private final static long MINUTE = 60 * 1000L;

  private ThreadSleepUtils() {}

  public static boolean sleep() {
    try {
      Thread.sleep(MINUTE);
      return false;
    } catch (InterruptedException e) {
      return true;
    }
  }
}
