package org.opensource.clearpool.xml;

import junit.framework.TestCase;

import org.opensource.clearpool.util.XMLUtil;

public class ValidateXML extends TestCase {

	private static final String PATH = "xml/validate.xml";

	public void test_validateXML() throws Exception {
		XMLUtil.createDocument(PATH);
	}
}
