package com.github.xionghuicoder.clearpool.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.sql.PooledConnection;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.console.MBeanFacade;
import com.github.xionghuicoder.clearpool.core.hook.IdleCheckHook;
import com.github.xionghuicoder.clearpool.core.hook.ShutdownHook;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;

/**
 * 支持管理一到多个{@link ConnectionPoolManager}
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
class ConnectionPoolContainer {
  private static final PoolLogger LOGGER =
      PoolLoggerFactory.getLogger(ConnectionPoolContainer.class);

  private volatile Thread idleCheckHookThread;

  private final Map<String, ConnectionPoolManager> poolMap =
      new HashMap<String, ConnectionPoolManager>();

  void load(MBeanFacade mbeanFacade, List<ConfigurationVO> voList) {
    List<ConfigurationVO> cfgVOList = new ArrayList<ConfigurationVO>();
    Set<String> nameSet = new HashSet<String>();
    for (ConfigurationVO vo : voList) {
      if (vo == null) {
        continue;
      }
      ConfigurationVO cfgVO = vo.clone();
      cfgVOList.add(cfgVO);
      cfgVO.init();
      if (!nameSet.add(cfgVO.getName())) {
        throw new ConnectionPoolException("name " + vo.getName() + " repeat");
      }
    }
    try {
      this.initPool(mbeanFacade, cfgVOList);
    } catch (Throwable e) {
      for (ConnectionPoolManager pool : this.poolMap.values()) {
        pool.remove();
      }
      this.poolMap.clear();
      throw new ConnectionPoolException(e);
    }
    this.startHooks(mbeanFacade);
  }

  private void startHooks(MBeanFacade mbeanFacade) {
    CountDownLatch startLatch = new CountDownLatch(2);
    MBeanFacade.start(mbeanFacade, startLatch);
    if (this.idleCheckHookThread == null) {
      Collection<ConnectionPoolManager> poolCollection = this.poolMap.values();
      ShutdownHook.registerHook(poolCollection);
      this.idleCheckHookThread = IdleCheckHook.startHook(poolCollection, startLatch);
    } else {
      startLatch.countDown();
    }
    try {
      startLatch.await();
    } catch (InterruptedException e) {
      throw new ConnectionPoolException(e);
    }
  }

  private void initPool(MBeanFacade mbeanFacade, List<ConfigurationVO> cfgVOList) {
    String packageName = this.getClass().getPackage().getName();
    for (ConfigurationVO cfgVO : cfgVOList) {
      long begin = System.currentTimeMillis();
      ConnectionPoolManager pool = new ConnectionPoolManager(cfgVO);
      try {
        pool.initPool();
      } catch (Throwable t) {
        pool.remove();
        throw new ConnectionPoolException(t);
      }
      String poolName = cfgVO.getName();
      this.poolMap.put(poolName, pool);
      String mbeanName = packageName + ":type=Pool" + (poolName == null ? "" : ",name=" + poolName);
      MBeanFacade.registerMBean(mbeanFacade, pool, mbeanName, poolName);
      LOGGER.info("init pool " + poolName + " success, cost " + (System.currentTimeMillis() - begin)
          + "ms");
    }
  }

  /**
   * 多个连接池时不支持{@link #getConnection getConnection}；<br>
   * 此时使用{@link #getConnection(String) getConnection(String)}来获取连接；
   *
   * @see #getConnection(String)
   */
  PooledConnection getConnection(long maxWait) throws SQLException {
    if (this.poolMap.size() != 1) {
      throw new UnsupportedOperationException(
          "not supported, poolMap's size is " + this.poolMap.size());
    }
    PooledConnection pooledConnection = null;
    for (ConnectionPoolManager pool : this.poolMap.values()) {
      pooledConnection = pool.exitPool(maxWait);
      break;
    }
    return pooledConnection;
  }

  PooledConnection getConnection(String name, long maxWait) throws SQLException {
    ConnectionPoolManager pool = this.poolMap.get(name);
    if (pool == null) {
      return null;
    }
    PooledConnection pooledConnection = pool.exitPool(maxWait);
    return pooledConnection;
  }

  void close(MBeanFacade mbeanFacade, String name) {
    ConnectionPoolManager realPool = this.poolMap.remove(name);
    if (realPool != null) {
      MBeanFacade.unregisterMBean(mbeanFacade, name);
      realPool.remove();
    }
  }

  void close(MBeanFacade mbeanFacade) {
    if (this.idleCheckHookThread != null) {
      this.idleCheckHookThread.interrupt();
    }
    for (Entry<String, ConnectionPoolManager> e : this.poolMap.entrySet()) {
      String poolName = e.getKey();
      MBeanFacade.unregisterMBean(mbeanFacade, poolName);
      ConnectionPoolManager pool = e.getValue();
      pool.remove();
    }
    this.poolMap.clear();
  }
}
