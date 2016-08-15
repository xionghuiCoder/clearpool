package org.opensource.clearpool.core;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.configuration.JDBCConfiguration;
import org.opensource.clearpool.configuration.XMLConfiguration;
import org.opensource.clearpool.configuration.console.Console;
import org.opensource.clearpool.datasource.AbstractDataSource;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;

/**
 * This class is used by IOC container.Or you can use it by programming.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class ClearPoolDataSource extends AbstractDataSource
    implements IConnectionPool, Closeable, ConnectionPoolDataSource {
  private static volatile boolean isInited = false;

  private Lock lock = new ReentrantLock();

  private String poolPath;
  private boolean isUsePath;
  private ConfigurationVO vo = new ConfigurationVO();
  private boolean isUseVO;

  private Console console;

  private CommonDataSource dataSource;

  private String driverClassName;
  private String jdbcUrl;
  private String jdbcUser;
  private String jdbcPassword;

  public static String getEncoding() {
    return XMLConfiguration.getEncoding();
  }

  public static void setEncoding(String encoding) {
    XMLConfiguration.setEncoding(encoding);
  }

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

  public void setDataSource(CommonDataSource dataSource) {
    this.checkCfgLegal();
    if (this.driverClassName != null || this.jdbcUrl != null || this.jdbcUser != null
        || this.jdbcPassword != null) {
      throw new ConnectionPoolXMLParseException(
          "we shouldn't use JDBC and dataSource at the same time");
    }
    this.dataSource = dataSource;
  }

  public void setDriverClassName(String driverClassName) {
    this.checkCfgLegal();
    boolean noneChange = (this.driverClassName == null ? driverClassName == null
        : this.driverClassName.equals(driverClassName));
    if (noneChange) {
      return;
    }
    this.checkAndInitJDBC();
    this.driverClassName = driverClassName;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.checkCfgLegal();
    boolean noneChange = (this.jdbcUrl == null ? jdbcUrl == null : this.jdbcUrl.equals(jdbcUrl));
    if (noneChange) {
      return;
    }
    this.checkAndInitJDBC();
    this.jdbcUrl = jdbcUrl;
  }

  public void setJdbcUser(String jdbcUser) {
    this.checkCfgLegal();
    boolean noneChange =
        (this.jdbcUser == null ? jdbcUser == null : this.jdbcUser.equals(jdbcUser));
    if (noneChange) {
      return;
    }
    this.checkAndInitJDBC();
    this.jdbcUser = jdbcUser;
  }

  public void setJdbcPassword(String jdbcPassword) {
    this.checkCfgLegal();
    boolean noneChange =
        (this.jdbcPassword == null ? jdbcPassword == null : this.jdbcPassword.equals(jdbcPassword));
    if (noneChange) {
      return;
    }
    this.checkAndInitJDBC();
    this.jdbcPassword = jdbcPassword;
  }

  public void setJtaSupport(boolean jtaSupport) {
    this.checkCfgLegal();
    this.vo.setJtaSupport(jtaSupport);
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

  public void setUselessConnectionException(boolean uselessConnectionException) {
    this.checkCfgLegal();
    this.vo.setUselessConnectionException(uselessConnectionException);
  }

  public void setLimitIdleTime(long limitIdleTime) {
    this.checkCfgLegal();
    this.vo.setLimitIdleTime(limitIdleTime);
  }

  public void setKeepTestPeriod(long keepTestPeriod) {
    this.checkCfgLegal();
    this.vo.setKeepTestPeriod(keepTestPeriod);
  }

  public void setTestTableName(String testTableName) {
    this.checkCfgLegal();
    this.vo.setTestTableName(testTableName);
  }

  public void setTestBeforeUse(boolean testBeforeUse) {
    this.checkCfgLegal();
    this.vo.setTestBeforeUse(testBeforeUse);
  }

  public void setTestQuerySql(String testQuerySql) {
    this.checkCfgLegal();
    this.vo.setTestQuerySql(testQuerySql);
  }

  public void setShowSql(boolean showSql) {
    this.checkCfgLegal();
    this.vo.setShowSql(showSql);
  }

  public void setSqlTimeFilter(long sqlTimeFilter) {
    this.checkCfgLegal();
    this.vo.setSqlTimeFilter(sqlTimeFilter);
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
  }

  /**
   * Init the pool
   */
  @Override
  public void init() {
    if (isInited) {
      return;
    }
    this.lock.lock();
    try {
      if (isInited) {
        return;
      }
      if (!this.isUseVO) {
        // Note:if we haven't set poolPath and ConfigurationVO,we will
        // use
        // the default path to init pool
        this.initPath(this.poolPath);
      } else {
        if (this.dataSource == null && (this.driverClassName != null || this.jdbcUrl != null
            || this.jdbcUser != null || this.jdbcPassword != null)) {
          // we are trying to use jdbc driver if dataSource is null.
          this.dataSource = JDBCConfiguration.getDataSource(this.driverClassName, this.jdbcUrl,
              this.jdbcUser, this.jdbcPassword);
        }
        this.vo.setCommonDataSource(this.dataSource);
        ConfigurationVO.setConsole(this.console);
        this.initVO(this.vo);
      }
      isInited = true;
    } finally {
      this.lock.unlock();
    }
  }

  /**
   * Init pool by the given path
   */
  @Override
  public void initPath(String path) {
    if (isInited) {
      return;
    }
    this.lock.lock();
    try {
      if (isInited) {
        return;
      }
      ConnectionPoolImpl.getInstance().initPath(path);
      isInited = true;
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  public void initVO(ConfigurationVO vo) {
    if (isInited) {
      return;
    }
    this.lock.lock();
    try {
      if (isInited) {
        return;
      }
      ConnectionPoolImpl.getInstance().initVO(vo);
      isInited = true;
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  public void initVOList(List<ConfigurationVO> voList) {
    if (isInited) {
      return;
    }
    this.lock.lock();
    try {
      if (isInited) {
        return;
      }
      ConnectionPoolImpl.getInstance().initVOList(voList);
      isInited = true;
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    this.init();
    return ConnectionPoolImpl.getInstance().getConnection();
  }

  @Override
  public Connection getConnection(String name) throws SQLException {
    this.init();
    return ConnectionPoolImpl.getInstance().getConnection(name);
  }

  @Override
  public PooledConnection getPooledConnection() throws SQLException {
    this.init();
    return ConnectionPoolImpl.getInstance().getPooledConnection();
  }

  @Override
  public PooledConnection getPooledConnection(String name) throws SQLException {
    this.init();
    return ConnectionPoolImpl.getInstance().getPooledConnection(name);
  }

  @Override
  public PooledConnection getPooledConnection(String user, String password) throws SQLException {
    throw new UnsupportedOperationException("not supported yet");
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
