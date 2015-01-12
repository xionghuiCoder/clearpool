package org.opensource.clearpool.datasource.factory;

import javax.sql.CommonDataSource;
import javax.sql.PooledConnection;

import org.opensource.clearpool.datasource.AbstractDataSource;
import org.opensource.clearpool.datasource.JDBCDataSource;
import org.opensource.clearpool.datasource.JDBCDataSourceWrapper;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.datasource.proxy.PoolConnectionImpl;

public class JDBCDataSourceFactory extends DataSourceAbstractFactory {

  @Override
  public AbstractDataSource createDataSource(CommonDataSource commonDataSource) {
    return new JDBCDataSourceWrapper((JDBCDataSource) commonDataSource);
  }

  @Override
  public PooledConnection createPooledConnection(ConnectionProxy conProxy) {
    PooledConnection pooledConnection = new PoolConnectionImpl(conProxy);
    return pooledConnection;
  }
}
