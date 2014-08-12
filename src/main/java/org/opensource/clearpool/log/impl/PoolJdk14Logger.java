package org.opensource.clearpool.log.impl;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensource.clearpool.log.PoolLog;

/**
 * This class is used to log when org.apache.commons.logging.LogFactory can't be
 * imported.
 */
public class PoolJdk14Logger implements PoolLog, Serializable {

	/** Serializable version identifier. */
	private static final long serialVersionUID = 4784713551416303804L;

	/**
	 * This member variable simply ensures that any attempt to initialise this
	 * class in a pre-1.4 JVM will result in an ExceptionInInitializerError. It
	 * must not be private, as an optimising compiler could detect that it is
	 * not used and optimise it away.
	 */
	protected static final Level dummyLevel = Level.FINE;

	// ----------------------------------------------------------- Constructors

	/**
	 * Construct a named instance of this Logger.
	 * 
	 * @param name
	 *            Name of the logger to be constructed
	 */
	public PoolJdk14Logger(String name) {
		this.name = name;
		this.logger = this.getLogger();
	}

	// ----------------------------------------------------- Instance Variables

	/**
	 * The underlying Logger implementation we are using.
	 */
	protected transient Logger logger = null;

	/**
	 * The name of the logger we are wrapping.
	 */
	protected String name = null;

	// --------------------------------------------------------- Protected
	// Methods

	protected void log(Level level, String msg, Throwable ex) {
		Logger logger = this.getLogger();
		if (logger.isLoggable(level)) {
			// Hack (?) to get the stack trace.
			Throwable dummyException = new Throwable();
			StackTraceElement locations[] = dummyException.getStackTrace();
			// LOGGING-132: use the provided logger name instead of the class
			// name
			String cname = this.name;
			String method = "unknown";
			// Caller will be the third element
			if (locations != null && locations.length > 2) {
				StackTraceElement caller = locations[2];
				method = caller.getMethodName();
			}
			if (ex == null) {
				logger.logp(level, cname, method, msg);
			} else {
				logger.logp(level, cname, method, msg, ex);
			}
		}
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Logs a message with <code>java.util.logging.Level.FINE</code>.
	 * 
	 * @param message
	 *            to log
	 * @see org.apache.commons.logging.Log#debug(Object)
	 */
	@Override
	public void debug(Object message) {
		this.log(Level.FINE, String.valueOf(message), null);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.FINE</code>.
	 * 
	 * @param message
	 *            to log
	 * @param exception
	 *            log this cause
	 * @see org.apache.commons.logging.Log#debug(Object, Throwable)
	 */
	@Override
	public void debug(Object message, Throwable exception) {
		this.log(Level.FINE, String.valueOf(message), exception);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
	 * 
	 * @param message
	 *            to log
	 * @see org.apache.commons.logging.Log#error(Object)
	 */
	@Override
	public void error(Object message) {
		this.log(Level.SEVERE, String.valueOf(message), null);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
	 * 
	 * @param message
	 *            to log
	 * @param exception
	 *            log this cause
	 * @see org.apache.commons.logging.Log#error(Object, Throwable)
	 */
	@Override
	public void error(Object message, Throwable exception) {
		this.log(Level.SEVERE, String.valueOf(message), exception);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
	 * 
	 * @param message
	 *            to log
	 * @see org.apache.commons.logging.Log#fatal(Object)
	 */
	@Override
	public void fatal(Object message) {
		this.log(Level.SEVERE, String.valueOf(message), null);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
	 * 
	 * @param message
	 *            to log
	 * @param exception
	 *            log this cause
	 * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
	 */
	@Override
	public void fatal(Object message, Throwable exception) {
		this.log(Level.SEVERE, String.valueOf(message), exception);
	}

	/**
	 * Return the native Logger instance we are using.
	 */
	public Logger getLogger() {
		if (this.logger == null) {
			this.logger = Logger.getLogger(this.name);
		}
		return this.logger;
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.INFO</code>.
	 * 
	 * @param message
	 *            to log
	 * @see org.apache.commons.logging.Log#info(Object)
	 */
	@Override
	public void info(Object message) {
		this.log(Level.INFO, String.valueOf(message), null);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.INFO</code>.
	 * 
	 * @param message
	 *            to log
	 * @param exception
	 *            log this cause
	 * @see org.apache.commons.logging.Log#info(Object, Throwable)
	 */
	@Override
	public void info(Object message, Throwable exception) {
		this.log(Level.INFO, String.valueOf(message), exception);
	}

	/**
	 * Is debug logging currently enabled?
	 */
	@Override
	public boolean isDebugEnabled() {
		return this.getLogger().isLoggable(Level.FINE);
	}

	/**
	 * Is error logging currently enabled?
	 */
	@Override
	public boolean isErrorEnabled() {
		return this.getLogger().isLoggable(Level.SEVERE);
	}

	/**
	 * Is fatal logging currently enabled?
	 */
	@Override
	public boolean isFatalEnabled() {
		return this.getLogger().isLoggable(Level.SEVERE);
	}

	/**
	 * Is info logging currently enabled?
	 */
	@Override
	public boolean isInfoEnabled() {
		return this.getLogger().isLoggable(Level.INFO);
	}

	/**
	 * Is trace logging currently enabled?
	 */
	@Override
	public boolean isTraceEnabled() {
		return this.getLogger().isLoggable(Level.FINEST);
	}

	/**
	 * Is warn logging currently enabled?
	 */
	@Override
	public boolean isWarnEnabled() {
		return this.getLogger().isLoggable(Level.WARNING);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.FINEST</code>.
	 * 
	 * @param message
	 *            to log
	 * @see org.apache.commons.logging.Log#trace(Object)
	 */
	@Override
	public void trace(Object message) {
		this.log(Level.FINEST, String.valueOf(message), null);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.FINEST</code>.
	 * 
	 * @param message
	 *            to log
	 * @param exception
	 *            log this cause
	 * @see org.apache.commons.logging.Log#trace(Object, Throwable)
	 */
	@Override
	public void trace(Object message, Throwable exception) {
		this.log(Level.FINEST, String.valueOf(message), exception);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.WARNING</code>.
	 * 
	 * @param message
	 *            to log
	 * @see org.apache.commons.logging.Log#warn(Object)
	 */
	@Override
	public void warn(Object message) {
		this.log(Level.WARNING, String.valueOf(message), null);
	}

	/**
	 * Logs a message with <code>java.util.logging.Level.WARNING</code>.
	 * 
	 * @param message
	 *            to log
	 * @param exception
	 *            log this cause
	 * @see org.apache.commons.logging.Log#warn(Object, Throwable)
	 */
	@Override
	public void warn(Object message, Throwable exception) {
		this.log(Level.WARNING, String.valueOf(message), exception);
	}
}
