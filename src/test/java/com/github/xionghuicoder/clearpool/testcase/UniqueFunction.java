package com.github.xionghuicoder.clearpool.testcase;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.github.xionghuicoder.clearpool.MockTestDriver;
import com.github.xionghuicoder.clearpool.core.ClearpoolDataSource;
import com.github.xionghuicoder.clearpool.core.ConfigurationVO;
import com.github.xionghuicoder.clearpool.security.SecretImpl;

import junit.framework.TestCase;

public class UniqueFunction extends TestCase {
  private ClearpoolDataSource dataSource = new ClearpoolDataSource();

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
    vo.setTestQuerySql("select 1");
    vo.setShowSql(true);
    this.dataSource.initVO(vo);
  }

  @Test
  public void testClearpool() throws Exception {
    CountDownLatch endLatch = new CountDownLatch(10);
    this.startThreads(endLatch, 10, 0);
    endLatch.await();
  }

  private void startThreads(final CountDownLatch endLatch, int count, final int order) {
    for (int i = 0; i < count; i++) {
      Thread thread = new Thread() {
        @Override
        public void run() {
          for (int j = 0; j < 3; j++) {
            try {
              Connection conn = UniqueFunction.this.dataSource.getConnection();
              conn.setReadOnly(true);
              Statement s = conn.createStatement();
              s.execute("select 1 from table;");
              s.close();
              conn.close();
            } catch (Exception e) {
              e.printStackTrace();
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
