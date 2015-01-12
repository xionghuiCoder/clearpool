package org.opensource.clearpool.datasource.factory;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import org.opensource.clearpool.datasource.AbstractDataSource;
import org.opensource.clearpool.datasource.DataSourceImpl;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.datasource.proxy.PoolConnectionImpl;

public class DataSourceFactory extends DataSourceAbstractFactory {

  @Override
  public AbstractDataSource createDataSource(CommonDataSource commonDataSource) {
    return new DataSourceImpl((DataSource) commonDataSource);
  }

  @Override
  public PooledConnection createPooledConnection(ConnectionProxy conProxy) {
    PooledConnection pooledConnection = new PoolConnectionImpl(conProxy);
    return pooledConnection;
  }
}
