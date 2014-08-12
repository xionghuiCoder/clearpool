package org.opensource.clearpool.datasource.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * The dynamic proxy of DatabaseMetaData.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class DatabaseMetaDataHandler implements InvocationHandler {
	private static final String TOSTRING_METHOD = "toString";
	private static final String EQUALS_METHOD = "equals";
	private static final String HASHCODE_METHOD = "hashCode";
	private static final String GETCONNECTION_METHOD = "getConnection";

	private Connection proxy;
	private DatabaseMetaData metaData;

	DatabaseMetaDataHandler(Connection proxy, DatabaseMetaData metaData) {
		this.proxy = proxy;
		this.metaData = metaData;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = null;
		String methodName = method.getName();
		if (TOSTRING_METHOD.equals(methodName)) {
			result = this.toString();
		} else if (EQUALS_METHOD.equals(methodName)) {
			result = this.equals(args[0]);
		} else if (HASHCODE_METHOD.equals(methodName)) {
			result = this.hashCode();
		} else if (GETCONNECTION_METHOD.equals(methodName)) {
			result = this.getConnection();
		} else {
			try {
				result = method.invoke(this.metaData, args);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}
		return result;
	}

	/**
	 * We don't want to ask the DatabaseMetaData for the connection and we will
	 * get the proxy instead of the it.
	 * 
	 * @see DatabaseMetaData#getConnection
	 */
	private Connection getConnection() {
		return this.proxy;
	}
}
