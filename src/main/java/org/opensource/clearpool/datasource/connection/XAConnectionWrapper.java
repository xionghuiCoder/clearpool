package org.opensource.clearpool.datasource.connection;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;

import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

public class XAConnectionWrapper extends CommonConnection {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(XAConnectionWrapper.class);

  private XAConnection xaCon;

  public XAConnectionWrapper(XAConnection xaCon) {
    this.xaCon = xaCon;
  }

  @Override
  public Connection getConnection() {
    Connection con = null;
    try {
      con = xaCon.getConnection();
    } catch (SQLException e) {
      LOGGER.error("XAConnectionWrapper.getConnection error:", e);
      throw new ConnectionPoolException(e);
    }
    return con;
  }

  @Override
  public XAConnection getXAConnection() {
    return xaCon;
  }
}
