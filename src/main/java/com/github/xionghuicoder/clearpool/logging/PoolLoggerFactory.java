package com.github.xionghuicoder.clearpool.logging;

import com.github.xionghuicoder.clearpool.logging.impl.NullLogger;

/**
 * 初始化log
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class PoolLoggerFactory {
  public static final String LOG_UNABLE = "clearpool.log.unable";

  private static boolean logUnable = false;

  static {
    logUnable = Boolean.getBoolean(LOG_UNABLE);
    if (!logUnable) {
      try {
        Class.forName("org.slf4j.LoggerFactory");
      } catch (ClassNotFoundException e) {
        logUnable = true;
      }
    }
  }

  public static PoolLogger getLogger(String name) {
    if (logUnable) {
      return new NullLogger();
    }
    return LoggerFactoryAdapter.getLogger(name);
  }

  public static PoolLogger getLogger(Class<?> clazz) {
    if (logUnable) {
      return new NullLogger();
    }
    return LoggerFactoryAdapter.getLogger(clazz);
  }
}
