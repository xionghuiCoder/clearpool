package org.opensource.clearpool.core;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.configuration.XMLConfiguration;
import org.opensource.clearpool.console.MBeanFacade;
import org.opensource.clearpool.core.hook.CommonHook;
import org.opensource.clearpool.core.hook.IdleCheckHook;
import org.opensource.clearpool.core.hook.ShutdownHook;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.PoolLatchUtil;

/**
 * This class is used to parse xml and manage hooks.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class ConnectionPoolContainer {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(ConnectionPoolContainer.class);

	private static final Lock lock = new ReentrantLock();

	private static Thread idleCheckHook;

	/**
	 * It carry the pool,and it make sure the pool should just be loaded one
	 * time.
	 */
	private static volatile Map<String, ConnectionPoolManager> poolMap = new HashMap<String, ConnectionPoolManager>();

	/**
	 * Load XML and init pool.
	 * 
	 * @param path
	 *            is used to find XML
	 * @return PoolContainer is {@link ConnectionPoolContainer} or
	 *         {@link UniquePoolContainer}
	 */
	static ConnectionPoolContainer load(String path,
			Map<String, ConfigurationVO> cfgMap) {
		if (cfgMap == null) {
			cfgMap = XMLConfiguration.getCfgVO(path);
		}
		Set<String> nameSet = cfgMap.keySet();
		// check if pool name repeat
		checkPoolName(nameSet);
		lock.lock();
		try {
			// double check
			checkPoolName(nameSet);
			ConnectionPoolContainer container = ConnectionPoolImpl.poolContainer;
			// if container is not null,it must be a distributed pool,so we
			// should fill it instead of create it.
			if (container == null) {
				container = new ConnectionPoolContainer();
			}
			container.initPool(cfgMap);
			// start hooks
			startHooks();
			// wait until hook running.
			PoolLatchUtil.await();
			return container;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Check if the name of the pool is already loaded.
	 */
	private static void checkPoolName(Set<String> nameSet) {
		if (poolMap.size() == 0) {
			return;
		}
		for (String name : nameSet) {
			// check if the pool name already init
			if (poolMap.get(name) != null) {
				throw new ConnectionPoolException("the pool " + name
						+ " had been loaded");
			}
		}
	}

	/**
	 * Init pool chain and start ShutdownHook,IdleGarbageHook.
	 */
	private static void startHooks() {
		Collection<ConnectionPoolManager> poolCollection = poolMap.values();
		CommonHook.initPoolChain(poolCollection);
		if (idleCheckHook == null) {
			// start MBean
			MBeanFacade.start();
			// register ShutdownHook
			ShutdownHook.registerHook();
			// start IdleCheckHook
			idleCheckHook = IdleCheckHook.startHook();
		}
	}

	/**
	 * Interrupt all the daemon hooks.
	 */
	static void destoryHooks() {
		if (idleCheckHook != null) {
			idleCheckHook.interrupt();
		}
	}

	/**
	 * Init pool and start MBean if necessary.
	 */
	private void initPool(Map<String, ConfigurationVO> cfgMap) {
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
	PooledConnection getConnection() throws SQLException {
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

	PooledConnection getConnection(String name) throws SQLException {
		name = (name == null ? null : name.trim());
		ConnectionPoolManager pool = poolMap.get(name);
		if (pool == null) {
			return null;
		}
		// get pool connection
		PooledConnection pooledConnection = pool.exitPool();
		return pooledConnection;
	}

	void remove(String name) {
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
	void remove() {
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
