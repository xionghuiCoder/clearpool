package org.opensource.clearpool.configuration;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.sql.CommonDataSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opensource.clearpool.datasource.JDBCDataSource;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;
import org.opensource.clearpool.security.Secret;
import org.opensource.clearpool.security.SecretAES;
import org.opensource.clearpool.util.JdbcUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class used to parse JDBC.
 *
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class JDBCConfiguration {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(JDBCConfiguration.class);

  private final static String CLASS = "class";
  private final static String URL = "url";
  private final static String USER = "user";
  private final static String PASSWORD = "password";
  private final static String SECURITY_CLASS = "security-class";
  private final static String FILE_PATH = "file-path";
  private final static String STATUS = "status";
  private final static String ENCRYPT = "encrypt";
  private final static String DECRYPT = "decrypt";

  public static CommonDataSource parse(Element element, Document document, String path) {
    String clazz = null;
    String url = null;
    String user = null;
    String password = null;
    String securityClass = null;
    String filePath = null;

    NodeList children = element.getChildNodes();
    for (int i = 0, size = children.getLength(); i < size; i++) {
      Node childNode = children.item(i);
      if (childNode instanceof Element) {
        Element child = (Element) childNode;
        String nodeName = child.getNodeName();
        String nodeValue = child.getTextContent();
        if (CLASS.equals(nodeName)) {
          clazz = nodeValue.trim();
        } else if (URL.equals(nodeName)) {
          url = nodeValue.trim();
        } else if (USER.equals(nodeName)) {
          user = nodeValue;
          checkUserPattern(user);
        } else if (PASSWORD.equals(nodeName)) {
          password = nodeValue;
        } else if (SECURITY_CLASS.equals(nodeName)) {
          securityClass = nodeValue.trim();
        } else if (FILE_PATH.equals(nodeName)) {
          filePath = nodeValue.trim();
        } else if (STATUS.equals(nodeName)) {
          String status = nodeValue.trim();
          password = handlerPassword(securityClass, filePath, status, password, child, document);
        }
      }
    }
    return getDataSource(clazz, url, user, password);
  }

  /**
   * Check if the pattern of user is valid.
   */
  private static void checkUserPattern(String value) {
    // note:the regex has a blank
    String regex = "[\\w" + " " + "]*";
    boolean right = Pattern.matches(regex, value);
    if (!right) {
      throw new ConnectionPoolXMLParseException(value + " of " + USER + " is illegal");
    }
  }

  /**
   * Get dataSource by JDBC.
   */
  public static CommonDataSource getDataSource(String clazz, String url, String user,
      String password) {
    if (url == null) {
      throw new ConnectionPoolException(JDBCConfiguration.URL + " is null");
    }
    Driver driver;
    try {
      if (clazz == null) {
        clazz = JdbcUtil.getDriverClassName(url);
      }
      driver = JdbcUtil.createDriver(clazz);
    } catch (SQLException e) {
      LOGGER.error("getDataSource error: ", e);
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

  /**
   * Handle the password
   */
  private static String handlerPassword(String securityClass, String filePath, String status,
      String password, Element element, Document document) {
    if (password == null) {
      throw new ConnectionPoolException(PASSWORD + " shouldn't be null when we are using STATUS");
    }
    try {
      Secret handler;
      if (securityClass == null) {
        handler = new SecretAES();
      } else {
        Class<?> clzz = Class.forName(securityClass);
        handler = (Secret) clzz.newInstance();
      }
      if (ENCRYPT.equals(status)) {
        String cipher = handler.encrypt(password);
        element.setTextContent(DECRYPT);
        Element parent = (Element) element.getParentNode();
        Element pwdElem = (Element) parent.getElementsByTagName(PASSWORD).item(0);
        pwdElem.setTextContent(cipher);
        if (filePath == null) {
          throw new ConnectionPoolXMLParseException(FILE_PATH + " shouldn't be null");
        }
        saveXML(document, filePath);
      } else {
        password = handler.decrypt(password);
      }
    } catch (Exception e) {
      LOGGER.error("handlerPassword error: ", e);
      throw new ConnectionPoolException(e.getMessage());
    }
    return password;
  }

  /**
   * Save the configuration.
   *
   * @param document
   * @param filePath
   * @throws Exception
   */
  private static void saveXML(Document document, String filePath) throws Exception {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    String encoding = XMLConfiguration.getEncoding();
    transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
    Source domSource = new DOMSource(document);
    Writer writer = getResourceAsWriter(encoding, filePath);
    Result result = new StreamResult(writer);
    // save it
    transformer.transform(domSource, result);
  }

  /**
   * Get writer by path and {@link XMLConfiguration#encoding}.
   *
   * @param encoding
   * @param filePath
   * @return
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  private static Writer getResourceAsWriter(String encoding, String filePath)
      throws FileNotFoundException, UnsupportedEncodingException {
    OutputStream outStream = new FileOutputStream(filePath);
    Writer writer = new OutputStreamWriter(outStream, encoding);
    return writer;
  }
}
