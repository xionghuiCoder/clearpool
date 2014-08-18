package org.opensource.clearpool.core;

import java.sql.SQLException;

import javax.sql.PooledConnection;

import org.opensource.clearpool.console.MBeanFacade;
import org.opensource.clearpool.exception.ConnectionPoolException;

/**
 * This class is created when we get more than a database configuration.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class DistributedPoolContainer extends CommonPoolContainer {

	DistributedPoolContainer() {
		this.poolKind = "DistributedPool";
	}

	/**
	 * distribute pool don't support {@link #getConnection},we should use
	 * {@link #getConnection(String)} to get connection.
	 */
	@Override
	public PooledConnection getConnection() throws SQLException {
		throw new UnsupportedOperationException(
				"we should get connection by a name if the pool is distributed");
	}

	@Override
	public PooledConnection getConnection(String name) throws SQLException {
		name = (name == null ? null : name.trim());
		ConnectionPoolManager pool = poolMap.get(name);
		if (pool == null) {
			throw new ConnectionPoolException("the pool " + name
					+ " is not existed");
		}
		// get pool connection
		PooledConnection pooledConnection = pool.exitPool();
		return pooledConnection;
	}

	@Override
	public void remove(String name) {
		name = (name == null ? null : name.trim());
		// interrupt PoolGrowHook
		Thread poolGrowHook = poolGrowHookMap.remove(name);
		if (poolGrowHook != null) {
			poolGrowHook.interrupt();
		}
		ConnectionPoolManager realPool = poolMap.remove(name);
		if (realPool != null) {
			MBeanFacade.UnregisterMBean(name);
			realPool.remove();
		}
	}
}
