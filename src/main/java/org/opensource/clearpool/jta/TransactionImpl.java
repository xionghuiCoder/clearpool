package org.opensource.clearpool.jta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.opensource.clearpool.exception.TransactionException;
import org.opensource.clearpool.jta.xa.XidImpl;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

/**
 * This class used to control jta transaction.
 *
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
class TransactionImpl implements Transaction {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(TransactionImpl.class);

  private int status = Status.STATUS_ACTIVE;

  private boolean rollbackOnly;

  private int seconds;

  private List<Synchronization> synList;

  private Map<XAResource, Xid> xaResMap = new HashMap<XAResource, Xid>();

  private List<ResourceCarry> resList = new ArrayList<ResourceCarry>();

  TransactionImpl(int seconds) {
    this.seconds = seconds;
  }

  public void setTransactionTimeout(int seconds) {
    this.seconds = seconds;
  }

  @Override
  public void commit() throws RollbackException, HeuristicMixedException,
      HeuristicRollbackException, SecurityException, SystemException {
    if (status == Status.STATUS_ROLLEDBACK) {
      throw new RollbackException("the transaction had been rolled back");
    }
    if (rollbackOnly) {
      this.rollback();
      return;
    }
    if (status != Status.STATUS_ACTIVE) {
      throw new IllegalStateException("the transaction is not active");
    }
    this.tryEndResource();
    boolean preparedSuccess = this.tryPrepared();
    if (preparedSuccess) {
      this.tryCommit();
      return;
    }
    try {
      this.tryRollback();
    } catch (SystemException e) {
      throw new HeuristicRollbackException(e.getMessage());
    }
    throw new HeuristicRollbackException("roll back all the transaction");
  }

  /**
   * Try to end XA Resource
   */
  private void tryEndResource() {
    for (ResourceCarry carry : resList) {
      try {
        carry.xaRes.end(carry.xid, XAResource.TMSUCCESS);
      } catch (XAException e) {
        LOGGER.error("can't end XA: (error code = " + e.errorCode + "): ", e);
      }
    }
  }

  /**
   * Try to prepared
   */
  private boolean tryPrepared() {
    status = Status.STATUS_PREPARING;
    boolean preparedSuccess = true;
    for (ResourceCarry carry : resList) {
      try {
        int result = carry.xaRes.prepare(carry.xid);
        preparedSuccess &= result == XAResource.XA_OK;
      } catch (XAException e) {
        LOGGER.error("can't prepare XA: (error code = " + e.errorCode + "): ", e);
        preparedSuccess = false;
      }
    }
    status = Status.STATUS_PREPARED;
    return preparedSuccess;
  }

  /**
   * Try to commit
   */
  private void tryCommit() throws HeuristicMixedException, SystemException {
    status = Status.STATUS_COMMITTING;
    ResourceCarry carryTemp = null;
    Iterator<ResourceCarry> itr = resList.iterator();
    while (itr.hasNext()) {
      ResourceCarry carry = itr.next();
      this.beforeCompletion();
      try {
        carry.xaRes.commit(carry.xid, false);
      } catch (XAException e) {
        LOGGER.error("can't commit XA: (error code = " + e.errorCode + "): ", e);
        carryTemp = carry;
        break;
      }
      this.afterCompletion(Status.STATUS_COMMITTED);
    }
    if (carryTemp == null) {
      status = Status.STATUS_COMMITTED;
      return;
    }
    this.rollbackMixed(carryTemp, itr);
  }

  /**
   * roll back Mixed
   */
  private void rollbackMixed(ResourceCarry carryTemp, Iterator<ResourceCarry> itr)
      throws HeuristicMixedException, SystemException {
    int st = Status.STATUS_ROLLEDBACK;
    status = Status.STATUS_ROLLEDBACK;
    this.beforeCompletion();
    try {
      carryTemp.xaRes.rollback(carryTemp.xid);
    } catch (XAException e) {
      LOGGER.error("can't roll back XA: (error code = " + e.errorCode + "): ", e);
      st = Status.STATUS_UNKNOWN;
    }
    this.afterCompletion(st);
    while (itr.hasNext()) {
      ResourceCarry carry = itr.next();
      this.beforeCompletion();
      st = Status.STATUS_ROLLEDBACK;
      try {
        carry.xaRes.rollback(carry.xid);
      } catch (XAException e) {
        LOGGER.error("can't roll back XA: (error code = " + e.errorCode + "): ", e);
        st = Status.STATUS_UNKNOWN;
      }
      this.afterCompletion(st);
    }
    throw new HeuristicMixedException("roll back some transaction");
  }

  /**
   * Invoke before the second phase
   */
  private void beforeCompletion() {
    if (synList == null) {
      return;
    }
    for (Synchronization syn : synList) {
      syn.beforeCompletion();
    }
  }

  /**
   * Invoke after the second phase
   */
  private void afterCompletion(int st) {
    if (synList == null) {
      return;
    }
    for (Synchronization syn : synList) {
      syn.afterCompletion(st);
    }
  }

  /**
   * Try to roll back
   */
  private void tryRollback() throws SystemException {
    status = Status.STATUS_ROLLING_BACK;
    StringBuilder error = new StringBuilder();
    for (ResourceCarry carry : resList) {
      try {
        carry.xaRes.rollback(carry.xid);
      } catch (XAException e) {
        LOGGER.error("tryRollback error: ", e);
        try {
          carry.xaRes.forget(carry.xid);
        } catch (XAException ex) {
          error.append("can't roll back XAException: ");
          error.append(ex);
          error.append(" (error code = ");
          error.append(ex.errorCode);
          error.append(") ");
          error.append(ex.getMessage());
          error.append("\n");
        }
      }
    }
    status = Status.STATUS_ROLLEDBACK;
    if (error.length() > 0) {
      error.deleteCharAt(error.length() - 1);
      throw new SystemException(error.toString());
    }
  }

  @Override
  public boolean delistResource(XAResource xaresource, int i) throws SystemException {
    if (status != Status.STATUS_ACTIVE) {
      throw new IllegalStateException("the transaction is not active");
    }
    TransactionAdapter txAdapt =
        (TransactionAdapter) TransactionManagerImpl.getManager().getTransaction();
    if (txAdapt.getTx() != this) {
      throw new IllegalStateException("the transaction is not held");
    }
    Xid xid = xaResMap.get(xaresource);
    if (xid == null) {
      return false;
    }
    try {
      xaresource.end(xid, i);
    } catch (XAException e) {
      String error = "can't end XA: " + e + " (error code = " + e.errorCode + ") " + e.getMessage();
      throw new SystemException(error);
    }
    return true;
  }

  @Override
  public boolean enlistResource(XAResource xaresource) throws RollbackException, SystemException {
    if (status != Status.STATUS_ACTIVE) {
      throw new IllegalStateException("the transaction is not active");
    }
    if (rollbackOnly) {
      throw new RollbackException("the transaction is signed to roll back only");
    }
    TransactionAdapter txAdapt =
        (TransactionAdapter) TransactionManagerImpl.getManager().getTransaction();
    if (txAdapt.getTx() != this) {
      throw new IllegalStateException("the transaction is not held");
    }
    try {
      if (seconds != 0) {
        boolean result = xaresource.setTransactionTimeout(seconds);
        // result is false when XAResource does not support setTransactionTimeout
        if (!result) {
          throw new TransactionException("XAResource setTransactionTimeout false.");
        }
      }
      if (!xaResMap.containsKey(xaresource)) {
        ResourceCarry carry = new ResourceCarry(xaresource);
        xaresource.start(carry.xid, XAResource.TMNOFLAGS);
        resList.add(carry);
        xaResMap.put(xaresource, carry.xid);
      }
    } catch (XAException e) {
      LOGGER.error("can't start XA: (error code = " + e.errorCode + "): ", e);
      return false;
    }
    return true;
  }

  @Override
  public int getStatus() throws SystemException {
    return status;
  }

  @Override
  public void registerSynchronization(Synchronization synchronization)
      throws RollbackException, SystemException {
    if (status != Status.STATUS_ACTIVE) {
      throw new IllegalStateException("the transaction is not active");
    }
    if (rollbackOnly) {
      throw new RollbackException("the transaction is signed to roll back only");
    }
    TransactionAdapter txAdapt =
        (TransactionAdapter) TransactionManagerImpl.getManager().getTransaction();
    if (txAdapt.getTx() != this) {
      throw new IllegalStateException("the transaction is not held");
    }
    if (synList == null) {
      synList = new ArrayList<Synchronization>();
    }
    synList.add(synchronization);
  }

  @Override
  public void rollback() throws SystemException {
    if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
      throw new IllegalStateException("the transaction is not active");
    }
    this.tryEndResource();
    this.tryRollback();
  }

  @Override
  public void setRollbackOnly() throws SystemException {
    rollbackOnly = true;
    status = Status.STATUS_MARKED_ROLLBACK;
  }

  void suspend() {
    for (ResourceCarry carry : resList) {
      try {
        carry.xaRes.end(carry.xid, XAResource.TMSUSPEND);
      } catch (XAException e) {
        LOGGER.error("can't end XA: (error code = " + e.errorCode + "): ", e);
      }
    }
  }

  void resume() {
    for (ResourceCarry carry : resList) {
      try {
        carry.xaRes.start(carry.xid, XAResource.TMRESUME);
      } catch (XAException e) {
        LOGGER.error("can't end XA: (error code = " + e.errorCode + "): ", e);
      }
    }
  }

  /**
   * The resource and xid carrier.
   *
   * @author xionghui
   * @date 16.08.2014
   * @version 1.0
   */
  private static class ResourceCarry {
    Xid xid;
    XAResource xaRes;

    ResourceCarry(XAResource xaRes) {
      xid = new XidImpl();
      this.xaRes = xaRes;
    }
  }
}
