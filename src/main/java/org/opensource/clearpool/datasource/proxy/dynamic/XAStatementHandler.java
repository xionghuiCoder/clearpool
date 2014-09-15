package org.opensource.clearpool.datasource.proxy.dynamic;

import java.sql.Statement;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.opensource.clearpool.exception.TransactionException;
import org.opensource.clearpool.jta.TransactionManagerImpl;
import org.opensource.clearpool.jta.xa.XAConnectionImpl;

class XAStatementHandler extends StatementHandler {
	protected static final String EXECUTE_UPDATE_METHOD = "executeUpdate";

	private XAConnectionImpl xaCon;

	XAStatementHandler(Statement statement, XAConnectionImpl xaCon, String sql) {
		super(statement, xaCon, sql);
		this.xaCon = xaCon;
	}

	/**
	 * @see StatementHandler#beforeInvoke
	 */
	@Override
	protected void beforeInvoke(String methodName) throws XAException,
			SystemException {
		if (this.needTransaction(methodName)) {
			Transaction ts = TransactionManagerImpl.getManager()
					.getTransaction();
			if (ts != null) {
				try {
					XAResource xaRes = this.xaCon.getXAResource();
					ts.enlistResource(xaRes);
				} catch (Exception e) {
					throw new TransactionException(e);
				}
			}
		}
	}

	private boolean needTransaction(String methodName) {
		return EXECUTE.equals(methodName)
				|| EXECUTE_BATCH_METHOD.equals(methodName)
				|| EXECUTE_UPDATE_METHOD.equals(methodName);
	}
}
