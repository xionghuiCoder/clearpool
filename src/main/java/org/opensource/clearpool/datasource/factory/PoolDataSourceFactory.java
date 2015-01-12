package org.opensource.clearpool.datasource.factory;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.opensource.clearpool.datasource.AbstractDataSource;
import org.opensource.clearpool.datasource.PoolDataSource;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.datasource.proxy.PoolConnectionImpl;

public class PoolDataSourceFactory extends DataSourceAbstractFactory {

  @Override
  public AbstractDataSource createDataSource(CommonDataSource commonDataSource) {
    return new PoolDataSource((ConnectionPoolDataSource) commonDataSource);
  }

  @Override
  public PooledConnection createPooledConnection(ConnectionProxy conProxy) {
    PooledConnection pooledConnection = new PoolConnectionImpl(conProxy);
    return pooledConnection;
  }
}
