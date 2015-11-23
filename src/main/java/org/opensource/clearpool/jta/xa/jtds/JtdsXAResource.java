package org.opensource.clearpool.jta.xa.jtds;

import java.lang.reflect.Method;
import java.sql.Connection;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

import net.sourceforge.jtds.jdbc.XASupport;

public class JtdsXAResource implements XAResource {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(JtdsXAResource.class);

  private final Connection connection;
  private final JtdsXAConnection xaConnection;
  private String rmHost;

  private static Method method;

  public JtdsXAResource(JtdsXAConnection xaConnection, Connection connection) {
    this.xaConnection = xaConnection;
    this.connection = connection;
    if (method == null) {
      try {
        method = connection.getClass().getMethod("getRmHost");
      } catch (Exception e) {
        LOGGER.error("getRmHost method error: ", e);
      }
    } else {
      try {
        rmHost = (String) method.invoke(connection);
      } catch (Exception e) {
        LOGGER.error("invoke " + method + " error: ", e);
      }
    }
  }

  protected JtdsXAConnection getResourceManager() {
    return xaConnection;
  }

  protected String getRmHost() {
    return rmHost;
  }

  @Override
  public void commit(Xid xid, boolean commit) throws XAException {
    XASupport.xa_commit(connection, xaConnection.getXAConnectionID(), xid, commit);
  }

  @Override
  public void end(Xid xid, int flags) throws XAException {
    XASupport.xa_end(connection, xaConnection.getXAConnectionID(), xid, flags);
  }

  @Override
  public void forget(Xid xid) throws XAException {
    XASupport.xa_forget(connection, xaConnection.getXAConnectionID(), xid);
  }

  @Override
  public int getTransactionTimeout() throws XAException {
    return 0;
  }

  @Override
  public boolean isSameRM(XAResource xares) throws XAException {
    if (xares instanceof JtdsXAResource) {
      if (((JtdsXAResource) xares).getRmHost().equals(rmHost)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int prepare(Xid xid) throws XAException {
    return XASupport.xa_prepare(connection, xaConnection.getXAConnectionID(), xid);
  }

  @Override
  public Xid[] recover(int flags) throws XAException {
    return XASupport.xa_recover(connection, xaConnection.getXAConnectionID(), flags);
  }

  @Override
  public void rollback(Xid xid) throws XAException {
    XASupport.xa_rollback(connection, xaConnection.getXAConnectionID(), xid);
  }

  @Override
  public boolean setTransactionTimeout(int seconds) throws XAException {
    return false;
  }

  @Override
  public void start(Xid xid, int flags) throws XAException {
    XASupport.xa_start(connection, xaConnection.getXAConnectionID(), xid, flags);
  }
}
