package com.github.xionghuicoder.clearpool.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;

public abstract class AbstractDataSource implements DataSource {

  @Override
  public Connection getConnection() throws SQLException {
    throw new UnsupportedOperationException();
  }

  public CommonConnection getCommonConnection() throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    throw new UnsupportedOperationException();
  }

  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public PooledConnection createPooledConnection(ConnectionProxy conProxy) throws SQLException {
    throw new UnsupportedOperationException();
  }

  protected class ConnectionWrapper extends CommonConnection {
    private Connection con;

    public ConnectionWrapper(Connection con) {
      this.con = con;
    }

    @Override
    public Connection getConnection() {
      return this.con;
    }

    @Override
    public XAConnection getXAConnection() {
      return null;
    }
  }

  protected class XAConnectionWrapper extends CommonConnection {
    private XAConnection xaCon;

    public XAConnectionWrapper(XAConnection xaCon) {
      this.xaCon = xaCon;
    }

    @Override
    public Connection getConnection() {
      Connection con;
      try {
        con = this.xaCon.getConnection();
      } catch (SQLException e) {
        throw new ConnectionPoolException(e);
      }
      return con;
    }

    @Override
    public XAConnection getXAConnection() {
      return this.xaCon;
    }
  }
}
