package com.github.xionghuicoder.clearpool.core.chain;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 带环链表
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class LockCircleChain<E> extends CommonChain<E> {
  private Lock lock = new ReentrantLock();

  private Node<E> head = new CircleNode<E>(null);

  {
    this.head.next = this.head;
  }

  /**
   * 增加到{@link #head head}.
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
   * 使用{@link Iterator#remove Iterator.remove}替代该方法.
   *
   * @see ChainIterator#remove()
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
   * 带环{@link Node Node}
   *
   * @author xionghui
   * @version 1.0.0
   * @since 1.0.0
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
   * 迭代器实现，一般在<tt>for..in</tt>里面使用。
   *
   * @author xionghui
   * @version 1.0.0
   * @since 1.0.0
   * @see Iterator
   */
  private class ChainIterator implements Iterator<E> {
    private volatile Node<E> pointer;
    private volatile Node<E> previous;

    ChainIterator() {
      this.pointer = this.previous = LockCircleChain.this.head;
    }

    @Override
    public boolean hasNext() {
      // 带环链表，总会返回true
      return true;
    }

    @Override
    public E next() {
      this.previous = this.pointer;
      this.pointer = this.pointer.next;
      return this.pointer.element;
    }

    /**
     * 从{@link #pointer pointer}移出
     */
    @Override
    public void remove() {
      if (this.pointer == LockCircleChain.this.head) {
        return;
      }
      /**
       * 当<tt>previous</tt>是<tt>head</tt>时，需要加锁，因为可能并发在<tt>head</tt>后面加元素。
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
