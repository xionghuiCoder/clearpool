package org.opensource.clearpool.jta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;
import org.opensource.clearpool.logging.PoolLogFactory;

/**
 * Note: replace database configuration in clearpool-test-jta-mysql-distributed1.xml with your
 * MySQL's real configuration and clearpool-test-jta-oracle-distributed2.xml with your Oracle's real
 * configuration please.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class JtaDistributedInMySQLAndOracle extends TestCase {
  private static final Random RANDOM = new Random();

  private static ClearPoolDataSource dataSource;

  private String tableName1;
  private String tableName2;

  static {
    System.setProperty(PoolLogFactory.LOG_UNABLE, "true");
  }

  @Override
  public void setUp() throws Exception {
    dataSource = new ClearPoolDataSource();
    dataSource.initPath("clearpool/jta/clearpool-test-jta-mysql-oracle-distributed.xml");
    this.tableName1 = this.init("myclearpool1");
    this.tableName2 = this.init("myclearpool2");
  }

  private String init(String poolName) throws Exception {
    Connection con = dataSource.getConnection(poolName);
    Statement st = con.createStatement();
    int count = 0;
    String tableName = null;
    for (;;) {
      tableName = "clearpool_jta_" + RANDOM.nextInt(1000000000);
      try {
        st.execute("select 1 from " + tableName);
      } catch (SQLException e) {
        break;
      }
      count++;
      if (count > 100000000) {
        throw new RuntimeException(
            "you got too many tables which's name begin with clearpool_jta_ in the database");
      }
    }
    st.execute("create table " + tableName + "(id int primary key,name varchar(10))");
    System.out.println(poolName + "'s table name is " + tableName);
    con.close();
    return tableName;
  }

  public void testJta_commit() throws Exception {
    if (this.tableName1 == null || this.tableName2 == null) {
      return;
    }
    System.out.println("test commit:");
    Connection con1 = dataSource.getConnection("myclearpool1");
    Connection con2 = dataSource.getConnection("myclearpool2");
    UserTransaction tx = new UserTransactionImpl();
    tx.begin();
    Statement st1 = con1.createStatement();
    st1.execute("insert into " + this.tableName1 + "(id,name) values(1,'name1')");
    Statement st2 = con2.createStatement();
    st2.execute("insert into " + this.tableName2 + "(id,name) values(2,'name2')");
    System.out.print("between the jtx:");
    this.showQueryResult(1, 2);
    tx.commit();
    System.out.print("end the jtx:");
    this.showQueryResult(1, 2);
  }

  public void testJta_rollback() throws Exception {
    if (this.tableName1 == null || this.tableName2 == null) {
      return;
    }
    System.out.println("test rollback:");
    Connection con1 = dataSource.getConnection("myclearpool1");
    Connection con2 = dataSource.getConnection("myclearpool2");
    UserTransaction tx = new UserTransactionImpl();
    tx.begin();
    Statement st1 = con1.createStatement();
    st1.execute("insert into " + this.tableName1 + "(id,name) values(3,'name3')");
    Statement st2 = con2.createStatement();
    st2.execute("insert into " + this.tableName2 + "(id,name) values(4,'name4')");
    System.out.print("between the jtx:");
    this.showQueryResult(3, 4);
    tx.rollback();
    System.out.print("end the jtx:");
    this.showQueryResult(3, 4);
  }

  private void showQueryResult(int id1, int id2) throws Exception {
    this.queryTable1(id1);
    this.queryTable2(id2);
  }

  private void queryTable1(int id) throws Exception {
    Connection con = dataSource.getConnection("myclearpool1");
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery("select name from " + this.tableName1 + " where id=" + id);
    System.out.println(this.tableName1);
    while (rs.next()) {
      System.out.println(rs.getString(1));
      return;
    }
    System.out.println(" has no result");
    con.close();
  }

  private void queryTable2(int id) throws Exception {
    Connection con = dataSource.getConnection("myclearpool2");
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery("select name from " + this.tableName2 + " where id=" + id);
    System.out.println(this.tableName2);
    while (rs.next()) {
      System.out.println(rs.getString(1));
      return;
    }
    System.out.println(" has no result");
    con.close();
  }

  @Override
  public void tearDown() throws Exception {
    if (this.tableName1 != null) {
      try {
        Connection con = dataSource.getConnection("myclearpool1");
        Statement st = con.createStatement();
        st.execute("drop table " + this.tableName1);
      } catch (SQLException e) {
        System.out.println(e.getMessage());
      }
    }
    if (this.tableName2 != null) {
      try {
        Connection con = dataSource.getConnection("myclearpool2");
        Statement st = con.createStatement();
        st.execute("drop table " + this.tableName2);
      } catch (SQLException e) {
        System.out.println(e.getMessage());
      }
    }
    dataSource.close();
  }
}
