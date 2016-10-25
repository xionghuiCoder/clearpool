package com.github.xionghuicoder.clearpool.datasource.factory;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;

import com.github.xionghuicoder.clearpool.datasource.AbstractDataSource;
import com.github.xionghuicoder.clearpool.datasource.PoolDataSource;

public class PoolDataSourceFactory extends DataSourceAbstractFactory {

  @Override
  public AbstractDataSource createDataSource(CommonDataSource commonDataSource) {
    return new PoolDataSource((ConnectionPoolDataSource) commonDataSource);
  }
}
