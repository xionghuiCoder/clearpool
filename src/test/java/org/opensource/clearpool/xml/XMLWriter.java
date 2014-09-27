package org.opensource.clearpool.xml;

import java.io.FileOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.opensource.clearpool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLWriter extends TestCase {
	private static final String PATH = "xml/xml_writer.xml";

	public void test_XMLWriter() throws Exception {
		Document doc = XMLUtil.createDocument(PATH);

		NodeList list = doc.getElementsByTagName("password");
		for (int i = 0; i < list.getLength(); i++) {
			Node brandElement = list.item(i);
			String brandName = brandElement.getNodeName();
			if (brandName.equals("password")) {
				// update text content
				brandElement.setTextContent("1");
			}
		}

		// save xml
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource domSource = new DOMSource(doc);
		// set code and use utf-8 as default encoding.
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");

		String path = this.getWholePath();
		System.out.println(path);
		StreamResult result = new StreamResult(new FileOutputStream(path));
		// transform DOM to xml file
		transformer.transform(domSource, result);
	}

	private String getWholePath() {
		String path = PATH;
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (classLoader == null) {
			path = ClassLoader.getSystemResource(path).getPath();
		} else {
			path = classLoader.getResource(path).getPath();
		}
		return path;
	}
}
