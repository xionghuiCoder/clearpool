package org.opensource.clearpool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;

public class DistributedFunction extends TestCase {
	private volatile boolean sign = false;

	private ClearPoolDataSource dataSource = new ClearPoolDataSource();

	private volatile AtomicBoolean showSql = new AtomicBoolean(true);

	private static final int TIME = 10;

	@Override
	public void setUp() throws Exception {
		this.dataSource.initPath("clearpool/clearpool-test-distributed.xml");
	}

	public void test_clearPool() throws Exception {
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(10);
		this.startThreads(startLatch, endLatch, 5, "myclearpool1");
		this.startThreads(startLatch, endLatch, 5, "myclearpool2");
		startLatch.countDown();
		Thread.sleep(TIME * 1000);
		this.sign = true;
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
							if (DistributedFunction.this.sign) {
								break;
							}
							Connection conn = DistributedFunction.this.dataSource
									.getConnection(name);
							if (DistributedFunction.this.showSql.get()) {
								if (DistributedFunction.this.showSql
										.compareAndSet(true, false)) {
									PreparedStatement stm = conn
											.prepareStatement("select 1 from geek where name=? and age=?");
									stm.setString(1, "Bill Joy");
									stm.setInt(2, 60);
									stm.execute();
									stm.close();
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
	}
}