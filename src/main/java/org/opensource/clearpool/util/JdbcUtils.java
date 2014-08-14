package org.opensource.clearpool.util;

import java.sql.Driver;
import java.sql.SQLException;

public final class JdbcUtils {
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	public static final String LOG4JDBC_DRIVER = "net.sf.log4jdbc.DriverSpy";
	public static final String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";
	public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	public static final String ALI_ORACLE_DRIVER = "com.alibaba.jdbc.AlibabaDriver";
	public static final String DB2_DRIVER = "COM.ibm.db2.jdbc.app.DB2Driver";
	public static final String H2_DRIVER = "org.h2.Driver";

	public static String getDriverClassName(String rawUrl) throws SQLException {
		if (rawUrl.startsWith("jdbc:derby:")) {
			return "org.apache.derby.jdbc.EmbeddedDriver";
		} else if (rawUrl.startsWith("jdbc:mysql:")) {
			return MYSQL_DRIVER;
		} else if (rawUrl.startsWith("jdbc:log4jdbc:")) {
			return LOG4JDBC_DRIVER;
		} else if (rawUrl.startsWith("jdbc:mariadb:")) {
			return MARIADB_DRIVER;
		} else if (rawUrl.startsWith("jdbc:oracle:")
				|| rawUrl.startsWith("JDBC:oracle:")) {
			return ORACLE_DRIVER;
		} else if (rawUrl.startsWith("jdbc:alibaba:oracle:")) {
			return ALI_ORACLE_DRIVER;
		} else if (rawUrl.startsWith("jdbc:microsoft:")) {
			return "com.microsoft.jdbc.sqlserver.SQLServerDriver";
		} else if (rawUrl.startsWith("jdbc:sqlserver:")) {
			return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		} else if (rawUrl.startsWith("jdbc:sybase:Tds:")) {
			return "com.sybase.jdbc2.jdbc.SybDriver";
		} else if (rawUrl.startsWith("jdbc:jtds:")) {
			return "net.sourceforge.jtds.jdbc.Driver";
		} else if (rawUrl.startsWith("jdbc:fake:")
				|| rawUrl.startsWith("jdbc:mock:")) {
			return "com.alibaba.druid.mock.MockDriver";
		} else if (rawUrl.startsWith("jdbc:postgresql:")) {
			return "org.postgresql.Driver";
		} else if (rawUrl.startsWith("jdbc:hsqldb:")) {
			return "org.hsqldb.jdbcDriver";
		} else if (rawUrl.startsWith("jdbc:db2:")) {
			return DB2_DRIVER;
		} else if (rawUrl.startsWith("jdbc:sqlite:")) {
			return "org.sqlite.JDBC";
		} else if (rawUrl.startsWith("jdbc:ingres:")) {
			return "com.ingres.jdbc.IngresDriver";
		} else if (rawUrl.startsWith("jdbc:h2:")) {
			return H2_DRIVER;
		} else if (rawUrl.startsWith("jdbc:mckoi:")) {
			return "com.mckoi.JDBCDriver";
		} else if (rawUrl.startsWith("jdbc:cloudscape:")) {
			return "COM.cloudscape.core.JDBCDriver";
		} else if (rawUrl.startsWith("jdbc:informix-sqli:")) {
			return "com.informix.jdbc.IfxDriver";
		} else if (rawUrl.startsWith("jdbc:timesten:")) {
			return "com.timesten.jdbc.TimesTenDriver";
		} else if (rawUrl.startsWith("jdbc:as400:")) {
			return "com.ibm.as400.access.AS400JDBCDriver";
		} else if (rawUrl.startsWith("jdbc:sapdb:")) {
			return "com.sap.dbtech.jdbc.DriverSapDB";
		} else if (rawUrl.startsWith("jdbc:JSQLConnect:")) {
			return "com.jnetdirect.jsql.JSQLDriver";
		} else if (rawUrl.startsWith("jdbc:JTurbo:")) {
			return "com.newatlanta.jturbo.driver.Driver";
		} else if (rawUrl.startsWith("jdbc:firebirdsql:")) {
			return "org.firebirdsql.jdbc.FBDriver";
		} else if (rawUrl.startsWith("jdbc:interbase:")) {
			return "interbase.interclient.Driver";
		} else if (rawUrl.startsWith("jdbc:pointbase:")) {
			return "com.pointbase.jdbc.jdbcUniversalDriver";
		} else if (rawUrl.startsWith("jdbc:edbc:")) {
			return "ca.edbc.jdbc.EdbcDriver";
		} else if (rawUrl.startsWith("jdbc:mimer:multi1:")) {
			return "com.mimer.jdbc.Driver";
		} else {
			throw new SQLException("unkow jdbc driver : " + rawUrl);
		}
	}

	public static Driver createDriver(String driverClassName)
			throws SQLException {
		return createDriver(null, driverClassName);
	}

	public static Driver createDriver(ClassLoader classLoader,
			String driverClassName) throws SQLException {
		Class<?> clazz = null;
		if (classLoader != null) {
			try {
				clazz = classLoader.loadClass(driverClassName);
			} catch (ClassNotFoundException e) {
				// skip
			}
		}

		if (clazz == null) {
			try {
				ClassLoader contextLoader = Thread.currentThread()
						.getContextClassLoader();
				if (contextLoader != null) {
					clazz = contextLoader.loadClass(driverClassName);
				}
			} catch (ClassNotFoundException e) {
				// skip
			}
		}

		if (clazz == null) {
			try {
				clazz = Class.forName(driverClassName);
			} catch (ClassNotFoundException e) {
				throw new SQLException(e.getMessage(), e);
			}
		}

		try {
			return (Driver) clazz.newInstance();
		} catch (IllegalAccessException e) {
			throw new SQLException(e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new SQLException(e.getMessage(), e);
		}
	}
}
