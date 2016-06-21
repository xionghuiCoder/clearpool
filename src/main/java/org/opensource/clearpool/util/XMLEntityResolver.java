package org.opensource.clearpool.util;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLEntityResolver implements EntityResolver {
  private static final String CONFIG_DTD = "org/opensource/clearpool/configuration/clearpool.xsd";

  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException, IOException {
    String path = CONFIG_DTD;
    InputSource source = this.getInputSource(path);
    source.setPublicId(publicId);
    source.setSystemId(systemId);
    return source;
  }

  private InputSource getInputSource(String path) {
    InputSource source;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream in;
    if (classLoader == null) {
      in = ClassLoader.getSystemResourceAsStream(path);
    } else {
      in = classLoader.getResourceAsStream(path);
    }
    source = new InputSource(in);
    return source;
  }
}
