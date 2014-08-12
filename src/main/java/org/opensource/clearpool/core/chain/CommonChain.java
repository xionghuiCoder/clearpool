package org.opensource.clearpool.core.chain;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.opensource.clearpool.exception.ConnectionPoolException;

import sun.misc.Unsafe;

public abstract class CommonChain<E> implements Iterable<E> {
	protected static final Unsafe UNSAFE;

	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe) unsafeField.get(null);
		} catch (Exception e) {
			throw new ConnectionPoolException("create Unsafe error");
		}
	}

	protected AtomicInteger size = new AtomicInteger();

	/**
	 * Add a element.
	 */
	public abstract void add(E e);

	/**
	 * Remove a element.
	 */
	public abstract E remove();

	/**
	 * Remove a element which is overtime.
	 */
	public abstract E removeIdle(long period);

	/**
	 * Get the number of the connection in the pool.
	 */
	public int size() {
		return this.size.get();
	}

	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException(
				"we don't need to use iterator now.");
	}

	/**
	 * This is a one-way chain,it just has the next chain but don't has the
	 * previous chain.
	 * 
	 * @author xionghui
	 * @date 26.07.2014
	 * @version 1.0
	 */
	static abstract class Node<E> {
		private static final long NEXT_OFFSET;

		static {
			try {
				NEXT_OFFSET = UNSAFE.objectFieldOffset(Node.class
						.getDeclaredField("next"));
			} catch (Exception e) {
				throw new ConnectionPoolException("get next position error");
			}
		}

		E element;

		volatile Node<E> next;

		/**
		 * we should know that entryTime is used by {@link AtomicSingleChain}
		 */
		long entryTime;

		Node(E element) {
			this.element = element;
		}

		abstract E getElement();

		abstract void setNext(Node<E> next);

		/**
		 * Atomically sets the Node to the given updated Node if the current
		 * Node is the expected Node.
		 * 
		 * @param expect
		 *            the expected Node
		 * @param update
		 *            the new Node
		 * @return true if successful. False return indicates that the actual
		 *         Node was not equal to the expected Node.
		 */
		boolean compareAndSetNext(Node<E> expect, Node<E> update) {
			return UNSAFE.compareAndSwapObject(this, NEXT_OFFSET, expect,
					update);
		}
	}
}
