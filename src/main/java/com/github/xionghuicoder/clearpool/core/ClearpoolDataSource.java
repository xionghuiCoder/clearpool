package com.github.xionghuicoder.clearpool.core;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.console.Console;
import com.github.xionghuicoder.clearpool.console.MBeanFacade;
import com.github.xionghuicoder.clearpool.datasource.AbstractDataSource;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;

/**
 * 数据库连接池clearpool
 *
 * <p>
 * 支持以下几种初始化方式：<br>
 * 通过{@link #init init}方法，此时会使用{@link #vo vo}进行初始化；<br>
 * 通过{@link #initVO initVO}方法，此时会使用参数<tt>vo</tt>进行初始化；<br>
 * 通过{@link #initVOList initVOList}方法，此时会使用参数<tt>voList</tt>初始化多个数据源的数据；<br>
 * </p>
 *
 * <p>
 * 支持以下几种关闭数据库方式：<br>
 * 通过{@link #close close}方法，此时会关闭所有的数据库连接池；<br>
 * 通过{@link #close(String) close(String)}方法，此时会关闭名称为<tt>name</tt>的数据库连接池；<br>
 * </p>
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ClearpoolDataSource extends AbstractDataSource
    implements IConnectionPool, Closeable, ConnectionPoolDataSource {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ClearpoolDataSource.class);

  private final Lock lock = new ReentrantLock();

  private volatile boolean isInited = false;
  private volatile boolean closed = false;

  private final ConnectionPoolContainer poolContainer = new ConnectionPoolContainer();

  private MBeanFacade mbeanFacade;

  private final ConfigurationVO vo = new ConfigurationVO();

  private Console console;

  public void setPort(int port) {
    if (this.console == null) {
      this.console = new Console();
    }
    this.console.setPort(port);
  }

  public void setSecurityMap(Map<String, String> securityMap) {
    if (this.console == null) {
      this.console = new Console();
    }
    this.console.setSecurityMap(securityMap);
  }

  public Console getConsole() {
    return this.console;
  }

  public void setConsole(Console console) {
    this.console = console;
  }

  public void setName(String name) {
    this.vo.setName(name);
  }

  public void setDataSource(CommonDataSource dataSource) {
    this.vo.setDataSource(dataSource);
  }

  public void setJndiName(String jndiName) {
    this.vo.setJndiName(jndiName);
  }

  public void setDriverClassName(String driverClassName) {
    this.vo.setDriverClassName(driverClassName);
  }

  public void setUrl(String url) {
    this.vo.setUrl(url);
  }

  public void setUsername(String username) {
    this.vo.setUsername(username);
  }

  public void setPassword(String password) {
    this.vo.setPassword(password);
  }

  public void setSecurityClassName(String securityClassName) {
    this.vo.setSecurityClassName(securityClassName);
  }

  public void setJtaSupport(boolean jtaSupport) {
    this.vo.setJtaSupport(jtaSupport);
  }

  public void setCorePoolSize(int corePoolSize) {
    this.vo.setCorePoolSize(corePoolSize);
  }

  public void setMaxPoolSize(int maxPoolSize) {
    this.vo.setMaxPoolSize(maxPoolSize);
  }

  public void setAcquireIncrement(int acquireIncrement) {
    this.vo.setAcquireIncrement(acquireIncrement);
  }

  public void setAcquireRetryTimes(int acquireRetryTimes) {
    this.vo.setAcquireRetryTimes(acquireRetryTimes);
  }

  public void setUselessConnectionException(boolean uselessConnectionException) {
    this.vo.setUselessConnectionException(uselessConnectionException);
  }

  public void setLimitIdleTime(long limitIdleTime) {
    this.vo.setLimitIdleTime(limitIdleTime);
  }

  public void setKeepTestPeriod(long keepTestPeriod) {
    this.vo.setKeepTestPeriod(keepTestPeriod);
  }

  public void setTestBeforeUse(boolean testBeforeUse) {
    this.vo.setTestBeforeUse(testBeforeUse);
  }

  public void setTestQuerySql(String testQuerySql) {
    this.vo.setTestQuerySql(testQuerySql);
  }

  public void setShowSql(boolean showSql) {
    this.vo.setShowSql(showSql);
  }

  public void setSqlTimeFilter(long sqlTimeFilter) {
    this.vo.setSqlTimeFilter(sqlTimeFilter);
  }

  @Override
  public void init() {
    this.initVO(this.vo);
  }

  @Override
  public void initVO(ConfigurationVO vo) {
    List<ConfigurationVO> voList = new ArrayList<ConfigurationVO>();
    voList.add(vo);
    this.initVOList(voList);
  }

  @Override
  public void initVOList(List<ConfigurationVO> voList) {
    this.load(voList);
  }

  private void load(List<ConfigurationVO> voList) {
    if (voList == null) {
      throw new ConnectionPoolException("voList is null");
    }
    if (this.isInited) {
      return;
    }
    this.lock.lock();
    try {
      if (this.isInited) {
        return;
      }
      long begin = System.currentTimeMillis();
      if (this.console != null) {
        this.mbeanFacade = new MBeanFacade(this.console);
      }
      this.poolContainer.load(this.mbeanFacade, voList);
      this.isInited = true;
      LOGGER.info("load success, cost " + (System.currentTimeMillis() - begin) + "ms");
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return this.getConnection(0L);
  }

  @Override
  public Connection getConnection(String name) throws SQLException {
    return this.getConnection(name, 0L);
  }

  @Override
  public Connection getConnection(long maxWait) throws SQLException {
    PooledConnection pooledCon = this.getPooledConnection(maxWait);
    return pooledCon == null ? null : pooledCon.getConnection();
  }

  @Override
  public Connection getConnection(String name, long maxWait) throws SQLException {
    PooledConnection pooledCon = this.getPooledConnection(name, maxWait);
    return pooledCon == null ? null : pooledCon.getConnection();
  }

  @Override
  public PooledConnection getPooledConnection() throws SQLException {
    return this.getPooledConnection(0L);
  }

  @Override
  public PooledConnection getPooledConnection(String name) throws SQLException {
    return this.getPooledConnection(name, 0L);
  }

  @Override
  public PooledConnection getPooledConnection(long maxWait) throws SQLException {
    this.init();
    return this.poolContainer.getConnection(maxWait);
  }

  @Override
  public PooledConnection getPooledConnection(String name, long maxWait) throws SQLException {
    this.init();
    return this.poolContainer.getConnection(name, maxWait);
  }

  @Override
  public PooledConnection getPooledConnection(String user, String password) throws SQLException {
    throw new UnsupportedOperationException("not supported yet");
  }

  @Override
  public void close() {
    if (this.closed) {
      LOGGER.warn("already closed");
      return;
    }
    if (!this.isInited) {
      LOGGER.warn("not inited");
      return;
    }
    this.lock.lock();
    try {
      if (this.closed) {
        LOGGER.warn("already closed");
        return;
      }
      if (!this.isInited) {
        LOGGER.warn("not inited");
        return;
      }
      this.poolContainer.close(this.mbeanFacade);
      MBeanFacade.stop(this.mbeanFacade);
      this.closed = true;
      LOGGER.info("closed");
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  public void close(String name) {
    this.poolContainer.close(this.mbeanFacade, name);
    LOGGER.info("closed pool " + name);
  }
}
