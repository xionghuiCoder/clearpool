package org.opensource.clearpool.configuration;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.CommonDataSource;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;

/**
 * How to parse jndi.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class JndiConfiguration {
	private final static String JNDI_NAME = "jndi-name";
	private final static String PROP = "prop";
	private final static String KEY = "key";

	public static CommonDataSource parse(XMLStreamReader reader)
			throws XMLStreamException {
		String jndiName = null;
		Hashtable<String, String> environment = new Hashtable<>();
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
				if (jndiName != null) {
					throw new ConnectionPoolException(
							JndiConfiguration.JNDI_NAME + " repeat");
				}
				jndiName = reader.getElementText().trim();
				break;
			case PROP:
				String key = reader.getAttributeValue(null, KEY);
				if (key == null) {
					throw new ConnectionPoolXMLParseException(PROP
							+ " should has a " + KEY);
				}
				if (environment.contains(key)) {
					throw new ConnectionPoolException(JndiConfiguration.KEY
							+ " repeat");
				}
				environment.put(key, reader.getElementText().trim());
				break;
			default:
				throw new ConnectionPoolXMLParseException(XMLConfiguration.JNDI
						+ " contains illegal element: " + parsing);
			}
		}
		return getDataSource(jndiName, environment);
	}

	/**
	 * Get dataSource by jndiName and environment.
	 */
	private static CommonDataSource getDataSource(String jndiName,
			Hashtable<String, String> environment) {
		if (jndiName == null) {
			throw new ConnectionPoolXMLParseException(JNDI_NAME + " is illegal");
		}
		CommonDataSource ds = null;
		try {
			Context initial = new InitialContext(environment);
			ds = (CommonDataSource) initial.lookup(jndiName);
		} catch (NamingException e) {
			throw new ConnectionPoolException(e);
		}
		return ds;
	}
}