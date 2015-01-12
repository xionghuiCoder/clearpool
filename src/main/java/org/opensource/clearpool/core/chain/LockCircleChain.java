package org.opensource.clearpool.core.chain;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a circle chain.We can add or remove a element by lock.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class LockCircleChain<E> extends CommonChain<E> {
  private Lock lock = new ReentrantLock();

  private Node<E> head = new CircleNode<E>(null);

  {
    this.head.next = this.head;
  }

  /**
   * Add a element to {@link #head} atomically.
   */
  @Override
  public void add(E e) {
    Node<E> newNode = new CircleNode<E>(e);
    this.lock.lock();
    try {
      newNode.next = this.head.next;
      this.head.setNext(newNode);
    } finally {
      this.lock.unlock();
    }
  }

  /**
   * We should use {@link Iterator#remove()} instead of this method.
   */
  @Override
  public E remove() {
    throw new UnsupportedOperationException("not supported yet");
  }

  @Override
  public Iterator<E> iterator() {
    return new ChainIterator();
  }

  /**
   * This class used to get element and set next.
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
    void setNext(Node<E> next) {
      this.next = next;
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
    private volatile Node<E> pointer;
    private volatile Node<E> previous;

    ChainIterator() {
      this.pointer = this.previous = LockCircleChain.this.head;
    }

    @Override
    public boolean hasNext() {
      // this is a circle chain,it always has next value.
      return true;
    }

    @Override
    public E next() {
      this.previous = this.pointer;
      this.pointer = this.pointer.next;
      return this.pointer.element;
    }

    /**
     * Remove a element from {@link #pointer}.
     */
    @Override
    public void remove() {
      if (this.pointer != LockCircleChain.this.head) {
        /**
         * when previous is equals with head,we need to lock it,because we may set next of head when
         * we add element.
         */
        if (this.previous == LockCircleChain.this.head) {
          LockCircleChain.this.lock.lock();
          try {
            this.previous.next = this.pointer.next;
          } finally {
            LockCircleChain.this.lock.unlock();
          }
        } else {
          this.previous.next = this.pointer.next;
        }
      }
    }
  }
}
