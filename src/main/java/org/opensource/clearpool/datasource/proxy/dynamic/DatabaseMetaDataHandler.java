package org.opensource.clearpool.datasource.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

/**
 * The dynamic proxy of DatabaseMetaData.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class DatabaseMetaDataHandler implements InvocationHandler {
  private static final PoolLogger LOGGER =
      PoolLoggerFactory.getLogger(DatabaseMetaDataHandler.class);

  private static final String TOSTRING_METHOD = "toString";
  private static final String EQUALS_METHOD = "equals";
  private static final String HASHCODE_METHOD = "hashCode";
  private static final String GETCONNECTION_METHOD = "getConnection";

  private Connection con;
  private DatabaseMetaData metaData;

  DatabaseMetaDataHandler(Connection con, DatabaseMetaData metaData) {
    this.con = con;
    this.metaData = metaData;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object result;
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
        result = method.invoke(metaData, args);
      } catch (InvocationTargetException e) {
        LOGGER.error("invoke " + method + " error:", e);
        throw e.getTargetException();
      }
    }
    return result;
  }

  /**
   * User may want to ask the DatabaseMetaData for the connection, so we will return the pool
   * connection instead of it.
   *
   * @see DatabaseMetaData#getConnection
   */
  private Connection getConnection() {
    return con;
  }
}
