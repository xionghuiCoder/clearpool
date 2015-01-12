package org.opensource.clearpool.jta;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * This is a adapter of transaction.It hide some methods which user shouldn't invoke directly.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
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
    throw new UnsupportedOperationException("you should invoke UserTransaction instead of this");
  }

  @Override
  public boolean delistResource(XAResource arg0, int arg1) throws IllegalStateException,
      SystemException {
    return this.tx.delistResource(arg0, arg1);
  }

  @Override
  public boolean enlistResource(XAResource arg0) throws RollbackException, IllegalStateException,
      SystemException {
    return this.tx.enlistResource(arg0);
  }

  @Override
  public int getStatus() throws SystemException {
    return this.tx.getStatus();
  }

  @Override
  public void registerSynchronization(Synchronization arg0) throws RollbackException,
      IllegalStateException, SystemException {
    this.tx.registerSynchronization(arg0);
  }

  @Override
  public void rollback() throws IllegalStateException, SystemException {
    throw new UnsupportedOperationException("you should invoke UserTransaction instead of this");
  }

  @Override
  public void setRollbackOnly() throws IllegalStateException, SystemException {
    throw new UnsupportedOperationException("you should invoke UserTransaction instead of this");
  }
}
