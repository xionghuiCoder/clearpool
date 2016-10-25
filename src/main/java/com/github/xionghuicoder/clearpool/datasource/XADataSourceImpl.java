package com.github.xionghuicoder.clearpool.datasource;

import java.sql.SQLException;

import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.jta.xa.XAConnectionImpl;

public class XADataSourceImpl extends AbstractDataSource {
  private final XADataSource ds;

  public XADataSourceImpl(XADataSource ds) {
    this.ds = ds;
  }

  @Override
  public CommonConnection getCommonConnection() throws SQLException {
    XAConnection xaCon = this.ds.getXAConnection();
    CommonConnection cmnCon = new XAConnectionWrapper(xaCon);
    return cmnCon;
  }

  @Override
  public PooledConnection createPooledConnection(ConnectionProxy conProxy) throws SQLException {
    PooledConnection pooledConnection = new XAConnectionImpl(conProxy);
    return pooledConnection;
  }
}
