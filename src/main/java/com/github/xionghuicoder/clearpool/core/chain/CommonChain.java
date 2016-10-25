package com.github.xionghuicoder.clearpool.core.chain;

import java.util.Iterator;

public abstract class CommonChain<E> implements Iterable<E> {
  protected int size;

  public abstract void add(E e);

  public abstract E remove();

  public E removeIdle(long period) {
    throw new UnsupportedOperationException("not supported");
  }

  public int size() {
    return this.size;
  }

  @Override
  public Iterator<E> iterator() {
    throw new UnsupportedOperationException("not supported");
  }

  /**
   * 单项链表
   *
   * @author xionghui
   * @version 1.0.0
   * @since 1.0.0
   */
  static abstract class Node<E> {
    E element;

    volatile Node<E> next;

    long entryTime;

    Node(E element) {
      this.element = element;
    }

    abstract E getElement();

    abstract void setNext(Node<E> next);
  }
}
