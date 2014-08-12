package org.opensource.clearpool.exception;

/**
 * This exception will be threw if we get a exception when we parse XML.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConnectionPoolXMLParseException extends RuntimeException {
	private static final long serialVersionUID = -1428257858942209361L;

	public ConnectionPoolXMLParseException() {
		super();
	}

	public ConnectionPoolXMLParseException(String meassage) {
		super(meassage);
	}

	public ConnectionPoolXMLParseException(Throwable cause) {
		super(cause);
	}
}
