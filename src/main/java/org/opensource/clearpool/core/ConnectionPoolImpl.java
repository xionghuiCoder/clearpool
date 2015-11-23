package org.opensource.clearpool.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.console.MBeanFacade;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolStateException;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

/**
 * The pool provide two kind of database connection pool.Please check {@link CommonPoolContainer} if
 * you want the details.
 *
 * The pool have 3 different states here.We can do nothing if the pool is unInitialized or
 * destroyed, and we can do everything if pool is initialized.
 *
 * Note:this class is a singleton class.The reason that we don't use ENUM singleton model which is
 * recommend by Joshua Bloch is because ENUM instance cann't be released.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class ConnectionPoolImpl implements IConnectionPool {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(ConnectionPoolImpl.class);

  // the INSTANCE should be the front of the SINGLETON_MARK
  private static ConnectionPoolImpl instance = new ConnectionPoolImpl();

  // the SINGLETON_MARK make sure the ClearPool is a singleton
  private final static boolean SINGLETON_MARK;

  static {
    SINGLETON_MARK = true;
  }

  /**
   * we get 3 states here.
   *
   * state=0:unInitialized; state=1:initialized; state=2:destroyed.
   *
   */
  private volatile int state = 0;

  // it is used to handle the pool.
  static volatile ConnectionPoolContainer poolContainer;

  /**
   * Hide the constructor
   */
  private ConnectionPoolImpl() {
    // whenever we invoke the constructor by reflection,we throw a
    // ConnectionPoolException.
    if (SINGLETON_MARK) {
      throw new ConnectionPoolException("create ClearPool illegal");
    }
  }

  /**
   * Get a instance of connection pool
   */
  static ConnectionPoolImpl getInstance() {
    ConnectionPoolImpl tempInstance = instance;
    if (tempInstance == null) {
      throw new ConnectionPoolStateException("clearpool had been destroyed");
    }
    return tempInstance;
  }

  /**
   * Init pool by the default path
   */
  @Override
  public void init() {
    this.initPath(null);
  }

  /**
   * Init pool by the given path
   */
  @Override
  public void initPath(String path) {
    this.load(path, null);
  }

  /**
   * Init pool by vo
   */
  @Override
  public void initVO(ConfigurationVO vo) {
    Map<String, ConfigurationVO> cfgMap = new HashMap<String, ConfigurationVO>();
    vo.init();
    cfgMap.put(vo.getAlias(), vo);
    this.load(null, cfgMap);
  }

  /**
   * Init pool by voList
   */
  @Override
  public void initVOList(List<ConfigurationVO> voList) {
    Map<String, ConfigurationVO> cfgMap = new HashMap<String, ConfigurationVO>();
    for (ConfigurationVO vo : voList) {
      vo.init();
      if (cfgMap.put(vo.getAlias(), vo) != null) {
        throw new ConnectionPoolStateException(
            "ConfigurationVOs' alias " + vo.getAlias() + " repeat");
      }
    }
    this.load(null, cfgMap);
  }

  /**
   * Init pool by path or cfgMap.
   *
   * Note:one of path and cfgMap is null.
   */
  private void load(String path, Map<String, ConfigurationVO> cfgMap) {
    long begin = System.currentTimeMillis();
    // load cfg to init pool
    ConnectionPoolContainer container = ConnectionPoolContainer.load(path, cfgMap);
    if (container != null) {
      poolContainer = container;
      LOGGER.info(
          "connection pool initialized.it cost " + (System.currentTimeMillis() - begin) + "ms");
    }
    this.checkDestroyed();
    state = 1;
  }

  public PooledConnection getPooledConnection() throws SQLException {
    this.checkDestroyed();
    return poolContainer.getConnection();
  }

  @Override
  public PooledConnection getPooledConnection(String name) throws SQLException {
    this.checkDestroyed();
    return poolContainer.getConnection(name);
  }

  @Override
  public Connection getConnection() throws SQLException {
    PooledConnection pooledCon = this.getPooledConnection();
    if (pooledCon == null) {
      return null;
    }
    return pooledCon.getConnection();
  }

  @Override
  public Connection getConnection(String name) throws SQLException {
    PooledConnection pooledCon = this.getPooledConnection(name);
    if (pooledCon == null) {
      return null;
    }
    return pooledCon.getConnection();
  }

  @Override
  public void close(String name) {
    this.checkDestroyed();
    ConnectionPoolContainer tempContainer = poolContainer;
    if (tempContainer == null) {
      return;
    }
    tempContainer.remove(name);
    LOGGER.info("remove pool " + name);
  }

  @Override
  public void close() {
    this.checkDestroyed();
    // remove all the pool
    this.removeAll();
    LOGGER.info("the pool is removed");
  }

  /**
   * Remove all the pool.
   */
  private void removeAll() {
    ConnectionPoolContainer tempContainer = poolContainer;
    if (tempContainer == null) {
      return;
    }
    tempContainer.remove();
  }

  @Override
  public void destory() {
    if (state == 2) {
      return;
    }
    /**
     * When we destroy the pool,we should destroy this singleton too,otherwise it will cause a
     * memory leak.
     */
    instance = null;
    state = 2;
    ConnectionPoolContainer.destoryHooks();
    // remove all the pool
    this.removeAll();
    MBeanFacade.stop();
    poolContainer = null;
    LOGGER.info("the pool is destroyed");
  }

  /**
   * Check the state if it's destroyed.
   */
  private void checkDestroyed() {
    if (state == 2) {
      throw new ConnectionPoolStateException("clearpool have been destroyed");
    }
  }
}
