package com.github.xionghuicoder.clearpool;

/**
 * 数据库连接池超过最大连接数异常类
 *
 * @author wufuxing
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConnectionPoolUselessConnectionException extends ConnectionPoolException {
  private static final long serialVersionUID = 5616466869647771156L;

  public ConnectionPoolUselessConnectionException() {
    super();
  }

  public ConnectionPoolUselessConnectionException(String message) {
    super(message);
  }

  public ConnectionPoolUselessConnectionException(Throwable cause) {
    super(cause);
  }

  public ConnectionPoolUselessConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
