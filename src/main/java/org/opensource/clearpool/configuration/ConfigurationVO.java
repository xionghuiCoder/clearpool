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
import org.opensource.clearpool.logging.PoolLog;
import org.opensource.clearpool.logging.PoolLogFactory;

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
  private static final PoolLog LOG = PoolLogFactory.getLog(ConfigurationVO.class);

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
    return this.factory;
  }

  public String getAlias() {
    return this.alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public AbstractDataSource getDataSource() {
    return this.dataSource;
  }

  public CommonDataSource getCommonDataSource() {
    return this.commonDataSource;
  }

  public void setCommonDataSource(CommonDataSource commonDataSource) {
    this.commonDataSource = commonDataSource;
  }

  public boolean isJtaSupport() {
    return this.jtaSupport;
  }

  public void setJtaSupport(boolean jtaSupport) {
    this.jtaSupport = jtaSupport;
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
    if (maxPoolSize <= 0) {
      LOG.warn("maxPoolSize should be positive");
      return;
    }
    this.maxPoolSize = maxPoolSize;
  }

  public int getAcquireIncrement() {
    return this.acquireIncrement;
  }

  public void setAcquireIncrement(int acquireIncrement) {
    if (acquireIncrement <= 0) {
      LOG.warn("acquireIncrement should be positive");
      return;
    }
    this.acquireIncrement = acquireIncrement;
  }

  public int getAcquireRetryTimes() {
    return this.acquireRetryTimes;
  }

  public void setAcquireRetryTimes(int acquireRetryTimes) {
    if (acquireRetryTimes < 0) {
      LOG.warn("acquireRetryTimes negative");
      return;
    }
    this.acquireRetryTimes = acquireRetryTimes;
  }

  public boolean getUselessConnectionException() {
    return this.uselessConnectionException;
  }

  public void setUselessConnectionException(boolean uselessConnectionException) {
    this.uselessConnectionException = uselessConnectionException;
  }

  public long getLimitIdleTime() {
    return this.limitIdleTime;
  }

  public void setLimitIdleTime(long limitIdleTime) {
    if (limitIdleTime < 0) {
      LOG.warn("limitIdleTime negative");
      return;
    }
    this.limitIdleTime = limitIdleTime;
  }

  public long getKeepTestPeriod() {
    return this.keepTestPeriod;
  }

  public void setKeepTestPeriod(long keepTestPeriod) {
    if (keepTestPeriod <= 0) {
      LOG.warn("keepTestPeriod should be positive");
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

  public void setTestQuerySql(String testQuerySql) {
    this.testQuerySql = testQuerySql;
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

  public long getSqlTimeFilter() {
    return this.sqlTimeFilter;
  }

  public void setSqlTimeFilter(long sqlTimeFilter) {
    if (sqlTimeFilter < 0) {
      LOG.warn("the sqlFilter is negative");
      return;
    }
    this.sqlTimeFilter = sqlTimeFilter;
  }

  /**
   * We check if this object is legal,and reset its default values.
   */
  public void init() {
    this.handlerDatasource();
    if (this.maxPoolSize < this.corePoolSize) {
      throw new ConnectionPoolXMLParseException("the maxPoolsize less than corePoolsize");
    }
    if (this.keepTestPeriod == -1) {
      this.testTableName = null;
    } else if (testQuerySql != null) {
      this.testTableName = null;
    } else {
      boolean right = false;
      if (this.testTableName != null && this.testTableName.length() > 0) {
        String regex = "^[a-z|A-Z]\\w{" + (this.testTableName.length() - 1) + "}";
        right = Pattern.matches(regex, this.testTableName);
      }
      if (!right) {
        throw new ConnectionPoolXMLParseException("the pattern of table name is illegal");
      }
      this.testQuerySql = "select 1 from " + this.testTableName + " where 0=1";
      this.testCreateSql = "create table " + this.testTableName + "(id char(1) primary key)";
    }
    if (this.sqlTimeFilter > 0 && !this.showSql) {
      throw new ConnectionPoolXMLParseException(
          "sqlTimeFilter shouldn't be set when showSql is false");
    }
  }

  /**
   * Set dataSource if dataSource is null, check if it's null after setting.And create dataSource by
   * factory.
   */
  private void handlerDatasource() {
    if (this.commonDataSource == null) {
      // if we haven't get dataSource from configuration,we should try to
      // get it by DataSourceHolder.
      Map<String, CommonDataSource> dataSourceMap = DataSourceHolder.getDataSourceMap();
      if (dataSourceMap != null) {
        this.commonDataSource = dataSourceMap.get(this.alias);
      }
      if (this.commonDataSource == null) {
        throw new ConnectionPoolXMLParseException(
            "cfg should have a driver or a jndi,otherwise you should set datasource in DataSourceHolder");
      }
    }
    if (this.jtaSupport) {
      if (this.commonDataSource instanceof JDBCDataSource) {
        this.factory = new JDBCXADataSourceFactory();
      } else if (this.commonDataSource instanceof XADataSource) {
        this.factory = new XADataSourceFactory();
      }
    } else {
      if (this.commonDataSource instanceof DataSource) {
        this.factory = new DataSourceFactory();
      } else if (this.commonDataSource instanceof JDBCDataSource) {
        this.factory = new JDBCDataSourceFactory();
      } else if (this.commonDataSource instanceof ConnectionPoolDataSource) {
        this.factory = new PoolDataSourceFactory();
      }
    }
    if (this.factory == null) {
      throw new UnsupportedOperationException("we don't support the datasource: "
          + this.commonDataSource.getClass().getName());
    }
    this.dataSource = this.factory.createDataSource(this.commonDataSource);
  }
}
