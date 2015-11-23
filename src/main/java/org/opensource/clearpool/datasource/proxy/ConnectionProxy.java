package org.opensource.clearpool.datasource.proxy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.XAConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.datasource.connection.CommonConnection;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

/**
 * This class is the proxy of connection.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConnectionProxy implements Comparable<ConnectionProxy> {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ConnectionProxy.class);

  // Dummy value to associate with an Object in the backing Map
  private static final Object PRESENT = new Object();

  private final ConnectionPoolManager pool;
  private final Connection connection;
  private final XAConnection xaConnection;

  private Map<String, Object> sqlMap = new WeakHashMap<String, Object>();
  private int sqlCount;

  boolean autoCommit;
  String catalog;
  int holdability;
  boolean readOnly;
  int transactionIsolation;

  boolean newAutoCommit;
  String newCatalog;
  int newHoldability;
  boolean newReadOnly;
  int newTransactionIsolation;

  Savepoint savepoint;

  public ConnectionProxy(ConnectionPoolManager pool, CommonConnection cmnCon) {
    this.pool = pool;
    connection = cmnCon.getConnection();
    xaConnection = cmnCon.getXAConnection();
    this.saveValue();
  }

  /**
   * Save the value which may changed after using.
   */
  private void saveValue() {
    try {
      newAutoCommit = autoCommit = connection.getAutoCommit();
      newCatalog = catalog = connection.getCatalog();
      newHoldability = holdability = connection.getHoldability();
      newReadOnly = readOnly = connection.isReadOnly();
      newTransactionIsolation = transactionIsolation = connection.getTransactionIsolation();
    } catch (SQLException e) {
      LOGGER.error("it calls a exception when we saveValue: ", e);
      throw new ConnectionPoolException(e);
    }
  }

  /**
   * Reset the value which had been changed.
   */
  void reset() throws SQLException {
    boolean autoCommit = connection.getAutoCommit();
    if (!autoCommit) {
      // we have to roll back commit before we return connection to
      // the pool
      connection.rollback();
    }

    if (newAutoCommit != this.autoCommit) {
      connection.setAutoCommit(this.autoCommit);
    }
    if (newCatalog != catalog) {
      connection.setCatalog(catalog);
    }
    if (newHoldability != holdability) {
      connection.setHoldability(holdability);
    }
    if (newReadOnly != readOnly) {
      connection.setReadOnly(readOnly);
    }
    if (newTransactionIsolation != transactionIsolation) {
      connection.setTransactionIsolation(transactionIsolation);
    }
    if (savepoint != null) {
      connection.releaseSavepoint(savepoint);
    }
    // clear warnings before return connection to pool
    connection.clearWarnings();
  }

  public Connection getConnection() {
    return connection;
  }

  public XAConnection getXaConnection() {
    return xaConnection;
  }

  /**
   * Return connection to the pool.
   */
  public void close() {
    try {
      // reset it
      this.reset();
    } catch (SQLException e) {
      LOGGER.error("it calls a exception when we reset the connection: ", e);
      this.reallyClose();
      return;
    }
    pool.entryPool(this);
  }

  /**
   * Really close the connection.
   */
  private void reallyClose() {
    // close connection
    pool.closeConnection(this);
    pool.decrementPoolSize();
    pool.incrementOneConnection();
  }

  public ConfigurationVO getCfgVO() {
    return pool.getCfgVO();
  }

  /**
   * deal if we need to increment {@link #sqlCount};
   *
   * @param sql
   */
  public void dealSqlCount(String sql) {
    if (sqlMap.put(sql, PRESENT) == null) {
      int count = sqlCount;
      count++;
      // in case sqlCount be negative
      if (count > 0) {
        sqlCount = count;
      }
    }
  }

  @Override
  public int compareTo(ConnectionProxy anoConnProxy) {
    int x = sqlCount;
    int y = anoConnProxy.sqlCount;
    return x < y ? -1 : x == y ? 0 : 1;
  }
}
