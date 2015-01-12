package org.opensource.clearpool.jta;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

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
   * we should know that ThreadLocal is a WeakReference,so we should always set ThreadLocal in a
   * field to in case it be collected.
   */
  private static ThreadLocal<Transaction> txHolder = new ThreadLocal<Transaction>();

  private Set<Transaction> suspendTx;

  private int transactionTimeout;

  private TransactionManagerImpl() {
    // whenever we invoke the constructor by reflection,we throw a
    // exception.
    if (SINGLETON_MARK) {
      throw new TransactionException("create TransactionManager illegal");
    }
  }

  public static TransactionManagerImpl getManager() {
    return MANAGER;
  }

  @Override
  public void begin() throws NotSupportedException, SystemException {
    Transaction action = txHolder.get();
    if (action != null) {
      throw new NotSupportedException("nested transaction is not supported");
    }
    action = new TransactionImpl(transactionTimeout);
    txHolder.set(action);
  }

  @Override
  public void commit() throws RollbackException, HeuristicMixedException,
      HeuristicRollbackException, SecurityException, SystemException {
    Transaction action = txHolder.get();
    if (action == null) {
      throw new IllegalStateException("this is no transaction held");
    }
    try {
      action.commit();
    } finally {
      txHolder.set(null);
    }
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
    Transaction tx = txHolder.get();
    if (tx != null) {
      return new TransactionAdapter(tx);
    }
    return null;
  }

  @Override
  public void resume(Transaction transaction) throws InvalidTransactionException, SystemException {
    if (!(transaction instanceof TransactionAdapter) || this.suspendTx == null
        || !this.suspendTx.remove(transaction)) {
      throw new InvalidTransactionException("the transaction is invalid");
    }
    Transaction tx = ((TransactionAdapter) transaction).getTx();
    Transaction current = txHolder.get();
    if (current != null) {
      throw new IllegalStateException("the thread already has a transaction");
    }
    ((TransactionImpl) tx).resume();
    txHolder.set(tx);
  }

  @Override
  public void rollback() throws SecurityException, SystemException {
    Transaction action = txHolder.get();
    if (action == null) {
      throw new TransactionException("this is no transaction holding");
    }
    try {
      action.rollback();
    } finally {
      txHolder.set(null);
    }
  }

  @Override
  public void setRollbackOnly() throws SystemException {
    Transaction action = txHolder.get();
    if (action == null) {
      throw new IllegalStateException("this is no transaction started");
    }
    action.setRollbackOnly();
  }

  @Override
  public void setTransactionTimeout(int i) throws SystemException {
    if (i < 0) {
      throw new SystemException("the parameter shouldn't be nagative");
    }
    this.transactionTimeout = i;
    Transaction action = txHolder.get();
    if (action != null && action instanceof TransactionImpl) {
      ((TransactionImpl) action).setTransactionTimeout(i);
    }
  }

  @Override
  public Transaction suspend() throws SystemException {
    Transaction tx = txHolder.get();
    if (tx != null) {
      if (this.suspendTx == null) {
        this.suspendTx = new HashSet<Transaction>();
      }
      ((TransactionImpl) tx).suspend();
      TransactionAdapter adapter = new TransactionAdapter(tx);
      this.suspendTx.add(adapter);
      txHolder.set(null);
      return adapter;
    }
    return null;
  }
}
