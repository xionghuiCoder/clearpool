package org.opensource.clearpool.core.chain;

import java.util.Iterator;

public abstract class CommonChain<E> implements Iterable<E> {
  protected int size;

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
  public E removeIdle(long period) {
    throw new UnsupportedOperationException("not supported by CommonChain");
  }

  public int size() {
    return this.size;
  }

  @Override
  public Iterator<E> iterator() {
    throw new UnsupportedOperationException("not supported by CommonChain");
  }

  /**
   * This is a one-way chain,it just has the next chain but don't has the previous chain.
   * 
   * @author xionghui
   * @date 26.07.2014
   * @version 1.0
   */
  static abstract class Node<E> {
    E element;

    volatile Node<E> next;

    /**
     * we should know that entryTime is used by {@link AtomicSingleChain} or {@link BinaryHeap}
     */
    long entryTime;

    Node(E element) {
      this.element = element;
    }

    abstract E getElement();

    abstract void setNext(Node<E> next);
  }
}
