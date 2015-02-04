package org.opensource.clearpool.core.chain;

import java.util.Arrays;

import org.opensource.clearpool.datasource.proxy.ConnectionProxy;

/**
 * This is a binary heap.<br />
 * It makes sure that the connection is most often reused.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class BinaryHeap {
  private static final int MAXIMUM_CAPACITY = 1 << 30;

  /**
   * The default initial capacity for this table, used when not otherwise specified in a
   * constructor.
   */
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
    // Find a power of 2 >= toSize
    initialCapacity = this.roundUpToPowerOf2(initialCapacity);
    this.queue = new ProxyNode[initialCapacity];
  }

  private int roundUpToPowerOf2(int number) {
    return number >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : number > 1 ? Integer
        .highestOneBit(number - 1 << 1) : 1;
  }

  /**
   * add a element
   */
  public void add(ConnectionProxy e) {
    // Grow backing store if necessary
    if (this.size + 1 == this.queue.length) {
      this.queue = Arrays.copyOf(this.queue, 2 * this.queue.length);
    }
    this.queue[++this.size] = new ProxyNode(e, System.currentTimeMillis());
    this.fixUp(this.size);
  }

  /**
   * Establishes the heap invariant (described above) assuming the heap satisfies the invariant
   * except possibly for the leaf-node indexed by k (which may have a element greater than its
   * parent's).
   * 
   * This method functions by "promoting" queue[k] up the hierarchy (by swapping it with its parent)
   * repeatedly until queue[k]'s element is less than or equal to that of its parent.
   */
  private void fixUp(int k) {
    while (k > 1) {
      int j = k >> 1;
      if (this.queue[j].element.compareTo(this.queue[k].element) >= 0) {
        break;
      }
      ProxyNode tmp = this.queue[j];
      this.queue[j] = this.queue[k];
      this.queue[k] = tmp;
      k = j;
    }
  }

  /**
   * remove a element
   */
  public ConnectionProxy remove() {
    if (this.size == 0) {
      return null;
    }
    ProxyNode first = this.queue[1];
    this.queue[1] = this.queue[this.size];
    // Drop extra reference to prevent memory leak
    this.queue[this.size--] = null;
    this.fixDown(1);
    return first.element;
  }

  /**
   * Establishes the heap invariant (described above) in the subtree rooted at k, which is assumed
   * to satisfy the heap invariant except possibly for node k itself (which may have a element less
   * than its children's).
   * 
   * This method functions by "demoting" queue[k] down the hierarchy (by swapping it with its
   * smaller child) repeatedly until queue[k]'s element is greater than or equal to those of its
   * children.
   */
  private void fixDown(int k) {
    int j;
    while ((j = k << 1) <= this.size && j > 0) {
      if (j < this.size && this.queue[j].element.compareTo(this.queue[j + 1].element) < 0) {
        // j indexes bigger kid
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
   * remove a element if it's over period(ms)
   */
  public ConnectionProxy removeIdle(long period) {
    if (this.size == 0) {
      return null;
    }
    // re-get current time
    long now = System.currentTimeMillis();
    // double check
    ProxyNode last = this.queue[this.size];
    if (last == null || now - last.entryTime < period) {
      return null;
    }
    // Drop extra reference to prevent memory leak
    this.queue[this.size--] = null;
    return last.element;
  }

  public int size() {
    return this.size;
  }

  /**
   * This class works as a time carrier.
   * 
   * @author xionghui
   * @date 26.07.2014
   * @version 1.0
   */
  private final static class ProxyNode {
    ConnectionProxy element;

    /**
     * we should know that entryTime is used by {@link AtomicSingleChain} or {@link BinaryHeap}
     */
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
