package org.opensource.clearpool.log;

import org.opensource.clearpool.log.impl.PoolJdk14Logger;
import org.opensource.clearpool.log.impl.PoolMockLogger;

/**
 * This log factory depend on commons-logging.jar.We use {@link PoolMockLogger}
 * to replace the log if we set the log unable or we don't have
 * commons-logging.jar.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class PoolLogFactory {
	public static final String PRIORITY_KEY = "priority";
	public static final String TCCL_KEY = "use_tccl";
	public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";
	public static final String FACTORY_DEFAULT = "org.apache.commons.logging.impl.LogFactoryImpl";
	public static final String FACTORY_PROPERTIES = "commons-logging.properties";
	public static final String DIAGNOSTICS_DEST_PROPERTY = "org.apache.commons.logging.diagnostics.dest";
	public static final String HASHTABLE_IMPLEMENTATION_PROPERTY = "org.apache.commons.logging.LogFactory.HashtableImpl";

	public static final String LOG_UNABLE = "org.clearpool.log.unable";

	private static final String CLAZZ = "org.apache.commons.logging.LogFactory";

	private static boolean logUnable;

	private static boolean commonsLogExist;

	private LogFactoryAdapter logFactoryAdapter;

	static {
		logUnable = Boolean.getBoolean(LOG_UNABLE);
		if (!logUnable) {
			try {
				Class.forName(CLAZZ);
				commonsLogExist = true;
			} catch (ClassNotFoundException e) {
				PoolLog log = new PoolJdk14Logger(
						PoolLogFactory.class.getName());
				log.info("\"" + CLAZZ + "\" is not existed.");
			}
		}
	}

	public PoolLogFactory(LogFactoryAdapter logFactoryAdapter) {
		this.logFactoryAdapter = logFactoryAdapter;
	}

	public Object getAttribute(String name) {
		if (this.logFactoryAdapter == null) {
			return null;
		}
		return this.logFactoryAdapter.getAttribute(name);
	}

	public String[] getAttributeNames() {
		if (this.logFactoryAdapter == null) {
			return null;
		}
		return this.logFactoryAdapter.getAttributeNames();
	}

	public PoolLog getInstance(Class<?> clazz) {
		if (this.logFactoryAdapter == null) {
			return null;
		}
		return this.logFactoryAdapter.getInstance(clazz);
	}

	public PoolLog getInstance(String name) {
		if (this.logFactoryAdapter == null) {
			return null;
		}
		return this.logFactoryAdapter.getInstance(name);
	}

	public void release() {
		if (this.logFactoryAdapter != null) {
			this.logFactoryAdapter.release();
		}
	}

	public void removeAttribute(String name) {
		if (this.logFactoryAdapter != null) {
			this.logFactoryAdapter.removeAttribute(name);
		}
	}

	public void setAttribute(String name, Object value) {
		if (this.logFactoryAdapter != null) {
			this.logFactoryAdapter.setAttribute(name, value);
		}
	}

	public static PoolLogFactory getFactory() {
		if (logUnable || !commonsLogExist) {
			return new PoolLogFactory(null);
		}
		LogFactoryAdapter logFactoryAdapter = LogFactoryAdapter.getFactory();
		return new PoolLogFactory(logFactoryAdapter);
	}

	public static PoolLog getLog(Class<?> clazz) {
		if (logUnable || !commonsLogExist) {
			return new PoolMockLogger();
		}
		return LogFactoryAdapter.getLog(clazz);
	}

	public static PoolLog getLog(String name) {
		if (logUnable || !commonsLogExist) {
			return new PoolMockLogger();
		}
		return LogFactoryAdapter.getLog(name);
	}

	public static void release(ClassLoader classLoader) {
		if (logUnable || !commonsLogExist) {
			return;
		}
		LogFactoryAdapter.release(classLoader);
	}

	public static void releaseAll() {
		if (logUnable || !commonsLogExist) {
			return;
		}
		LogFactoryAdapter.releaseAll();
	}

	public static String objectId(Object o) {
		return LogFactoryAdapter.objectId(o);
	}
}
