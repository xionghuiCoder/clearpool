package org.opensource.clearpool.core;

import java.sql.SQLException;

import javax.sql.PooledConnection;

import org.opensource.clearpool.exception.ConnectionPoolException;

/**
 * This class is created when we just get one database configuration.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class UniquePoolContainer extends CommonPoolContainer {

	UniquePoolContainer() {
		this.poolKind = "UniquePool";
	}

	@Override
	public PooledConnection getConnection() throws SQLException {
		PooledConnection pooledConnection = null;
		for (ConnectionPoolManager pool : poolMap.values()) {
			// get pool connection
			pooledConnection = pool.exitPool();
			break;
		}
		return pooledConnection;
	}

	@Override
	public PooledConnection getConnection(String name) throws SQLException {
		name = (name == null ? null : name.trim());
		// if name is unmatched,return null
		if (poolMap.containsKey(name)) {
			return this.getConnection();
		}
		throw new ConnectionPoolException("the pool " + name
				+ " is not existed");
	}

	@Override
	public void remove(String name) {
		// do nothing if name is unmatched
		if (name == null || poolMap.containsKey(name.trim())) {
			this.remove();
		}
	}
}
