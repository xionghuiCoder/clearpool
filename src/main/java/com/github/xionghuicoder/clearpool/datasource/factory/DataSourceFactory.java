package com.github.xionghuicoder.clearpool.datasource.factory;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;

import com.github.xionghuicoder.clearpool.datasource.AbstractDataSource;
import com.github.xionghuicoder.clearpool.datasource.DataSourceImpl;

public class DataSourceFactory extends DataSourceAbstractFactory {

  @Override
  public AbstractDataSource createDataSource(CommonDataSource commonDataSource) {
    return new DataSourceImpl((DataSource) commonDataSource);
  }
}
