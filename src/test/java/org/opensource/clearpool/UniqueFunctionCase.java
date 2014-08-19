package org.opensource.clearpool;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;

public class UniqueFunctionCase extends TestCase {
	private ClearPoolDataSource dataSource = new ClearPoolDataSource();

	private volatile boolean[] signs = new boolean[10];

	private int time = 10;

	@Override
	public void setUp() throws Exception {
		this.dataSource.initPath("clearpool/clearpool-test-unique.xml");
		System.out.println("init");
		Thread.sleep(this.time * 1000);
	}

	public void testClearPool() throws Exception {
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(100);
		this.startThreads(startLatch, endLatch, 10, 0);
		startLatch.countDown();
		System.out.println("start 10 threads");
		// add 100 thread every 30s
		for (int i = 1; i < 10; i++) {
			Thread.sleep(this.time * 1000);
			startLatch = new CountDownLatch(1);
			this.startThreads(startLatch, endLatch, 10, i);
			startLatch.countDown();
			System.out.println("start " + ((1 + i) * 10) + " threads");
		}
		// remove 100 thread every 30s
		for (int i = 0; i < 10; i++) {
			Thread.sleep(this.time * 1000);
			this.signs[i] = true;
			System.out.println("left " + ((9 - i) * 10) + " threads");
		}
		Thread.sleep(this.time * 1000);
		endLatch.await();
	}

	private void startThreads(final CountDownLatch startLatch,
			final CountDownLatch endLatch, int count, final int order) {
		for (int i = 0; i < count; i++) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						startLatch.await();
						for (;;) {
							if (UniqueFunctionCase.this.signs[order]) {
								break;
							}
							Connection conn = UniqueFunctionCase.this.dataSource
									.getConnection();
							conn.setReadOnly(true);
							Statement s = conn.createStatement();
							s.execute("select 1 from hello");
							s.close();
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
		Thread.sleep(this.time * 1000);
		this.dataSource.destory();
		System.out.println("destory");
	}
}
