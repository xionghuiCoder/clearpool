package org.opensource.clearpool.core;

import java.sql.SQLException;
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
import org.opensource.clearpool.core.hook.IdleCheckHook;
import org.opensource.clearpool.core.hook.IdleGarbageHook;
import org.opensource.clearpool.core.hook.PoolGrowHook;
import org.opensource.clearpool.core.hook.ShutdownHook;
import org.opensource.clearpool.core.util.PoolLatchUtil;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

/**
 * This class make sure there is just one thread to init the pool,the other
 * threads must wait until the thread return.
 * 
 * We have two kind of PoolContainer here.One of them is
 * {@link UniquePoolContainer},we build it if we get more than a cfgVO here.The
 * another one is {@link UniquePoolContainer},we build it if we only get one
 * cfgVO.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
abstract class CommonPoolContainer {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(CommonPoolContainer.class);

	private static final Lock lock = new ReentrantLock();

	// save the thread for remove and destroy to interrupt it.
	protected static volatile Map<String, Thread> poolGrowHookMap = new HashMap<>();
	private static Thread idleGarbageHook;
	private static Thread idleCheckHook;

	protected String poolKind;

	/**
	 * It carry the pool,and it make sure the pool should just be loaded one
	 * time.
	 */
	protected static volatile Map<String, ConnectionPoolManager> poolMap = new HashMap<>();

	/**
	 * Load XML and init pool.
	 * 
	 * @param path
	 *            is used to find XML
	 * @return PoolContainer is {@link DistributedPoolContainer} or
	 *         {@link UniquePoolContainer}
	 */
	static CommonPoolContainer load(String path,
			Map<String, ConfigurationVO> cfgMap) {
		if (ConnectionPoolImpl.poolContainer instanceof UniquePoolContainer) {
			throw new ConnectionPoolException(
					"this pool is a unique pool,please remove it before fill other pools.");
		}
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
			CommonPoolContainer container = ConnectionPoolImpl.poolContainer;
			// if the pool is a unique pool,we shouldn't fill it.
			if (container instanceof UniquePoolContainer) {
				throw new ConnectionPoolException(
						"this pool is a unique pool,please remove it before fill other pools.");
			}
			// if container is not null,it must be a distributed pool,so we
			// should fill it instead of create it.
			if (container == null) {
				if (cfgMap.size() == 1) {
					container = new UniquePoolContainer();
				} else {
					container = new DistributedPoolContainer();
				}
			}
			// start MBean
			MBeanFacade.start();
			PoolLatchUtil.initLatch(cfgMap.size());
			boolean needStartCheck = container.initPool(cfgMap);
			// start hooks
			startHook(needStartCheck);
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
	 * Init pool and start MBean if necessary.
	 */
	private boolean initPool(Map<String, ConfigurationVO> cfgMap) {
		// this is a sign of we need to start idle check.
		boolean needStartCheck = false;
		long begin = System.currentTimeMillis();
		for (Map.Entry<String, ConfigurationVO> e : cfgMap.entrySet()) {
			ConfigurationVO cfgVO = e.getValue();
			if (cfgVO.getKeepTestPeriod() > 0) {
				needStartCheck = true;
			}
			ConnectionPoolManager pool = new ConnectionPoolManager(cfgVO);
			String poolName = e.getKey();
			poolMap.put(poolName, pool);
			pool.initPool();
			// create a daemon thread to get connection if needed
			Thread connectionIncrementHook = PoolGrowHook.startHook(pool);
			poolGrowHookMap.put(poolName, connectionIncrementHook);
			String alias = cfgVO.getAlias();
			String mbeanName = "org.clearpool:type=Pool,kind=" + this.poolKind
					+ (alias == null ? "" : ",name=" + alias);
			MBeanFacade.registerMBean(pool, mbeanName, poolName);
		}
		long cost = System.currentTimeMillis() - begin;
		LOG.info("initPool cost " + cost + "ms");
		return needStartCheck;
	}

	/**
	 * start ShutdownHook,IdleGarbageHook and IdleCheckHook(if necessary).
	 */
	private static void startHook(boolean needStartCheck) {
		ConnectionPoolManager[] poolArray = poolMap.values().toArray(
				new ConnectionPoolManager[0]);
		// register ShutdownHook
		ShutdownHook.registerHook(poolArray);
		// start IdleGarbageHook
		idleGarbageHook = IdleGarbageHook.startHook(poolArray);
		if (needStartCheck) {
			// start IdleCheckHook
			idleCheckHook = IdleCheckHook.startHook(poolArray);
		} else {
			/**
			 * Maybe IdleCheckHook don't need to start,so don't forget count
			 * down latch,otherwise CommonPoolContainer will wait forever.
			 */
			PoolLatchUtil.countDownStartLatch();
		}
	}

	protected abstract PooledConnection getConnection() throws SQLException;

	protected abstract PooledConnection getConnection(String name)
			throws SQLException;

	protected abstract void remove(String name);

	/**
	 * Remove the pool
	 */
	void remove() {
		// interrupt PoolGrowHook
		interruptPoolGrowHook();
		Map<String, ConnectionPoolManager> tempMap = poolMap;
		// reset pool map
		poolMap = new HashMap<>();
		for (Entry<String, ConnectionPoolManager> e : tempMap.entrySet()) {
			String poolName = e.getKey();
			MBeanFacade.UnregisterMBean(poolName);
			ConnectionPoolManager pool = e.getValue();
			pool.remove();
		}
	}

	/**
	 * Interrupt all the daemon thread
	 */
	static void destory() {
		if (idleGarbageHook != null) {
			idleGarbageHook.interrupt();
		}
		if (idleCheckHook != null) {
			idleCheckHook.interrupt();
		}
	}

	/**
	 * Interrupt all the PoolGrowHook
	 */
	private static void interruptPoolGrowHook() {
		Map<String, Thread> tempMap = poolGrowHookMap;
		poolGrowHookMap = new HashMap<>();
		Thread[] threadArray = tempMap.values().toArray(new Thread[0]);
		for (Thread t : threadArray) {
			t.interrupt();
		}
	}
}
