package com.github.xionghuicoder.clearpool.core.chain;

import java.util.Arrays;

import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;

/**
 * 使用二项堆（binary heap）来存储连接
 *
 * <p>
 * 使用LRU算法来获取数据库连接池，使得最常用的连接被用到的概率变大，以便提高性能
 * </p>
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 * @see java.util.Timer
 */
public class BinaryHeap {
  private static final int MAXIMUM_CAPACITY = 1 << 30;

  private static final int DEFAULT_INITIAL_CAPACITY = 16;

  private ProxyNode[] queue;

  private volatile int size;

  public BinaryHeap() {
    this(DEFAULT_INITIAL_CAPACITY);
  }

  public BinaryHeap(int initialCapacity) {
    if (initialCapacity <= 0) {
      throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    }
    initialCapacity = this.roundUpToPowerOf2(initialCapacity);
    this.queue = new ProxyNode[initialCapacity];
  }

  private int roundUpToPowerOf2(int number) {
    return number >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY
        : number > 1 ? Integer.highestOneBit(number - 1 << 1) : 1;
  }

  public void add(ConnectionProxy e) {
    if (this.size + 1 == this.queue.length) {
      this.queue = Arrays.copyOf(this.queue, 2 * this.queue.length);
    }
    this.queue[++this.size] = new ProxyNode(e, System.currentTimeMillis());
    this.fixUp(this.size);
  }

  /**
   * Establishes the heap invariant (described above) assuming the heap satisfies the invariant
   * except possibly for the leaf-node indexed by k (which may have a nextExecutionTime less than
   * its parent's).
   *
   * This method functions by "promoting" queue[k] up the hierarchy (by swapping it with its parent)
   * repeatedly until queue[k]'s nextExecutionTime is greater than or equal to that of its parent.
   */
  private void fixUp(int k) {
    while (k > 1) {
      int j = k >> 1;
      if (this.queue[j].element.compareTo(this.queue[k].element) > 0) {
        break;
      }
      ProxyNode tmp = this.queue[j];
      this.queue[j] = this.queue[k];
      this.queue[k] = tmp;
      k = j;
    }
  }

  public ConnectionProxy removeFirst() {
    return this.remove(1);
  }

  private ConnectionProxy remove(int i) {
    if (this.size == 0) {
      return null;
    }
    ProxyNode removeProxyNode = this.queue[i];
    this.queue[i] = this.queue[this.size];
    this.queue[this.size--] = null;
    this.fixDown(i);
    return removeProxyNode.element;
  }

  /**
   * Establishes the heap invariant (described above) in the subtree rooted at k, which is assumed
   * to satisfy the heap invariant except possibly for node k itself (which may have a
   * nextExecutionTime greater than its children's).
   *
   * This method functions by "demoting" queue[k] down the hierarchy (by swapping it with its
   * smaller child) repeatedly until queue[k]'s nextExecutionTime is less than or equal to those of
   * its children.
   */
  private void fixDown(int k) {
    int j;
    while ((j = k << 1) <= this.size && j > 0) {
      if (j < this.size && this.queue[j].element.compareTo(this.queue[j + 1].element) < 0) {
        j++;
      }
      if (this.queue[k].element.compareTo(this.queue[j].element) >= 0) {
        break;
      }
      ProxyNode tmp = this.queue[j];
      this.queue[j] = this.queue[k];
      this.queue[k] = tmp;
      k = j;
    }
  }

  /**
   * 移除超时<tt>period</tt>(ms)的元素
   *
   * @param period 空闲时间大于等于<tt>period</tt>(ms)的元素会被移除掉
   * @return 移除的元素，如果所有元素都为超时，则返回<tt>null</tt>
   */
  public ConnectionProxy removeIdle(long period) {
    for (int i = this.size; i > 0; i--) {
      // double check
      ProxyNode last = this.queue[i];
      if (last != null) {
        long now = System.currentTimeMillis();
        if (now - last.entryTime >= period) {
          return this.remove(i);
        }
      }
    }
    return null;
  }

  public int size() {
    return this.size;
  }

  /**
   * 携带时间的bean，方便计算是否超时并移出
   *
   * @author xionghui
   * @version 1.0.0
   * @since 1.0.0
   */
  private final static class ProxyNode {
    ConnectionProxy element;

    long entryTime;

    ProxyNode(ConnectionProxy element) {
      this.element = element;
    }

    ProxyNode(ConnectionProxy element, long entryTime) {
      this(element);
      this.entryTime = entryTime;
    }
  }
}
