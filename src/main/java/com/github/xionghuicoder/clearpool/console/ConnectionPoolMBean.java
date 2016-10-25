package com.github.xionghuicoder.clearpool.console;

public interface ConnectionPoolMBean {

  String get00_Name();

  String get01_JndiName();

  String get02_DataSourceClass();

  String get03_DriverClassName();

  String get04_Url();

  String get05_Username();

  String get06_Password();

  String get07_SecurityClassName();

  boolean is08_JtaSupport();

  int get09_CorePoolSize();

  int get10_MaxPoolSize();

  int get11_AcquireIncrement();

  int get12_AcquireRetryTimes();

  boolean is13_UselessConnectionException();

  String get14_LimitIdleTime();

  String get15_KeepTestPeriod();

  boolean is16_TestBeforeUse();

  String get17_TestQuerySql();

  boolean is18_ShowSql();

  String get19_SqlTimeFilter();

  int get20_PeakPoolSize();

  int get21_PoolSize();

  int get22_ConnectionUsing();

  int get23_ConnectionLeft();
}
