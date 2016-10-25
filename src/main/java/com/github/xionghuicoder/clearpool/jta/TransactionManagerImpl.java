package com.github.xionghuicoder.clearpool.jta;

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

import com.github.xionghuicoder.clearpool.ConnectionPoolException;

public class TransactionManagerImpl implements TransactionManager {
  private static final TransactionManagerImpl MANAGER = new TransactionManagerImpl();

  private static final ThreadLocal<Transaction> TX_HOLDER = new ThreadLocal<Transaction>();

  private final static boolean SINGLETON_MARK;

  static {
    SINGLETON_MARK = true;
  }

  private Set<Transaction> suspendTx;

  private int transactionTimeout;

  private TransactionManagerImpl() {
    if (SINGLETON_MARK) {
      throw new ConnectionPoolException("create TransactionManager illegal");
    }
  }

  public static TransactionManagerImpl getManager() {
    return MANAGER;
  }

  @Override
  public void begin() throws NotSupportedException, SystemException {
    Transaction action = TX_HOLDER.get();
    if (action != null) {
      throw new NotSupportedException("nested transaction is not supported");
    }
    action = new TransactionImpl(this.transactionTimeout);
    TX_HOLDER.set(action);
  }

  @Override
  public void commit() throws RollbackException, HeuristicMixedException,
      HeuristicRollbackException, SecurityException, SystemException {
    Transaction action = TX_HOLDER.get();
    if (action == null) {
      throw new IllegalStateException("this is no transaction held");
    }
    try {
      action.commit();
    } finally {
      TX_HOLDER.set(null);
    }
  }

  @Override
  public int getStatus() throws SystemException {
    Transaction action = TX_HOLDER.get();
    if (action == null) {
      return Status.STATUS_NO_TRANSACTION;
    }
    return action.getStatus();
  }

  @Override
  public Transaction getTransaction() throws SystemException {
    Transaction tx = TX_HOLDER.get();
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
    Transaction current = TX_HOLDER.get();
    if (current != null) {
      throw new IllegalStateException("the thread already has a transaction");
    }
    ((TransactionImpl) tx).resume();
    TX_HOLDER.set(tx);
  }

  @Override
  public void rollback() throws SecurityException, SystemException {
    Transaction action = TX_HOLDER.get();
    if (action == null) {
      throw new ConnectionPoolException("this is no transaction holding");
    }
    try {
      action.rollback();
    } finally {
      TX_HOLDER.set(null);
    }
  }

  @Override
  public void setRollbackOnly() throws SystemException {
    Transaction action = TX_HOLDER.get();
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
    Transaction action = TX_HOLDER.get();
    if (action != null && action instanceof TransactionImpl) {
      ((TransactionImpl) action).setTransactionTimeout(i);
    }
  }

  @Override
  public Transaction suspend() throws SystemException {
    Transaction tx = TX_HOLDER.get();
    if (tx != null) {
      if (this.suspendTx == null) {
        this.suspendTx = new HashSet<Transaction>();
      }
      ((TransactionImpl) tx).suspend();
      TransactionAdapter adapter = new TransactionAdapter(tx);
      this.suspendTx.add(adapter);
      TX_HOLDER.set(null);
      return adapter;
    }
    return null;
  }
}
