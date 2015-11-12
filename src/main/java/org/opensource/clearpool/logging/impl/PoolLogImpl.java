package org.opensource.clearpool.logging.impl;

import org.opensource.clearpool.logging.PoolLog;
import org.slf4j.Logger;

/**
 * This class is used to adapter org.apache.commons.logging.Log.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class PoolLogImpl implements PoolLog {
  private Logger log;

  public PoolLogImpl(Logger log) {
    this.log = log;
  }

  @Override
  public void debug(Object message) {
    log.debug(String.valueOf(message));
  }

  @Override
  public void debug(Object message, Throwable t) {
    log.debug(String.valueOf(message), t);
  }

  @Override
  public void error(Object message) {
    log.error(String.valueOf(message));
  }

  @Override
  public void error(Object message, Throwable t) {
    log.error(String.valueOf(message), t);
  }

  @Override
  public void info(Object message) {
    log.info(String.valueOf(message));
  }

  @Override
  public void info(Object message, Throwable t) {
    log.info(String.valueOf(message), t);
  }

  @Override
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return log.isErrorEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return log.isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return log.isWarnEnabled();
  }

  @Override
  public void trace(Object message) {
    log.trace(String.valueOf(message));
  }

  @Override
  public void trace(Object message, Throwable t) {
    log.trace(String.valueOf(message), t);
  }

  @Override
  public void warn(Object message) {
    log.warn(String.valueOf(message));
  }

  @Override
  public void warn(Object message, Throwable t) {
    log.warn(String.valueOf(message), t);
  }
}
