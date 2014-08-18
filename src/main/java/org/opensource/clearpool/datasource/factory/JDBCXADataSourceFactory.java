package org.opensource.clearpool.datasource.factory;

import javax.sql.CommonDataSource;
import javax.sql.PooledConnection;

import org.opensource.clearpool.datasource.AbstractDataSource;
import org.opensource.clearpool.datasource.JDBCDataSource;
import org.opensource.clearpool.datasource.JDBCXADataSource;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.jta.xa.XAConnectionImpl;

public class JDBCXADataSourceFactory extends DataSourceAbstractFactory {

	@Override
	public AbstractDataSource createDataSource(
			CommonDataSource commonDataSource) {
		return new JDBCXADataSource((JDBCDataSource) commonDataSource);
	}

	@Override
	public PooledConnection createPooledConnection(ConnectionProxy conProxy) {
		PooledConnection pooledConnection = new XAConnectionImpl(conProxy);
		return pooledConnection;
	}
}