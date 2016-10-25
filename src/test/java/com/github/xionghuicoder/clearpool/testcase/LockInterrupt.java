package com.github.xionghuicoder.clearpool.testcase;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

import junit.framework.TestCase;

public class LockInterrupt extends TestCase {
  private volatile boolean sign = true;

  private int count = 1000000;

  @Test
  public void testLockInterrupt() {
    final Lock lock = new ReentrantLock();
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        while (LockInterrupt.this.sign) {
        }
        try {
          lock.lockInterruptibly();
        } catch (InterruptedException e) {
          System.out.println("interrupt it");
        }
        LockInterrupt.this.sign = true;
      }
    });
    t.start();
    t.interrupt();
    this.sign = false;
    while (!LockInterrupt.this.sign) {
    }
    lock.lock();
    lock.unlock();
    System.out.println("lock success");
  }

  @Test
  public void testLockAndLockInterrupt() {
    Lock lock = new ReentrantLock();
    this.printLockTime(lock);
    this.printLockInterruptiblyTime(lock);
  }

  private void printLockTime(Lock lock) {
    long begin = System.currentTimeMillis();
    for (int i = 0; i < this.count; i++) {
      lock.lock();
      lock.unlock();
    }
    System.out.println("LockTime: " + (System.currentTimeMillis() - begin));
  }

  private void printLockInterruptiblyTime(Lock lock) {
    try {
      long begin = System.currentTimeMillis();
      for (int i = 0; i < this.count; i++) {
        lock.lockInterruptibly();
        lock.unlock();
      }
      System.out.println("LockInterruptiblyTime: " + (System.currentTimeMillis() - begin));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
