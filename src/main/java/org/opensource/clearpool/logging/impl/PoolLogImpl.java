package org.opensource.clearpool.logging.impl;

import org.apache.commons.logging.Log;
import org.opensource.clearpool.logging.PoolLog;

/**
 * This class is used to adapter org.apache.commons.logging.Log.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class PoolLogImpl implements PoolLog {
  private Log log;

  public PoolLogImpl(Log log) {
    this.log = log;
  }

  @Override
  public void debug(Object message) {
    this.log.debug(message);
  }

  @Override
  public void debug(Object message, Throwable t) {
    this.log.debug(message, t);
  }

  @Override
  public void error(Object message) {
    this.log.error(message);
  }

  @Override
  public void error(Object message, Throwable t) {
    this.log.error(message, t);
  }

  @Override
  public void fatal(Object message) {
    this.log.fatal(message);
  }

  @Override
  public void fatal(Object message, Throwable t) {
    this.log.fatal(message, t);
  }

  @Override
  public void info(Object message) {
    this.log.info(message);
  }

  @Override
  public void info(Object message, Throwable t) {
    this.log.info(message, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return this.log.isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return this.log.isErrorEnabled();
  }

  @Override
  public boolean isFatalEnabled() {
    return this.log.isFatalEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return this.log.isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return this.log.isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return this.log.isWarnEnabled();
  }

  @Override
  public void trace(Object message) {
    this.log.trace(message);
  }

  @Override
  public void trace(Object message, Throwable t) {
    this.log.trace(message, t);
  }

  @Override
  public void warn(Object message) {
    this.log.warn(message);
  }

  @Override
  public void warn(Object message, Throwable t) {
    this.log.warn(message, t);
  }
}
