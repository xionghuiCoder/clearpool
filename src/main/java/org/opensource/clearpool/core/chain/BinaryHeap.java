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
    queue = new ProxyNode[initialCapacity];
  }

  private int roundUpToPowerOf2(int number) {
    return number >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY
        : number > 1 ? Integer.highestOneBit(number - 1 << 1) : 1;
  }

  /**
   * add a element
   */
  public void add(ConnectionProxy e) {
    // Grow backing store if necessary
    if (size + 1 == queue.length) {
      queue = Arrays.copyOf(queue, 2 * queue.length);
    }
    queue[++size] = new ProxyNode(e, System.currentTimeMillis());
    this.fixUp(size);
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
      if (queue[j].element.compareTo(queue[k].element) > 0) {
        break;
      }
      ProxyNode tmp = queue[j];
      queue[j] = queue[k];
      queue[k] = tmp;
      k = j;
    }
  }

  /**
   * remove the first element
   */
  public ConnectionProxy removeFirst() {
    return remove(1);
  }

  /**
   * remove a element
   */
  private ConnectionProxy remove(int i) {
    if (size == 0) {
      return null;
    }
    ProxyNode removeProxyNode = queue[i];
    queue[i] = queue[size];
    // Drop extra reference to prevent memory leak
    queue[size--] = null;
    this.fixDown(i);
    return removeProxyNode.element;
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
    while ((j = k << 1) <= size && j > 0) {
      if (j < size && queue[j].element.compareTo(queue[j + 1].element) < 0) {
        // j indexes bigger kid
        j++;
      }
      if (queue[k].element.compareTo(queue[j].element) >= 0) {
        break;
      }
      ProxyNode tmp = queue[j];
      queue[j] = queue[k];
      queue[k] = tmp;
      k = j;
    }
  }

  /**
   * remove a element if it's over period(ms)
   */
  public ConnectionProxy removeIdle(long period) {
    for (int i = size; i > 0; i--) {
      // double check
      ProxyNode last = queue[i];
      if (last != null) {
        // re-get current time
        long now = System.currentTimeMillis();
        if (now - last.entryTime >= period) {
          return remove(i);
        }
      }
    }
    return null;
  }

  public int size() {
    return size;
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
