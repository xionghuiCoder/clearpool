package org.opensource.clearpool.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.opensource.clearpool.datasource.connection.ConnectionWrapper;
import org.opensource.clearpool.datasource.connection.CommonConnection;

public class DataSourceImpl extends AbstractDataSource {
	private DataSource ds;

	public DataSourceImpl(DataSource ds) {
		this.ds = ds;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return this.ds.getConnection();
	}

	@Override
	public CommonConnection getCommonConnection() throws SQLException {
		Connection con = this.getConnection();
		CommonConnection cmnCon = new ConnectionWrapper(con);
		return cmnCon;
	}
}
