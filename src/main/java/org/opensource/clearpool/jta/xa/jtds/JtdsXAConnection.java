package org.opensource.clearpool.jta.xa.jtds;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import net.sourceforge.jtds.jdbc.XASupport;

import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

public class JtdsXAConnection implements XAConnection {
  private static final PoolLog LOG = PoolLogFactory.getLog(JtdsXAConnection.class);

  private Connection connection;

  private final XAResource resource;
  private final int xaConnectionId;

  public JtdsXAConnection(Connection connection) throws SQLException {
    this.resource = new JtdsXAResource(this, connection);
    this.connection = connection;
    this.xaConnectionId = XASupport.xa_open(connection);
  }

  int getXAConnectionID() {
    return this.xaConnectionId;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return this.connection;
  }

  @Override
  public void close() throws SQLException {
    try {
      XASupport.xa_close(this.connection, this.xaConnectionId);
    } catch (SQLException e) {
      // Swallow it
    }
    if (this.connection != null) {
      try {
        this.connection.close();
      } catch (Exception e) {
        LOG.error("close connection error", e);
      }
    }
  }

  @Override
  public void addConnectionEventListener(ConnectionEventListener listener) {

  }

  @Override
  public void removeConnectionEventListener(ConnectionEventListener listener) {

  }

  @Override
  public void addStatementEventListener(StatementEventListener listener) {

  }

  @Override
  public void removeStatementEventListener(StatementEventListener listener) {

  }

  @Override
  public XAResource getXAResource() throws SQLException {
    return this.resource;
  }
}
