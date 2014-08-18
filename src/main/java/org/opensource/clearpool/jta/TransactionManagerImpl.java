package org.opensource.clearpool.jta;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.TransactionException;

/**
 * This is a bridge of UserTransaction and Transaction.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class TransactionManagerImpl implements TransactionManager {

	// the INSTANCE should be the front of the SINGLETON_MARK
	private static final TransactionManagerImpl MANAGER = new TransactionManagerImpl();

	// the SINGLETON_MARK make sure the TransactionManagerImpl is a singleton
	private final static boolean SINGLETON_MARK;

	static {
		SINGLETON_MARK = true;
	}

	/**
	 * we should know that ThreadLocal is a WeakReference,so we should always
	 * set ThreadLocal in a field to in case it be collected.
	 */
	private static ThreadLocal<Transaction> txHolder = new ThreadLocal<Transaction>();

	private TransactionManagerImpl() {
		// whenever we invoke the constructor by reflection,we throw a
		// ConnectionPoolException.
		if (SINGLETON_MARK) {
			throw new ConnectionPoolException("create ClearPool illegal");
		}
	}

	public static TransactionManagerImpl getManager() {
		return MANAGER;
	}

	@Override
	public void begin() throws NotSupportedException, SystemException {
		Transaction action = txHolder.get();
		if (action != null) {
			throw new NotSupportedException(
					"nested transaction is not supported");
		}
		action = new TransactionImpl();
		txHolder.set(action);
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException,
			IllegalStateException, SystemException {
		Transaction action = txHolder.get();
		if (action == null) {
			return;
		}
		action.commit();
		txHolder.set(null);
	}

	@Override
	public int getStatus() throws SystemException {
		Transaction action = txHolder.get();
		if (action == null) {
			return Status.STATUS_NO_TRANSACTION;
		}
		return action.getStatus();
	}

	@Override
	public Transaction getTransaction() throws SystemException {
		Transaction action = txHolder.get();
		return action;
	}

	@Override
	public void resume(Transaction transaction)
			throws InvalidTransactionException, IllegalStateException,
			SystemException {

	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException,
			SystemException {
		Transaction action = txHolder.get();
		if (action == null) {
			throw new TransactionException("this is no tranaction beginning");
		}
		action.rollback();
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		Transaction action = txHolder.get();
		if (action == null) {
			throw new IllegalStateException("this is no tranaction started");
		}
		action.setRollbackOnly();
	}

	@Override
	public void setTransactionTimeout(int i) throws SystemException {
		if (i < 0) {
			throw new SystemException("the parameter shouldn't be negative");
		}
		Transaction action = txHolder.get();
		if (action != null) {
			;
		}
	}

	@Override
	public Transaction suspend() throws SystemException {
		Transaction action = txHolder.get();
		if (action != null) {
			;
		}
		return action;
	}
}
