package com.github.xionghuicoder.clearpool.jta.xa;

import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.XAConnection;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.datasource.proxy.PoolConnectionImpl;
import com.github.xionghuicoder.clearpool.datasource.proxy.dynamic.ProxyFactory;
import com.github.xionghuicoder.clearpool.jta.TransactionManagerImpl;

public class XAConnectionImpl extends PoolConnectionImpl implements XAConnection {
  private final XAConnection xaCon;

  public XAConnectionImpl(ConnectionProxy conProxy) {
    super(conProxy);
    this.xaCon = conProxy.getXaConnection();
  }

  @Override
  public XAResource getXAResource() throws SQLException {
    this.checkState();
    return this.xaCon.getXAResource();
  }

  @Override
  public Statement createProxyStatement(Statement statement, String sql) {
    Statement statementProxy =
        ProxyFactory.createProxyXAStatement(statement, this, this.conProxy, sql);
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

  private boolean isTsBeginning() {
    Transaction ts;
    try {
      ts = TransactionManagerImpl.getManager().getTransaction();
    } catch (SystemException e) {
      throw new ConnectionPoolException(e);
    }
    return ts != null;
  }
}
