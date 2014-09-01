package org.opensource.clearpool.core;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.configuration.XMLConfiguration;
import org.opensource.clearpool.console.MBeanFacade;
import org.opensource.clearpool.core.hook.CommonHook;
import org.opensource.clearpool.core.hook.IdleCheckHook;
import org.opensource.clearpool.core.hook.IdleGarbageHook;
import org.opensource.clearpool.core.hook.PoolGrowHook;
import org.opensource.clearpool.core.hook.ShutdownHook;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.util.PoolLatchUtil;

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
	private static final Lock lock = new ReentrantLock();

	private static Thread idleGarbageHook;
	private static Thread poolGrowHook;
	protected static Thread idleCheckHook;

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
			// if container is not null,it must be a distributed pool,so we
			// should fill it instead of create it.
			if (idleGarbageHook == null) {
				container = new DistributedPoolContainer();
			}
			boolean needIdleCheck = container.initPool(cfgMap);
			// start hooks
			startHooks(needIdleCheck);
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
	 * init pool chain and start ShutdownHook,IdleGarbageHook and
	 * IdleCheckHook(if necessary).
	 */
	private static void startHooks(boolean needIdleCheck) {
		Collection<ConnectionPoolManager> poolCollection = poolMap.values();
		CommonHook.initPoolChain(poolCollection);
		if (needIdleCheck && idleCheckHook == null) {
			PoolLatchUtil.initIdleCheckLatch();
			// start IdleCheckHook
			idleCheckHook = IdleCheckHook.startHook();
		}
		if (idleGarbageHook == null) {
			// start MBean
			MBeanFacade.start();
			// register ShutdownHook
			ShutdownHook.registerHook();
			// start IdleGarbageHook
			idleGarbageHook = IdleGarbageHook.startHook();
			// create a daemon thread to get connection if needed
			poolGrowHook = PoolGrowHook.startHook();
		}
	}

	/**
	 * Interrupt all the daemon hooks.
	 */
	static void destoryHooks() {
		if (poolGrowHook != null) {
			poolGrowHook.interrupt();
			poolGrowHook = null;
		}
		if (idleGarbageHook != null) {
			idleGarbageHook.interrupt();
			idleGarbageHook = null;
		}
	}

	protected abstract boolean initPool(Map<String, ConfigurationVO> cfgMap);

	protected abstract PooledConnection getConnection() throws SQLException;

	protected abstract PooledConnection getConnection(String name)
			throws SQLException;

	protected abstract void remove(String name);

	protected abstract void remove();
}
