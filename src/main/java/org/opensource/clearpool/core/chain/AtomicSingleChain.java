package org.opensource.clearpool.core.chain;

import org.opensource.clearpool.exception.ConnectionPoolException;

import sun.misc.Unsafe;

/**
 * This is a one-way chain,it is provide atomic operation like {@link #add(E)},
 * {@link #get} and getIdle(long).
 * 
 * Note:the class used {@link Unsafe} by reflection.We should know that
 * {@link Unsafe} is not encouraged to use.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class AtomicSingleChain<E> extends AtomicCommonChain<E> {
	private static final long HEAD_OFFSET;
	private static final long TAIL_OFFSET;

	static {
		try {
			HEAD_OFFSET = UNSAFE.objectFieldOffset(AtomicSingleChain.class
					.getDeclaredField("head"));
			TAIL_OFFSET = UNSAFE.objectFieldOffset(AtomicSingleChain.class
					.getDeclaredField("tail"));
		} catch (Exception e) {
			throw new ConnectionPoolException("use Unsafe error");
		}
	}

	private volatile Node<E> head = this.tail = new SingleNode<E>(null);
	private volatile Node<E> tail;

	/**
	 * Add a element to {@link #tail} atomically.
	 */
	@Override
	public void add(E e) {
		Node<E> update = new SingleNode<E>(e);
		for (;;) {
			Node<E> current = this.tail;
			if (this.compareAndSetTail(current, update)) {
				update.entryTime = System.currentTimeMillis();
				current.next = update;
				this.size.getAndIncrement();
				return;
			}
		}
	}

	/**
	 * Remove a element from {@link #head} atomically.
	 */
	@Override
	public E remove() {
		for (;;) {
			Node<E> current = this.head;
			Node<E> update = current.next;
			if (update == null && current == this.head) {
				E element = current.getElement();
				if (element != null) {
					this.size.getAndDecrement();
					return element;
				} else if (this.size.get() == 0) {
					// in case the pool grow so fast
					return null;
				}
			} else if (this.compareAndSetHead(current, update)) {
				// help gc
				current.next = null;
				E element = current.getElement();
				if (element != null) {
					this.size.getAndDecrement();
					return element;
				}
			}
		}
	}

	/**
	 * Remove a element which is overtime from {@link #head} atomically.
	 */
	@Override
	public E removeIdle(long period) {
		for (;;) {
			Node<E> current = this.head;
			long now = System.currentTimeMillis();
			if (now - current.entryTime < period) {
				return null;
			}
			Node<E> update = current.next;
			if (update == null && current == this.head) {
				E element = current.getElement();
				if (element != null) {
					this.size.getAndDecrement();
				}
				return element;
			} else if (this.compareAndSetHead(current, update)) {
				// help gc
				E element = current.getElement();
				if (element != null) {
					this.size.getAndDecrement();
					return element;
				}
			}
		}
	}

	/**
	 * Atomically sets the Node to the given updated Node if the current Node is
	 * the expected Node.
	 * 
	 * @param expect
	 *            the expected Node
	 * @param update
	 *            the new Node
	 * @return true if successful. False return indicates that the actual Node
	 *         was not equal to the expected Node.
	 */
	private boolean compareAndSetHead(Node<E> expect, Node<E> update) {
		return UNSAFE.compareAndSwapObject(this, HEAD_OFFSET, expect, update);
	}

	/**
	 * @see {@link #compareAndSetHead(Node,Node)}
	 */
	private boolean compareAndSetTail(Node<E> expect, Node<E> update) {
		return UNSAFE.compareAndSwapObject(this, TAIL_OFFSET, expect, update);
	}

	/**
	 * This class used to get element atomically.
	 * 
	 * @author xionghui
	 * @date 26.07.2014
	 * @version 1.0
	 */
	private final static class SingleNode<E> extends Node<E> {
		private static final long ELEMENT_OFFSET;

		static {
			try {
				ELEMENT_OFFSET = UNSAFE.objectFieldOffset(Node.class
						.getDeclaredField("element"));
			} catch (Exception e) {
				throw new ConnectionPoolException("get element position error");
			}
		}

		SingleNode(E element) {
			super(element);
		}

		@Override
		E getElement() {
			for (;;) {
				E current = this.element;
				if (this.compareAndSetElement(current, null)) {
					return current;
				}
			}
		}

		@Override
		void setNext(Node<E> next) {
			this.next = next;
		}

		/**
		 * @see {@link #compareAndSetHead(Node,Node)}
		 */
		boolean compareAndSetElement(E expect, E update) {
			return UNSAFE.compareAndSwapObject(this, ELEMENT_OFFSET, expect,
					update);
		}
	}
}
