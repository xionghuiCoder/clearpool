package org.opensource.clearpool.exception;

/**
 * This exception is the main exception of jta.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class TransactionException extends RuntimeException {
  private static final long serialVersionUID = -5820107382164090965L;

  /**
   * Constructs a TransactionException.
   */
  public TransactionException() {
    super();
  }

  /**
   * Constructs a TransactionException using the given exception message.
   * 
   * @param message The message explaining the reason for the exception
   */
  public TransactionException(String message) {
    super(message);
  }

  /**
   * Constructs a TransactionException using the underlying cause.
   * 
   * @param cause The underlying cause.
   */
  public TransactionException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a TransactionException using the given message and underlying cause.
   * 
   * @param message The message explaining the reason for the exception.
   * @param cause The underlying cause.
   */
  public TransactionException(String message, Throwable cause) {
    super(message, cause);
  }
}
