package com.github.xionghuicoder.clearpool.jta;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;

/**
 * 分布式事务处理类
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class UserTransactionImpl implements UserTransaction {

  @Override
  public void begin() throws NotSupportedException, SystemException {
    TransactionManagerImpl.getManager().begin();
  }

  @Override
  public void commit() throws RollbackException, HeuristicMixedException,
      HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
    TransactionManagerImpl.getManager().commit();
  }

  @Override
  public int getStatus() throws SystemException {
    return TransactionManagerImpl.getManager().getStatus();
  }

  @Override
  public void rollback() throws IllegalStateException, SecurityException, SystemException {
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

  public void resume(Transaction tobj) throws InvalidTransactionException, SystemException {
    TransactionManagerImpl.getManager().resume(tobj);
  }

  public Transaction suspend() throws SystemException {
    return TransactionManagerImpl.getManager().suspend();
  }
}
