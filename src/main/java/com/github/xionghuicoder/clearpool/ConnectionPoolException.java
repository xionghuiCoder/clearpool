package com.github.xionghuicoder.clearpool;

/**
 * 数据库连接池异常类
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConnectionPoolException extends RuntimeException {
  private static final long serialVersionUID = -5820107382164090965L;

  public ConnectionPoolException() {
    super();
  }

  public ConnectionPoolException(String message) {
    super(message);
  }

  public ConnectionPoolException(Throwable cause) {
    super(cause);
  }

  public ConnectionPoolException(String message, Throwable cause) {
    super(message, cause);
  }
}
