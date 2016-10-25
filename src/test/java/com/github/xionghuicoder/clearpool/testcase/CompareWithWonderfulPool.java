package com.github.xionghuicoder.clearpool.testcase;

import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.xionghuicoder.clearpool.MockTestDriver;
import com.github.xionghuicoder.clearpool.core.ClearpoolDataSource;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;
import com.github.xionghuicoder.clearpool.util.MemoryUtils;
import com.github.xionghuicoder.clearpool.util.ThreadProcessUtils;

import junit.framework.TestCase;

public class CompareWithWonderfulPool extends TestCase {
  private String url;
  private String username;
  private String password;
  private String driverClassName;
  private int corePoolSize = 20;
  private int maxPoolSize = 50;
  private static final int threadCount = 100;
  private int loop = 5;
  private int count = 1000000 / threadCount;

  private static AtomicLong physicalCon = MockTestDriver.physicalCon;

  @Override
  public void setUp() throws Exception {
    MemoryUtils.printMemoryInfo();
    System.setProperty(PoolLoggerFactory.LOG_UNABLE, "true");
    DriverManager.registerDriver(new MockTestDriver());
    this.driverClassName = MockTestDriver.CLASS;
    this.url = MockTestDriver.URL;
    this.username = "1";
    this.password = "1";
    physicalCon.set(0);
  }

  @Test
  public void testClearpool() throws Exception {
    ClearpoolDataSource dataSource = new ClearpoolDataSource();
    dataSource.setCorePoolSize(this.corePoolSize);
    dataSource.setMaxPoolSize(this.maxPoolSize);
    dataSource.setDriverClassName(this.driverClassName);
    dataSource.setUrl(this.url);
    dataSource.setUsername(this.username);
    dataSource.setPassword(this.password);
    for (int i = 0; i < this.loop; ++i) {
      ThreadProcessUtils.process(dataSource, "clearpool", this.count, threadCount, physicalCon);
    }
    System.out.println();
  }

  @Test
  public void testDruid() throws Exception {
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setInitialSize(this.corePoolSize);
    dataSource.setMaxActive(this.maxPoolSize);
    dataSource.setMinIdle(this.corePoolSize);
    dataSource.setPoolPreparedStatements(true);
    dataSource.setDriverClassName(this.driverClassName);
    dataSource.setUrl(this.url);
    dataSource.setPoolPreparedStatements(true);
    dataSource.setUsername(this.username);
    dataSource.setPassword(this.password);
    dataSource.setValidationQuery("select 1");
    dataSource.setTestOnBorrow(false);
    for (int i = 0; i < this.loop; ++i) {
      ThreadProcessUtils.process(dataSource, "druid", this.count, threadCount, physicalCon);
    }
    System.out.println();
  }

  @Test
  public void testTomcatJdbc() throws Exception {
    org.apache.tomcat.jdbc.pool.DataSource dataSource =
        new org.apache.tomcat.jdbc.pool.DataSource();
    dataSource.setMaxIdle(this.maxPoolSize);
    dataSource.setMinIdle(this.corePoolSize);
    dataSource.setMaxActive(this.maxPoolSize);
    dataSource.setDriverClassName(this.driverClassName);
    dataSource.setUrl(this.url);
    dataSource.setUsername(this.username);
    dataSource.setPassword(this.password);
    for (int i = 0; i < this.loop; ++i) {
      ThreadProcessUtils.process(dataSource, "tomcat-jdbc", this.count, threadCount, physicalCon);
    }
    System.out.println();
  }
}
