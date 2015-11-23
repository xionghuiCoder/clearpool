package org.opensource.clearpool.jta.xa;

import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.XAConnection;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.datasource.proxy.PoolConnectionImpl;
import org.opensource.clearpool.datasource.proxy.dynamic.ProxyFactory;
import org.opensource.clearpool.exception.TransactionException;
import org.opensource.clearpool.jta.TransactionManagerImpl;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

public class XAConnectionImpl extends PoolConnectionImpl implements XAConnection {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(XAConnectionImpl.class);

  private final XAConnection xaCon;

  public XAConnectionImpl(ConnectionProxy conProxy) {
    super(conProxy);
    xaCon = conProxy.getXaConnection();
  }

  @Override
  public XAResource getXAResource() throws SQLException {
    this.checkState();
    return xaCon.getXAResource();
  }

  @Override
  public Statement createProxyStatement(Statement statement, String sql) {
    Statement statementProxy = ProxyFactory.createProxyXAStatement(statement, this, conProxy, sql);
    return statementProxy;
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    if (this.isTsBeginning()) {
      return;
    }
    super.setAutoCommit(autoCommit);
  }

  @Override
  public void commit() throws SQLException {
    if (this.isTsBeginning()) {
      return;
    }
    super.commit();
  }

  /**
   * Check if we have started the transaction.
   */
  private boolean isTsBeginning() {
    Transaction ts = null;
    try {
      ts = TransactionManagerImpl.getManager().getTransaction();
    } catch (SystemException e) {
      LOGGER.error("getTransaction error: ", e);
      throw new TransactionException(e);
    }
    return ts != null;
  }
}
