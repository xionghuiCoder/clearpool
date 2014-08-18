package org.opensource.clearpool.log.impl;

import java.io.Serializable;

import org.opensource.clearpool.log.PoolLog;

/**
 * This class is used when we set log unable
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class NullLogger implements PoolLog, Serializable {
	private static final long serialVersionUID = 711131452761300222L;

	@Override
	public void debug(Object message) {
	}

	@Override
	public void debug(Object message, Throwable t) {
	}

	@Override
	public void error(Object message) {
	}

	@Override
	public void error(Object message, Throwable t) {
	}

	@Override
	public void fatal(Object message) {
	}

	@Override
	public void fatal(Object message, Throwable t) {
	}

	@Override
	public void info(Object message) {
	}

	@Override
	public void info(Object message, Throwable t) {
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public boolean isErrorEnabled() {
		return false;
	}

	@Override
	public boolean isFatalEnabled() {
		return false;
	}

	@Override
	public boolean isInfoEnabled() {
		return false;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public boolean isWarnEnabled() {
		return false;
	}

	@Override
	public void trace(Object message) {
	}

	@Override
	public void trace(Object message, Throwable t) {
	}

	@Override
	public void warn(Object message) {
	}

	@Override
	public void warn(Object message, Throwable t) {
	}
}
