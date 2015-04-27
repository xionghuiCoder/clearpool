package org.opensource.clearpool.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.CommonDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;

import org.opensource.clearpool.configuration.console.Console;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.opensource.clearpool.logging.PoolLog;
import org.opensource.clearpool.logging.PoolLogFactory;
import org.opensource.clearpool.util.XMLEntityResolver;
import org.opensource.clearpool.util.XMLErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class load,check and resolve XML.You can get the details of how to resolve XML by the method
 * {@link #parseXML}.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class XMLConfiguration {
  private static final PoolLog LOG = PoolLogFactory.getLog(XMLConfiguration.class);

  /**
   * encoding used to read and write xml.
   */
  private static String encoding = "UTF-8";

  /**
   * JAXP attribute used to configure the schema language for validation.
   */
  private static final String SCHEMA_LANGUAGE_ATTRIBUTE =
      "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
  /**
   * JAXP attribute value indicating the XSD schema language.
   */
  private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

  // we can set the configuration's path by the SYSTEM_KEY
  private final static String SYSTEM_PATH_KEY = "clearpool.cfg.path";
  // the configuration's default path
  private final static String DEFAULT_PATH = "/clearpool.xml";

  public final static String CONSOLE = "console";
  public final static String JDBC = "jdbc";
  public final static String JNDI = "jndi";

  private final static String ALIAS = "alias";
  private final static String DISTRIBUTE_URL = "distribute-url";
  private final static String JTA_SUPPORT = "jta-support";
  private final static String CORE_POOL_SIZE = "core-pool-size";
  private final static String MAX_POOL_SIZE = "max-pool-size";
  private final static String ACQUIRE_INCREMENT = "acquire-increment";
  private final static String ACQUIRE_RETRY_TIMES = "acquire-retry-times";
  private final static String USELESS_CONNECTION_EXCEPTION = "useless-connection-exception";
  private final static String LIMIT_IDLE_TIME = "limit-idle-time";
  private final static String KEEP_TEST_PERIOD = "keep-test-period";
  private final static String TEST_TABLE_NAME = "test-table-name";
  private final static String TEST_BEFORE_USE = "test-before-use";
  private final static String TEST_QUER_YSQL = "test-query-sql";
  private final static String SHOW_SQL = "show-sql";
  private final static String SQL_TIME_FILTER = "sql-time-filter";

  // the public entry to {@link #Configuration}.
  public static Map<String, ConfigurationVO> getCfgVO(String path) {
    path = getRealPath(path);
    Map<String, ConfigurationVO> cfgMap = new HashMap<String, ConfigurationVO>();
    XMLInputFactory xmlFac = XMLInputFactory.newInstance();
    long begin = System.currentTimeMillis();
    parseXML(cfgMap, path, xmlFac, true, false);
    long cost = System.currentTimeMillis() - begin;
    LOG.info("XML parsing cost " + cost + "ms");
    return cfgMap;
  }

  /**
   * If path is null,we get path from {@code SYSTEM_KEY}. If path is not set in {@code SYSTEM_KEY}
   * ,we set it as {@code DEFAULT_PATH}.
   *
   * @param path the path of the XML
   */
  private static String getRealPath(String path) {
    if (path == null) {
      path = System.getProperty(SYSTEM_PATH_KEY);
      if (path == null) {
        path = DEFAULT_PATH;
      }
    } else {
      path = path.trim();
    }
    return path;
  }

  /**
   * If xml has {@code DISTRIBUTE_URL},we parse XML recursive.
   *
   * If reader don't has {@code DISTRIBUTE_URL},we treat the XML as a {@link ConfigurationVO}.After
   * we fill {@link ConfigurationVO},we check if {@link ConfigurationVO} is legal.IF
   * {@link ConfigurationVO} is legal,we add it to cfgMap,otherwise we throw a
   * {@link ConnectionPoolXMLParseException}.
   *
   * @param cfgMap is hashMap of the alias to cfgVO
   * @param path is the path of the cfg
   * @param xmlFac is used to parse path
   * @param isFirst if is the first cfg
   * @param distributed if is distributed
   */
  private static void parseXML(Map<String, ConfigurationVO> cfgMap, String path,
      XMLInputFactory xmlFac, boolean isFirst, boolean distributed) {
    Set<String> urls = new HashSet<String>();
    ConfigurationVO cfgVO = new ConfigurationVO();
    // get the Reader by path.
    Reader reader = getResourceAsReader(path);
    try {
      Document document = createDocument(reader);
      Element rootElement = document.getDocumentElement();
      NodeList children = rootElement.getChildNodes();
      for (int i = 0, size = children.getLength(); i < size; i++) {
        Node childNode = children.item(i);
        if (childNode instanceof Element) {
          Element child = (Element) childNode;
          String nodeName = child.getNodeName();
          if (CONSOLE.equals(nodeName)) {
            if (!isFirst) {
              throw new ConnectionPoolXMLParseException(CONSOLE
                  + " should set in the first configuration");
            }
            Console console = new Console();
            console.parse(child);
            ConfigurationVO.setConsole(console);
          } else if (JDBC.equals(nodeName)) {
            CommonDataSource jdbcDs = JDBCConfiguration.parse(child, document, path);
            cfgVO.setCommonDataSource(jdbcDs);
          } else if (JNDI.equals(nodeName)) {
            CommonDataSource jndiDs = JndiConfiguration.parse(child);
            cfgVO.setCommonDataSource(jndiDs);
          } else {
            String nodeValue = child.getTextContent().trim();
            if (DISTRIBUTE_URL.equals(nodeName)) {
              distributed = true;
              urls.add(nodeValue);
            } else {
              fillNodeValue(nodeName, nodeValue, cfgVO);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new ConnectionPoolXMLParseException(e);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        LOG.error(e);
      }
    }
    if (urls.size() > 0) {
      for (String url : urls) {
        // invoke parseXML recursive
        parseXML(cfgMap, url, xmlFac, false, distributed);
      }
    } else {
      cfgVO.init();
      if (cfgMap.put(cfgVO.getAlias(), cfgVO) != null) {
        throw new ConnectionPoolXMLParseException("cfg's alias " + cfgVO.getAlias() + " repeat");
      }
    }
  }

  /**
   * Fill normal node value.
   *
   * @param nodeName
   * @param nodeValue
   * @param cfgVO
   */
  private static void fillNodeValue(String nodeName, String nodeValue, ConfigurationVO cfgVO) {
    if (ALIAS.equals(nodeName)) {
      cfgVO.setAlias(nodeValue);
    } else if (JTA_SUPPORT.equals(nodeName)) {
      cfgVO.setJtaSupport(Boolean.valueOf(nodeValue));
    } else if (CORE_POOL_SIZE.equals(nodeName)) {
      cfgVO.setCorePoolSize(Integer.valueOf(nodeValue));
    } else if (MAX_POOL_SIZE.equals(nodeName)) {
      cfgVO.setMaxPoolSize(Integer.valueOf(nodeValue));
    } else if (ACQUIRE_INCREMENT.equals(nodeName)) {
      cfgVO.setAcquireIncrement(Integer.valueOf(nodeValue));
    } else if (ACQUIRE_RETRY_TIMES.equals(nodeName)) {
      cfgVO.setAcquireRetryTimes(Integer.valueOf(nodeValue));
    } else if (USELESS_CONNECTION_EXCEPTION.equals(nodeName)) {
      cfgVO.setUselessConnectionException(Boolean.valueOf(nodeValue));
    } else if (LIMIT_IDLE_TIME.equals(nodeName)) {
      cfgVO.setLimitIdleTime(Integer.valueOf(nodeValue) * 1000L);
    } else if (KEEP_TEST_PERIOD.equals(nodeName)) {
      cfgVO.setKeepTestPeriod(Integer.valueOf(nodeValue) * 1000L);
    } else if (TEST_TABLE_NAME.equals(nodeName)) {
      cfgVO.setTestTableName(nodeValue);
    } else if (TEST_BEFORE_USE.equals(nodeName)) {
      cfgVO.setTestBeforeUse(Boolean.valueOf(nodeValue));
    } else if (TEST_QUER_YSQL.equals(nodeName)) {
      cfgVO.setTestQuerySql(nodeValue);
    } else if (SHOW_SQL.equals(nodeName)) {
      cfgVO.setShowSql(Boolean.valueOf(nodeValue));
    } else if (SQL_TIME_FILTER.equals(nodeName)) {
      cfgVO.setSqlTimeFilter(Integer.valueOf(nodeValue) * 1000L);
    }
  }

  /**
   * Get reader by path and {@link #encoding}.
   *
   * @param resource
   * @return
   * @throws UnsupportedEncodingException
   */
  private static Reader getResourceAsReader(String path) {
    Reader reader = null;
    try {
      reader = new InputStreamReader(getResourceAsStream(path), encoding);
    } catch (UnsupportedEncodingException e) {
      throw new ConnectionPoolXMLParseException(e);
    }
    return reader;
  }

  /**
   * The rule of searching resource is base on ClassLoader searching rule.
   *
   * @see java.lang.Class#getResourceAsStream(String)
   * @param path is the url of the resource
   * @return the inputstream of the resource
   */
  private static InputStream getResourceAsStream(String path) {
    path = path.startsWith("/") ? path.substring(1) : path;
    InputStream inStream = null;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      inStream = ClassLoader.getSystemResourceAsStream(path);
    } else {
      inStream = classLoader.getResourceAsStream(path);
    }
    if (inStream == null) {
      throw new ConnectionPoolXMLParseException(path + " not found");
    }
    return inStream;
  }

  /**
   * Create document by reader.
   *
   * @param reader
   * @return
   * @throws Exception
   */
  private static Document createDocument(Reader reader) throws Exception {
    InputSource inputSource = new InputSource(reader);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(true);
    factory.setNamespaceAware(true);
    factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);

    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(new XMLEntityResolver());
    builder.setErrorHandler(new XMLErrorHandler());
    return builder.parse(inputSource);
  }

  public static String getEncoding() {
    return encoding;
  }

  public static void setEncoding(String encoding) {
    XMLConfiguration.encoding = encoding;
  }
}
