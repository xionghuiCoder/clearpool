package com.github.xionghuicoder.clearpool.logging.impl;

import java.io.Serializable;

import org.slf4j.Marker;

import com.github.xionghuicoder.clearpool.logging.PoolLogger;

/**
 * 空log
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class NullLogger implements PoolLogger, Serializable {
  private static final long serialVersionUID = 711131452761300222L;

  @Override
  public String getName() {
    return null;
  }

  @Override
  public boolean isTraceEnabled() {
    return false;
  }

  @Override
  public void trace(String msg) {}

  @Override
  public void trace(String format, Object arg) {}

  @Override
  public void trace(String format, Object arg1, Object arg2) {}

  @Override
  public void trace(String format, Object... arguments) {}

  @Override
  public void trace(String msg, Throwable t) {}

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return false;
  }

  @Override
  public void trace(Marker marker, String msg) {}

  @Override
  public void trace(Marker marker, String format, Object arg) {}

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {}

  @Override
  public void trace(Marker marker, String format, Object... argArray) {}

  @Override
  public void trace(Marker marker, String msg, Throwable t) {}

  @Override
  public boolean isDebugEnabled() {
    return false;
  }

  @Override
  public void debug(String msg) {}

  @Override
  public void debug(String format, Object arg) {}

  @Override
  public void debug(String format, Object arg1, Object arg2) {}

  @Override
  public void debug(String format, Object... arguments) {}

  @Override
  public void debug(String msg, Throwable t) {}

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return false;
  }

  @Override
  public void debug(Marker marker, String msg) {}

  @Override
  public void debug(Marker marker, String format, Object arg) {}

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {}

  @Override
  public void debug(Marker marker, String format, Object... arguments) {}

  @Override
  public void debug(Marker marker, String msg, Throwable t) {}

  @Override
  public boolean isInfoEnabled() {
    return false;
  }

  @Override
  public void info(String msg) {}

  @Override
  public void info(String format, Object arg) {}

  @Override
  public void info(String format, Object arg1, Object arg2) {}

  @Override
  public void info(String format, Object... arguments) {}

  @Override
  public void info(String msg, Throwable t) {}

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return false;
  }

  @Override
  public void info(Marker marker, String msg) {}

  @Override
  public void info(Marker marker, String format, Object arg) {}

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {}

  @Override
  public void info(Marker marker, String format, Object... arguments) {}

  @Override
  public void info(Marker marker, String msg, Throwable t) {}

  @Override
  public boolean isWarnEnabled() {
    return false;
  }

  @Override
  public void warn(String msg) {}

  @Override
  public void warn(String format, Object arg) {}

  @Override
  public void warn(String format, Object... arguments) {}

  @Override
  public void warn(String format, Object arg1, Object arg2) {}

  @Override
  public void warn(String msg, Throwable t) {}

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return false;
  }

  @Override
  public void warn(Marker marker, String msg) {}

  @Override
  public void warn(Marker marker, String format, Object arg) {}

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {}

  @Override
  public void warn(Marker marker, String format, Object... arguments) {}

  @Override
  public void warn(Marker marker, String msg, Throwable t) {}

  @Override
  public boolean isErrorEnabled() {
    return false;
  }

  @Override
  public void error(String msg) {}

  @Override
  public void error(String format, Object arg) {}

  @Override
  public void error(String format, Object arg1, Object arg2) {}

  @Override
  public void error(String format, Object... arguments) {}

  @Override
  public void error(String msg, Throwable t) {}

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return false;
  }

  @Override
  public void error(Marker marker, String msg) {}

  @Override
  public void error(Marker marker, String format, Object arg) {}

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {}

  @Override
  public void error(Marker marker, String format, Object... arguments) {}

  @Override
  public void error(Marker marker, String msg, Throwable t) {}
}
