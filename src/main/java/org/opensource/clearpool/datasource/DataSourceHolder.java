package org.opensource.clearpool.datasource;

import java.util.Map;

import javax.sql.DataSource;

/**
 * This class is used to get DataSource for pool,if you haven't set JDBC and
 * JNDI to get connection,you should set this class with a valid dataSource or a
 * valid dataSourceMap.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public abstract class DataSourceHolder {
	// dataSource is used for unique pool to get connection
	private static DataSource dataSource;

	// dataSourceMap is used for distributed pool to get connection
	private static Map<String, DataSource> dataSourceMap;

	public static DataSource getDataSource() {
		return dataSource;
	}

	public static void setDataSource(DataSource dataSource) {
		DataSourceHolder.dataSource = dataSource;
	}

	public static Map<String, DataSource> getDataSourceMap() {
		return dataSourceMap;
	}

	public static void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
		DataSourceHolder.dataSourceMap = dataSourceMap;
	}
}
