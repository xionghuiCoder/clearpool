package com.github.xionghuicoder.clearpool.jta.xa.jtds;

import java.lang.reflect.Method;
import java.sql.Connection;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;

import net.sourceforge.jtds.jdbc.XASupport;

public class JtdsXAResource implements XAResource {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(JtdsXAResource.class);

  private final Connection connection;
  private final JtdsXAConnection xaConnection;
  private String rmHost;

  public JtdsXAResource(JtdsXAConnection xaConnection, Connection connection) {
    this.xaConnection = xaConnection;
    this.connection = connection;
    Method method = null;
    try {
      method = connection.getClass().getMethod("getRmHost");
    } catch (Exception e) {
      LOGGER.error("getRmHost method error: ", e);
    }
    if (method != null) {
      try {
        this.rmHost = (String) method.invoke(connection);
      } catch (Exception e) {
        LOGGER.error("getRmHost error: ", e);
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
      String anoRmHost = ((JtdsXAResource) xares).getRmHost();
      if (anoRmHost == null ? this.rmHost == null : anoRmHost.equals(this.rmHost)) {
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
