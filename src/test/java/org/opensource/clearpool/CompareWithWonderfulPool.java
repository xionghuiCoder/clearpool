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
 * Oracle Test.
 * 
 * Note: <br />
 * 1.replace jdbcClass with your database's jdbc-class please; <br />
 * 2.replace url with your database's url please; <br />
 * 3.replace user with your database's user please; <br />
 * 4.replace password with your database's password please; <br />
 * 5.replace sql with your valid sql please.
 * 
 * @author xionghui
 * @date 24.09.2014
 * @version 1.0
 */
public class CompareWithWonderfulPool extends TestCase {
  private String jdbcUrl;
  private String user;
  private String password;
  private String driverClass;
  private int corePoolSize = 20;
  private int maxPoolSize = 50;
  private static final int threadCount = 100;
  private int loop = 5;
  private int count = 1000000 / threadCount;

  private static AtomicLong physicalCon = MockTestDriver.physicalCon;

  private static final String PATH = "log4j/special_log4j.properties";

  static {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
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
    dataSource.setCorePoolSize(this.corePoolSize);
    dataSource.setMaxPoolSize(this.maxPoolSize);
    dataSource.setDriverClass(this.driverClass);
    dataSource.setJdbcUrl(this.jdbcUrl);
    dataSource.setJdbcUser(this.user);
    dataSource.setJdbcPassword(this.password);
    for (int i = 0; i < this.loop; ++i) {
      ThreadProcessUtil.process(dataSource, "clearpool", this.count, threadCount, physicalCon);
    }
    System.out.println();
  }

  public void test_druid() throws Exception {
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
    for (int i = 0; i < this.loop; ++i) {
      ThreadProcessUtil.process(dataSource, "druid", this.count, threadCount, physicalCon);
    }
    System.out.println();
  }

  public void test_tomcat_jdbc() throws Exception {
    org.apache.tomcat.jdbc.pool.DataSource dataSource =
        new org.apache.tomcat.jdbc.pool.DataSource();
    dataSource.setMaxIdle(this.maxPoolSize);
    dataSource.setMinIdle(this.corePoolSize);
    dataSource.setMaxActive(this.maxPoolSize);
    dataSource.setDriverClassName(this.driverClass);
    dataSource.setUrl(this.jdbcUrl);
    dataSource.setUsername(this.user);
    dataSource.setPassword(this.password);
    for (int i = 0; i < this.loop; ++i) {
      ThreadProcessUtil.process(dataSource, "tomcat-jdbc", this.count, threadCount, physicalCon);
    }
    System.out.println();
  }
}
