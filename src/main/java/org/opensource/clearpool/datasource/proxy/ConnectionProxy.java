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
import org.opensource.clearpool.logging.PoolLog;
import org.opensource.clearpool.logging.PoolLogFactory;

/**
 * This class is the proxy of connection.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConnectionProxy implements Comparable<ConnectionProxy> {
  private static final PoolLog LOG = PoolLogFactory.getLog(ConnectionProxy.class);

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
    this.connection = cmnCon.getConnection();
    this.xaConnection = cmnCon.getXAConnection();
    this.saveValue();
  }

  /**
   * Save the value which may changed after using.
   */
  private void saveValue() {
    try {
      this.newAutoCommit = this.autoCommit = this.connection.getAutoCommit();
      this.newCatalog = this.catalog = this.connection.getCatalog();
      this.newHoldability = this.holdability = this.connection.getHoldability();
      this.newReadOnly = this.readOnly = this.connection.isReadOnly();
      this.newTransactionIsolation =
          this.transactionIsolation = this.connection.getTransactionIsolation();
    } catch (SQLException e) {
      throw new ConnectionPoolException(e);
    }
  }

  /**
   * Reset the value which had been changed.
   */
  void reset() throws SQLException {
    boolean autoCommit = this.connection.getAutoCommit();
    if (!autoCommit) {
      // we have to roll back commit before we return connection to
      // the pool
      this.connection.rollback();
    }

    if (this.newAutoCommit != this.autoCommit) {
      this.connection.setAutoCommit(this.autoCommit);
    }
    if (this.newCatalog != this.catalog) {
      this.connection.setCatalog(this.catalog);
    }
    if (this.newHoldability != this.holdability) {
      this.connection.setHoldability(this.holdability);
    }
    if (this.newReadOnly != this.readOnly) {
      this.connection.setReadOnly(this.readOnly);
    }
    if (this.newTransactionIsolation != this.transactionIsolation) {
      this.connection.setTransactionIsolation(this.transactionIsolation);
    }
    if (this.savepoint != null) {
      this.connection.releaseSavepoint(this.savepoint);
    }
    // clear warnings before return connection to pool
    this.connection.clearWarnings();
  }

  public Connection getConnection() {
    return this.connection;
  }

  public XAConnection getXaConnection() {
    return this.xaConnection;
  }

  /**
   * Return connection to the pool.
   */
  public void close() {
    try {
      // reset it
      this.reset();
    } catch (SQLException e) {
      LOG.warn("it calls a exception when we reset the connection");
      this.reallyClose();
      return;
    }
    this.pool.entryPool(this);
  }

  /**
   * Really close the connection.
   */
  private void reallyClose() {
    // close connection
    this.pool.closeConnection(this);
    this.pool.decrementPoolSize();
    this.pool.incrementOneConnection();
  }

  public ConfigurationVO getCfgVO() {
    return this.pool.getCfgVO();
  }

  /**
   * deal if we need to increment {@link #sqlCount};
   * 
   * @param sql
   */
  public void dealSqlCount(String sql) {
    if (this.sqlMap.put(sql, PRESENT) == null) {
      int count = this.sqlCount;
      count++;
      // in case sqlCount be negative
      if (count > 0) {
        this.sqlCount = count;
      }
    }
  }

  @Override
  public int compareTo(ConnectionProxy anoConnProxy) {
    int x = this.sqlCount;
    int y = anoConnProxy.sqlCount;
    return x < y ? -1 : x == y ? 0 : 1;
  }
}
