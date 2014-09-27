package org.opensource.clearpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;
import org.opensource.clearpool.util.MockTestDriver;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * This TestCase is used to monitor the jvm by JProfilter.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class JProfilter extends TestCase {
	private String jdbcUrl;
	private String user;
	private String password;
	private String driverClass;
	private int corePoolSize = 10;
	private int maxPoolSize = 50;
	private int threadCount = 10;

	private static AtomicLong physicalCon = MockTestDriver.physicalCon;

	@Override
	public void setUp() throws Exception {
		System.setProperty("org.clearpool.log.unable", "true");
		DriverManager.registerDriver(new MockTestDriver());
		this.driverClass = MockTestDriver.CLASS;
		this.jdbcUrl = MockTestDriver.URL;
		this.user = "1";
		this.password = "1";
		physicalCon.set(0);
	}

	public void test_clearpool() throws Exception {
		ClearPoolDataSource dataSource = new ClearPoolDataSource();
		dataSource.setCorePoolSize(this.corePoolSize);
		dataSource.setMaxPoolSize(this.maxPoolSize);
		dataSource.setDriverClass(this.driverClass);
		dataSource.setJdbcUrl(this.jdbcUrl);
		dataSource.setJdbcUser(this.user);
		dataSource.setJdbcPassword(this.password);
		this.process(dataSource, this.threadCount);
	}

	public void stop_test_druid() throws Exception {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setInitialSize(this.corePoolSize);
		dataSource.setMaxActive(this.maxPoolSize);
		dataSource.setMinIdle(this.corePoolSize);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setDriverClassName(this.driverClass);
		dataSource.setUrl(this.jdbcUrl);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setUsername(this.user);
		dataSource.setPassword(this.password);
		dataSource.setValidationQuery("select 1");
		dataSource.setTestOnBorrow(false);
		this.process(dataSource, this.threadCount);
	}

	private void process(final DataSource dataSource, int threadCount)
			throws Exception {
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch endLatch = new CountDownLatch(threadCount);
		final CountDownLatch dumpLatch = new CountDownLatch(1);

		for (int i = 0; i < threadCount; ++i) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					endLatch.countDown();
					try {
						startLatch.await();
						for (;;) {
							Connection conn = dataSource.getConnection();
							conn.close();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			thread.start();
		}
		endLatch.await();
		startLatch.countDown();
		dumpLatch.await();
	}
}
