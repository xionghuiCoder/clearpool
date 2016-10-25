package com.github.xionghuicoder.clearpool.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.datasource.proxy.PoolConnectionImpl;

public class PoolDataSource extends AbstractDataSource {
  private final ConnectionPoolDataSource ds;

  public PoolDataSource(ConnectionPoolDataSource ds) {
    this.ds = ds;
  }

  @Override
  public Connection getConnection() throws SQLException {
    PooledConnection poolCon = this.ds.getPooledConnection();
    return poolCon.getConnection();
  }

  @Override
  public CommonConnection getCommonConnection() throws SQLException {
    Connection con = this.getConnection();
    CommonConnection cmnCon = new ConnectionWrapper(con);
    return cmnCon;
  }

  @Override
  public PooledConnection createPooledConnection(ConnectionProxy conProxy) throws SQLException {
    PooledConnection pooledConnection = new PoolConnectionImpl(conProxy);
    return pooledConnection;
  }
}
