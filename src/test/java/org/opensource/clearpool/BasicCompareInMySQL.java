package org.opensource.clearpool;

import java.sql.Driver;
import java.util.Properties;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.opensource.clearpool.BasicCompareInOracle.NopoolDataSource;
import org.opensource.clearpool.core.ClearPoolDataSource;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.JdbcUtil;
import org.opensource.clearpool.util.MemoryUtil;
import org.opensource.clearpool.util.ThreadProcessUtil;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * Oracle Test.
 * 
 * Note: <br />
 * 1.replace driverClass with your database's jdbc-class please; <br />
 * 2.replace jdbcUrl with your database's url please; <br />
 * 3.replace user with your database's user please; <br />
 * 4.replace password with your database's password please; <br />
 * 5.replace sql with your valid sql please.
 * 
 * @author xionghui
 * @date 24.09.2014
 * @version 1.0
 */
public class BasicCompareInMySQL extends TestCase {
  private String sql = "select count(1) from test";

  private String driverClass = "com.mysql.jdbc.Driver";
  private String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/test";
  private String user = "root";
  private String password = "1";

  private int corePoolSize = 10;
  private int maxPoolSize = 10;
  private static final int threadCount = 10;
  private int loop = 5;
  private int count = 100 / threadCount;

  private static final String PATH = "log4j/special_log4j.properties";

  static {
    System.out.println("MySQL:");
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String path = classLoader.getResource(PATH).getPath();
    PropertyConfigurator.configure(path);
  }

  @Override
  public void setUp() throws Exception {
    MemoryUtil.printMemoryInfo();
    System.setProperty(PoolLogFactory.LOG_UNABLE, "true");
  }

  public void test_Nopool() throws Exception {
    System.setProperty("jdbc.drivers", this.driverClass);
    Driver driver = JdbcUtil.createDriver(this.driverClass);
    Properties connectProperties = new Properties();
    if (this.user != null) {
      connectProperties.put("user", this.user);
    }
    if (this.password != null) {
      connectProperties.put("password", this.password);
    }
    DataSource dataSource =
        new NopoolDataSource(this.driverClass, this.jdbcUrl, driver, connectProperties);
    for (int i = 0; i < this.loop; ++i) {
      ThreadProcessUtil.processSql(dataSource, "Nopool", this.count, threadCount, this.sql);
    }
    System.out.println();
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
      ThreadProcessUtil.processSql(dataSource, "clearpool", this.count, threadCount, this.sql);
    }
    dataSource.destory();
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
      ThreadProcessUtil.processSql(dataSource, "druid", this.count, threadCount, this.sql);
    }
    dataSource.close();
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
      ThreadProcessUtil.processSql(dataSource, "tomcat-jdbc", this.count, threadCount, this.sql);
    }
    dataSource.close();
    System.out.println();
  }
}
