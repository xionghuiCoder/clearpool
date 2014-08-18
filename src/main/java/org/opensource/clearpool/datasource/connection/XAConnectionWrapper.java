package org.opensource.clearpool.datasource.connection;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;

import org.opensource.clearpool.exception.ConnectionPoolException;

public class XAConnectionWrapper extends CommonConnection {
	private XAConnection xaCon;

	public XAConnectionWrapper(XAConnection xaCon) {
		this.xaCon = xaCon;
	}

	@Override
	public Connection getConnection() {
		Connection con = null;
		try {
			con = this.xaCon.getConnection();
		} catch (SQLException e) {
			throw new ConnectionPoolException(e);
		}
		return con;
	}

	@Override
	public XAConnection getXAConnection() {
		return this.xaCon;
	}
}
