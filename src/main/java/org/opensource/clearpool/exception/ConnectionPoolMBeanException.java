package org.opensource.clearpool.exception;

/**
 * This exception will be threw if we get a exception when we use MBean.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConnectionPoolMBeanException extends RuntimeException {
  private static final long serialVersionUID = -1428257858942209361L;

  public ConnectionPoolMBeanException() {
    super();
  }

  public ConnectionPoolMBeanException(String meassage) {
    super(meassage);
  }

  public ConnectionPoolMBeanException(Throwable cause) {
    super(cause);
  }
}
