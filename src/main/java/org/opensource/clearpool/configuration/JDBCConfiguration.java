package org.opensource.clearpool.configuration;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.sql.CommonDataSource;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.opensource.clearpool.datasource.JDBCDataSource;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.opensource.clearpool.util.JdbcUtil;

/**
 * How to parse JDBC.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class JDBCConfiguration {
	private final static String CLASS = "class";
	private final static String URL = "url";
	private final static String USER = "user";
	private final static String PASSWORD = "password";

	public static CommonDataSource parse(XMLStreamReader reader)
			throws XMLStreamException {
		String clazz = null;
		String url = null;
		String user = null;
		String password = null;
		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLStreamConstants.END_ELEMENT
					&& XMLConfiguration.JDBC == reader.getLocalName()) {
				break;
			}
			if (event != XMLStreamConstants.START_ELEMENT) {
				continue;
			}
			String parsing = reader.getLocalName();
			if (CLASS.equals(parsing)) {
				clazz = reader.getElementText().trim();
			} else if (URL.equals(parsing)) {
				if (url != null) {
					throw new ConnectionPoolException(JDBCConfiguration.URL
							+ " repeat");
				}
				url = reader.getElementText().trim();
			} else if (USER.equals(parsing)) {
				if (user != null) {
					throw new ConnectionPoolException(JDBCConfiguration.USER
							+ " repeat");
				}
				user = reader.getElementText();
				boolean rightUser = checkSecurityPattern(user);
				if (!rightUser) {
					throw new ConnectionPoolXMLParseException("the pattern of "
							+ JDBCConfiguration.USER + " in "
							+ XMLConfiguration.JDBC + " is illegal");
				}
			} else if (PASSWORD.equals(parsing)) {
				if (password != null) {
					throw new ConnectionPoolException(
							JDBCConfiguration.PASSWORD + " repeat");
				}
				password = reader.getElementText();
				boolean rightPsd = checkSecurityPattern(password);
				if (!rightPsd) {
					throw new ConnectionPoolXMLParseException("the pattern of "
							+ JDBCConfiguration.PASSWORD + " in "
							+ XMLConfiguration.JDBC + " is illegal");
				}
			} else {
				throw new ConnectionPoolXMLParseException(XMLConfiguration.JDBC
						+ " contains illegal element: " + parsing);
			}
		}
		return getDataSource(clazz, url, user, password);
	}

	/**
	 * Check if the pattern of user and password is valid.
	 */
	private static boolean checkSecurityPattern(String value) {
		// note:the regex has a blank
		String regex = "[\\w" + " " + "]*";
		boolean right = Pattern.matches(regex, value);
		return right;
	}

	/**
	 * Get dataSource by JDBC.
	 */
	public static CommonDataSource getDataSource(String clazz, String url,
			String user, String password) {
		if (url == null) {
			throw new ConnectionPoolException(JDBCConfiguration.URL
					+ " is null");
		}
		Driver driver = null;
		try {
			if (clazz == null) {
				clazz = JdbcUtil.getDriverClassName(url);
			}
			driver = JdbcUtil.createDriver(clazz);
		} catch (SQLException e) {
			throw new ConnectionPoolException(e);
		}
		Properties connectProperties = new Properties();
		if (user != null) {
			connectProperties.put("user", user);
		}
		if (password != null) {
			connectProperties.put("password", password);
		}
		return new JDBCDataSource(clazz, url, driver, connectProperties);
	}
}
