package org.opensource.clearpool.configuration;

import java.util.Map;
import java.util.regex.Pattern;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.opensource.clearpool.configuration.console.Console;
import org.opensource.clearpool.datasource.AbstractDataSource;
import org.opensource.clearpool.datasource.DataSourceHolder;
import org.opensource.clearpool.datasource.JDBCDataSource;
import org.opensource.clearpool.datasource.factory.DataSourceAbstractFactory;
import org.opensource.clearpool.datasource.factory.DataSourceFactory;
import org.opensource.clearpool.datasource.factory.JDBCDataSourceFactory;
import org.opensource.clearpool.datasource.factory.JDBCXADataSourceFactory;
import org.opensource.clearpool.datasource.factory.PoolDataSourceFactory;
import org.opensource.clearpool.datasource.factory.XADataSourceFactory;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

/**
 * This is the VO of the configuration XML.It has 3 main field:driverUrl,user and password.CfgVO
 * also carry urls of other XML.
 *
 * Note:Two CfgVOs is equals If their 3 main fields are the same.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ConfigurationVO {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ConfigurationVO.class);

  private static Console console;

  private DataSourceAbstractFactory factory = null;

  private String alias;
  private AbstractDataSource dataSource;
  private CommonDataSource commonDataSource;
  private boolean jtaSupport;
  private int corePoolSize;
  private int maxPoolSize = Integer.MAX_VALUE;
  private int acquireIncrement = 1;
  private int acquireRetryTimes;
  private boolean uselessConnectionException;
  private long limitIdleTime = 60 * 1000L;
  private long keepTestPeriod = -1;
  private String testTableName = "clearpool_test";
  private boolean testBeforeUse;
  private String testQuerySql;
  private String testCreateSql;
  private boolean showSql;
  private long sqlTimeFilter = -1;

  public static Console getConsole() {
    return console;
  }

  public static void setConsole(Console console) {
    ConfigurationVO.console = console;
  }

  public DataSourceAbstractFactory getFactory() {
    return factory;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public AbstractDataSource getDataSource() {
    return dataSource;
  }

  public CommonDataSource getCommonDataSource() {
    return commonDataSource;
  }

  public void setCommonDataSource(CommonDataSource commonDataSource) {
    this.commonDataSource = commonDataSource;
  }

  public boolean isJtaSupport() {
    return jtaSupport;
  }

  public void setJtaSupport(boolean jtaSupport) {
    this.jtaSupport = jtaSupport;
  }

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    if (corePoolSize < 0) {
      LOGGER.warn("the corePoolsize is negative");
      return;
    }
    this.corePoolSize = corePoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    if (maxPoolSize <= 0) {
      LOGGER.warn("maxPoolSize should be positive");
      return;
    }
    this.maxPoolSize = maxPoolSize;
  }

  public int getAcquireIncrement() {
    return acquireIncrement;
  }

  public void setAcquireIncrement(int acquireIncrement) {
    if (acquireIncrement <= 0) {
      LOGGER.warn("acquireIncrement should be positive");
      return;
    }
    this.acquireIncrement = acquireIncrement;
  }

  public int getAcquireRetryTimes() {
    return acquireRetryTimes;
  }

  public void setAcquireRetryTimes(int acquireRetryTimes) {
    if (acquireRetryTimes < 0) {
      LOGGER.warn("acquireRetryTimes negative");
      return;
    }
    this.acquireRetryTimes = acquireRetryTimes;
  }

  public boolean getUselessConnectionException() {
    return uselessConnectionException;
  }

  public void setUselessConnectionException(boolean uselessConnectionException) {
    this.uselessConnectionException = uselessConnectionException;
  }

  public long getLimitIdleTime() {
    return limitIdleTime;
  }

  public void setLimitIdleTime(long limitIdleTime) {
    if (limitIdleTime < 0) {
      LOGGER.warn("limitIdleTime negative");
      return;
    }
    this.limitIdleTime = limitIdleTime;
  }

  public long getKeepTestPeriod() {
    return keepTestPeriod;
  }

  public void setKeepTestPeriod(long keepTestPeriod) {
    if (keepTestPeriod <= 0) {
      LOGGER.warn("keepTestPeriod should be positive");
      return;
    }
    this.keepTestPeriod = keepTestPeriod;
  }

  public String getTestTableName() {
    return testTableName;
  }

  public void setTestTableName(String testTableName) {
    this.testTableName = testTableName;
  }

  public boolean isTestBeforeUse() {
    return testBeforeUse;
  }

  public void setTestBeforeUse(boolean testBeforeUse) {
    this.testBeforeUse = testBeforeUse;
  }

  public String getTestQuerySql() {
    return testQuerySql;
  }

  public void setTestQuerySql(String testQuerySql) {
    this.testQuerySql = testQuerySql;
  }

  public String getTestCreateSql() {
    return testCreateSql;
  }

  public boolean isShowSql() {
    return showSql;
  }

  public void setShowSql(boolean showSql) {
    this.showSql = showSql;
  }

  public long getSqlTimeFilter() {
    return sqlTimeFilter;
  }

  public void setSqlTimeFilter(long sqlTimeFilter) {
    if (sqlTimeFilter < 0) {
      LOGGER.warn("the sqlFilter is negative");
      return;
    }
    this.sqlTimeFilter = sqlTimeFilter;
  }

  /**
   * We check if this object is legal,and reset its default values.
   */
  public void init() {
    this.handlerDatasource();
    if (maxPoolSize < corePoolSize) {
      throw new ConnectionPoolXMLParseException("the maxPoolsize less than corePoolsize");
    }
    if (keepTestPeriod == -1) {
      testTableName = null;
    } else if (testQuerySql != null) {
      testTableName = null;
    } else {
      boolean right = false;
      if (testTableName != null && testTableName.length() > 0) {
        String regex = "^[a-z|A-Z]\\w{" + (testTableName.length() - 1) + "}";
        right = Pattern.matches(regex, testTableName);
      }
      if (!right) {
        throw new ConnectionPoolXMLParseException("the pattern of table name is illegal");
      }
      testQuerySql = "select 1 from " + testTableName + " where 0=1";
      testCreateSql = "create table " + testTableName + "(id char(1) primary key)";
    }
    if (sqlTimeFilter > 0 && !showSql) {
      throw new ConnectionPoolXMLParseException(
          "sqlTimeFilter shouldn't be set when showSql is false");
    }
  }

  /**
   * Set dataSource if dataSource is null, check if it's null after setting.And create dataSource by
   * factory.
   */
  private void handlerDatasource() {
    if (commonDataSource == null) {
      // if we haven't get dataSource from configuration,we should try to
      // get it by DataSourceHolder.
      Map<String, CommonDataSource> dataSourceMap = DataSourceHolder.getDataSourceMap();
      if (dataSourceMap != null) {
        commonDataSource = dataSourceMap.get(alias);
      }
      if (commonDataSource == null) {
        throw new ConnectionPoolXMLParseException(
            "cfg should have a driver or a jndi,otherwise you should set datasource in DataSourceHolder");
      }
    }
    if (jtaSupport) {
      if (commonDataSource instanceof JDBCDataSource) {
        factory = new JDBCXADataSourceFactory();
      } else if (commonDataSource instanceof XADataSource) {
        factory = new XADataSourceFactory();
      }
    } else {
      if (commonDataSource instanceof DataSource) {
        factory = new DataSourceFactory();
      } else if (commonDataSource instanceof JDBCDataSource) {
        factory = new JDBCDataSourceFactory();
      } else if (commonDataSource instanceof ConnectionPoolDataSource) {
        factory = new PoolDataSourceFactory();
      }
    }
    if (factory == null) {
      throw new UnsupportedOperationException(
          "we don't support the datasource: " + commonDataSource.getClass().getName());
    }
    dataSource = factory.createDataSource(commonDataSource);
  }
}
