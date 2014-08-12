package org.opensource.clearpool.console;

import javax.sql.DataSource;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.datasource.JDBCDataSource;

class ConnectionPool implements ConnectionPoolMBean {
	private ConnectionPoolManager pool;

	private DataSource dataSource;

	private int poolSize = -1;

	private int connectionLeft = -1;

	ConnectionPool(ConnectionPoolManager pool) {
		this.pool = pool;
		this.dataSource = this.pool.getCfgVO().getDataSource();
	}

	@Override
	public String getAlias() {
		return this.pool.getCfgVO().getAlias();
	}

	@Override
	public String getDataSourceClass() {
		return this.dataSource.getClass().getName();
	}

	@Override
	public String getDriverUrl() {
		if (this.dataSource instanceof JDBCDataSource) {
			JDBCDataSource jdbcDataSource = (JDBCDataSource) this.dataSource;
			String url = jdbcDataSource.getUrl();
			return url;
		}
		return "-";
	}

	@Override
	public String getDriverClass() {
		if (this.dataSource instanceof JDBCDataSource) {
			JDBCDataSource jdbcDataSource = (JDBCDataSource) this.dataSource;
			String clazz = jdbcDataSource.getClazz();
			if (clazz != null) {
				return clazz;
			}
		}
		return "-";
	}

	@Override
	public int getCorePoolSize() {
		return this.pool.getCfgVO().getCorePoolSize();
	}

	@Override
	public int getMaxPoolSize() {
		return this.pool.getCfgVO().getMaxPoolSize();
	}

	@Override
	public int getAcquireIncrement() {
		return this.pool.getCfgVO().getAcquireIncrement();
	}

	@Override
	public int getAcquireRetryTimes() {
		return this.pool.getCfgVO().getAcquireRetryTimes();
	}

	@Override
	public String getLimitIdleTime() {
		long time = this.pool.getCfgVO().getLimitIdleTime();
		if (time != 0) {
			return time / 1000 + "(s)";
		}
		return null;
	}

	@Override
	public String getKeepTestPeriod() {
		long period = this.pool.getCfgVO().getKeepTestPeriod() / 1000;
		return period + "(s)";
	}

	@Override
	public String getTestTableName() {
		return this.pool.getCfgVO().getTestTableName();
	}

	@Override
	public boolean isShowSql() {
		return this.pool.getCfgVO().isShowSql();
	}

	@Override
	public int getPeakPoolSize() {
		return this.pool.getPeakPoolSize();
	}

	/**
	 * Make sure {@link #getConnectionUsing()} and this method get the same
	 * {@link #poolSize}.
	 */
	@Override
	public int getPoolSize() {
		int size = this.poolSize;
		if (this.poolSize == -1) {
			this.poolSize = this.pool.getPoolSize();
			size = this.poolSize;
		} else {
			this.poolSize = -1;
		}
		return size;
	}

	@Override
	public int getConnectionUsing() {
		int size = this.poolSize;
		if (this.poolSize == -1) {
			this.poolSize = this.pool.getPoolSize();
			size = this.poolSize;
		} else {
			this.poolSize = -1;
		}
		int connLeft = this.connectionLeft;
		if (this.connectionLeft == -1) {
			this.connectionLeft = this.pool.getConnectionChain().size();
			connLeft = this.connectionLeft;
		} else {
			this.connectionLeft = -1;
		}
		return size - connLeft;
	}

	/**
	 * Make sure {@link #getConnectionUsing()} and this method get the same
	 * {@link #connectionLeft}.
	 */
	@Override
	public int getConnectionLeft() {
		int connLeft = this.connectionLeft;
		if (this.connectionLeft == -1) {
			this.connectionLeft = this.pool.getConnectionChain().size();
			connLeft = this.connectionLeft;
		} else {
			this.connectionLeft = -1;
		}
		return connLeft;
	}
}
