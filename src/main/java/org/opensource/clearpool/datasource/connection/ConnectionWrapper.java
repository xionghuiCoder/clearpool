package org.opensource.clearpool.datasource.connection;

import java.sql.Connection;

import javax.sql.XAConnection;

public class ConnectionWrapper extends CommonConnection {
	private Connection con;

	public ConnectionWrapper(Connection con) {
		this.con = con;
	}

	@Override
	public Connection getConnection() {
		return this.con;
	}

	@Override
	public XAConnection getXAConnection() {
		return null;
	}
}
