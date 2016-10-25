package com.github.xionghuicoder.clearpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.druid.mock.MockConnection;
import com.alibaba.druid.mock.MockDriver;

public class MockTestDriver extends MockDriver {
  public static final String CLASS = MockTestDriver.class.getName();
  public static final String URL = "jdbc:test:mock";

  public static AtomicLong physicalCon = new AtomicLong();

  @Override
  public boolean acceptsURL(String url) throws SQLException {
    if (url.startsWith("jdbc:test:")) {
      return true;
    }
    return super.acceptsURL(url);
  }

  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    physicalCon.incrementAndGet();
    return new MockConnection(this, "jdbc:mock:case", info);
  }
}
