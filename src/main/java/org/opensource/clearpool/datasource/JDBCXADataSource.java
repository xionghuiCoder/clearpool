package org.opensource.clearpool.datasource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

import javax.sql.XAConnection;

import org.opensource.clearpool.datasource.connection.CommonConnection;
import org.opensource.clearpool.datasource.connection.XAConnectionWrapper;
import org.opensource.clearpool.exception.TransactionException;
import org.opensource.clearpool.jta.xa.jtds.JtdsXAConnection;
import org.opensource.clearpool.util.H2Util;
import org.opensource.clearpool.util.JdbcUtil;
import org.opensource.clearpool.util.MysqlUtil;
import org.opensource.clearpool.util.OracleUtil;
import org.opensource.clearpool.util.PGUtil;

/**
 * JDBC XA Manager
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class JDBCXADataSource extends AbstractDataSource {
	private JDBCDataSource jdbcDs;

	private Object h2Factory = null;

	public JDBCXADataSource(JDBCDataSource jdbcDs) {
		this.jdbcDs = jdbcDs;
		this.initCheck();
	}

	/**
	 * Check url
	 */
	private void initCheck() {
		String url = this.jdbcDs.getUrl();
		if (url.startsWith(JdbcUtil.ORACLE_DRIVER)) {
			Driver driver = this.jdbcDs.getDriver();
			if (driver.getMajorVersion() < 10) {
				throw new TransactionException("not support oracle driver "
						+ driver.getMajorVersion() + "."
						+ driver.getMinorVersion());
			}
		}
		if (url.startsWith(JdbcUtil.H2_DRIVER)) {
			this.h2Factory = H2Util.createJdbcDataSourceFactory();
		}
	}

	@Override
	public CommonConnection getCommonConnection() throws SQLException {
		XAConnection xaCon = this.createXAConnetion();
		CommonConnection cmnCon = new XAConnectionWrapper(xaCon);
		return cmnCon;
	}

	/**
	 * Build a XAConnetion by url
	 */
	private XAConnection createXAConnetion() throws SQLException {
		Connection con = this.jdbcDs.getConnection();
		String url = this.jdbcDs.getUrl();
		if (url.startsWith(JdbcUtil.MYSQL_DRIVER)) {
			return MysqlUtil.mysqlXAConnection(con);
		}
		if (url.startsWith(JdbcUtil.ORACLE_DRIVER)) {
			return OracleUtil.oracleXAConnection(con);
		}
		if (url.startsWith(JdbcUtil.H2_DRIVER)) {
			return H2Util.createXAConnection(this.h2Factory, con);
		}
		if (url.contains(JdbcUtil.POSTGRESQL)) {
			return PGUtil.createXAConnection(con);
		}
		if (url.contains(JdbcUtil.JTDS)) {
			return new JtdsXAConnection(con);
		}
		throw new SQLException("xa does not support url: " + url);
	}
}
