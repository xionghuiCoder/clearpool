package org.opensource.clearpool.util;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * This class come from alibaba.druid
 */
public class GCUtil {

  private GCUtil() {
  }

  public static long getYoungGC() {
    try {
      MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
      ObjectName objectName;
      if (mbeanServer.isRegistered(new ObjectName("java.lang:type=GarbageCollector,name=ParNew"))) {
        objectName = new ObjectName("java.lang:type=GarbageCollector,name=ParNew");
      } else if (mbeanServer.isRegistered(new ObjectName(
          "java.lang:type=GarbageCollector,name=Copy"))) {
        objectName = new ObjectName("java.lang:type=GarbageCollector,name=Copy");
      } else {
        objectName = new ObjectName("java.lang:type=GarbageCollector,name=PS Scavenge");
      }

      return (Long) mbeanServer.getAttribute(objectName, "CollectionCount");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static long getFullGC() {
    try {
      MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
      ObjectName objectName;

      if (mbeanServer.isRegistered(new ObjectName(
          "java.lang:type=GarbageCollector,name=ConcurrentMarkSweep"))) {
        objectName = new ObjectName("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep");
      } else if (mbeanServer.isRegistered(new ObjectName(
          "java.lang:type=GarbageCollector,name=MarkSweepCompact"))) {
        objectName = new ObjectName("java.lang:type=GarbageCollector,name=MarkSweepCompact");
      } else {
        objectName = new ObjectName("java.lang:type=GarbageCollector,name=PS MarkSweep");
      }
      return (Long) mbeanServer.getAttribute(objectName, "CollectionCount");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
