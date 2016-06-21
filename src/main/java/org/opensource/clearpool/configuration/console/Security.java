package org.opensource.clearpool.configuration.console;

import java.util.regex.Pattern;

import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Security {
  static final String USER = "user";
  private static final String PASSWORD = "password";

  private String user;
  private String password;

  public void parse(Element element) {
    NodeList children = element.getChildNodes();
    for (int i = 0, size = children.getLength(); i < size; i++) {
      Node childNode = children.item(i);
      if (childNode instanceof Element) {
        Element child = (Element) childNode;
        String nodeName = child.getNodeName();
        String nodeValue = child.getTextContent();
        if (USER.equals(nodeName)) {
          this.user = nodeValue;
          boolean rightUser = this.checkSecurityPattern(this.user);
          if (!rightUser) {
            throw new ConnectionPoolXMLParseException(
                "the pattern of " + Security.USER + " in " + Console.SECURITY + " is illegal");
          }
        } else if (PASSWORD.equals(nodeName)) {
          this.password = nodeValue;
          boolean rightPsd = this.checkSecurityPattern(this.password);
          if (!rightPsd) {
            throw new ConnectionPoolXMLParseException(
                "the pattern of " + Security.PASSWORD + " is illegal");
          }
        }
      }
    }
    this.validate();
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
   * We throw a {@link ConnectionPoolException} if user or password is empty or null.
   */
  private void validate() {
    if (this.user == null) {
      throw new ConnectionPoolXMLParseException(Security.USER + " shouldn't be null");
    }
    if (this.password == null) {
      throw new ConnectionPoolXMLParseException(Security.PASSWORD + " shouldn't be null");
    }
  }

  public String getUser() {
    return this.user;
  }

  public String getPassword() {
    return this.password;
  }
}
