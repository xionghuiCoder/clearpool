package org.opensource.clearpool;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.TestCase;

public class LockInterruptCase extends TestCase {
	private Lock lock = new ReentrantLock();
	private volatile boolean sign = true;

	public void test() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (LockInterruptCase.this.sign) {
				}
				try {
					LockInterruptCase.this.lock.lockInterruptibly();
				} catch (InterruptedException e) {
					System.out.println("interrupt it");
				}
				LockInterruptCase.this.sign = true;
			}
		});
		t.start();
		t.interrupt();
		this.sign = false;
		while (!LockInterruptCase.this.sign) {
		}
		this.lock.lock();
		this.lock.unlock();
		System.out.println("lock success");
	}
}
