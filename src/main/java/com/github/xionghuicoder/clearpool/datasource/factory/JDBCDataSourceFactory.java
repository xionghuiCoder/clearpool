package com.github.xionghuicoder.clearpool.datasource.factory;

import javax.sql.CommonDataSource;

import com.github.xionghuicoder.clearpool.datasource.AbstractDataSource;
import com.github.xionghuicoder.clearpool.datasource.JDBCDataSource;
import com.github.xionghuicoder.clearpool.datasource.JDBCDataSourceWrapper;

public class JDBCDataSourceFactory extends DataSourceAbstractFactory {

  @Override
  public AbstractDataSource createDataSource(CommonDataSource commonDataSource) {
    return new JDBCDataSourceWrapper((JDBCDataSource) commonDataSource);
  }
}
