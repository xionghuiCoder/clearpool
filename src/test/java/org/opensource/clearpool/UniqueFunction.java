package org.opensource.clearpool;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;

/**
 * Note: replace file-path which is in clearpool-test-unique.xml with your xml's real absolute path
 * please.
 * 
 * @author xionghui
 * @date 24.09.2014
 * @version 1.0
 */
public class UniqueFunction extends TestCase {
  private final static int TIME = 10;

  private ClearPoolDataSource dataSource = new ClearPoolDataSource();

  private volatile boolean[] signs = new boolean[10];

  @Override
  public void setUp() throws Exception {
    this.dataSource.initPath("clearpool/clearpool-test-unique.xml");
    Thread.sleep(TIME * 1000);
  }

  public void test_clearPool() throws Exception {
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(100);
    this.startThreads(startLatch, endLatch, 10, 0);
    startLatch.countDown();
    System.out.println("start 10 threads");
    // add 10 thread every TIME(s)
    for (int i = 1; i < 10; i++) {
      Thread.sleep(TIME * 1000);
      startLatch = new CountDownLatch(1);
      this.startThreads(startLatch, endLatch, 10, i);
      startLatch.countDown();
      System.out.println("start " + (1 + i) * 10 + " threads");
    }
    // remove 10 thread every TIME(s)
    for (int i = 0; i < 10; i++) {
      Thread.sleep(TIME * 1000);
      this.signs[i] = true;
      System.out.println("left " + (9 - i) * 10 + " threads");
    }
    Thread.sleep(TIME * 1000);
    endLatch.await();
  }

  private void startThreads(final CountDownLatch startLatch, final CountDownLatch endLatch,
      int count, final int order) {
    for (int i = 0; i < count; i++) {
      Thread thread = new Thread() {
        @Override
        public void run() {
          try {
            startLatch.await();
            for (;;) {
              if (UniqueFunction.this.signs[order]) {
                break;
              }
              Connection conn = UniqueFunction.this.dataSource.getConnection();
              conn.setReadOnly(true);
              Statement s = conn.createStatement();
              s.execute("select 1 from table");
              s.close();
              conn.close();
            }
            endLatch.countDown();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
      thread.start();
    }
  }

  @Override
  public void tearDown() throws Exception {
    Thread.sleep(TIME * 1000);
    this.dataSource.destory();
  }
}
