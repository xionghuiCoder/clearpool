package com.github.xionghuicoder.clearpool.console;

import javax.sql.CommonDataSource;

import com.github.xionghuicoder.clearpool.core.ConnectionPoolManager;
import com.github.xionghuicoder.clearpool.datasource.JDBCDataSource;

class ConnectionPool implements ConnectionPoolMBean {
  private final ThreadLocal<PoolInfo> poolInfoHolder = new ThreadLocal<PoolInfo>();

  private final ConnectionPoolManager pool;

  ConnectionPool(ConnectionPoolManager pool) {
    this.pool = pool;
  }

  @Override
  public String get00_Name() {
    return this.pool.getCfgVO().getName();
  }

  @Override
  public String get01_JndiName() {
    return this.pool.getCfgVO().getJndiName();
  }

  @Override
  public String get02_DataSourceClass() {
    CommonDataSource dataSource = this.pool.getCfgVO().getDataSource();
    return dataSource.getClass().getName();
  }

  @Override
  public String get03_DriverClassName() {
    CommonDataSource dataSource = this.pool.getCfgVO().getDataSource();
    if (dataSource instanceof JDBCDataSource) {
      JDBCDataSource jdbcDataSource = (JDBCDataSource) dataSource;
      String clazz = jdbcDataSource.getClazz();
      if (clazz != null) {
        return clazz;
      }
    }
    return "-";
  }

  @Override
  public String get04_Url() {
    CommonDataSource dataSource = this.pool.getCfgVO().getDataSource();
    if (dataSource instanceof JDBCDataSource) {
      JDBCDataSource jdbcDataSource = (JDBCDataSource) dataSource;
      String url = jdbcDataSource.getUrl();
      if (url != null) {
        return url;
      }
    }
    return "-";
  }

  @Override
  public String get05_Username() {
    return "-";
  }

  @Override
  public String get06_Password() {
    return "-";
  }

  @Override
  public String get07_SecurityClassName() {
    return this.pool.getCfgVO().getSecurityClassName();
  }

  @Override
  public boolean is08_JtaSupport() {
    return this.pool.getCfgVO().isJtaSupport();
  }

  @Override
  public int get09_CorePoolSize() {
    return this.pool.getCfgVO().getCorePoolSize();
  }

  @Override
  public int get10_MaxPoolSize() {
    return this.pool.getCfgVO().getMaxPoolSize();
  }

  @Override
  public int get11_AcquireIncrement() {
    return this.pool.getCfgVO().getAcquireIncrement();
  }

  @Override
  public int get12_AcquireRetryTimes() {
    return this.pool.getCfgVO().getAcquireRetryTimes();
  }

  @Override
  public boolean is13_UselessConnectionException() {
    return this.pool.getCfgVO().isUselessConnectionException();
  }

  @Override
  public String get14_LimitIdleTime() {
    long time = this.pool.getCfgVO().getLimitIdleTime();
    return time + "(ms)";
  }

  @Override
  public String get15_KeepTestPeriod() {
    long period = this.pool.getCfgVO().getKeepTestPeriod();
    if (period == -1) {
      return "-";
    }
    return period + "(ms)";
  }

  @Override
  public boolean is16_TestBeforeUse() {
    return this.pool.getCfgVO().isTestBeforeUse();
  }

  @Override
  public String get17_TestQuerySql() {
    return this.pool.getCfgVO().getTestQuerySql();
  }

  @Override
  public boolean is18_ShowSql() {
    return this.pool.getCfgVO().isShowSql();
  }

  @Override
  public String get19_SqlTimeFilter() {
    long sqlTimeFilter = this.pool.getCfgVO().getSqlTimeFilter();
    if (sqlTimeFilter == -1) {
      return "-";
    }
    return sqlTimeFilter + "(ms)";
  }

  @Override
  public int get20_PeakPoolSize() {
    return this.pool.getPeakPoolSize();
  }

  @Override
  public int get21_PoolSize() {
    PoolInfo poolInfo = this.poolInfoHolder.get();
    if (poolInfo == null) {
      poolInfo = new PoolInfo(this.pool.getPoolSize(), this.pool.getConnectionChain().size());
      this.poolInfoHolder.set(poolInfo);
    }
    int poolSize = poolInfo.getPoolSize();
    if (poolInfo.isRefresh()) {
      this.poolInfoHolder.remove();
    }
    return poolSize;
  }

  @Override
  public int get22_ConnectionUsing() {
    PoolInfo poolInfo = this.poolInfoHolder.get();
    if (poolInfo == null) {
      poolInfo = new PoolInfo(this.pool.getPoolSize(), this.pool.getConnectionChain().size());
      this.poolInfoHolder.set(poolInfo);
    }
    int connectionUsing = poolInfo.getConnectionUsing();
    if (poolInfo.isRefresh()) {
      this.poolInfoHolder.remove();
    }
    return connectionUsing;
  }

  @Override
  public int get23_ConnectionLeft() {
    PoolInfo poolInfo = this.poolInfoHolder.get();
    if (poolInfo == null) {
      poolInfo = new PoolInfo(this.pool.getPoolSize(), this.pool.getConnectionChain().size());
      this.poolInfoHolder.set(poolInfo);
    }
    int connectionLeft = poolInfo.getConnectionLeft();
    if (poolInfo.isRefresh()) {
      this.poolInfoHolder.remove();
    }
    return connectionLeft;
  }

  /**
   * 存储连接池信息
   *
   * @author xionghui
   * @version 1.0.0
   * @since 1.0.0
   */
  private static class PoolInfo {
    final int poolSize;
    final int connectionLeft;
    final int connectionUsing;

    boolean poolSizeBoolean = false;
    boolean connectionLeftBoolean = false;
    boolean connectionUsingBoolean = false;

    PoolInfo(int poolSize, int connectionLeft) {
      if (poolSize < connectionLeft) {
        poolSize = connectionLeft;
      }
      this.poolSize = poolSize;
      this.connectionLeft = connectionLeft;
      this.connectionUsing = poolSize - connectionLeft;
    }

    public int getPoolSize() {
      this.poolSizeBoolean = true;
      return this.poolSize;
    }

    public int getConnectionLeft() {
      this.connectionLeftBoolean = true;
      return this.connectionLeft;
    }

    public int getConnectionUsing() {
      this.connectionUsingBoolean = true;
      return this.connectionUsing;
    }

    public boolean isRefresh() {
      return this.poolSizeBoolean && this.connectionLeftBoolean && this.connectionUsingBoolean;
    }
  }
}
