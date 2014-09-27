package org.opensource.clearpool;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.TestCase;

public class LockInterrupt extends TestCase {
	private Lock lock = new ReentrantLock();
	private volatile boolean sign = true;

	public void test_lockInterrupt() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (LockInterrupt.this.sign) {
				}
				try {
					LockInterrupt.this.lock.lockInterruptibly();
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
		this.lock.lock();
		this.lock.unlock();
		System.out.println("lock success");
	}
}
