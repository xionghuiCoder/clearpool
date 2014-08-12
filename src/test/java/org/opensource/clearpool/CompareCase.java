package org.opensource.clearpool;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.opensource.clearpool.core.ClearPoolDataSource;
import org.opensource.clearpool.util.TestUtil;

import com.alibaba.druid.mock.MockConnection;
import com.alibaba.druid.mock.MockDriver;
import com.alibaba.druid.pool.DruidDataSource;
import com.jolbox.bonecp.BoneCPDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Compare with other Database Pool.
 */
public class CompareCase extends TestCase {
	private String jdbcUrl;
	private String user;
	private String password;
	private String driverClass;
	private int minPoolSize = 10;
	private int maxPoolSize = 50;
	private int threadCount = 1000;
	private int loopCount = 5;
	private int LOOP_COUNT = 100_000 / this.threadCount;

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
		printMemoryInfo();
		System.setProperty("org.clearpool.log.unable", "true");
		DriverManager.registerDriver(TestDriver.instance);
		this.driverClass = "org.opensource.clearpool.CompareCase$TestDriver";
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
		this.intPool(dataSource);

		for (int i = 0; i < this.loopCount; ++i) {
			this.process(dataSource, "clearpool", this.threadCount);
		}
		System.out.println();
	}

	public void test_druid() throws Exception {
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
		this.intPool(dataSource);

		for (int i = 0; i < this.loopCount; ++i) {
			this.process(dataSource, "druid", this.threadCount);
		}
		System.out.println();
	}

	public void test_dbcp() throws Exception {
		final BasicDataSource dataSource = new BasicDataSource();
		dataSource.setInitialSize(this.minPoolSize);
		dataSource.setMaxActive(this.maxPoolSize);
		dataSource.setMinIdle(this.minPoolSize);
		dataSource.setMaxIdle(this.maxPoolSize);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setDriverClassName(this.driverClass);
		dataSource.setUrl(this.jdbcUrl);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setUsername(this.user);
		dataSource.setPassword(this.password);
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setTestOnBorrow(false);
		// init pool
		this.intPool(dataSource);
		for (int i = 0; i < this.loopCount; ++i) {
			this.process(dataSource, "dbcp", this.threadCount);
		}
		System.out.println();
	}

	public void test_bonecp() throws Exception {
		BoneCPDataSource dataSource = new BoneCPDataSource();
		dataSource.setMinConnectionsPerPartition(this.minPoolSize);
		dataSource.setMaxConnectionsPerPartition(this.maxPoolSize);
		dataSource.setDriverClass(this.driverClass);
		dataSource.setJdbcUrl(this.jdbcUrl);
		dataSource.setStatementsCacheSize(100);
		dataSource.setServiceOrder("LIFO");
		dataSource.setUsername(this.user);
		dataSource.setPassword(this.password);
		dataSource.setPartitionCount(1);
		dataSource.setAcquireIncrement(5);
		// init pool
		this.intPool(dataSource);
		for (int i = 0; i < this.loopCount; ++i) {
			this.process(dataSource, "boneCP", this.threadCount);
		}
		System.out.println();
	}

	public void test_c3p0() throws Exception {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setMinPoolSize(this.minPoolSize);
		dataSource.setMaxPoolSize(this.maxPoolSize);
		dataSource.setDriverClass(this.driverClass);
		dataSource.setJdbcUrl(this.jdbcUrl);
		dataSource.setUser(this.user);
		dataSource.setPassword(this.password);
		// init pool
		this.intPool(dataSource);
		for (int i = 0; i < this.loopCount; ++i) {
			this.process(dataSource, "c3p0", this.threadCount);
		}
		System.out.println();
	}

	public void test_tomcat_jdbc() throws Exception {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
		dataSource.setMaxIdle(this.maxPoolSize);
		dataSource.setMinIdle(this.minPoolSize);
		dataSource.setMaxActive(this.maxPoolSize);
		dataSource.setDriverClassName(this.driverClass);
		dataSource.setUrl(this.jdbcUrl);
		dataSource.setUsername(this.user);
		dataSource.setPassword(this.password);
		// init pool
		this.intPool(dataSource);
		for (int i = 0; i < this.loopCount; ++i) {
			this.process(dataSource, "tomcat-jdbc", this.threadCount);
		}
		System.out.println();
	}

	/**
	 * Init the pool,fair compare.
	 */
	private void intPool(DataSource dataSource) throws Exception {
		Connection conn = dataSource.getConnection();
		conn.setReadOnly(true);
		conn.close();
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
						for (int i = 0; i < CompareCase.this.LOOP_COUNT; i++) {
							Connection conn = dataSource.getConnection();
							conn.setReadOnly(true);
							Statement s = conn.createStatement();
							s.execute("select 1 from hello");
							s.close();
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
		long startYGC = TestUtil.getYoungGC();
		long startFullGC = TestUtil.getFullGC();

		startLatch.countDown();
		endLatch.await();

		long millis = System.currentTimeMillis() - startMillis;
		long ygc = TestUtil.getYoungGC() - startYGC;
		long fullGC = TestUtil.getFullGC() - startFullGC;

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

	/**
	 * Show memory
	 */
	private static void printMemoryInfo() {
		Runtime currRuntime = Runtime.getRuntime();
		int nFreeMemory = (int) (currRuntime.freeMemory() / 1024 / 1024);
		int nTotalMemory = (int) (currRuntime.totalMemory() / 1024 / 1024);
		String message = nFreeMemory + "M/" + nTotalMemory + "M(free/total)";
		System.out.println("memory:" + message);
		System.out.println();
	}
}
