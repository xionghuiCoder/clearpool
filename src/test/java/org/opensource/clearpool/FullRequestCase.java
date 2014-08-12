package org.opensource.clearpool;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;

public class FullRequestCase extends TestCase {
	private volatile boolean sign = false;

	private ClearPoolDataSource dataSource = new ClearPoolDataSource();

	@Override
	public void setUp() throws Exception {
		this.dataSource.initPath("clearpool/clearpool-test-unique.xml");
		System.out.println("init");
	}

	public void testClearPool() throws Exception {
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(500);
		this.startThreads(startLatch, endLatch, 500);
		startLatch.countDown();
		System.out.println("start 500 threads");
		Thread.sleep(60 * 1000);
		this.sign = true;
		System.out.println("finish");
		endLatch.await();
	}

	private void startThreads(final CountDownLatch startLatch,
			final CountDownLatch endLatch, int count) {
		for (int i = 0; i < count; i++) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						startLatch.await();
						for (;;) {
							if (FullRequestCase.this.sign) {
								break;
							}
							Connection conn = FullRequestCase.this.dataSource
									.getConnection();
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								// swallow the exception
							}
							conn.close();
						}
						endLatch.countDown();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		}
	}

	@Override
	public void tearDown() throws Exception {
		this.dataSource.destory();
		System.out.println("destory");
	}
}
