package com.github.xionghuicoder.clearpool.datasource.factory;

import javax.sql.CommonDataSource;

import com.github.xionghuicoder.clearpool.datasource.AbstractDataSource;

public abstract class DataSourceAbstractFactory {

  public abstract AbstractDataSource createDataSource(CommonDataSource commonDataSource);
}
