package org.opensource.clearpool.jta.xa.jtds;

import java.lang.reflect.Method;
import java.sql.Connection;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import net.sourceforge.jtds.jdbc.XASupport;

import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

public class JtdsXAResource implements XAResource {
  private static final PoolLog LOG = PoolLogFactory.getLog(JtdsXAResource.class);

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
        LOG.error("getRmHost method error", e);
      }
    }

    if (method != null) {
      try {
        this.rmHost = (String) method.invoke(connection);
      } catch (Exception e) {
        LOG.error("getRmHost error", e);
      }
    }
  }

  protected JtdsXAConnection getResourceManager() {
    return this.xaConnection;
  }

  protected String getRmHost() {
    return this.rmHost;
  }

  @Override
  public void commit(Xid xid, boolean commit) throws XAException {
    XASupport.xa_commit(this.connection, this.xaConnection.getXAConnectionID(), xid, commit);
  }

  @Override
  public void end(Xid xid, int flags) throws XAException {
    XASupport.xa_end(this.connection, this.xaConnection.getXAConnectionID(), xid, flags);
  }

  @Override
  public void forget(Xid xid) throws XAException {
    XASupport.xa_forget(this.connection, this.xaConnection.getXAConnectionID(), xid);
  }

  @Override
  public int getTransactionTimeout() throws XAException {
    return 0;
  }

  @Override
  public boolean isSameRM(XAResource xares) throws XAException {
    if (xares instanceof JtdsXAResource) {
      if (((JtdsXAResource) xares).getRmHost().equals(this.rmHost)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int prepare(Xid xid) throws XAException {
    return XASupport.xa_prepare(this.connection, this.xaConnection.getXAConnectionID(), xid);
  }

  @Override
  public Xid[] recover(int flags) throws XAException {
    return XASupport.xa_recover(this.connection, this.xaConnection.getXAConnectionID(), flags);
  }

  @Override
  public void rollback(Xid xid) throws XAException {
    XASupport.xa_rollback(this.connection, this.xaConnection.getXAConnectionID(), xid);
  }

  @Override
  public boolean setTransactionTimeout(int seconds) throws XAException {
    return false;
  }

  @Override
  public void start(Xid xid, int flags) throws XAException {
    XASupport.xa_start(this.connection, this.xaConnection.getXAConnectionID(), xid, flags);
  }
}
