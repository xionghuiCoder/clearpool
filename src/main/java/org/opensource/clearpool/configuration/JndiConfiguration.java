package org.opensource.clearpool.configuration;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.CommonDataSource;

import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

	public static CommonDataSource parse(Element element) {
		String jndiName = null;
		Hashtable<String, String> environment = new Hashtable<String, String>();

		NodeList children = element.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				Element child = (Element) childNode;
				String nodeName = child.getNodeName();
				String nodeValue = child.getTextContent().trim();
				if (JNDI_NAME.equals(nodeName)) {
					if (jndiName != null) {
						throw new ConnectionPoolException(
								JndiConfiguration.JNDI_NAME + " repeat");
					}
					jndiName = nodeValue;
				} else if (PROP.equals(nodeName)) {
					String key = child.getAttributeNode(KEY).getNodeValue();
					environment.put(key, nodeValue);
				}
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