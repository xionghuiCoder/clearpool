package org.opensource.clearpool.configuration.console;

import java.util.HashMap;
import java.util.Map;

import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Console {
	public final static String PORT = "port";
	final static String SECURITY = "security";

	private int port = 8082;
	private Map<String, String> securityMap = new HashMap<String, String>();

	public void parse(Element element) {
		NodeList children = element.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				Element child = (Element) childNode;
				String nodeName = child.getNodeName();
				if (PORT.equals(nodeName)) {
					String nodeValue = child.getTextContent().trim();
					int port = (Integer.valueOf(nodeValue));
					this.setPort(port);
				} else if (SECURITY.equals(nodeName)) {
					Security security = new Security();
					security.parse(child);
					if (this.securityMap.put(security.getUser(),
							security.getPassword()) != null) {
						throw new ConnectionPoolXMLParseException(Security.USER
								+ " in " + Console.SECURITY + " repeat");
					}
				}
			}
		}
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		if (port < 0 || port > 0xFFFF) {
			throw new ConnectionPoolXMLParseException(Console.PORT + " " + port
					+ " is out of range: ");
		}
		this.port = port;
	}

	public Map<String, String> getSecurityMap() {
		return this.securityMap;
	}

	public void setSecurityMap(Map<String, String> securityMap) {
		if (securityMap == null) {
			securityMap = new HashMap<String, String>();
		}
		this.securityMap = securityMap;
	}
}
