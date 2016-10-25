package com.github.xionghuicoder.clearpool.jta;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * transaction适配器
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class TransactionAdapter implements Transaction {
  private Transaction tx;

  TransactionAdapter(Transaction tx) {
    this.tx = tx;
  }

  Transaction getTx() {
    return this.tx;
  }

  @Override
  public void commit() throws RollbackException, HeuristicMixedException,
      HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
    throw new UnsupportedOperationException("please invoke UserTransaction instead");
  }

  @Override
  public boolean delistResource(XAResource arg0, int arg1)
      throws IllegalStateException, SystemException {
    return this.tx.delistResource(arg0, arg1);
  }

  @Override
  public boolean enlistResource(XAResource arg0)
      throws RollbackException, IllegalStateException, SystemException {
    return this.tx.enlistResource(arg0);
  }

  @Override
  public int getStatus() throws SystemException {
    return this.tx.getStatus();
  }

  @Override
  public void registerSynchronization(Synchronization arg0)
      throws RollbackException, IllegalStateException, SystemException {
    this.tx.registerSynchronization(arg0);
  }

  @Override
  public void rollback() throws IllegalStateException, SystemException {
    throw new UnsupportedOperationException("please invoke UserTransaction instead");
  }

  @Override
  public void setRollbackOnly() throws IllegalStateException, SystemException {
    throw new UnsupportedOperationException("please invoke UserTransaction instead");
  }
}
