package org.opensource.clearpool.jta;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * This is a interface for user,and every method invoke will pass to
 * TransactionManagerImpl which is a singleton.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class UserTransactionImpl implements UserTransaction {

	@Override
	public void begin() throws NotSupportedException, SystemException {
		TransactionManagerImpl.getManager().begin();
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException,
			IllegalStateException, SystemException {
		TransactionManagerImpl.getManager().commit();
	}

	@Override
	public int getStatus() throws SystemException {
		return TransactionManagerImpl.getManager().getStatus();
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException,
			SystemException {
		TransactionManagerImpl.getManager().rollback();
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		TransactionManagerImpl.getManager().setRollbackOnly();
	}

	@Override
	public void setTransactionTimeout(int i) throws SystemException {
		TransactionManagerImpl.getManager().setTransactionTimeout(i);
	}
}
