package com.github.xionghuicoder.clearpool.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xionghuicoder.clearpool.logging.impl.PoolLoggerImpl;

/**
 * log工厂
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
class LoggerFactoryAdapter {

  public static PoolLogger getLogger(String name) {
    Logger logger = LoggerFactory.getLogger(name);
    PoolLogger poolLogger = new PoolLoggerImpl(logger);
    return poolLogger;
  }

  public static PoolLogger getLogger(Class<?> clazz) {
    Logger logger = LoggerFactory.getLogger(clazz);
    PoolLogger poolLogger = new PoolLoggerImpl(logger);
    return poolLogger;
  }
}
