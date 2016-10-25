package com.github.xionghuicoder.clearpool.datasource.factory;

import javax.sql.CommonDataSource;
import javax.sql.XADataSource;

import com.github.xionghuicoder.clearpool.datasource.AbstractDataSource;
import com.github.xionghuicoder.clearpool.datasource.XADataSourceImpl;

public class XADataSourceFactory extends DataSourceAbstractFactory {

  @Override
  public AbstractDataSource createDataSource(CommonDataSource commonDataSource) {
    return new XADataSourceImpl((XADataSource) commonDataSource);
  }
}
