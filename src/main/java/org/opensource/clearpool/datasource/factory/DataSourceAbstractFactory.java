package org.opensource.clearpool.datasource.factory;

import javax.sql.CommonDataSource;
import javax.sql.PooledConnection;

import org.opensource.clearpool.datasource.AbstractDataSource;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;

/**
 * This is a abstract factory,it is used to build product tree,such as
 * dataSource and pooledConnection.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public abstract class DataSourceAbstractFactory {
	public abstract AbstractDataSource createDataSource(
			CommonDataSource commonDataSource);

	public abstract PooledConnection createPooledConnection(
			ConnectionProxy conProxy);
}
