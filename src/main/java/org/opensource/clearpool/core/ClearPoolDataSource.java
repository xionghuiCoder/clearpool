package org.opensource.clearpool.core;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.configuration.console.Console;
import org.opensource.clearpool.datasource.CommonDataSource;
import org.opensource.clearpool.datasource.JDBCDataSource;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;

/**
 * This class is the provide for IOC container.Or you can't use it to program.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ClearPoolDataSource extends CommonDataSource implements
		IConnectionPool, Closeable, ConnectionPoolDataSource {
	private String poolPath;
	private boolean isUsePath;
	private ConfigurationVO vo = new ConfigurationVO();
	private boolean isUseVO;

	private Console console;

	private DataSource dataSource;
	private JDBCDataSource jdbc;

	public void setPoolPath(String poolPath) {
		if (this.isUseVO) {
			throw new ConnectionPoolXMLParseException(
					"we shouldn't use ConfigurationVO and poolPath at the same time");
		}
		this.isUsePath = true;
		this.poolPath = poolPath;
	}

	public void setPort(int port) {
		this.checkCfgLegal();
		if (this.console == null) {
			this.console = new Console();
		}
		this.console.setPort(port);
	}

	public void setSecurityMap(Map<String, String> securityMap) {
		this.checkCfgLegal();
		if (this.console == null) {
			this.console = new Console();
		}
		this.console.setSecurityMap(securityMap);
	}

	public void setAlias(String poolName) {
		this.checkCfgLegal();
		this.vo.setAlias(poolName);
	}

	public void setDataSource(DataSource dataSource) {
		this.checkCfgLegal();
		if (this.jdbc != null) {
			throw new ConnectionPoolXMLParseException(
					"we shouldn't use JDBC and dataSource at the same time");
		}
		this.dataSource = dataSource;
		this.vo.setDataSource(dataSource);
	}

	public void setDriverClass(String driverClass) {
		this.checkCfgLegal();
		this.checkAndInitJDBC();
		this.jdbc.setClazz(driverClass);
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.checkCfgLegal();
		this.checkAndInitJDBC();
		this.jdbc.setUrl(jdbcUrl);
	}

	public void setJdbcUser(String JdbcUser) {
		this.checkCfgLegal();
		this.checkAndInitJDBC();
		this.jdbc.setUser(JdbcUser);
	}

	public void setJdbcPassword(String JdbcPassword) {
		this.checkCfgLegal();
		this.checkAndInitJDBC();
		this.jdbc.setPassword(JdbcPassword);
	}

	public void setCorePoolSize(int corePoolSize) {
		this.checkCfgLegal();
		this.vo.setCorePoolSize(corePoolSize);
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.checkCfgLegal();
		this.vo.setMaxPoolSize(maxPoolSize);
	}

	public void setAcquireIncrement(int acquireIncrement) {
		this.checkCfgLegal();
		this.vo.setAcquireIncrement(acquireIncrement);
	}

	public void setAcquireRetryTimes(int acquireRetryTimes) {
		this.checkCfgLegal();
		this.vo.setAcquireRetryTimes(acquireRetryTimes);
	}

	public void setLimitIdleTime(int limitIdleTime) {
		this.checkCfgLegal();
		this.vo.setLimitIdleTime(limitIdleTime);
	}

	public void setKeepTestPeriod(int keepTestPeriod) {
		this.checkCfgLegal();
		this.vo.setKeepTestPeriod(keepTestPeriod);
	}

	public void setTestTableName(String testTableName) {
		this.checkCfgLegal();
		this.vo.setTestTableName(testTableName);
	}

	public void setShowSql(boolean showSql) {
		this.checkCfgLegal();
		this.vo.setShowSql(showSql);
	}

	/**
	 * Check if cfg is legal.
	 */
	private void checkCfgLegal() {
		if (this.isUsePath) {
			throw new ConnectionPoolXMLParseException(
					"we shouldn't use poolPath and ConfigurationVO at the same time");
		}
		this.isUseVO = true;
	}

	/**
	 * Check if JDBC is legal,and we will init it if necessary.
	 */
	private void checkAndInitJDBC() {
		if (this.dataSource != null) {
			throw new ConnectionPoolXMLParseException(
					"we shouldn't use dataSource and JDBC at the same time");
		}
		if (this.jdbc == null) {
			this.jdbc = new JDBCDataSource();
		}
	}

	/**
	 * Init the pool
	 */
	@Override
	public void init() {
		if (!this.isUseVO) {
			// Note:if you haven't set poolPath and ConfigurationVO,we will use
			// the default path to init pool
			this.initPath(this.poolPath);
			return;
		}
		if (this.dataSource == null && this.jdbc != null) {
			// we use jdbc driver if dataSource is null.
			this.jdbc.volidate();
			this.vo.setDataSource(this.jdbc);
		}
		this.vo.init();
		Map<String, ConfigurationVO> cfgMap = new HashMap<>();
		cfgMap.put(this.vo.getAlias(), this.vo);
		this.initMap(cfgMap);
	}

	/**
	 * Init pool by the given path
	 */
	@Override
	public void initPath(String path) {
		ConnectionPoolImpl.getInstance().initPath(path);
	}

	/**
	 * Init pool by cfgMap
	 */
	@Override
	public void initMap(Map<String, ConfigurationVO> cfgMap) {
		ConnectionPoolImpl.getInstance().initMap(cfgMap);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return ConnectionPoolImpl.getInstance().getConnection();
	}

	@Override
	public Connection getConnection(String name) throws SQLException {
		return ConnectionPoolImpl.getInstance().getConnection(name);
	}

	@Override
	public PooledConnection getPooledConnection() throws SQLException {
		return ConnectionPoolImpl.getInstance().getPooledConnection();
	}

	@Override
	public PooledConnection getPooledConnection(String user, String password)
			throws SQLException {
		throw new UnsupportedOperationException("Not supported now");
	}

	@Override
	public void close(String name) {
		ConnectionPoolImpl.getInstance().close(name);
	}

	@Override
	public void close() {
		ConnectionPoolImpl.getInstance().close();
	}

	@Override
	public void destory() {
		ConnectionPoolImpl.getInstance().destory();
	}
}
