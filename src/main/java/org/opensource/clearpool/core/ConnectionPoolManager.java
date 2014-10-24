package org.opensource.clearpool.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.core.chain.BinaryHeap;
import org.opensource.clearpool.datasource.connection.CommonConnection;
import org.opensource.clearpool.datasource.factory.DataSourceAbstractFactory;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

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

	private Lock lock = new ReentrantLock();
	private Condition notEmpty = this.lock.newCondition();

	private final BinaryHeap connectionChain = new BinaryHeap();;

	// save all the connection to close in case we shutdown the JVM
	private volatile Set<ConnectionProxy> connectionSet = new HashSet<ConnectionProxy>();

	// this is the sign of if the pool is removed
	private volatile boolean closed;

	private ConfigurationVO cfgVO;

	private int poolSize;

	// record the peak of the connection in the pool.
	private int peakPoolSize;

	ConnectionPoolManager(ConfigurationVO cfgVO) {
		this.cfgVO = cfgVO;
	}

	/**
	 * Init the pool by the corePoolsize of {@link #cfgVO}
	 */
	void initPool() {
		int coreSize = this.cfgVO.getCorePoolSize();
		this.fillPool(coreSize);
		String tableName = this.cfgVO.getTestTableName();
		if (tableName != null) {
			this.initTestTable();
		}
	}

	/**
	 * Return connection to the pool
	 */
	public void entryPool(ConnectionProxy conProxy) {
		if (conProxy == null) {
			throw new NullPointerException();
		}
		this.lock.lock();
		try {
			this.connectionChain.add(conProxy);
			this.notEmpty.signal();
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * Get a connection from the pool
	 */
	public PooledConnection exitPool() throws SQLException {
		ConnectionProxy conProxy = null;
		this.lock.lock();
		try {
			do {
				conProxy = this.connectionChain.remove();
				// if we couln't get a connection from the pool,we should get
				// new
				// connection.
				if (conProxy == null) {
					int maxIncrement = this.cfgVO.getMaxPoolSize()
							- this.poolSize;
					// if pool is full,we shouldn't grow it
					if (maxIncrement == 0) {
						if (this.cfgVO.getUselessConnectionException()) {
							throw new ConnectionPoolException(
									"there is no connection left in the pool");
						} else {
							// wait for connection
							while (this.connectionChain.size() == 0) {
								this.notEmpty.await();
							}
						}
					} else {
						this.fillPoolByAcquireIncrement();
					}
				}
			} while (conProxy == null);
		} catch (InterruptedException e) {
			throw new ConnectionPoolException(e);
		} finally {
			this.lock.unlock();
		}
		DataSourceAbstractFactory factory = this.cfgVO.getFactory();
		return factory.createPooledConnection(conProxy);
	}

	public ConnectionProxy exitPool(long period) {
		this.lock.lock();
		try {
			return this.connectionChain.removeIdle(period);
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * fill the pool by acquireIncrement
	 */
	private synchronized void fillPoolByAcquireIncrement() {
		int maxIncrement = this.cfgVO.getMaxPoolSize() - this.poolSize;
		// double check
		if (maxIncrement != 0) {
			int increment = this.cfgVO.getAcquireIncrement();
			if (increment > maxIncrement) {
				increment = maxIncrement;
			}
			this.fillPool(increment);
		}
	}

	/**
	 * increment one connection
	 */
	public synchronized void incrementOneConnection() {
		int maxIncrement = this.cfgVO.getMaxPoolSize() - this.poolSize;
		// if pool is full,we shouldn't grow it
		if (maxIncrement != 0) {
			this.fillPool(1);
		}
	}

	/**
	 * fill the pool by poolNum
	 */
	private void fillPool(int poolNum) {
		int retryTimes = this.cfgVO.getAcquireRetryTimes();
		for (int i = 0; i < poolNum; i++) {
			// try to get a connection
			ConnectionProxy conProxy = this.tryGetConnection(retryTimes);
			if (this.closed) {
				this.remove();
				return;
			}
			this.connectionChain.add(conProxy);
			this.handlePeakPoolSize(i + 1);
		}
		this.poolSize += poolNum;
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
					throw new ConnectionPoolException("get connection error"
							+ e.getMessage());
				}
			}
		} while (cmnCon == null);
		ConnectionProxy conProxy = new ConnectionProxy(this, cmnCon);
		this.connectionSet.add(conProxy);
		return conProxy;
	}

	/**
	 * Init test table by testTableName in {@link #cfgVO}
	 */
	private void initTestTable() {
		int coreSize = this.cfgVO.getCorePoolSize();
		ConnectionProxy conProxy = null;
		if (coreSize > 0) {
			conProxy = this.connectionChain.remove();
		} else {
			int retryTimes = this.cfgVO.getAcquireRetryTimes();
			conProxy = this.tryGetConnection(retryTimes);
		}
		try {
			Connection con = conProxy.getConnection();
			boolean auto = con.getAutoCommit();
			con.setAutoCommit(true);
			this.checkTestTable(conProxy, true);
			con.setAutoCommit(auto);
		} catch (SQLException e) {
			throw new ConnectionPoolException(e);
		}
		if (coreSize > 0) {
			this.connectionChain.add(conProxy);
		} else {
			this.closeConnection(conProxy);
		}
	}

	public BinaryHeap getConnectionChain() {
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
		return this.poolSize > this.cfgVO.getCorePoolSize();
	}

	/**
	 * Save the peak pool size
	 */
	private void handlePeakPoolSize(int poolNum) {
		int size = this.poolSize + poolNum;
		if (size > this.peakPoolSize) {
			this.peakPoolSize = size;
		}
	}

	public void decrementPoolSize() {
		this.poolSize--;
	}

	public int getPoolSize() {
		return this.poolSize;
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
		this.connectionSet = new HashSet<ConnectionProxy>();
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
