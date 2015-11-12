package org.opensource.clearpool.logging;

import org.opensource.clearpool.logging.impl.PoolLogImpl;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to invoke LoggerFactory if LoggerFactory could be imported.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class LogFactoryAdapter {

  /**
   * Return a logger named according to the name parameter using the statically bound
   * {@link ILoggerFactory} instance.
   *
   * @param name The name of the logger.
   * @return poolLog
   */
  public static PoolLog getLog(String name) {
    Logger log = LoggerFactory.getLogger(name);
    PoolLog poolLog = new PoolLogImpl(log);
    return poolLog;
  }

  /**
   * Return a logger named corresponding to the class passed as parameter, using the statically
   * bound {@link ILoggerFactory} instance.
   *
   * <p>
   * In case the the <code>clazz</code> parameter differs from the name of the caller as computed
   * internally by SLF4J, a logger name mismatch warning will be printed but only if the
   * <code>slf4j.detectLoggerNameMismatch</code> system property is set to true. By default, this
   * property is not set and no warnings will be printed even in case of a logger name mismatch.
   *
   * @param clazz the returned logger will be named after clazz
   * @return poolLog
   */
  public static PoolLog getLog(Class<?> clazz) {
    Logger log = LoggerFactory.getLogger(clazz);
    PoolLog poolLog = new PoolLogImpl(log);
    return poolLog;
  }
}
