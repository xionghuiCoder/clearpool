package com.github.xionghuicoder.clearpool.datasource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

import javax.sql.PooledConnection;
import javax.sql.XAConnection;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.jta.xa.XAConnectionImpl;
import com.github.xionghuicoder.clearpool.jta.xa.jtds.JtdsXAConnection;
import com.github.xionghuicoder.clearpool.util.H2Utils;
import com.github.xionghuicoder.clearpool.util.JdbcUtils;
import com.github.xionghuicoder.clearpool.util.MysqlUtils;
import com.github.xionghuicoder.clearpool.util.OracleUtils;
import com.github.xionghuicoder.clearpool.util.PGUtils;

public class JDBCXADataSource extends AbstractDataSource {
  private final JDBCDataSource jdbcDs;

  private Object h2Factory = null;

  public JDBCXADataSource(JDBCDataSource jdbcDs) {
    this.jdbcDs = jdbcDs;
    this.initCheck();
  }

  private void initCheck() {
    String url = this.jdbcDs.getUrl();
    if (url.startsWith(JdbcUtils.ORACLE_ONE_PREFIX)
        || url.startsWith(JdbcUtils.ORACLE_ANO_PREFIX)) {
      Driver driver = this.jdbcDs.getDriver();
      if (driver.getMajorVersion() < 10) {
        throw new ConnectionPoolException("not support oracle driver " + driver.getMajorVersion()
            + "." + driver.getMinorVersion());
      }
    }
    if (url.startsWith("jdbc:h2:")) {
      this.h2Factory = H2Utils.createJdbcDataSourceFactory();
    }
  }

  @Override
  public CommonConnection getCommonConnection() throws SQLException {
    XAConnection xaCon = this.createXAConnetion();
    CommonConnection cmnCon = new XAConnectionWrapper(xaCon);
    return cmnCon;
  }

  private XAConnection createXAConnetion() throws SQLException {
    Connection con = this.jdbcDs.getConnection();
    String url = this.jdbcDs.getUrl();
    if (url.startsWith(JdbcUtils.MYSQL_PREFIX)) {
      return MysqlUtils.mysqlXAConnection(con);
    }
    if (url.startsWith(JdbcUtils.ORACLE_ONE_PREFIX)
        || url.startsWith(JdbcUtils.ORACLE_ANO_PREFIX)) {
      return OracleUtils.oracleXAConnection(con);
    }
    if (url.startsWith(JdbcUtils.H2_PREFIX)) {
      return H2Utils.createXAConnection(this.h2Factory, con);
    }
    if (url.startsWith(JdbcUtils.POSTGRESQL_PREFIX)) {
      return PGUtils.createXAConnection(con);
    }
    if (url.startsWith(JdbcUtils.JTDS_PREFIX)) {
      return new JtdsXAConnection(con);
    }
    throw new SQLException("xa does not support url: " + url);
  }

  @Override
  public PooledConnection createPooledConnection(ConnectionProxy conProxy) throws SQLException {
    PooledConnection pooledConnection = new XAConnectionImpl(conProxy);
    return pooledConnection;
  }
}
