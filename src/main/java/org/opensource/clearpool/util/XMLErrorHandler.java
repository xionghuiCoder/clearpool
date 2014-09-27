package org.opensource.clearpool.util;

import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;
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
	private static final PoolLog LOG = PoolLogFactory
			.getLog(XMLErrorHandler.class);

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		LOG.warn(exception);
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
