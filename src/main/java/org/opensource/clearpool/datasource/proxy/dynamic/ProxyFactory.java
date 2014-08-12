package org.opensource.clearpool.datasource.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.opensource.clearpool.datasource.proxy.PooledConnectionImpl;

/**
 * The Factory's duty is to create dynamic proxy.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ProxyFactory {
	/**
	 * this method is used to create the proxy of {@link Statement}
	 */
	public static Statement createProxyStatement(Statement statement,
			PooledConnectionImpl conProxy, String sql) {
		Class<?>[] interfaces = new Class[1];
		if (statement instanceof CallableStatement) {
			interfaces[0] = CallableStatement.class;
		} else if (statement instanceof PreparedStatement) {
			interfaces[0] = PreparedStatement.class;
		} else {
			interfaces[0] = Statement.class;
		}
		InvocationHandler handler = new StatementHandler(statement, conProxy,
				sql);
		return (Statement) Proxy.newProxyInstance(
				ProxyFactory.class.getClassLoader(), interfaces, handler);
	}

	/**
	 * This method is used to create the proxy of {@link DatabaseMetaData}
	 */
	public static DatabaseMetaData createProxyDatabaseMetaData(
			Connection proxy, DatabaseMetaData metaData) {
		InvocationHandler handler = new DatabaseMetaDataHandler(proxy, metaData);
		return (DatabaseMetaData) Proxy.newProxyInstance(
				ProxyFactory.class.getClassLoader(),
				new Class[] { DatabaseMetaData.class }, handler);
	}
}
