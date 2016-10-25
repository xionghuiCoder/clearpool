package com.github.xionghuicoder.clearpool.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.PooledConnection;

import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.datasource.proxy.PoolConnectionImpl;

public class JDBCDataSourceWrapper extends AbstractDataSource {
  private final JDBCDataSource jdbcDs;

  public JDBCDataSourceWrapper(JDBCDataSource jdbcDs) {
    this.jdbcDs = jdbcDs;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return this.jdbcDs.getConnection();
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
