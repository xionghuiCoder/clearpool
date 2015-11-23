package org.opensource.clearpool.util;

import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class used to deal the error when we parse configuration.
 *
 * @author xionghui
 * @date 24.09.2014
 * @version 1.0
 */
public class XMLErrorHandler implements ErrorHandler {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(XMLErrorHandler.class);

  @Override
  public void warning(SAXParseException exception) throws SAXException {
    LOGGER.warn("", exception);
  }

  @Override
  public void error(SAXParseException exception) throws SAXException {
    throw exception;
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXException {
    throw exception;
  }
}
