package org.opensource.clearpool.console;

public interface ConnectionPoolMBean {
  public String getAlias();

  public String getDataSourceClass();

  public String getDriverUrl();

  public String getDriverClass();

  public int getCorePoolSize();

  public int getMaxPoolSize();

  public int getAcquireIncrement();

  public int getAcquireRetryTimes();

  public boolean getUselessConnectionException();

  public String getLimitIdleTime();

  public String getKeepTestPeriod();

  public String getTestTableName();

  public boolean isShowSql();

  public int getPeakPoolSize();

  public int getPoolSize();

  public int getConnectionUsing();

  public int getConnectionLeft();
}
