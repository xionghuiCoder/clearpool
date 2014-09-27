package org.opensource.clearpool;

import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.opensource.clearpool.core.ClearPoolDataSource;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.MemoryUtil;
import org.opensource.clearpool.util.MockTestDriver;
import org.opensource.clearpool.util.ThreadProcessUtil;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * Compare with wonderful Database Pool.
 */
public class CompareWithWonderfulPool extends TestCase {
	private String jdbcUrl;
	private String user;
	private String password;
	private String driverClass;
	private int minPoolSize = 20;
	private int maxPoolSize = 50;
	private int threadCount = 100;
	private int loop = 5;
	private int count = 1000000 / this.threadCount;

	private static AtomicLong physicalCon = MockTestDriver.physicalCon;

	private static final String PATH = "log4j/special_log4j.properties";

	static {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		String path = classLoader.getResource(PATH).getPath();
		PropertyConfigurator.configure(path);
	}

	@Override
	public void setUp() throws Exception {
		MemoryUtil.printMemoryInfo();
		System.setProperty(PoolLogFactory.LOG_UNABLE, "true");
		DriverManager.registerDriver(new MockTestDriver());
		this.driverClass = MockTestDriver.CLASS;
		this.jdbcUrl = MockTestDriver.URL;
		this.user = "1";
		this.password = "1";
		physicalCon.set(0);
	}

	public void test_clearpool() throws Exception {
		ClearPoolDataSource dataSource = new ClearPoolDataSource();
		dataSource.setCorePoolSize(this.minPoolSize);
		dataSource.setMaxPoolSize(this.maxPoolSize);
		dataSource.setDriverClass(this.driverClass);
		dataSource.setJdbcUrl(this.jdbcUrl);
		dataSource.setJdbcUser(this.user);
		dataSource.setJdbcPassword(this.password);
		for (int i = 0; i < this.loop; ++i) {
			ThreadProcessUtil.process(dataSource, "clearpool", this.count,
					this.threadCount, physicalCon);
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
		for (int i = 0; i < this.loop; ++i) {
			ThreadProcessUtil.process(dataSource, "druid", this.count,
					this.threadCount, physicalCon);
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
		for (int i = 0; i < this.loop; ++i) {
			ThreadProcessUtil.process(dataSource, "tomcat-jdbc", this.count,
					this.threadCount, physicalCon);
		}
		System.out.println();
	}

}
