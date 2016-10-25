package com.github.xionghuicoder.clearpool.testcase;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import org.junit.Test;

import com.github.xionghuicoder.clearpool.MockTestDriver;
import com.github.xionghuicoder.clearpool.util.JdbcUtils;

import junit.framework.TestCase;

public class OpenAndCloseConnection extends TestCase {
  private Driver driver;
  private Properties connectProperties = new Properties();

  private int loop = 5;
  private int count = 100;

  @Override
  public void setUp() throws Exception {
    System.setProperty("jdbc.drivers", MockTestDriver.CLASS);
    this.driver = JdbcUtils.createDriver(MockTestDriver.CLASS);
    this.connectProperties.put("user", "root");
    this.connectProperties.put("password", "1");
  }

  @Test
  public void testStatement() throws Exception {
    System.out.print("time:	");
    for (int i = 0; i < this.loop; i++) {
      long begin = System.currentTimeMillis();
      for (int j = 0; j < this.count; j++) {
        Connection conn = this.driver.connect(MockTestDriver.URL, this.connectProperties);
        conn.close();
      }
      System.out.print((System.currentTimeMillis() - begin) + "ms	");
    }
  }
}
