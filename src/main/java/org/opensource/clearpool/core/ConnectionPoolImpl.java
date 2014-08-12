package org.opensource.clearpool.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.console.MBeanFacade;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolStateException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

/**
 * The pool provide two kind of database connection pool.Please check
 * {@link CommonPoolContainer} if you want the details.
 * 
 * The pool have 3 different states here.We can do nothing if the pool is
 * unInitialized or destroyed, and we can do everything if pool is initialized.
 * 
 * Note:this class is a singleton class.The reason that we don't use ENUM
 * singleton model which is recommend by Joshua Bloch is because ENUM instance
 * cann't be released.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class ConnectionPoolImpl implements IConnectionPool {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(ConnectionPoolImpl.class);

	// the INSTANCE should be the front of the SINGLETON_MARK
	private static ConnectionPoolImpl instance = new ConnectionPoolImpl();

	// the SINGLETON_MARK make sure the ClearPool is singleton
	private final static boolean SINGLETON_MARK;

	static {
		SINGLETON_MARK = true;
	}

	/**
	 * we get 3 states here.
	 * 
	 * state=0:unInitialized; state=1:initialized; state=2:destroyed.
	 * 
	 */
	private volatile int state = 0;

	// it is used to handle the pool.
	static volatile CommonPoolContainer poolContainer;

	/**
	 * Hide the constructor
	 */
	private ConnectionPoolImpl() {
		// whenever we invoke the constructor by reflection,we throw a
		// ConnectionPoolException.
		if (SINGLETON_MARK) {
			throw new ConnectionPoolException("create ClearPool illegal");
		}
	}

	/**
	 * Get a instance of connection pool
	 */
	static ConnectionPoolImpl getInstance() {
		ConnectionPoolImpl tempInstance = instance;
		if (tempInstance == null) {
			throw new ConnectionPoolStateException(
					"clearpool had been destroyed");
		}
		return tempInstance;
	}

	/**
	 * Init pool by the default path
	 */
	@Override
	public void init() {
		this.initPath(null);
	}

	/**
	 * Init pool by the given path
	 */
	@Override
	public void initPath(String path) {
		this.load(path, null);
	}

	/**
	 * Init pool by cfgMap
	 */
	@Override
	public void initMap(Map<String, ConfigurationVO> cfgMap) {
		this.load(null, cfgMap);
	}

	/**
	 * Init pool by path or cfgMap.
	 * 
	 * Note:one of path and cfgMap is null.
	 */
	private void load(String path, Map<String, ConfigurationVO> cfgMap) {
		this.checkDestroyed();
		long begin = System.currentTimeMillis();
		// load cfg to init pool
		CommonPoolContainer container = CommonPoolContainer.load(path, cfgMap);
		if (container != null) {
			poolContainer = container;
			LOG.info("connection pool initialized.it cost "
					+ (System.currentTimeMillis() - begin) + "ms\n");
		}
		// initialized
		this.state = 1;
	}

	PooledConnection getPooledConnection() throws SQLException {
		this.checkState();
		return poolContainer.getConnection();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return this.getPooledConnection().getConnection();
	}

	@Override
	public Connection getConnection(String name) throws SQLException {
		this.checkState();
		return poolContainer.getConnection(name).getConnection();
	}

	@Override
	public void close(String name) {
		this.checkState();
		CommonPoolContainer tempContainer = poolContainer;
		if (tempContainer == null) {
			return;
		}
		tempContainer.remove(name);
		LOG.info("remove pool " + name);
	}

	@Override
	public void close() {
		this.checkState();
		// remove all the pool
		this.removeAll();
		LOG.info("the pool is removed");
	}

	/**
	 * Remove all the pool.
	 */
	private void removeAll() {
		CommonPoolContainer tempContainer = poolContainer;
		if (tempContainer == null) {
			return;
		}
		// reset pool container
		poolContainer = null;
		tempContainer.remove();
	}

	@Override
	public void destory() {
		if (this.state == 2) {
			return;
		}
		this.state = 2;
		/**
		 * When we destroy the pool,we should destroy this singleton
		 * too,otherwise it will cause a memory reveal.
		 */
		instance = null;
		CommonPoolContainer.destory();
		MBeanFacade.stop();
		// remove all the pool
		this.removeAll();
		LOG.info("the pool is destroyed");
	}

	/**
	 * Check the state if it's initialized.
	 */
	private void checkInited() {
		if (this.state == 0) {
			throw new ConnectionPoolStateException(
					"clearpool haven't been initialized");
		}
	}

	/**
	 * Check the state if it's destroyed.
	 */
	private void checkDestroyed() {
		if (this.state == 2) {
			throw new ConnectionPoolStateException(
					"clearpool have been destroyed");
		}
	}

	/**
	 * Check the state if it's initialized and not destroyed.
	 */
	private void checkState() {
		this.checkInited();
		this.checkDestroyed();
	}
}
