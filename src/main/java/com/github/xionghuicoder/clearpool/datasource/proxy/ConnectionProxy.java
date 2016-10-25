package com.github.xionghuicoder.clearpool.datasource.proxy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.XAConnection;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.core.ConfigurationVO;
import com.github.xionghuicoder.clearpool.core.ConnectionPoolManager;
import com.github.xionghuicoder.clearpool.datasource.CommonConnection;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;

/**
 * connection的代理类
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConnectionProxy implements Comparable<ConnectionProxy> {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ConnectionProxy.class);

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
   * 使用connection前保存connection属性
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
   * reset connection属性
   */
  void reset() throws SQLException {
    boolean autoCommit = this.connection.getAutoCommit();
    if (!autoCommit) {
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
    this.connection.clearWarnings();
  }

  public Connection getConnection() {
    return this.connection;
  }

  public XAConnection getXaConnection() {
    return this.xaConnection;
  }

  public void close() {
    try {
      this.reset();
    } catch (SQLException e) {
      LOGGER.error("reset error: ", e);
      this.reallyClose();
      return;
    }
    this.pool.entryPool(this);
  }

  private void reallyClose() {
    this.pool.closeConnection(this);
    this.pool.decrementPoolSize();
    this.pool.incrementOneConnection();
  }

  public ConfigurationVO getCfgVO() {
    return this.pool.getCfgVO();
  }

  public void dealSqlCount(String sql) {
    if (this.sqlMap.put(sql, PRESENT) == null) {
      int count = this.sqlCount;
      count++;
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
