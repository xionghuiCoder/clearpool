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
public class ClearPoolDataSource extends AbstractDataSource implements IConnectionPool, Closeable,
    ConnectionPoolDataSource {
  private static volatile boolean isInited = false;

  private Lock lock = new ReentrantLock();

  private String poolPath;
  private boolean isUsePath;
  private ConfigurationVO vo = new ConfigurationVO();
  private boolean isUseVO;

  private Console console;

  private CommonDataSource dataSource;

  private String driverClass;
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
    if (this.driverClass != null || this.jdbcUrl != null || this.jdbcUser != null
        || this.jdbcPassword != null) {
      throw new ConnectionPoolXMLParseException(
          "we shouldn't use JDBC and dataSource at the same time");
    }
    this.dataSource = dataSource;
  }

  public void setDriverClass(String driverClass) {
    this.checkCfgLegal();
    this.checkAndInitJDBC();
    this.driverClass = driverClass;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.checkCfgLegal();
    this.checkAndInitJDBC();
    this.jdbcUrl = jdbcUrl;
  }

  public void setJdbcUser(String jdbcUser) {
    this.checkCfgLegal();
    this.checkAndInitJDBC();
    this.jdbcUser = jdbcUser;
  }

  public void setJdbcPassword(String jdbcPassword) {
    this.checkCfgLegal();
    this.checkAndInitJDBC();
    this.jdbcPassword = jdbcPassword;
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
    if (!this.isUseVO) {
      // Note:if we haven't set poolPath and ConfigurationVO,we will
      // use
      // the default path to init pool
      this.initPath(this.poolPath);
      return;
    }
    if (this.dataSource == null
        && (this.driverClass != null || this.jdbcUrl != null || this.jdbcUser != null || this.jdbcPassword != null)) {
      // we are trying to use jdbc driver if dataSource is null.
      this.dataSource =
          JDBCConfiguration.getDataSource(this.driverClass, this.jdbcUrl, this.jdbcUser,
              this.jdbcPassword);
    }
    this.vo.setCommonDataSource(this.dataSource);
    ConfigurationVO.setConsole(console);
    this.initVO(this.vo);
  }

  /**
   * Try to init the pool.
   */
  private void tryInit() {
    if (isInited) {
      return;
    }
    this.lock.lock();
    try {
      if (isInited) {
        return;
      }
      this.init();
    } finally {
      this.lock.unlock();
    }
  }

  /**
   * Init pool by the given path
   */
  @Override
  public void initPath(String path) {
    ConnectionPoolImpl.getInstance().initPath(path);
    isInited = true;
  }

  @Override
  public void initVO(ConfigurationVO vo) {
    ConnectionPoolImpl.getInstance().initVO(vo);
    isInited = true;
  }

  @Override
  public void initVOList(List<ConfigurationVO> voList) {
    ConnectionPoolImpl.getInstance().initVOList(voList);
    isInited = true;
  }

  @Override
  public Connection getConnection() throws SQLException {
    this.tryInit();
    return ConnectionPoolImpl.getInstance().getConnection();
  }

  @Override
  public Connection getConnection(String name) throws SQLException {
    this.tryInit();
    return ConnectionPoolImpl.getInstance().getConnection(name);
  }

  @Override
  public PooledConnection getPooledConnection() throws SQLException {
    this.tryInit();
    return ConnectionPoolImpl.getInstance().getPooledConnection();
  }

  @Override
  public PooledConnection getPooledConnection(String name) throws SQLException {
    this.tryInit();
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
