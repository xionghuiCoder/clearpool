package com.github.xionghuicoder.clearpool.core;

import java.util.Map;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.datasource.AbstractDataSource;
import com.github.xionghuicoder.clearpool.datasource.DataSourceHolder;
import com.github.xionghuicoder.clearpool.datasource.JDBCDataSource;
import com.github.xionghuicoder.clearpool.datasource.factory.DataSourceAbstractFactory;
import com.github.xionghuicoder.clearpool.datasource.factory.DataSourceFactory;
import com.github.xionghuicoder.clearpool.datasource.factory.JDBCDataSourceFactory;
import com.github.xionghuicoder.clearpool.datasource.factory.JDBCXADataSourceFactory;
import com.github.xionghuicoder.clearpool.datasource.factory.PoolDataSourceFactory;
import com.github.xionghuicoder.clearpool.datasource.factory.XADataSourceFactory;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;
import com.github.xionghuicoder.clearpool.security.ISecret;
import com.github.xionghuicoder.clearpool.util.DataSourceUtils;

/**
 * 连接池相关配置
 *
 * <p>
 * 初始化是按以下顺序进行数据源配置：<br>
 * 1. 配置<tt>dataSource</tt>；<br>
 * 2. 配置<tt>jndiName</tt>；<br>
 * 3. 配置<tt>driverClassName</tt>，<tt>url</tt>，<tt>username</tt>，<tt>password</tt>；<br>
 * 4. 通过{@link DataSourceHolder#getDataSourceMap DataSourceHolder.getDataSourceMap}获取数据源；<br>
 * </p>
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigurationVO implements Cloneable {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ConfigurationVO.class);

  private AbstractDataSource abstractDataSource;

  /**
   * 数据库连接池名称，一个名称唯一标识一个数据库连接池
   */
  private String name;

  /**
   * 直接设置数据源。该值不为空时会忽略jndiName和driverClassName，url，username，password。
   */
  private CommonDataSource dataSource;
  /**
   * 通过JNDI查找数据源。该值不为空时会忽略driverClassName，url，username，password。
   */
  private String jndiName;
  /**
   * dataSource和jndiName为null时使用driverClassName，url，username，password获取数据源。
   */
  private String driverClassName;
  private String url;
  private String username;
  private String password;
  private String securityClassName;

  private boolean jtaSupport;
  private int corePoolSize;
  private int maxPoolSize = Integer.MAX_VALUE;
  private int acquireIncrement = 1;
  private int acquireRetryTimes;
  private boolean uselessConnectionException;
  private long limitIdleTime = 60 * 1000L;
  private long keepTestPeriod = -1;
  private boolean testBeforeUse;
  private String testQuerySql;
  private boolean showSql;
  private long sqlTimeFilter;

  public AbstractDataSource getAbstractDataSource() {
    return this.abstractDataSource;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CommonDataSource getDataSource() {
    return this.dataSource;
  }

  public void setDataSource(CommonDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public boolean isJtaSupport() {
    return this.jtaSupport;
  }

  public String getJndiName() {
    return this.jndiName;
  }

  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  public String getDriverClassName() {
    return this.driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSecurityClassName() {
    return this.securityClassName;
  }

  public void setSecurityClassName(String securityClassName) {
    this.securityClassName = securityClassName;
  }

  public void setJtaSupport(boolean jtaSupport) {
    this.jtaSupport = jtaSupport;
  }

  public int getCorePoolSize() {
    return this.corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    if (corePoolSize < 0) {
      LOGGER.warn("corePoolsize is negative");
      return;
    }
    this.corePoolSize = corePoolSize;
  }

  public int getMaxPoolSize() {
    return this.maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    if (maxPoolSize <= 0) {
      LOGGER.warn("maxPoolSize should be positive");
      return;
    }
    this.maxPoolSize = maxPoolSize;
  }

  public int getAcquireIncrement() {
    return this.acquireIncrement;
  }

  public void setAcquireIncrement(int acquireIncrement) {
    if (acquireIncrement <= 0) {
      LOGGER.warn("acquireIncrement should be positive");
      return;
    }
    this.acquireIncrement = acquireIncrement;
  }

  public int getAcquireRetryTimes() {
    return this.acquireRetryTimes;
  }

  public void setAcquireRetryTimes(int acquireRetryTimes) {
    if (acquireRetryTimes < 0) {
      LOGGER.warn("acquireRetryTimes is negative");
      return;
    }
    this.acquireRetryTimes = acquireRetryTimes;
  }

  public boolean isUselessConnectionException() {
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
      LOGGER.warn("limitIdleTime is negative");
      return;
    }
    this.limitIdleTime = limitIdleTime;
  }

  public long getKeepTestPeriod() {
    return this.keepTestPeriod;
  }

  public void setKeepTestPeriod(long keepTestPeriod) {
    if (keepTestPeriod < 0) {
      LOGGER.warn("keepTestPeriod is negative");
      return;
    }
    this.keepTestPeriod = keepTestPeriod;
  }

  public boolean isTestBeforeUse() {
    return this.testBeforeUse;
  }

  public void setTestBeforeUse(boolean testBeforeUse) {
    this.testBeforeUse = testBeforeUse;
  }

  public String getTestQuerySql() {
    return this.testQuerySql;
  }

  public void setTestQuerySql(String testQuerySql) {
    this.testQuerySql = testQuerySql;
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
      LOGGER.warn("sqlTimeFilter is negative");
      return;
    }
    this.sqlTimeFilter = sqlTimeFilter;
  }

  /**
   * 初始化配置
   *
   * <p>
   * 如果配置了keepTestPeriod，默认使用testQuerySql，若此时testQuerySql为null，<br>
   * 则使用testTableName，若testTableName不为null，则会按testTableName设置testCreateSql。
   * </p>
   *
   */
  public void init() {
    this.initDatasource();
    if (this.maxPoolSize < this.corePoolSize) {
      throw new ConnectionPoolException("maxPoolsize less than corePoolsize");
    }
    if (this.acquireIncrement <= 0) {
      throw new ConnectionPoolException("acquireIncrement should be positive");
    }
    if ((this.keepTestPeriod != -1 || this.testBeforeUse) && this.testQuerySql == null) {
      throw new ConnectionPoolException("keepTestPeriod, testBeforeUse or testQuerySql illegal");
    }
  }

  /**
   * 初始化数据源
   */
  private void initDatasource() {
    if (this.dataSource == null) {
      if (this.jndiName != null) {
        this.dataSource = DataSourceUtils.getJndiDataSource(this.jndiName);
      } else if ((this.driverClassName != null || this.url != null || this.username != null
          || this.password != null)) {
        String finalPassword = this.password;
        if (this.securityClassName != null) {
          ISecret secret = this.createSecurity(this.securityClassName);
          finalPassword = secret.decrypt(this.password);
        }
        this.dataSource = DataSourceUtils.getJDBCDataSource(this.driverClassName, this.url,
            this.username, finalPassword);
      } else {
        Map<String, CommonDataSource> dataSourceMap = DataSourceHolder.getDataSourceMap();
        if (dataSourceMap != null) {
          this.dataSource = dataSourceMap.get(this.name);
        }
      }
      if (this.dataSource == null) {
        throw new ConnectionPoolException(
            "cfg should have a dataSource, jndi or driver, otherwise should set datasource in DataSourceHolder");
      }
    }

    DataSourceAbstractFactory factory = null;
    if (this.jtaSupport) {
      if (this.dataSource instanceof JDBCDataSource) {
        factory = new JDBCXADataSourceFactory();
      } else if (this.dataSource instanceof XADataSource) {
        factory = new XADataSourceFactory();
      }
    } else {
      if (this.dataSource instanceof DataSource) {
        factory = new DataSourceFactory();
      } else if (this.dataSource instanceof JDBCDataSource) {
        factory = new JDBCDataSourceFactory();
      } else if (this.dataSource instanceof ConnectionPoolDataSource) {
        factory = new PoolDataSourceFactory();
      }
    }
    if (factory == null) {
      throw new UnsupportedOperationException(
          "we don't support the datasource: " + this.dataSource.getClass().getName());
    }
    this.abstractDataSource = factory.createDataSource(this.dataSource);
  }

  private ISecret createSecurity(String securityClassName) {
    Class<?> clazz = null;
    if (clazz == null) {
      try {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
          clazz = contextLoader.loadClass(securityClassName);
        }
      } catch (ClassNotFoundException e) {
        // swallow
      }
    }
    if (clazz == null) {
      try {
        clazz = Class.forName(securityClassName);
      } catch (ClassNotFoundException e) {
        throw new ConnectionPoolException(e.getMessage(), e);
      }
    }
    try {
      return (ISecret) clazz.newInstance();
    } catch (IllegalAccessException e) {
      throw new ConnectionPoolException(e.getMessage(), e);
    } catch (InstantiationException e) {
      throw new ConnectionPoolException(e.getMessage(), e);
    }
  }

  @Override
  public ConfigurationVO clone() {
    ConfigurationVO cfgVO = null;
    try {
      cfgVO = (ConfigurationVO) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new ConnectionPoolException(e);
    }
    return cfgVO;
  }

  @Override
  public String toString() {
    return "ConfigurationVO [abstractDataSource=" + this.abstractDataSource + ", dataSource="
        + this.dataSource + ", jndiName=" + this.jndiName + ", driverClassName="
        + this.driverClassName + ", url=" + this.url + ", username=" + this.username + ", password="
        + this.password + ", securityClassName=" + this.securityClassName + ", name=" + this.name
        + ", jtaSupport=" + this.jtaSupport + ", corePoolSize=" + this.corePoolSize
        + ", maxPoolSize=" + this.maxPoolSize + ", acquireIncrement=" + this.acquireIncrement
        + ", acquireRetryTimes=" + this.acquireRetryTimes + ", uselessConnectionException="
        + this.uselessConnectionException + ", limitIdleTime=" + this.limitIdleTime
        + ", keepTestPeriod=" + this.keepTestPeriod + ", testBeforeUse=" + this.testBeforeUse
        + ", testQuerySql=" + this.testQuerySql + ", showSql=" + this.showSql + ", sqlTimeFilter="
        + this.sqlTimeFilter + "]";
  }
}
