package org.opensource.clearpool.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.opensource.clearpool.datasource.connection.ConnectionWrapper;
import org.opensource.clearpool.datasource.connection.CommonConnection;

public class PoolDataSource extends AbstractDataSource {
  private ConnectionPoolDataSource ds;

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
}
