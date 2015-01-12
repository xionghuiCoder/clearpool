package org.opensource.clearpool.datasource.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.datasource.proxy.PoolConnectionImpl;
import org.opensource.clearpool.jta.xa.XAConnectionImpl;

/**
 * The Factory's duty is to create dynamic proxy.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ProxyFactory {
  /**
   * this method is used to create the handler of {@link Statement}
   */
  public static Statement createProxyStatement(Statement statement,
      PoolConnectionImpl pooledConnection, ConnectionProxy conProxy, String sql) {
    Class<?>[] interfaces = new Class[1];
    if (statement instanceof CallableStatement) {
      interfaces[0] = CallableStatement.class;
    } else if (statement instanceof PreparedStatement) {
      interfaces[0] = PreparedStatement.class;
    } else {
      interfaces[0] = Statement.class;
    }
    InvocationHandler handler = new StatementHandler(statement, pooledConnection, conProxy, sql);
    return (Statement) Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(), interfaces,
        handler);
  }

  /**
   * this method is used to create the handler of XAStatement
   */
  public static Statement createProxyXAStatement(Statement statement, XAConnectionImpl xaCon,
      ConnectionProxy conProxy, String sql) {
    Class<?>[] interfaces = new Class[1];
    if (statement instanceof CallableStatement) {
      interfaces[0] = CallableStatement.class;
    } else if (statement instanceof PreparedStatement) {
      interfaces[0] = PreparedStatement.class;
    } else {
      interfaces[0] = Statement.class;
    }
    InvocationHandler handler = new XAStatementHandler(statement, xaCon, conProxy, sql);
    return (Statement) Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(), interfaces,
        handler);
  }

  /**
   * This method is used to create the handler of {@link DatabaseMetaData}
   */
  public static DatabaseMetaData createProxyDatabaseMetaData(Connection con,
      DatabaseMetaData metaData) {
    InvocationHandler handler = new DatabaseMetaDataHandler(con, metaData);
    return (DatabaseMetaData) Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(),
        new Class[] {DatabaseMetaData.class}, handler);
  }
}
