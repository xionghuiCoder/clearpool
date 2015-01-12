package org.opensource.clearpool.util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;

import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.jdbc2.optional.JDBC4SuspendableXAConnection;
import com.mysql.jdbc.jdbc2.optional.MysqlXAConnection;
import com.mysql.jdbc.jdbc2.optional.SuspendableXAConnection;

public class MysqlUtil {
  public static XAConnection mysqlXAConnection(Connection con) throws SQLException {
    ConnectionImpl mysqlConn = (ConnectionImpl) con;
    if (mysqlConn.getPinGlobalTxToPhysicalConnection()) {
      if (!Util.isJdbc4()) {
        return new SuspendableXAConnection(mysqlConn);
      }
      return new JDBC4SuspendableXAConnection(mysqlConn);
    }
    return new MysqlXAConnection(mysqlConn, false);
  }
}
