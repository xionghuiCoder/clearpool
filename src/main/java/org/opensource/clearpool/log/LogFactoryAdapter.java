package org.opensource.clearpool.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensource.clearpool.log.impl.PoolLogImpl;

/**
 * This class is used to invoke LogFactory if LogFactory could be imported.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
class LogFactoryAdapter {
  private LogFactory logFactory;

  private LogFactoryAdapter(LogFactory logFactory) {
    this.logFactory = logFactory;
  }

  public Object getAttribute(String name) {
    if (this.logFactory == null) {
      return null;
    }
    return this.logFactory.getAttribute(name);
  }

  public String[] getAttributeNames() {
    if (this.logFactory == null) {
      return null;
    }
    return this.logFactory.getAttributeNames();
  }

  public PoolLog getInstance(Class<?> clazz) {
    if (this.logFactory == null) {
      return null;
    }
    Log log = this.logFactory.getInstance(clazz);
    PoolLog poolLog = new PoolLogImpl(log);
    return poolLog;
  }

  public PoolLog getInstance(String name) {
    if (this.logFactory == null) {
      return null;
    }
    Log log = this.logFactory.getInstance(name);
    PoolLog poolLog = new PoolLogImpl(log);
    return poolLog;
  }

  public void release() {
    if (this.logFactory != null) {
      this.logFactory.release();
    }
  }

  public void removeAttribute(String name) {
    if (this.logFactory != null) {
      this.logFactory.removeAttribute(name);
    }
  }

  public void setAttribute(String name, Object value) {
    if (this.logFactory != null) {
      this.logFactory.setAttribute(name, value);
    }
  }

  public static LogFactoryAdapter getFactory() {
    LogFactory factory = LogFactory.getFactory();
    return new LogFactoryAdapter(factory);
  }

  public static PoolLog getLog(Class<?> clazz) {
    Log log = LogFactory.getLog(clazz);
    PoolLog poolLog = new PoolLogImpl(log);
    return poolLog;
  }

  public static PoolLog getLog(String name) {
    Log log = LogFactory.getLog(name);
    PoolLog poolLog = new PoolLogImpl(log);
    return poolLog;
  }

  public static void release(ClassLoader classLoader) {
    LogFactory.release(classLoader);
  }

  public static void releaseAll() {
    LogFactory.releaseAll();
  }

  public static String objectId(Object o) {
    if (o == null) {
      return "null";
    } else {
      return o.getClass().getName() + "@" + System.identityHashCode(o);
    }
  }
}
