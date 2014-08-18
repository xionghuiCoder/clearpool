package org.opensource.clearpool.jta.xa;

import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.datasource.proxy.PoolConnectionImpl;
import org.opensource.clearpool.datasource.proxy.dynamic.ProxyFactory;

public class XAConnectionImpl extends PoolConnectionImpl implements
		XAConnection {
	private final XAConnection xaCon;

	public XAConnectionImpl(ConnectionProxy conProxy) {
		super(conProxy);
		this.xaCon = conProxy.getXaConnection();
	}

	@Override
	public XAResource getXAResource() throws SQLException {
		this.checkState();
		return this.xaCon.getXAResource();
	}

	@Override
	public Statement createProxyStatement(Statement statement, String sql) {
		Statement statementProxy = ProxyFactory.createProxyXAStatement(
				statement, this, sql);
		return statementProxy;
	}
}
