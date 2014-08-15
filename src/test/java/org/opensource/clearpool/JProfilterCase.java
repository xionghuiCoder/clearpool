package org.opensource.clearpool;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;
import org.opensource.clearpool.util.GCUtil;

import com.alibaba.druid.mock.MockConnection;
import com.alibaba.druid.mock.MockDriver;
import com.alibaba.druid.pool.DruidDataSource;

public class JProfilterCase extends TestCase {
	private String jdbcUrl;
	private String user;
	private String password;
	private String driverClass;
	private int minPoolSize = 10;
	private int maxPoolSize = 50;
	private int threadCount = 10;

	private static AtomicLong physicalConnStat = new AtomicLong();

	public static class TestDriver extends MockDriver {
		public static TestDriver instance = new TestDriver();

		@Override
		public boolean acceptsURL(String url) throws SQLException {
			if (url.startsWith("jdbc:test:")) {
				return true;
			}
			return super.acceptsURL(url);
		}

		@Override
		public Connection connect(String url, Properties info)
				throws SQLException {
			physicalConnStat.incrementAndGet();
			// to support clearpool
			return new MockConnection(this, "jdbc:mock:case", info) {
				@Override
				public String getSchema() throws SQLException {
					return null;
				}
			};
		}
	}

	@Override
	public void setUp() throws Exception {
		System.setProperty("org.clearpool.log.unable", "true");
		DriverManager.registerDriver(TestDriver.instance);
		this.driverClass = "org.opensource.clearpool.CompareWithWonderfulPoolCase$TestDriver";
		this.jdbcUrl = "jdbc:test:comparecase:";
		this.user = "1";
		this.password = "1";
		physicalConnStat.set(0);
	}

	public void test_clearpool() throws Exception {
		ClearPoolDataSource dataSource = new ClearPoolDataSource();
		dataSource.setCorePoolSize(this.minPoolSize);
		dataSource.setMaxPoolSize(this.maxPoolSize);
		dataSource.setDriverClass(this.driverClass);
		dataSource.setJdbcUrl(this.jdbcUrl);
		dataSource.setJdbcUser(this.user);
		dataSource.setJdbcPassword(this.password);
		// init pool
		dataSource.init();
		this.process(dataSource, "clearpool", this.threadCount);
		System.out.println();
	}

	public void stop_test_druid() throws Exception {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setInitialSize(this.minPoolSize);
		dataSource.setMaxActive(this.maxPoolSize);
		dataSource.setMinIdle(this.minPoolSize);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setDriverClassName(this.driverClass);
		dataSource.setUrl(this.jdbcUrl);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setUsername(this.user);
		dataSource.setPassword(this.password);
		dataSource.setValidationQuery("select 1");
		dataSource.setTestOnBorrow(false);
		// init pool
		dataSource.init();
		this.process(dataSource, "druid", this.threadCount);
		System.out.println();
	}

	/**
	 * Fight for connection
	 */
	private void process(final DataSource dataSource, String name,
			int threadCount) throws Exception {
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch endLatch = new CountDownLatch(threadCount);
		final CountDownLatch dumpLatch = new CountDownLatch(1);

		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; ++i) {
			Thread thread = new Thread() {

				@Override
				public void run() {
					try {
						startLatch.await();
						for (;;) {
							Connection conn = dataSource.getConnection();
							conn.close();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					endLatch.countDown();
					try {
						dumpLatch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			threads[i] = thread;
			thread.start();
		}
		long startMillis = System.currentTimeMillis();
		long startYGC = GCUtil.getYoungGC();
		long startFullGC = GCUtil.getFullGC();

		startLatch.countDown();
		endLatch.await();

		long millis = System.currentTimeMillis() - startMillis;
		long ygc = GCUtil.getYoungGC() - startYGC;
		long fullGC = GCUtil.getFullGC() - startFullGC;

		long[] threadIdArray = new long[threads.length];
		for (int i = 0; i < threads.length; ++i) {
			threadIdArray[i] = threads[i].getId();
		}
		ThreadInfo[] threadInfoArray = ManagementFactory.getThreadMXBean()
				.getThreadInfo(threadIdArray);

		dumpLatch.countDown();

		long blockedCount = 0;
		long waitedCount = 0;
		for (int i = 0; i < threadInfoArray.length; ++i) {
			ThreadInfo threadInfo = threadInfoArray[i];
			blockedCount += threadInfo.getBlockedCount();
			waitedCount += threadInfo.getWaitedCount();
		}

		System.out.println("thread " + threadCount + " " + name + " millis : "
				+ NumberFormat.getInstance().format(millis) + "; YGC " + ygc
				+ " FGC " + fullGC
				+ " blocked "
				+ NumberFormat.getInstance().format(blockedCount) //
				+ " waited " + NumberFormat.getInstance().format(waitedCount)
				+ " physicalConn " + physicalConnStat.get());

	}
}
