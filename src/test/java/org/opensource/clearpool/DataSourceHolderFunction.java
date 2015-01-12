package org.opensource.clearpool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;
import org.opensource.clearpool.datasource.DataSourceHolder;

import com.alibaba.druid.mock.MockConnection;

public class DataSourceHolderFunction extends TestCase {
  private ClearPoolDataSource dataSource = new ClearPoolDataSource();

  @Override
  public void setUp() throws Exception {
    Map<String, CommonDataSource> dataSourceMap = new HashMap<String, CommonDataSource>();
    // just use the method
    new MockDataSource().getParentLogger();
    dataSourceMap.put("testholder", new MockDataSource());
    DataSourceHolder.setDataSourceMap(dataSourceMap);
    this.dataSource.initPath("clearpool/clearpool-test-holder.xml");
  }

  public void test_clearPool() throws Exception {
    Connection conn = this.dataSource.getConnection();
    System.out.println(conn);
  }

  @Override
  public void tearDown() throws Exception {
    this.dataSource.destory();
  }

  private static class MockDataSource implements DataSource {

    @Override
    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {}

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {}

    @Override
    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
    }

    @Override
    public Connection getConnection() throws SQLException {
      return new MockConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
      return new MockConnection();
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
    }
  }
}
