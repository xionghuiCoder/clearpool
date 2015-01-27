package org.opensource.clearpool.console;

import javax.sql.CommonDataSource;

import org.opensource.clearpool.core.ConnectionPoolManager;
import org.opensource.clearpool.datasource.JDBCDataSource;

class ConnectionPool implements ConnectionPoolMBean {
  private ConnectionPoolManager pool;

  private CommonDataSource dataSource;

  private int poolSize = -1;

  private int connectionLeft = -1;

  ConnectionPool(ConnectionPoolManager pool) {
    this.pool = pool;
    this.dataSource = this.pool.getCfgVO().getCommonDataSource();
  }

  @Override
  public String getAlias() {
    String alias = this.pool.getCfgVO().getAlias();
    if (alias == null) {
      alias = "-";
    }
    return alias;
  }

  @Override
  public String getDataSourceClass() {
    return this.dataSource.getClass().getName();
  }

  @Override
  public String getDriverUrl() {
    if (this.dataSource instanceof JDBCDataSource) {
      JDBCDataSource jdbcDataSource = (JDBCDataSource) this.dataSource;
      String url = jdbcDataSource.getUrl();
      if (url != null) {
        return url;
      }
    }
    return "-";
  }

  @Override
  public String getDriverClass() {
    if (this.dataSource instanceof JDBCDataSource) {
      JDBCDataSource jdbcDataSource = (JDBCDataSource) this.dataSource;
      String clazz = jdbcDataSource.getClazz();
      if (clazz != null) {
        return clazz;
      }
    }
    return "-";
  }

  @Override
  public int getCorePoolSize() {
    return this.pool.getCfgVO().getCorePoolSize();
  }

  @Override
  public int getMaxPoolSize() {
    return this.pool.getCfgVO().getMaxPoolSize();
  }

  @Override
  public int getAcquireIncrement() {
    return this.pool.getCfgVO().getAcquireIncrement();
  }

  @Override
  public int getAcquireRetryTimes() {
    return this.pool.getCfgVO().getAcquireRetryTimes();
  }

  @Override
  public boolean getUselessConnectionException() {
    return this.pool.getCfgVO().getUselessConnectionException();
  }

  @Override
  public String getLimitIdleTime() {
    long time = this.pool.getCfgVO().getLimitIdleTime();
    return time / 1000 + "(s)";
  }

  @Override
  public String getKeepTestPeriod() {
    long period = this.pool.getCfgVO().getKeepTestPeriod();
    if (period == -1) {
      return "-";
    }
    return period / 1000 + "(s)";
  }

  @Override
  public String getTestTableName() {
    String name = this.pool.getCfgVO().getTestTableName();
    if (name == null) {
      name = "-";
    }
    return name;
  }

  @Override
  public boolean isShowSql() {
    return this.pool.getCfgVO().isShowSql();
  }

  @Override
  public String getSqlTimeFilter() {
    long sqlTimeFilter = this.pool.getCfgVO().getSqlTimeFilter();
    if (sqlTimeFilter == -1) {
      return "-";
    }
    return sqlTimeFilter / 1000 + "(s)";
  }

  @Override
  public int getPeakPoolSize() {
    return this.pool.getPeakPoolSize();
  }

  /**
   * Make sure {@link #getConnectionUsing()} and this method get the same {@link #poolSize}.
   */
  @Override
  public int getPoolSize() {
    int size = this.poolSize;
    if (this.poolSize == -1) {
      this.poolSize = this.pool.getPoolSize();
      size = this.poolSize;
    } else {
      this.poolSize = -1;
    }
    return size;
  }

  @Override
  public int getConnectionUsing() {
    int size = this.poolSize;
    if (this.poolSize == -1) {
      this.poolSize = this.pool.getPoolSize();
      size = this.poolSize;
    } else {
      this.poolSize = -1;
    }
    int connLeft = this.connectionLeft;
    if (this.connectionLeft == -1) {
      this.connectionLeft = this.pool.getConnectionChain().size();
      connLeft = this.connectionLeft;
    } else {
      this.connectionLeft = -1;
    }
    return size - connLeft;
  }

  /**
   * Make sure {@link #getConnectionUsing()} and this method get the same {@link #connectionLeft}.
   */
  @Override
  public int getConnectionLeft() {
    int connLeft = this.connectionLeft;
    if (this.connectionLeft == -1) {
      this.connectionLeft = this.pool.getConnectionChain().size();
      connLeft = this.connectionLeft;
    } else {
      this.connectionLeft = -1;
    }
    return connLeft;
  }
}
