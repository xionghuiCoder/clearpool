package org.opensource.clearpool.datasource.factory;

import javax.sql.CommonDataSource;
import javax.sql.PooledConnection;
import javax.sql.XADataSource;

import org.opensource.clearpool.datasource.AbstractDataSource;
import org.opensource.clearpool.datasource.XADataSourceImpl;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.jta.xa.XAConnectionImpl;

public class XADataSourceFactory extends DataSourceAbstractFactory {

  @Override
  public AbstractDataSource createDataSource(CommonDataSource commonDataSource) {
    return new XADataSourceImpl((XADataSource) commonDataSource);
  }

  @Override
  public PooledConnection createPooledConnection(ConnectionProxy conProxy) {
    PooledConnection pooledConnection = new XAConnectionImpl(conProxy);
    return pooledConnection;
  }
}
