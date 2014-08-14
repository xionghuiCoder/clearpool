package org.opensource.clearpool.datasource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.opensource.clearpool.configuration.XMLConfiguration;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.opensource.clearpool.util.JdbcUtils;

public class JDBCDataSource extends CommonDataSource {
	private final static String CLASS = "class";
	private final static String URL = "url";
	private final static String USER = "user";
	private final static String PASSWORD = "password";

	private String clazz;
	private String url;
	private String user;
	private String password;

	private Driver driver;
	private Properties connectProperties = new Properties();

	public String getClazz() {
		return this.clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void parse(XMLStreamReader reader) throws XMLStreamException {
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
			switch (parsing) {
			case CLASS:
				this.setClazz(reader.getElementText().trim());
				break;
			case URL:
				if (this.url != null) {
					throw new ConnectionPoolException(JDBCDataSource.URL
							+ " repeat");
				}
				this.url = reader.getElementText().trim();
				break;
			case USER:
				if (this.user != null) {
					throw new ConnectionPoolException(JDBCDataSource.USER
							+ " repeat");
				}
				this.user = reader.getElementText();
				boolean rightUser = this.checkSecurityPattern(this.user);
				if (!rightUser) {
					throw new ConnectionPoolXMLParseException("the pattern of "
							+ JDBCDataSource.USER + " in "
							+ XMLConfiguration.JDBC + " is illegal");
				}
				break;
			case PASSWORD:
				if (this.password != null) {
					throw new ConnectionPoolException(JDBCDataSource.PASSWORD
							+ " repeat");
				}
				this.password = reader.getElementText();
				boolean rightPsd = this.checkSecurityPattern(this.password);
				if (!rightPsd) {
					throw new ConnectionPoolXMLParseException("the pattern of "
							+ JDBCDataSource.PASSWORD + " in "
							+ XMLConfiguration.JDBC + " is illegal");
				}
				break;
			default:
				throw new ConnectionPoolXMLParseException(XMLConfiguration.JDBC
						+ " contains illegal elements");
			}
		}
		this.volidate();
	}

	/**
	 * check if the pattern of user and password is valid.
	 */
	private boolean checkSecurityPattern(String value) {
		// note:the regex has a blank
		String regex = "[\\w" + " " + "]*";
		boolean right = Pattern.matches(regex, value);
		return right;
	}

	/**
	 * We throw a {@link ConnectionPoolException} if url is null.And we init
	 * driver if legal.
	 * 
	 * @throws SQLException
	 */
	public void volidate() {
		if (this.url == null) {
			throw new ConnectionPoolException(JDBCDataSource.URL + " is null");
		}
		try {
			if (this.clazz == null) {
				this.clazz = JdbcUtils.getDriverClassName(this.url);
			}
			this.driver = JdbcUtils.createDriver(this.clazz);
		} catch (SQLException e) {
			throw new ConnectionPoolException(e);
		}
		if (this.user != null) {
			this.connectProperties.put("user", this.user);
		}
		if (this.password != null) {
			this.connectProperties.put("password", this.password);
		}

	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection con = this.driver.connect(this.url, this.connectProperties);
		return con;
	}

	/**
	 * the {@link #hashCode} is base on <<Effective Java>> suggesting.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		result = prime * result
				+ ((this.url == null) ? 0 : this.url.hashCode());
		result = prime * result
				+ ((this.password == null) ? 0 : this.password.hashCode());
		result = prime * result
				+ ((this.user == null) ? 0 : this.user.hashCode());
		return result;
	}

	/**
	 * the {@link #equals(Object)} is base on <<Effective Java>> suggesting.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof JDBCDataSource) {
			JDBCDataSource other = (JDBCDataSource) obj;
			if (this.url == null ? other.url == null : this.url
					.equals(other.url)) {
				if (this.password == null ? other.password == null
						: this.password.equals(other.password)) {
					if (this.user == null ? other.user == null : this.user
							.equals(other.user)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("url=");
		builder.append(String.valueOf(this.url));
		builder.append("\n");
		builder.append("user=");
		builder.append(String.valueOf(this.user));
		builder.append("\n");
		builder.append("password=");
		builder.append(String.valueOf(this.password));
		return builder.toString();
	}
}
