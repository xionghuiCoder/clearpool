package com.github.xionghuicoder.clearpool.testcase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.github.xionghuicoder.clearpool.MockTestDriver;
import com.github.xionghuicoder.clearpool.core.ClearpoolDataSource;
import com.github.xionghuicoder.clearpool.core.ConfigurationVO;

import junit.framework.TestCase;

public class DistributedFunction extends TestCase {
  private ClearpoolDataSource dataSource = new ClearpoolDataSource();

  private final static int TIME = 1;

  @Override
  public void setUp() throws Exception {
    this.dataSource.setPort(8085);
    Map<String, String> securityMap = new HashMap<String, String>();
    securityMap.put("1", "1");
    this.dataSource.setSecurityMap(securityMap);

    List<ConfigurationVO> voList = new ArrayList<ConfigurationVO>();
    ConfigurationVO vo1 = new ConfigurationVO();
    vo1.setName("myclearpool1");
    vo1.setDriverClassName(MockTestDriver.class.getName());
    vo1.setUrl("jdbc:mock:case");
    vo1.setCorePoolSize(10);
    vo1.setMaxPoolSize(50);
    vo1.setAcquireIncrement(10);
    vo1.setAcquireRetryTimes(3);
    vo1.setLimitIdleTime(5);
    vo1.setShowSql(true);
    voList.add(vo1);
    ConfigurationVO vo2 = new ConfigurationVO();
    vo2.setName("myclearpool2");
    vo2.setDriverClassName(MockTestDriver.class.getName());
    vo2.setUrl("jdbc:mock:case");
    vo2.setCorePoolSize(10);
    vo2.setMaxPoolSize(50);
    vo2.setAcquireIncrement(10);
    vo2.setAcquireRetryTimes(3);
    vo2.setLimitIdleTime(5);
    vo2.setShowSql(true);
    voList.add(vo2);
    this.dataSource.initVOList(voList);
  }

  @Test
  public void testClearpool() throws Exception {
    CountDownLatch endLatch = new CountDownLatch(10);
    this.startThreads(endLatch, 5, "myclearpool1");
    this.startThreads(endLatch, 5, "myclearpool2");
    Thread.sleep(TIME);
    endLatch.await();
  }

  private void startThreads(final CountDownLatch endLatch, int count, final String name) {
    for (int i = 0; i < count; i++) {
      Thread thread = new Thread() {
        @Override
        public void run() {
          for (int i = 0; i < 10; i++) {
            Connection conn = null;
            try {
              conn = DistributedFunction.this.dataSource.getConnection(name);
              PreparedStatement stmt =
                  conn.prepareStatement("select 1 from geek where name=? and age=?;");
              stmt.setString(1, "Bill Joy");
              stmt.setInt(2, 60);
              stmt.execute();
              stmt.close();
            } catch (Exception e) {
              // swallow
            } finally {
              if (conn != null) {
                try {
                  conn.close();
                } catch (Exception e) {
                  // swallow
                }
              }
            }
          }
          endLatch.countDown();
        }
      };
      thread.start();
    }
  }

  @Override
  public void tearDown() throws Exception {
    this.dataSource.close();
  }
}
