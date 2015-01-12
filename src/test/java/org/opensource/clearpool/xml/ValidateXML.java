package org.opensource.clearpool.xml;

import junit.framework.TestCase;

import org.opensource.clearpool.util.XMLUtil;

public class ValidateXML extends TestCase {
  private static final String JDBC_PATH = "xml/jdbc.xml";

  private static final String JNDI_PATH = "xml/jndi.xml";

  public void test_jdbcXML() throws Exception {
    XMLUtil.createDocument(JDBC_PATH);
  }

  public void test_jndiXML() throws Exception {
    XMLUtil.createDocument(JNDI_PATH);
  }
}
