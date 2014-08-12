package org.opensource.clearpool.core.chain;

import java.util.Iterator;

/**
 * This is a circle chain.We can add or remove a element atomically by its
 * object.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class AtomicCircleChain<E> extends CommonChain<E> {
	private Node<E> head = this.previous = this.pointer = new CircleNode<E>(
			null);
	private Node<E> previous;
	private Node<E> pointer;

	{
		this.head.next = this.head;
	}

	/**
	 * Add a element to {@link #head} atomically.
	 */
	@Override
	public void add(E e) {
		Node<E> newNode = new CircleNode<E>(e);
		this.head.setNext(newNode);
	}

	/**
	 * Remove a element from {@link #pointer} atomically.
	 */
	@Override
	public E remove() {
		Node<E> update = this.pointer.next;
		if (this.pointer != this.head
				&& this.previous.compareAndSetNext(this.pointer, update)) {
			return this.pointer.element;
		}
		return null;
	}

	@Override
	public E removeIdle(long period) {
		throw new UnsupportedOperationException(
				"we shouldn't use getIdle in AtomicCircleChain");
	}

	@Override
	public Iterator<E> iterator() {
		return new ChainIterator();
	}

	/**
	 * This class used to get element atomically.
	 * 
	 * @author xionghui
	 * @date 26.07.2014
	 * @version 1.0
	 */
	private final static class CircleNode<E> extends Node<E> {
		CircleNode(E element) {
			super(element);
		}

		@Override
		E getElement() {
			return this.element;
		}

		@Override
		void setNext(Node<E> update) {
			for (;;) {
				Node<E> current = this.next;
				update.next = current;
				if (this.compareAndSetNext(current, update)) {
					return;
				}
			}
		}
	}

	/**
	 * This class is used by for..in
	 * 
	 * @author xionghui
	 * @date 26.07.2014
	 * @version 1.0
	 */
	private class ChainIterator implements Iterator<E> {

		@Override
		public boolean hasNext() {
			// this is a circle chain,it always has next value.
			return true;
		}

		@Override
		public E next() {
			AtomicCircleChain.this.previous = AtomicCircleChain.this.pointer;
			AtomicCircleChain.this.pointer = AtomicCircleChain.this.pointer.next;
			return AtomicCircleChain.this.pointer.element;
		}

		@Override
		public void remove() {
			AtomicCircleChain.this.remove();
		}
	}
}
