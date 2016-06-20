package org.opensource.clearpool.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opensource.clearpool.exception.ConnectionPoolException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XMLUtil {
  private static final String SCHEMA_LANGUAGE_ATTRIBUTE =
      "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
  private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

  private XMLUtil() {
  }

  public static Document createDocument(String path) throws Exception {
    Reader reader = getResourceAsReader(path);
    InputSource inputSource = new InputSource(reader);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(true);
    factory.setNamespaceAware(true);
    factory.setAttribute(XMLUtil.SCHEMA_LANGUAGE_ATTRIBUTE, XMLUtil.XSD_SCHEMA_LANGUAGE);

    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(new XMLEntityResolver());
    builder.setErrorHandler(new XMLErrorHandler());
    return builder.parse(inputSource);
  }

  private static Reader getResourceAsReader(String path) {
    Reader reader = null;
    reader = new InputStreamReader(getResourceAsStream(path));
    return reader;
  }

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
      throw new ConnectionPoolException(path + " not found");
    }
    return inStream;
  }
}
