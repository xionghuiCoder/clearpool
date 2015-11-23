package org.opensource.clearpool.exception;

/**
 * This exception will be threw if {@link #ClearPool} have a illegal state.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConnectionPoolStateException extends RuntimeException {
  private static final long serialVersionUID = -1428257858942209361L;

  public ConnectionPoolStateException() {
    super();
  }

  public ConnectionPoolStateException(String meassage) {
    super(meassage);
  }

  public ConnectionPoolStateException(Throwable cause) {
    super(cause);
  }

  public ConnectionPoolStateException(String message, Throwable cause) {
    super(message, cause);
  }
}
