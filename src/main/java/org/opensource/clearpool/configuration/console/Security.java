package org.opensource.clearpool.configuration.console;

import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;

public class Security {
	static final String USER = "user";
	private static final String PASSWORD = "password";

	private String user;
	private String password;

	public void parse(XMLStreamReader reader) throws XMLStreamException {
		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLStreamConstants.END_ELEMENT
					&& Console.SECURITY == reader.getLocalName()) {
				break;
			}
			if (event != XMLStreamConstants.START_ELEMENT) {
				continue;
			}
			String parsing = reader.getLocalName();
			switch (parsing) {
			case USER:
				if (this.user != null) {
					throw new ConnectionPoolXMLParseException(Security.USER
							+ " is repeat");
				}
				this.user = reader.getElementText();
				boolean rightUser = this.checkSecurityPattern(this.user);
				if (!rightUser) {
					throw new ConnectionPoolXMLParseException("the pattern of "
							+ Security.USER + " in " + Console.SECURITY
							+ " is illegal");
				}
				break;
			case PASSWORD:
				if (this.password != null) {
					throw new ConnectionPoolXMLParseException(Security.PASSWORD
							+ " in " + Console.SECURITY + " is repeat");
				}
				this.password = reader.getElementText();
				boolean rightPsd = this.checkSecurityPattern(this.password);
				if (!rightPsd) {
					throw new ConnectionPoolXMLParseException("the pattern of "
							+ Security.PASSWORD + " is illegal");
				}
				break;
			default:
				throw new ConnectionPoolXMLParseException(Console.SECURITY
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
		String regex = "[\\w" + " " + "]+";
		boolean right = Pattern.matches(regex, value);
		return right;
	}

	/**
	 * We throw a {@link ConnectionPoolException} if user or password is empty
	 * or null.
	 */
	private void volidate() {
		if (this.user == null) {
			throw new ConnectionPoolXMLParseException(Security.USER
					+ " shouldn't be null");
		}
		if (this.password == null) {
			throw new ConnectionPoolXMLParseException(Security.PASSWORD
					+ " shouldn't be null");
		}
	}

	public String getUser() {
		return this.user;
	}

	public String getPassword() {
		return this.password;
	}
}
