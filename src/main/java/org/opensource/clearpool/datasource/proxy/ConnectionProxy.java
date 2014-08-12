package org.opensource.clearpool.datasource.proxy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Properties;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

/**
 * This class is the proxy of connection.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConnectionProxy {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(ConnectionProxy.class);

	private final ConnectionPoolManager pool;

	protected final Connection connection;

	boolean autoCommit;
	String catalog;
	int holdability;
	boolean readOnly;
	int transactionIsolation;
	String schema;

	boolean newAutoCommit;
	String newCatalog;
	int newHoldability;
	boolean newReadOnly;
	int newTransactionIsolation;
	String newSchema;

	boolean clientInfoChanged;
	boolean typeMapChanged;
	Savepoint savepoint;

	public ConnectionProxy(ConnectionPoolManager pool, Connection connection) {
		this.pool = pool;
		this.connection = connection;
		this.saveValue();
	}

	/**
	 * Save the value which may changed after using.
	 */
	private void saveValue() {
		try {
			this.connection.setClientInfo(new Properties());
			this.connection.setTypeMap(new HashMap<String, Class<?>>());
			this.newAutoCommit = this.autoCommit = this.connection
					.getAutoCommit();
			this.newCatalog = this.catalog = this.connection.getCatalog();
			this.newHoldability = this.holdability = this.connection
					.getHoldability();
			this.newReadOnly = this.readOnly = this.connection.isReadOnly();
			this.newTransactionIsolation = this.transactionIsolation = this.connection
					.getTransactionIsolation();
			this.newSchema = this.schema = this.connection.getSchema();
		} catch (SQLException e) {
			throw new ConnectionPoolException(e);
		}
	}

	/**
	 * Reset the value which had been changed.
	 */
	void reset() throws SQLException {
		boolean autoCommit = this.connection.getAutoCommit();
		if (!autoCommit) {
			// we have to roll back commit before we return connection to
			// the pool
			this.connection.rollback();
		}

		if (this.newAutoCommit != this.autoCommit) {
			this.connection.setAutoCommit(this.autoCommit);
		}
		if (this.newCatalog != this.catalog) {
			this.connection.setCatalog(this.catalog);
		}
		if (this.newHoldability != this.holdability) {
			this.connection.setHoldability(this.holdability);
		}
		if (this.newReadOnly != this.readOnly) {
			this.connection.setReadOnly(this.readOnly);
		}
		if (this.newTransactionIsolation != this.transactionIsolation) {
			this.connection.setTransactionIsolation(this.transactionIsolation);
		}
		if (this.newSchema != this.schema) {
			this.connection.setSchema(this.schema);
		}
		if (this.savepoint != null) {
			this.connection.releaseSavepoint(this.savepoint);
		}
		if (this.clientInfoChanged) {
			this.connection.setClientInfo(new Properties());
		}
		if (this.typeMapChanged) {
			this.connection.setTypeMap(new HashMap<String, Class<?>>());
		}
		// clear warnings before return connection to pool
		this.connection.clearWarnings();
	}

	public Connection getConnection() {
		return this.connection;
	}

	/**
	 * Return connection to the pool.
	 */
	public void close() {
		try {
			// reset it
			this.reset();
		} catch (SQLException e) {
			LOG.warn("it calls a exception when we reset the connection");
			this.reallyClose();
			return;
		}
		this.pool.entryPool(this);
	}

	/**
	 * Really close the connection.
	 */
	private void reallyClose() {
		// close connection
		this.pool.closeConnection(this);
		this.pool.decrementPoolSize();
		this.pool.incrementLackCount();
	}

	/**
	 * need to show sql?
	 */
	boolean isShowSql() {
		boolean showSql = this.pool.getCfgVO().isShowSql();
		return showSql;
	}
}
