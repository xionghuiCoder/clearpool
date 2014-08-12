package org.opensource.clearpool.exception;

/**
 * This exception is the main exception of ClearPool.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConnectionPoolException extends RuntimeException {
	private static final long serialVersionUID = -5820107382164090965L;

	/**
	 * Constructs a ClearPoolException.
	 */
	public ConnectionPoolException() {
		super();
	}

	/**
	 * Constructs a ClearPoolException using the given exception message.
	 * 
	 * @param message
	 *            The message explaining the reason for the exception
	 */
	public ConnectionPoolException(String message) {
		super(message);
	}

	/**
	 * Constructs a ClearPoolException using the underlying cause.
	 * 
	 * @param cause
	 *            The underlying cause.
	 */
	public ConnectionPoolException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a ClearPoolException using the given message and underlying
	 * cause.
	 * 
	 * @param message
	 *            The message explaining the reason for the exception.
	 * @param cause
	 *            The underlying cause.
	 */
	public ConnectionPoolException(String message, Throwable cause) {
		super(message, cause);
	}
}