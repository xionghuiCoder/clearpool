package org.opensource.clearpool.configuration;

import java.util.Map;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.opensource.clearpool.configuration.console.Console;
import org.opensource.clearpool.datasource.DataSourceHolder;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

/**
 * This is the VO of the configuration XML.It has 3 main field:driverUrl,user
 * and password.CfgVO also carry urls of other XML.
 * 
 * Note:Two CfgVOs is equals If their 3 main fields are the same.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConfigurationVO {
	private static final PoolLog LOG = PoolLogFactory.getLog(ConfigurationVO.class);

	private static Console console;
	private String alias;
	private DataSource dataSource;
	private int corePoolSize;
	private int maxPoolSize;
	private int acquireIncrement;
	private int acquireRetryTimes;
	private long limitIdleTime = 60 * 1000L;
	private long keepTestPeriod;
	private String testTableName = "clearpool_test";
	private String testQuerySql;
	private String testCreateSql;
	private boolean showSql;

	public static Console getConsole() {
		return console;
	}

	public static void setConsole(Console console) {
		ConfigurationVO.console = console;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public int getCorePoolSize() {
		return this.corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		if (corePoolSize < 0) {
			LOG.warn("the corePoolsize is negative");
			return;
		}
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return this.maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getAcquireIncrement() {
		return this.acquireIncrement;
	}

	public void setAcquireIncrement(int acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}

	public int getAcquireRetryTimes() {
		return this.acquireRetryTimes;
	}

	public void setAcquireRetryTimes(int acquireRetryTimes) {
		if (acquireRetryTimes < 0) {
			LOG.warn("the acquireRetryTimes negative");
			return;
		}
		this.acquireRetryTimes = acquireRetryTimes;
	}

	public long getLimitIdleTime() {
		return this.limitIdleTime;
	}

	public void setLimitIdleTime(long limitIdleTime) {
		if (limitIdleTime < 0) {
			LOG.warn("the limitIdleTime negative");
			return;
		}
		this.limitIdleTime = limitIdleTime;
	}

	public long getKeepTestPeriod() {
		return this.keepTestPeriod;
	}

	public void setKeepTestPeriod(long keepTestPeriod) {
		if (keepTestPeriod < 0) {
			LOG.warn("the keepTestPeriod negative");
			return;
		}
		this.keepTestPeriod = keepTestPeriod;
	}

	public String getTestTableName() {
		return this.testTableName;
	}

	public void setTestTableName(String testTableName) {
		this.testTableName = testTableName;
	}

	public String getTestQuerySql() {
		return this.testQuerySql;
	}

	public String getTestCreateSql() {
		return this.testCreateSql;
	}

	public boolean isShowSql() {
		return this.showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	/**
	 * We check if this object is legal,and reset its default values.
	 */
	public void init() {
		if (this.dataSource == null) {
			// if we haven't get dataSource from configuration,we should try to
			// get it by DataSourceHolder.
			Map<String, DataSource> dataSourceMap = DataSourceHolder
					.getDataSourceMap();
			if (dataSourceMap != null) {
				this.dataSource = dataSourceMap.get(this.alias);
			}
			if (this.dataSource == null) {
				throw new ConnectionPoolXMLParseException(
						"cfg should have a driver or a jndi,otherwise you should set datasource in DataSourceHolder");
			}
		}
		if (this.maxPoolSize < this.corePoolSize) {
			LOG.warn("the maxPoolsize less than corePoolsize");
			this.maxPoolSize = Integer.MAX_VALUE;
		}
		if (this.acquireIncrement <= 0) {
			this.acquireIncrement = this.maxPoolSize - this.corePoolSize;
		}
		if (this.keepTestPeriod == 0) {
			this.testTableName = null;
		} else {
			boolean right = false;
			if (this.testTableName != null && this.testTableName.length() > 0) {
				String regex = "^[a-z|A-Z]\\w{"
						+ (this.testTableName.length() - 1) + "}";
				right = Pattern.matches(regex, this.testTableName);
			}
			if (!right) {
				throw new ConnectionPoolXMLParseException(
						"the pattern of table name is illegal");
			}
			this.testQuerySql = "select 1 from " + this.testTableName
					+ " where 0=1";
			this.testCreateSql = "create table " + this.testTableName
					+ "(id char(1) primary key)";
		}
	}
}
