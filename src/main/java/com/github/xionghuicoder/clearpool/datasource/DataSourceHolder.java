package com.github.xionghuicoder.clearpool.datasource;

import java.util.Map;

import javax.sql.CommonDataSource;

import com.github.xionghuicoder.clearpool.core.ConfigurationVO;

/**
 * 缓存<tt>dataSource</tt>，初始化{@link ConfigurationVO#initDatasource ConfigurationVO.initDatasource}会取该数据。
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 * @see ConfigurationVO
 */
public abstract class DataSourceHolder {
  private static Map<String, CommonDataSource> dataSourceMap;

  public static Map<String, CommonDataSource> getDataSourceMap() {
    return dataSourceMap;
  }

  public static void setDataSourceMap(Map<String, CommonDataSource> dataSourceMap) {
    DataSourceHolder.dataSourceMap = dataSourceMap;
  }
}
