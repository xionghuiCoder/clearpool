package org.opensource.clearpool.datasource.proxy;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import org.opensource.clearpool.datasource.proxy.dynamic.ProxyFactory;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

/**
 * This class is the implement of PooledConnection.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class PoolConnectionImpl implements PooledConnection, Connection {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(PoolConnectionImpl.class);

  private Connection connection;
  protected final ConnectionProxy conProxy;

  private List<ConnectionEventListener> connectionEventListeners;
  private List<StatementEventListener> statementEventListeners;

  private final Set<Statement> statementSet = new HashSet<Statement>();

  private volatile boolean isClosed;

  public PoolConnectionImpl(ConnectionProxy conProxy) {
    connection = conProxy.getConnection();
    this.conProxy = conProxy;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface == null) {
      return null;
    }
    try {
      // This works for classes that aren't actually wrapping
      // anything
      return iface.cast(this);
    } catch (ClassCastException e) {
      throw new ConnectionPoolException("Unable to unwrap to " + iface.toString());
    }
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    if (iface == null) {
      return false;
    }
    return iface.isInstance(this);
  }

  @Override
  public Statement createStatement() throws SQLException {
    this.checkState();
    Statement statement = null;
    try {
      statement = connection.createStatement();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    Statement statementProxy = this.createProxyStatement(statement, null);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    this.checkState();
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(sql);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    PreparedStatement statementProxy =
        (PreparedStatement) this.createProxyStatement(statement, sql);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    this.checkState();
    CallableStatement statement = null;
    try {
      statement = connection.prepareCall(sql);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    CallableStatement statementProxy =
        (CallableStatement) this.createProxyStatement(statement, sql);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    this.checkState();
    return connection.nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    this.checkState();
    try {
      connection.setAutoCommit(autoCommit);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    conProxy.newAutoCommit = autoCommit;
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    this.checkState();
    return connection.getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
    this.checkState();
    try {
      connection.commit();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
  }

  @Override
  public void rollback() throws SQLException {
    this.checkState();
    try {
      connection.rollback();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
  }

  @Override
  public boolean isClosed() throws SQLException {
    this.checkState();
    return connection.isClosed();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    this.checkState();
    DatabaseMetaData metaData = null;
    try {
      metaData = connection.getMetaData();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    DatabaseMetaData metaDataProxy = ProxyFactory.createProxyDatabaseMetaData(this, metaData);
    return metaDataProxy;
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    this.checkState();
    try {
      connection.setReadOnly(readOnly);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    conProxy.newReadOnly = readOnly;
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    this.checkState();
    return connection.isReadOnly();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    this.checkState();
    try {
      connection.setCatalog(catalog);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    conProxy.newCatalog = catalog;
  }

  @Override
  public String getCatalog() throws SQLException {
    this.checkState();
    return connection.getCatalog();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    this.checkState();
    try {
      connection.setTransactionIsolation(level);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    conProxy.newTransactionIsolation = level;
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    this.checkState();
    return connection.getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    this.checkState();
    return connection.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    this.checkState();
    try {
      connection.clearWarnings();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    this.checkState();
    Statement statement = null;
    try {
      statement = connection.createStatement(resultSetType, resultSetConcurrency);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    Statement statementProxy = this.createProxyStatement(statement, null);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    this.checkState();
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    PreparedStatement statementProxy =
        (PreparedStatement) this.createProxyStatement(statement, sql);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    this.checkState();
    CallableStatement statement = null;
    try {
      statement = connection.prepareCall(sql, resultSetType, resultSetConcurrency);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    CallableStatement statementProxy =
        (CallableStatement) this.createProxyStatement(statement, sql);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    this.checkState();
    return connection.getTypeMap();
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    this.checkState();
    try {
      connection.setTypeMap(map);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    this.checkState();
    try {
      connection.setHoldability(holdability);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    conProxy.newHoldability = holdability;
  }

  @Override
  public int getHoldability() throws SQLException {
    this.checkState();
    return connection.getHoldability();
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    this.checkState();
    Savepoint savepoint = null;
    try {
      savepoint = connection.setSavepoint();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    conProxy.savepoint = savepoint;
    return savepoint;
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    this.checkState();
    Savepoint savepoint = null;
    try {
      savepoint = connection.setSavepoint(name);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    conProxy.savepoint = savepoint;
    return savepoint;
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    this.checkState();
    try {
      connection.rollback();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    this.checkState();
    try {
      connection.releaseSavepoint(savepoint);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    this.checkState();
    Statement statement = null;
    try {
      statement =
          connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    Statement statementProxy = this.createProxyStatement(statement, null);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    this.checkState();
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(sql, resultSetType, resultSetConcurrency,
          resultSetHoldability);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    PreparedStatement statementProxy =
        (PreparedStatement) this.createProxyStatement(statement, sql);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    this.checkState();
    CallableStatement statement = null;
    try {
      statement =
          connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    CallableStatement statementProxy =
        (CallableStatement) this.createProxyStatement(statement, sql);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    this.checkState();
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(sql, autoGeneratedKeys);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    PreparedStatement statementProxy =
        (PreparedStatement) this.createProxyStatement(statement, sql);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    this.checkState();
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(sql, columnIndexes);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    PreparedStatement statementProxy =
        (PreparedStatement) this.createProxyStatement(statement, sql);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    this.checkState();
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(sql, columnNames);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    PreparedStatement statementProxy =
        (PreparedStatement) this.createProxyStatement(statement, sql);
    statementSet.add(statementProxy);
    return statementProxy;
  }

  /**
   * This is a template method for XAConnectionImpl
   */
  protected Statement createProxyStatement(Statement statement, String sql) {
    Statement statementProxy = ProxyFactory.createProxyStatement(statement, this, conProxy, sql);
    return statementProxy;
  }

  @Override
  public Clob createClob() throws SQLException {
    this.checkState();
    Clob clob = null;
    try {
      clob = connection.createClob();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    return clob;
  }

  @Override
  public Blob createBlob() throws SQLException {
    this.checkState();
    Blob blob = null;
    try {
      blob = connection.createBlob();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    return blob;
  }

  @Override
  public NClob createNClob() throws SQLException {
    this.checkState();
    NClob nclob = null;
    try {
      nclob = connection.createNClob();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    return nclob;
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    this.checkState();
    SQLXML sqlXML = null;
    try {
      sqlXML = connection.createSQLXML();
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    return sqlXML;
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    this.checkState();
    return connection.isValid(timeout);
  }

  @Override
  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    if (isClosed) {
      throw new SQLClientInfoException();
    }
    connection.setClientInfo(name, value);
  }

  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    if (isClosed) {
      throw new SQLClientInfoException();
    }
    connection.setClientInfo(properties);
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    this.checkState();
    return connection.getClientInfo(name);
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    this.checkState();
    return connection.getClientInfo();
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    this.checkState();
    Array array = null;
    try {
      array = connection.createArrayOf(typeName, elements);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    return array;
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    this.checkState();
    Struct struct = null;
    try {
      struct = connection.createStruct(typeName, attributes);
    } catch (SQLException ex) {
      this.handleException(ex);
    }
    return struct;
  }

  public void setSchema(String schema) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public String getSchema() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void abort(Executor executor) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public int getNetworkTimeout() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Connection getConnection() throws SQLException {
    return this;
  }

  @Override
  public void close() throws SQLException {
    if (isClosed) {
      return;
    }
    isClosed = true;
    for (Statement stmt : statementSet) {
      stmt.close();
    }
    statementSet.clear();
    if (connectionEventListeners != null) {
      ConnectionEvent event = new ConnectionEvent(this);
      for (ConnectionEventListener listener : connectionEventListeners) {
        listener.connectionClosed(event);
      }
      connectionEventListeners = null;
    }
    if (statementEventListeners != null) {
      statementEventListeners = null;
    }
    connection = null;
    conProxy.close();
  }

  /**
   * Check if pool connection closed.
   */
  protected void checkState() throws SQLException {
    if (isClosed) {
      throw new SQLException("connection closed");
    }
  }

  @Override
  public void addConnectionEventListener(ConnectionEventListener listener) {
    if (connectionEventListeners == null) {
      connectionEventListeners = new ArrayList<ConnectionEventListener>();
    }
    connectionEventListeners.add(listener);
  }

  @Override
  public void removeConnectionEventListener(ConnectionEventListener listener) {
    if (connectionEventListeners != null) {
      connectionEventListeners.remove(listener);
    }
  }

  @Override
  public void addStatementEventListener(StatementEventListener listener) {
    if (statementEventListeners == null) {
      statementEventListeners = new ArrayList<StatementEventListener>();
    }
    statementEventListeners.add(listener);
  }

  public List<StatementEventListener> getStatementEventListeners() {
    return statementEventListeners;
  }

  @Override
  public void removeStatementEventListener(StatementEventListener listener) {
    if (statementEventListeners != null) {
      statementEventListeners.remove(listener);
    }
  }

  /**
   * Handle the SQLException
   */
  private SQLException handleException(SQLException e) throws SQLException {
    LOGGER.error("handleException get SQLException: ", e);
    ConnectionEvent event = new ConnectionEvent(this, e);
    if (connectionEventListeners != null) {
      for (ConnectionEventListener eventListener : connectionEventListeners) {
        eventListener.connectionErrorOccurred(event);
      }
    }
    throw e;
  }

  /**
   * Remove statement
   */
  public void removeStatement(Statement statement) {
    statementSet.remove(statement);
  }
}
