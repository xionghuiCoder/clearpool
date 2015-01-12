package org.opensource.clearpool.util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;

import org.postgresql.core.BaseConnection;
import org.postgresql.xa.PGXAConnection;

public class PGUtil {
  public static XAConnection createXAConnection(Connection physicalConn) throws SQLException {
    return new PGXAConnection((BaseConnection) physicalConn);
  }
}
