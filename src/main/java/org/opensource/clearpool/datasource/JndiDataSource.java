package org.opensource.clearpool.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.opensource.clearpool.configuration.XMLConfiguration;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;

public class JndiDataSource extends CommonDataSource {
	private final static String JNDI_NAME = "jndi-name";
	private final static String PROP = "prop";
	private final static String KEY = "key";

	private DataSource ds = null;

	private String jndiName;
	private Hashtable<String, String> environment = new Hashtable<>();

	public void parse(XMLStreamReader reader) throws XMLStreamException {
		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLStreamConstants.END_ELEMENT
					&& XMLConfiguration.JNDI == reader.getLocalName()) {
				break;
			}
			if (event != XMLStreamConstants.START_ELEMENT) {
				continue;
			}
			String parsing = reader.getLocalName();
			switch (parsing) {
			case JNDI_NAME:
				if (this.jndiName != null) {
					throw new ConnectionPoolException(JndiDataSource.JNDI_NAME
							+ " repeat");
				}
				this.jndiName = reader.getElementText().trim();
				break;
			case PROP:
				String key = reader.getAttributeValue(null, KEY);
				if (key == null) {
					throw new ConnectionPoolXMLParseException(PROP
							+ " should has a " + KEY);
				}
				if (this.environment.contains(key)) {
					throw new ConnectionPoolException(JndiDataSource.KEY
							+ " repeat");
				}
				this.environment.put(key, reader.getElementText().trim());
				break;
			default:
				throw new ConnectionPoolXMLParseException(XMLConfiguration.JNDI
						+ " contains illegal elements");
			}
		}
		this.volidate();
	}

	/**
	 * We throw a {@link ConnectionPoolXMLParseException} if jndi name is null.
	 */
	private void volidate() {
		if (this.jndiName == null) {
			throw new ConnectionPoolXMLParseException(JNDI_NAME + " is illegal");
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (this.ds == null) {
			try {
				Context initial = new InitialContext(this.environment);
				this.ds = (DataSource) initial.lookup(this.jndiName);
			} catch (NamingException e) {
				throw new ConnectionPoolException(e);
			}
		}
		Connection con = this.ds.getConnection();
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
				+ ((this.jndiName == null) ? 0 : this.jndiName.hashCode());
		result = prime
				* result
				+ ((this.environment == null) ? 0 : this.environment.hashCode());
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
		if (obj instanceof JndiDataSource) {
			JndiDataSource other = (JndiDataSource) obj;
			if (this.jndiName == null ? other.jndiName == null : this.jndiName
					.equals(other.jndiName)) {
				if (this.environment == null ? other.environment == null
						: this.environment.equals(other.environment)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("name=");
		builder.append(String.valueOf(this.jndiName));
		builder.append("\n");
		builder.append("environment:");
		builder.append(String.valueOf(this.environment));
		return builder.toString();
	}
}