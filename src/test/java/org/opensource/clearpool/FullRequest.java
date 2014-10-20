package org.opensource.clearpool;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;

/**
 * Note: replace file-path which is in clearpool-test-unique.xml with your xml's
 * real absolute path please.
 * 
 * @author xionghui
 * @date 24.09.2014
 * @version 1.0
 */
public class FullRequest extends TestCase {
	private volatile boolean sign = false;

	private ClearPoolDataSource dataSource = new ClearPoolDataSource();

	private final static int TIME = 60;

	@Override
	public void setUp() throws Exception {
		this.dataSource.initPath("clearpool/clearpool-test-unique.xml");
	}

	public void test_clearPool() throws Exception {
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(50);
		this.startThreads(startLatch, endLatch, 50);
		startLatch.countDown();
		Thread.sleep(TIME * 1000);
		this.sign = true;
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
							if (FullRequest.this.sign) {
								break;
							}
							Connection conn = FullRequest.this.dataSource
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
	}
}
