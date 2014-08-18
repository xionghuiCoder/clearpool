package org.opensource.clearpool.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.core.chain.AtomicSingleChain;
import org.opensource.clearpool.core.chain.CommonChain;
import org.opensource.clearpool.datasource.connection.CommonConnection;
import org.opensource.clearpool.datasource.factory.DataSourceAbstractFactory;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.ThreadSleepUtil;

/**
 * This class save the connection to {@link #connectionChain},it's duty is to
 * manage the pool.
 * 
 * The pool will increment when there is no free connection in the pool's size
 * is less than the max pool size.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConnectionPoolManager {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(ConnectionPoolManager.class);

	// private final CommonChain<ConnectionProxy> connectionChain = new
	// AtomicSingleChain<ConnectionProxy>();
	private final CommonChain<ConnectionProxy> connectionChain;

	// save all the connection to close in case we shutdown the JVM
	private volatile Set<ConnectionProxy> connectionSet = new HashSet<>();

	// this is the sign of if the pool is removed
	private volatile boolean closed;

	private ConfigurationVO cfgVO;

	private volatile SQLException createConnectionException;

	// it is the count of how many connection the pool need to increment.
	private final AtomicInteger lackCount = new AtomicInteger();

	private final AtomicInteger poolSize = new AtomicInteger();

	// record the peak of the connection in the pool.
	private int peakPoolSize;

	ConnectionPoolManager(ConfigurationVO cfgVO) {
		this.cfgVO = cfgVO;
		// this.connectionChain = new
		// LockCircleChain<ConnectionProxy>(cfgVO.getMaxPoolSize());
		this.connectionChain = new AtomicSingleChain<ConnectionProxy>();
	}

	/**
	 * Init the pool by the corePoolsize of {@link #cfgVO}
	 */
	void initPool() {
		int coreSize = this.cfgVO.getCorePoolSize();
		this.lackCount.getAndAdd(coreSize);
		this.fillPool();
		String tableName = this.cfgVO.getTestTableName();
		if (tableName != null) {
			this.initTestTable();
		}
	}

	/**
	 * Return connection to the pool
	 */
	public void entryPool(ConnectionProxy conProxy) {
		this.connectionChain.add(conProxy);
	}

	/**
	 * Get a connection from the pool
	 */
	public PooledConnection exitPool() throws SQLException {
		ConnectionProxy conProxy = null;
		do {
			conProxy = this.connectionChain.remove();
			// if we couln't get a connection from the pool,we should get new
			// connection.
			if (conProxy == null) {
				// if get connection error,we throw it to user
				SQLException e = this.createConnectionException;
				if (e != null) {
					throw e;
				}
				int count = this.lackCount.get();
				if (count == 0) {
					int maxIncrement = this.cfgVO.getMaxPoolSize()
							- this.poolSize.get();
					// if pool is full,we shouldn't grow it
					if (maxIncrement != 0) {
						int increment = this.cfgVO.getAcquireIncrement();
						if (increment > maxIncrement) {
							increment = maxIncrement;
						}
						this.lackCount.compareAndSet(count, increment);
					}
				}
				// rest for a while
				ThreadSleepUtil.sleep();
			}
		} while (conProxy == null);
		DataSourceAbstractFactory factory = this.cfgVO.getFactory();
		return factory.createPooledConnection(conProxy);
	}

	/**
	 * fill the pool by lackCount
	 */
	public void fillPool() {
		int poolNum = this.lackCount.get();
		int retryTimes = this.cfgVO.getAcquireRetryTimes();
		for (int i = 0; i < poolNum; i++) {
			// try to get a connection
			ConnectionProxy conProxy = this.tryGetConnection(retryTimes);
			this.connectionChain.add(conProxy);
			if (this.closed) {
				this.remove();
				return;
			}
		}
		this.poolSize.addAndGet(poolNum);
		this.lackCount.addAndGet(-poolNum);
	}

	/**
	 * try retryTimes times to get a connection
	 */
	private ConnectionProxy tryGetConnection(int retryTimes) {
		int count = 0;
		CommonConnection cmnCon = null;
		do {
			try {
				cmnCon = this.cfgVO.getDataSource().getCommonConnection();
			} catch (SQLException e) {
				LOG.error("try connect error(" + count++ + " time)");
				if (count > retryTimes) {
					// record exception
					this.createConnectionException = e;
					throw new ConnectionPoolException("get connection error");
				}
			}
		} while (cmnCon == null);
		// reset exception if necessary
		if (this.createConnectionException != null) {
			this.createConnectionException = null;
		}
		ConnectionProxy conProxy = new ConnectionProxy(this, cmnCon);
		this.connectionSet.add(conProxy);
		return conProxy;
	}

	/**
	 * Init test table by testTableName in {@link #cfgVO}
	 */
	private void initTestTable() {
		int retryTimes = this.cfgVO.getAcquireRetryTimes();
		ConnectionProxy conProxy = this.tryGetConnection(retryTimes);
		try {
			Connection con = conProxy.getConnection();
			boolean auto = con.getAutoCommit();
			con.setAutoCommit(true);
			this.checkTestTable(conProxy, true);
			con.setAutoCommit(auto);
		} catch (SQLException e) {
			throw new ConnectionPoolException(e);
		}
		int coreSize = this.cfgVO.getCorePoolSize();
		if (coreSize > 0) {
			this.connectionChain.add(conProxy);
			this.lackCount.getAndDecrement();
			this.incrementPoolSize();
		} else {
			this.closeConnection(conProxy);
		}
	}

	public CommonChain<ConnectionProxy> getConnectionChain() {
		return this.connectionChain;
	}

	public Set<ConnectionProxy> getConnectionSet() {
		return this.connectionSet;
	}

	public ConfigurationVO getCfgVO() {
		return this.cfgVO;
	}

	public boolean isClosed() {
		return this.closed;
	}

	public boolean isNeedCollected() {
		return this.poolSize.get() > this.cfgVO.getCorePoolSize();
	}

	public int getLackCount() {
		return this.lackCount.get();
	}

	public void incrementLackCount() {
		this.lackCount.getAndIncrement();
	}

	private void incrementPoolSize() {
		int size = this.poolSize.incrementAndGet();
		if (size > this.peakPoolSize) {
			this.peakPoolSize = size;
		}
	}

	public void decrementPoolSize() {
		this.poolSize.getAndDecrement();
	}

	public int getPoolSize() {
		return this.poolSize.get();
	}

	public int getPeakPoolSize() {
		return this.peakPoolSize;
	}

	/**
	 * check if test table existed
	 */
	public boolean checkTestTable(ConnectionProxy conProxy,
			boolean autoCreateTable) {
		PreparedStatement queryPreparedStatement = null;
		try {
			Connection con = conProxy.getConnection();
			queryPreparedStatement = con.prepareStatement(this.cfgVO
					.getTestQuerySql());
			queryPreparedStatement.execute();
		} catch (SQLException e) {
			LOG.info(this.cfgVO.getTestQuerySql() + " error");
			if (autoCreateTable) {
				this.createTestTable(conProxy);
			} else {
				return false;
			}
		} finally {
			if (queryPreparedStatement != null) {
				try {
					queryPreparedStatement.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
		}
		return true;
	}

	/**
	 * create test table if test table is not existed
	 */
	private void createTestTable(ConnectionProxy conProxy) {
		PreparedStatement createPreparedStatement = null;
		try {
			Connection con = conProxy.getConnection();
			createPreparedStatement = con.prepareStatement(this.cfgVO
					.getTestCreateSql());
			createPreparedStatement.execute();
		} catch (SQLException e) {
			throw new ConnectionPoolException(e);
		} finally {
			if (createPreparedStatement != null) {
				try {
					createPreparedStatement.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
		}
	}

	/**
	 * remove the connection of the free connection. note:we shouldn't close the
	 * using connection because it may cause a exception when people is using
	 * it.
	 */
	public void remove() {
		this.closed = true;
		Set<ConnectionProxy> tempSet = this.connectionSet;
		// help gc
		this.connectionSet = new HashSet<>();
		for (ConnectionProxy conProxy : tempSet) {
			this.closeConnection(conProxy);
		}
	}

	/**
	 * close pool connection
	 */
	public void closeConnection(ConnectionProxy conProxy) {
		if (conProxy != null) {
			try {
				conProxy.getConnection().close();
			} catch (SQLException e) {
				LOG.error(
						"it cause a exception when we close a pool connection",
						e);
			}
			this.connectionSet.remove(conProxy);
		}
	}
}
