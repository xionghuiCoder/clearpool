package org.opensource.clearpool.configuration.console;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.opensource.clearpool.configuration.XMLConfiguration;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;

public class Console {
	public final static String PORT = "port";
	final static String SECURITY = "security";

	private int port = 8082;
	private Map<String, String> securityMap = new HashMap<>();

	public void parse(XMLStreamReader reader) throws XMLStreamException {
		boolean repeatPort = false;
		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLStreamConstants.END_ELEMENT
					&& XMLConfiguration.CONSOLE == reader.getLocalName()) {
				break;
			}
			if (event != XMLStreamConstants.START_ELEMENT) {
				continue;
			}
			String parsing = reader.getLocalName();
			switch (parsing) {
			case PORT:
				if (repeatPort) {
					throw new ConnectionPoolXMLParseException(Console.PORT
							+ " repeat");
				}
				repeatPort = true;
				int port = (Integer.valueOf(reader.getElementText().trim()));
				this.setPort(port);
				break;
			case SECURITY:
				Security security = new Security();
				security.parse(reader);
				if (this.securityMap.put(security.getUser(),
						security.getPassword()) != null) {
					throw new ConnectionPoolXMLParseException(Security.USER
							+ " in " + Console.SECURITY + " repeat");
				}
				break;
			default:
				throw new ConnectionPoolXMLParseException(
						XMLConfiguration.CONSOLE + " contains illegal elements");
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
			securityMap = new HashMap<>();
		}
		this.securityMap = securityMap;
	}
}
