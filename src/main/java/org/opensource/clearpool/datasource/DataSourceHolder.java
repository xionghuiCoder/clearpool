package org.opensource.clearpool.datasource;

import java.util.Map;

import javax.sql.CommonDataSource;

/**
 * This class is used to get DataSource for pool,if you haven't set JDBC and
 * JNDI to get connection,you should set this class with a valid dataSourceMap.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public abstract class DataSourceHolder {
	// dataSourceMap is used for distributed pool to get connection
	private static Map<String, CommonDataSource> dataSourceMap;

	public static Map<String, CommonDataSource> getDataSourceMap() {
		return dataSourceMap;
	}

	public static void setDataSourceMap(
			Map<String, CommonDataSource> dataSourceMap) {
		DataSourceHolder.dataSourceMap = dataSourceMap;
	}
}
