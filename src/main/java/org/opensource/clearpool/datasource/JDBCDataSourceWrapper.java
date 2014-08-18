package org.opensource.clearpool.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import org.opensource.clearpool.datasource.connection.ConnectionWrapper;
import org.opensource.clearpool.datasource.connection.CommonConnection;

/**
 * JDBC Manager
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class JDBCDataSourceWrapper extends AbstractDataSource {
	private JDBCDataSource jdbcDs;

	public JDBCDataSourceWrapper(JDBCDataSource jdbcDs) {
		this.jdbcDs = jdbcDs;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return this.jdbcDs.getConnection();
	}

	@Override
	public CommonConnection getCommonConnection() throws SQLException {
		Connection con = this.getConnection();
		CommonConnection cmnCon = new ConnectionWrapper(con);
		return cmnCon;
	}
}
