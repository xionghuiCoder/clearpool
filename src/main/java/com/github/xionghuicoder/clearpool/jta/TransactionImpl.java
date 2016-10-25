package com.github.xionghuicoder.clearpool.jta;

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

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.jta.xa.XidImpl;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;

/**
 * jta事务处理
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
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
    if (this.status == Status.STATUS_ROLLEDBACK) {
      throw new RollbackException("the transaction had been rolled back");
    }
    if (this.rollbackOnly) {
      this.rollback();
      return;
    }
    if (this.status != Status.STATUS_ACTIVE) {
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

  private void tryEndResource() {
    for (ResourceCarry carry : this.resList) {
      try {
        carry.xaRes.end(carry.xid, XAResource.TMSUCCESS);
      } catch (XAException e) {
        LOGGER.error("end XA error(code=" + e.errorCode + "): ", e);
      }
    }
  }

  private boolean tryPrepared() {
    this.status = Status.STATUS_PREPARING;
    boolean preparedSuccess = true;
    for (ResourceCarry carry : this.resList) {
      try {
        int result = carry.xaRes.prepare(carry.xid);
        preparedSuccess &= result == XAResource.XA_OK;
      } catch (XAException e) {
        LOGGER.error("prepare XA error(code=" + e.errorCode + "): ", e);
        preparedSuccess = false;
      }
    }
    this.status = Status.STATUS_PREPARED;
    return preparedSuccess;
  }

  private void tryCommit() throws HeuristicMixedException, SystemException {
    this.status = Status.STATUS_COMMITTING;
    ResourceCarry carryTemp = null;
    Iterator<ResourceCarry> itr = this.resList.iterator();
    while (itr.hasNext()) {
      ResourceCarry carry = itr.next();
      this.beforeCompletion();
      try {
        carry.xaRes.commit(carry.xid, false);
      } catch (XAException e) {
        LOGGER.error("commit XA error(code=" + e.errorCode + "): ", e);
        carryTemp = carry;
        break;
      }
      this.afterCompletion(Status.STATUS_COMMITTED);
    }
    if (carryTemp == null) {
      this.status = Status.STATUS_COMMITTED;
      return;
    }
    this.rollbackMixed(carryTemp, itr);
  }

  private void rollbackMixed(ResourceCarry carryTemp, Iterator<ResourceCarry> itr)
      throws HeuristicMixedException, SystemException {
    int st = Status.STATUS_ROLLEDBACK;
    this.status = Status.STATUS_ROLLEDBACK;
    this.beforeCompletion();
    try {
      carryTemp.xaRes.rollback(carryTemp.xid);
    } catch (XAException e) {
      LOGGER.error("rollback XA error(code=" + e.errorCode + "): ", e);
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
        LOGGER.error("rollback XA error(code=" + e.errorCode + "): ", e);
        st = Status.STATUS_UNKNOWN;
      }
      this.afterCompletion(st);
    }
    throw new HeuristicMixedException("roll back some transaction");
  }

  private void beforeCompletion() {
    if (this.synList == null) {
      return;
    }
    for (Synchronization syn : this.synList) {
      syn.beforeCompletion();
    }
  }

  private void afterCompletion(int st) {
    if (this.synList == null) {
      return;
    }
    for (Synchronization syn : this.synList) {
      syn.afterCompletion(st);
    }
  }

  private void tryRollback() throws SystemException {
    this.status = Status.STATUS_ROLLING_BACK;
    StringBuilder error = new StringBuilder();
    for (ResourceCarry carry : this.resList) {
      try {
        carry.xaRes.rollback(carry.xid);
      } catch (XAException e) {
        LOGGER.error("rollback error: ", e);
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
    this.status = Status.STATUS_ROLLEDBACK;
    if (error.length() > 0) {
      error.deleteCharAt(error.length() - 1);
      throw new SystemException(error.toString());
    }
  }

  @Override
  public boolean delistResource(XAResource xaresource, int i) throws SystemException {
    if (this.status != Status.STATUS_ACTIVE) {
      throw new IllegalStateException("the transaction is not active");
    }
    TransactionAdapter txAdapt =
        (TransactionAdapter) TransactionManagerImpl.getManager().getTransaction();
    if (txAdapt.getTx() != this) {
      throw new IllegalStateException("the transaction is not held");
    }
    Xid xid = this.xaResMap.get(xaresource);
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
    if (this.status != Status.STATUS_ACTIVE) {
      throw new IllegalStateException("the transaction is not active");
    }
    if (this.rollbackOnly) {
      throw new RollbackException("the transaction is signed to roll back only");
    }
    TransactionAdapter txAdapt =
        (TransactionAdapter) TransactionManagerImpl.getManager().getTransaction();
    if (txAdapt.getTx() != this) {
      throw new IllegalStateException("the transaction is not held");
    }
    try {
      if (this.seconds != 0) {
        boolean result = xaresource.setTransactionTimeout(this.seconds);
        // result is false when XAResource does not support setTransactionTimeout
        if (!result) {
          throw new ConnectionPoolException("XAResource setTransactionTimeout false.");
        }
      }
      if (!this.xaResMap.containsKey(xaresource)) {
        ResourceCarry carry = new ResourceCarry(xaresource);
        xaresource.start(carry.xid, XAResource.TMNOFLAGS);
        this.resList.add(carry);
        this.xaResMap.put(xaresource, carry.xid);
      }
    } catch (XAException e) {
      LOGGER.error("start XA error(code=" + e.errorCode + "): ", e);
      return false;
    }
    return true;
  }

  @Override
  public int getStatus() throws SystemException {
    return this.status;
  }

  @Override
  public void registerSynchronization(Synchronization synchronization)
      throws RollbackException, SystemException {
    if (this.status != Status.STATUS_ACTIVE) {
      throw new IllegalStateException("the transaction is not active");
    }
    if (this.rollbackOnly) {
      throw new RollbackException("the transaction is signed to roll back only");
    }
    TransactionAdapter txAdapt =
        (TransactionAdapter) TransactionManagerImpl.getManager().getTransaction();
    if (txAdapt.getTx() != this) {
      throw new IllegalStateException("the transaction is not held");
    }
    if (this.synList == null) {
      this.synList = new ArrayList<Synchronization>();
    }
    this.synList.add(synchronization);
  }

  @Override
  public void rollback() throws SystemException {
    if (this.status != Status.STATUS_ACTIVE && this.status != Status.STATUS_MARKED_ROLLBACK) {
      throw new IllegalStateException("the transaction is not active");
    }
    this.tryEndResource();
    this.tryRollback();
  }

  @Override
  public void setRollbackOnly() throws SystemException {
    this.rollbackOnly = true;
    this.status = Status.STATUS_MARKED_ROLLBACK;
  }

  void suspend() {
    for (ResourceCarry carry : this.resList) {
      try {
        carry.xaRes.end(carry.xid, XAResource.TMSUSPEND);
      } catch (XAException e) {
        LOGGER.error("end XA error(code=" + e.errorCode + "): ", e);
      }
    }
  }

  void resume() {
    for (ResourceCarry carry : this.resList) {
      try {
        carry.xaRes.start(carry.xid, XAResource.TMRESUME);
      } catch (XAException e) {
        LOGGER.error("start XA error(code=" + e.errorCode + "): ", e);
      }
    }
  }

  /**
   * resource和xid的bean
   *
   * @author xionghui
   * @version 1.0.0
   * @since 1.0.0
   */
  private static class ResourceCarry {
    Xid xid;
    XAResource xaRes;

    ResourceCarry(XAResource xaRes) {
      this.xid = new XidImpl();
      this.xaRes = xaRes;
    }
  }
}
