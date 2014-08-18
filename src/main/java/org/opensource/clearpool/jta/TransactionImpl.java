package org.opensource.clearpool.jta;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.opensource.clearpool.jta.xa.XidImpl;

public class TransactionImpl implements Transaction {
	private Xid xid = new XidImpl();

	private Set<XAResource> xaResSet = new HashSet<>();

	TransactionImpl() {
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException,
			IllegalStateException, SystemException {
		if (this.xaResSet.size() == 1) {
			for (XAResource xaRes : this.xaResSet) {
				try {
					xaRes.end(this.xid, XAResource.TMSUCCESS);
					xaRes.prepare(this.xid);
					xaRes.commit(this.xid, false);
				} catch (XAException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean delistResource(XAResource xaresource, int i)
			throws IllegalStateException, SystemException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean enlistResource(XAResource xaresource)
			throws RollbackException, IllegalStateException, SystemException {
		if (!this.xaResSet.contains(xaresource)) {
			try {
				for (XAResource xaRes : this.xaResSet) {
					if (xaRes.isSameRM(xaresource)) {
						xaresource.start(this.xid, XAResource.TMJOIN);
						return true;
					}
				}
				xaresource.start(this.xid, XAResource.TMNOFLAGS);
			} catch (XAException e) {
				return false;
			}
			this.xaResSet.add(xaresource);
		}
		return true;
	}

	@Override
	public int getStatus() throws SystemException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void registerSynchronization(Synchronization synchronization)
			throws RollbackException, IllegalStateException, SystemException {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollback() throws IllegalStateException, SystemException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		// TODO Auto-generated method stub

	}
}
