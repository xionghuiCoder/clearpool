package org.opensource.clearpool.datasource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCDataSource extends AbstractDataSource {
	private String clazz;
	private String url;

	private Driver driver;
	private Properties connectProperties = new Properties();

	public JDBCDataSource(String clazz, String url, Driver driver,
			Properties connectProperties) {
		this.clazz = clazz;
		this.url = url;
		this.driver = driver;
		this.connectProperties = connectProperties;
	}

	public String getClazz() {
		return this.clazz;
	}

	public String getUrl() {
		return this.url;
	}

	public Driver getDriver() {
		return this.driver;
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
		result = prime * result + this.connectProperties.hashCode();
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
				return this.connectProperties.equals(other.connectProperties);
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("url=");
		builder.append(String.valueOf(this.url));
		builder.append("\n");
		builder.append("Properties=");
		builder.append(this.connectProperties);
		return builder.toString();
	}
}
