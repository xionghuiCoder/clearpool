package org.opensource.clearpool.core.chain;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a normal, lock-control chain.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class LockSingleChain<E> extends CommonChain<E> {
	private Lock addLock = new ReentrantLock();
	private Lock removeLock = new ReentrantLock();

	private volatile Node<E> head = this.tail = new SingleNode<E>(null);
	private volatile Node<E> tail;

	private int size;

	@Override
	public void add(E e) {
		this.addLock.lock();
		try {
			this.tail.element = e;
			this.tail.entryTime = System.currentTimeMillis();
			this.tail.next = new SingleNode<E>(null);
			this.tail = this.tail.next;
		} finally {
			this.addLock.unlock();
		}
	}

	@Override
	public E remove() {
		this.removeLock.lock();
		try {
			if (this.head == this.tail) {
				return null;
			}
			Node<E> current = this.head;
			this.head = this.head.next;
			return current.element;
		} finally {
			this.removeLock.unlock();
		}
	}

	@Override
	public E removeIdle(long period) {
		if (this.head == this.tail) {
			return null;
		}
		for (;;) {
			Node<E> current = this.head;
			long now = System.currentTimeMillis();
			if (now - current.entryTime < period) {
				return null;
			}
			if (this.removeLock.tryLock()) {
				try {
					if (this.head == this.tail) {
						return null;
					}
					current = this.head;
					this.head = this.head.next;
					return current.element;
				} finally {
					this.removeLock.unlock();
				}
			}
		}
	}

	@Override
	public int size() {
		return this.size;
	}

	/**
	 * This class works as a normal single chain.
	 * 
	 * @author xionghui
	 * @date 26.07.2014
	 * @version 1.0
	 */
	private final static class SingleNode<E> extends Node<E> {
		SingleNode(E element) {
			super(element);
		}

		@Override
		E getElement() {
			return this.element;
		}

		@Override
		void setNext(Node<E> next) {
			this.next = next;
		}
	}
}
