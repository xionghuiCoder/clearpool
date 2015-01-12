package org.opensource.clearpool;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import junit.framework.TestCase;

import org.opensource.clearpool.util.JdbcUtil;

/**
 * Note: <br />
 * 1.replace jdbcClass with your database's jdbc-class please; <br />
 * 2.replace url with your database's url please; <br />
 * 3.replace user with your database's user please; <br />
 * 4.replace password with your database's password please;
 * 
 * @author xionghui
 * @date 24.09.2014
 * @version 1.0
 */
public class OpenAndCloseConnection extends TestCase {
  private String jdbcClass = "com.mysql.jdbc.Driver";
  private String url = "jdbc:mysql://127.0.0.1:3306/test";
  private String user = "root";
  private String password = "1";

  private Driver driver;
  private Properties connectProperties = new Properties();

  private int loop = 5;
  private int count = 100;

  @Override
  public void setUp() throws Exception {
    System.setProperty("jdbc.drivers", this.jdbcClass);
    this.driver = JdbcUtil.createDriver(this.jdbcClass);
    this.connectProperties.put("user", this.user);
    this.connectProperties.put("password", this.password);
  }

  public void test_Statement() throws Exception {
    System.out.print("time:	");
    for (int i = 0; i < this.loop; i++) {
      long begin = System.currentTimeMillis();
      for (int j = 0; j < this.count; j++) {
        Connection conn = this.driver.connect(this.url, this.connectProperties);
        conn.close();
      }
      System.out.print((System.currentTimeMillis() - begin) + "ms	");
    }
  }
}
