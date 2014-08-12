package org.opensource.clearpool.log;

/**
 * This class is used to instead of adapter org.apache.commons.logging.Log.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public interface PoolLog {

	/**
	 * Logs a message with debug log level.
	 * 
	 * @param message
	 *            log this message
	 */
	void debug(Object message);

	/**
	 * Logs an error with debug log level.
	 * 
	 * @param message
	 *            log this message
	 * @param t
	 *            log this cause
	 */
	void debug(Object message, Throwable t);

	/**
	 * Logs a message with error log level.
	 * 
	 * @param message
	 *            log this message
	 */
	void error(Object message);

	/**
	 * Logs an error with error log level.
	 * 
	 * @param message
	 *            log this message
	 * @param t
	 *            log this cause
	 */
	void error(Object message, Throwable t);

	/**
	 * Logs a message with fatal log level.
	 * 
	 * @param message
	 *            log this message
	 */
	void fatal(Object message);

	/**
	 * Logs an error with fatal log level.
	 * 
	 * @param message
	 *            log this message
	 * @param t
	 *            log this cause
	 */
	void fatal(Object message, Throwable t);

	/**
	 * Logs a message with info log level.
	 * 
	 * @param message
	 *            log this message
	 */
	void info(Object message);

	/**
	 * Logs an error with info log level.
	 * 
	 * @param message
	 *            log this message
	 * @param t
	 *            log this cause
	 */
	void info(Object message, Throwable t);

	/**
	 * Is debug logging currently enabled?
	 * <p>
	 * Call this method to prevent having to perform expensive operations (for
	 * example, <code>String</code> concatenation) when the log level is more
	 * than debug.
	 * 
	 * @return true if debug is enabled in the underlying logger.
	 */
	boolean isDebugEnabled();

	/**
	 * Is error logging currently enabled?
	 * <p>
	 * Call this method to prevent having to perform expensive operations (for
	 * example, <code>String</code> concatenation) when the log level is more
	 * than error.
	 * 
	 * @return true if error is enabled in the underlying logger.
	 */
	boolean isErrorEnabled();

	/**
	 * Is fatal logging currently enabled?
	 * <p>
	 * Call this method to prevent having to perform expensive operations (for
	 * example, <code>String</code> concatenation) when the log level is more
	 * than fatal.
	 * 
	 * @return true if fatal is enabled in the underlying logger.
	 */
	boolean isFatalEnabled();

	/**
	 * Is info logging currently enabled?
	 * <p>
	 * Call this method to prevent having to perform expensive operations (for
	 * example, <code>String</code> concatenation) when the log level is more
	 * than info.
	 * 
	 * @return true if info is enabled in the underlying logger.
	 */
	boolean isInfoEnabled();

	/**
	 * Is trace logging currently enabled?
	 * <p>
	 * Call this method to prevent having to perform expensive operations (for
	 * example, <code>String</code> concatenation) when the log level is more
	 * than trace.
	 * 
	 * @return true if trace is enabled in the underlying logger.
	 */
	boolean isTraceEnabled();

	/**
	 * Is warn logging currently enabled?
	 * <p>
	 * Call this method to prevent having to perform expensive operations (for
	 * example, <code>String</code> concatenation) when the log level is more
	 * than warn.
	 * 
	 * @return true if warn is enabled in the underlying logger.
	 */
	boolean isWarnEnabled();

	/**
	 * Logs a message with trace log level.
	 * 
	 * @param message
	 *            log this message
	 */
	void trace(Object message);

	/**
	 * Logs an error with trace log level.
	 * 
	 * @param message
	 *            log this message
	 * @param t
	 *            log this cause
	 */
	void trace(Object message, Throwable t);

	/**
	 * Logs a message with warn log level.
	 * 
	 * @param message
	 *            log this message
	 */
	void warn(Object message);

	/**
	 * Logs an error with warn log level.
	 * 
	 * @param message
	 *            log this message
	 * @param t
	 *            log this cause
	 */
	void warn(Object message, Throwable t);
}
