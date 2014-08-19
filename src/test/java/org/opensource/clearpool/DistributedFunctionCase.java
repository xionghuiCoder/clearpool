package org.opensource.clearpool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;

public class DistributedFunctionCase extends TestCase {
	private volatile boolean sign = false;

	private ClearPoolDataSource dataSource = new ClearPoolDataSource();

	private volatile AtomicBoolean showSql = new AtomicBoolean(true);

	@Override
	public void setUp() throws Exception {
		this.dataSource.initPath("clearpool/clearpool-test-distributed.xml");
		System.out.println("init");
	}

	public void testClearPool() throws Exception {
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(10);
		this.startThreads(startLatch, endLatch, 5, "myclearpool1");
		this.startThreads(startLatch, endLatch, 5, "myclearpool2");
		startLatch.countDown();
		System.out.println("start 10 threads");
		Thread.sleep(30 * 1000);
		this.sign = true;
		System.out.println("finish");
		endLatch.await();
	}

	private void startThreads(final CountDownLatch startLatch,
			final CountDownLatch endLatch, int count, final String name) {
		for (int i = 0; i < count; i++) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						startLatch.await();
						for (;;) {
							if (DistributedFunctionCase.this.sign) {
								break;
							}
							Connection conn = DistributedFunctionCase.this.dataSource
									.getConnection(name);
							if (DistributedFunctionCase.this.showSql.get()) {
								if (DistributedFunctionCase.this.showSql
										.compareAndSet(true, false)) {
									PreparedStatement s = conn
											.prepareStatement("select 1 from ?");
									s.setString(1, "hello");
									s.execute();
									s.close();
								}
							}
							try {
								Thread.sleep(1);
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