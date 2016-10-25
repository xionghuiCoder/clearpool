package com.github.xionghuicoder.clearpool.datasource.proxy.dynamic;

import java.sql.Statement;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.jta.TransactionManagerImpl;
import com.github.xionghuicoder.clearpool.jta.xa.XAConnectionImpl;

class XAStatementHandler extends StatementHandler {
  private static final String EXECUTE_UPDATE_METHOD = "executeUpdate";

  private XAConnectionImpl xaCon;

  XAStatementHandler(Statement statement, XAConnectionImpl xaCon, ConnectionProxy conProxy,
      String sql) {
    super(statement, xaCon, conProxy, sql);
    this.xaCon = xaCon;
  }

  @Override
  protected void beforeInvoke(String methodName) throws XAException, SystemException {
    if (this.needTransaction(methodName)) {
      Transaction ts = TransactionManagerImpl.getManager().getTransaction();
      if (ts != null) {
        try {
          XAResource xaRes = this.xaCon.getXAResource();
          ts.enlistResource(xaRes);
        } catch (Exception e) {
          throw new ConnectionPoolException(e);
        }
      }
    }
  }

  private boolean needTransaction(String methodName) {
    return EXECUTE.equals(methodName) || EXECUTE_BATCH_METHOD.equals(methodName)
        || EXECUTE_UPDATE_METHOD.equals(methodName);
  }
}
