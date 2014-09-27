package org.opensource.clearpool.core;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.console.MBeanFacade;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

/**
 * This class is created when we get more than a database configuration.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class DistributedPoolContainer extends CommonPoolContainer {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(DistributedPoolContainer.class);

	/**
	 * Init pool and start MBean if necessary.
	 */
	@Override
	public void initPool(Map<String, ConfigurationVO> cfgMap) {
		long begin = System.currentTimeMillis();
		for (Map.Entry<String, ConfigurationVO> e : cfgMap.entrySet()) {
			ConfigurationVO cfgVO = e.getValue();
			ConnectionPoolManager pool = new ConnectionPoolManager(cfgVO);
			try {
				pool.initPool();
			} catch (Throwable t) {
				// in case memory reveal.
				pool.remove();
				throw new ConnectionPoolException(t);
			}
			String poolName = e.getKey();
			poolMap.put(poolName, pool);
			String alias = cfgVO.getAlias();
			String mbeanName = "org.clearpool:type=Pool"
					+ (alias == null ? "" : ",name=" + alias);
			MBeanFacade.registerMBean(pool, mbeanName, poolName);
		}
		long cost = System.currentTimeMillis() - begin;
		LOG.info("initPool cost " + cost + "ms");
	}

	/**
	 * distribute pool don't support {@link #getConnection},we should use
	 * {@link #getConnection(String)} to get connection.
	 */
	@Override
	public PooledConnection getConnection() throws SQLException {
		if (poolMap.size() > 1) {
			return this.getConnection(null);
		}
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
		ConnectionPoolManager pool = poolMap.get(name);
		if (pool == null) {
			return null;
		}
		// get pool connection
		PooledConnection pooledConnection = pool.exitPool();
		return pooledConnection;
	}

	@Override
	public void remove(String name) {
		name = (name == null ? null : name.trim());
		ConnectionPoolManager realPool = poolMap.remove(name);
		if (realPool != null) {
			MBeanFacade.unregisterMBean(name);
			realPool.remove();
		}
	}

	/**
	 * Remove the pool
	 */
	@Override
	public void remove() {
		Map<String, ConnectionPoolManager> tempMap = poolMap;
		// reset pool map
		poolMap = new HashMap<String, ConnectionPoolManager>();
		for (Entry<String, ConnectionPoolManager> e : tempMap.entrySet()) {
			String poolName = e.getKey();
			MBeanFacade.unregisterMBean(poolName);
			ConnectionPoolManager pool = e.getValue();
			pool.remove();
		}
	}
}
