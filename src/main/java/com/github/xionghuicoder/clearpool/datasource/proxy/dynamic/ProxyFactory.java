package com.github.xionghuicoder.clearpool.datasource.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Statement;

import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.datasource.proxy.PoolConnectionImpl;
import com.github.xionghuicoder.clearpool.jta.xa.XAConnectionImpl;

/**
 * 动态代理工厂
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProxyFactory {

  /**
   * {@link ConnectionProxy ConnectionProxy}的{@link Statement Statement}动态代理
   *
   * @param statement statement
   * @param pooledConnection pooledConnection
   * @param conProxy conProxy
   * @param sql sql
   * @return ConnectionProxy的动态代理
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
   * {@link XAConnectionImpl XAConnectionImpl}的@link Statement Statement}动态代理
   *
   * @param statement statement
   * @param xaCon xaCon
   * @param conProxy conProxy
   * @param sql sql
   * @return xaCon的动态代理
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
   * {@link DatabaseMetaData DatabaseMetaData}的动态代理
   *
   * @param con con
   * @param metaData metaData
   * @return metaData的动态代理
   */
  public static DatabaseMetaData createProxyDatabaseMetaData(Connection con,
      DatabaseMetaData metaData) {
    InvocationHandler handler = new DatabaseMetaDataHandler(con, metaData);
    return (DatabaseMetaData) Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(),
        new Class[] {DatabaseMetaData.class}, handler);
  }
}
