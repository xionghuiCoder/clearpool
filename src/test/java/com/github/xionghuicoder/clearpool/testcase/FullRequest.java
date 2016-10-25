package com.github.xionghuicoder.clearpool.testcase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.github.xionghuicoder.clearpool.MockTestDriver;
import com.github.xionghuicoder.clearpool.core.ClearpoolDataSource;
import com.github.xionghuicoder.clearpool.core.ConfigurationVO;
import com.github.xionghuicoder.clearpool.security.SecretImpl;

import junit.framework.TestCase;

public class FullRequest extends TestCase {
  private volatile boolean sign = false;

  private ClearpoolDataSource dataSource = new ClearpoolDataSource();

  private final static int TIME = 1;

  @Override
  public void setUp() throws Exception {
    this.dataSource.setPort(8085);
    Map<String, String> securityMap = new HashMap<String, String>();
    securityMap.put("1", "1");
    this.dataSource.setSecurityMap(securityMap);

    ConfigurationVO vo = new ConfigurationVO();
    vo.setName("myclearpool");
    vo.setDriverClassName(MockTestDriver.class.getName());
    vo.setUrl("jdbc:mock:case");
    vo.setUsername("1");
    vo.setPassword("EfGEu9LQRub8S7JmjmHjEQ==");
    vo.setSecurityClassName(SecretImpl.class.getName());

    vo.setCorePoolSize(10);
    vo.setMaxPoolSize(50);
    vo.setAcquireIncrement(10);
    vo.setAcquireRetryTimes(3);
    vo.setLimitIdleTime(5);
    vo.setTestBeforeUse(true);
    vo.setKeepTestPeriod(20);
    vo.setTestQuerySql("select 1");
    vo.setShowSql(false);

    this.dataSource.initVO(vo);
  }

  @Test
  public void testClearpool() throws Exception {
    CountDownLatch endLatch = new CountDownLatch(50);
    this.startThreads(endLatch, 50);
    Thread.sleep(TIME);
    this.sign = true;
    endLatch.await();
  }

  private void startThreads(final CountDownLatch endLatch, int count) {
    for (int i = 0; i < count; i++) {
      Thread thread = new Thread() {
        @Override
        public void run() {
          for (;;) {
            Connection conn = null;
            try {
              conn = FullRequest.this.dataSource.getConnection();
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                // swallow
              }
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              if (conn != null) {
                try {
                  conn.close();
                } catch (SQLException e) {
                  // swallow
                }
              }
            }
            if (FullRequest.this.sign) {
              break;
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
