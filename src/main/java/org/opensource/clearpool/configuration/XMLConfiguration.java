package org.opensource.clearpool.configuration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.opensource.clearpool.configuration.console.Console;
import org.opensource.clearpool.datasource.JDBCDataSource;
import org.opensource.clearpool.datasource.JndiDataSource;
import org.opensource.clearpool.exception.ConnectionPoolException;
import org.opensource.clearpool.exception.ConnectionPoolXMLParseException;
import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

/**
 * This class load,check and resolve XML.You can get the details of how to
 * resolve XML by the method {@link #parseXML}.
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public class XMLConfiguration {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(XMLConfiguration.class);

	// we can set the configuration's path by the SYSTEM_KEY
	private final static String SYSTEM_PATH_KEY = "clearpool.cfg.path";
	// the configuration's default path
	private final static String DEFAULT_PATH = "/clearpool.xml";

	public final static String CONSOLE = "console";
	public final static String JDBC = "jdbc";
	public final static String JNDI = "jndi";

	private final static String DOCUMENT_NAME = "clearpool";
	private final static String ALIAS = "alias";
	private final static String DISTRIBUTE_URL = "distribute-url";
	private final static String CORE_POOL_SIZE = "core-pool-size";
	private final static String MAX_POOL_SIZE = "max-pool-size";
	private final static String ACQUIRE_INCREMENT = "acquire-increment";
	private final static String ACQUIRE_RETRY_TIMES = "acquire-retry-times";
	private final static String LIMIT_IDLE_TIME = "limit-idle-time";
	private final static String KEEP_TEST_PERIOD = "keep-test-period";
	private final static String TEST_TABLE_NAME = "test-table-name";
	private final static String SHOW_SQL = "show-sql";

	// the public entry to {@link #Configuration}.
	public static Map<String, ConfigurationVO> getCfgVO(String path) {
		path = getRealPath(path);
		Map<String, ConfigurationVO> cfgMap = new HashMap<>();
		Set<ConfigurationVO> cfgSet = new HashSet<>();
		XMLInputFactory xmlFac = XMLInputFactory.newInstance();
		try {
			long begin = System.currentTimeMillis();
			parseXML(cfgMap, cfgSet, path, xmlFac, false, false);
			long cost = System.currentTimeMillis() - begin;
			LOG.info("XML parsing cost " + cost + "ms");
		} catch (XMLStreamException e) {
			throw new ConnectionPoolXMLParseException(e);
		}
		return cfgMap;
	}

	/**
	 * If path is null,we get path from {@code SYSTEM_KEY}. If path is not set
	 * in {@code SYSTEM_KEY},we set it as {@code DEFAULT_PATH}.
	 * 
	 * @param path
	 *            the path of the XML
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
	 * The rule of searching resource is base on ClassLoader searching rule.
	 * 
	 * @see java.lang.Class#getResourceAsStream(String)
	 * @param path
	 *            is the url of the resource
	 * @return the inputstream of the resource
	 */
	private static InputStream getResourceAsStream(String path) {
		path = path.startsWith("/") ? path.substring(1) : path;
		InputStream inStream = null;
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
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

	/**
	 * Parse XML recursive.If reader has {@code DISTRIBUTE_URL},we give up this
	 * XML and parse the url in {@code DISTRIBUTE_URL}.When we get a repeat url
	 * from {@code DISTRIBUTE_URL},we throw a
	 * {@link ConnectionPoolXMLParseException} .If we get a element in
	 * {@code DISTRIBUTE_URL} which isn't {@link DISTRIBUTE_VALUE},we throw a
	 * {@link ConnectionPoolXMLParseException}.
	 * 
	 * If reader don't has {@code DISTRIBUTE_URL},we treat the XML as a
	 * {@link ConfigurationVO}.After we fill {@link ConfigurationVO},we check if
	 * {@link ConfigurationVO} is legal.IF {@link ConfigurationVO} is legal,we
	 * add it to cfgVOs,otherwise we throw a
	 * {@link ConnectionPoolXMLParseException}.And we throw a
	 * {@link ConnectionPoolXMLParseException} if {@link ConfigurationVO}
	 * repeat.
	 * 
	 * @param cfgVOs
	 *            is hashset of the cfgVO
	 * @param reader
	 *            is the XMLStreamReader of the root stream
	 * @param xmlFac
	 *            is used to parse path
	 * @param distributed
	 *            if is distributed
	 * @throws XMLStreamException
	 *             {@link XMLStreamException}
	 */
	private static void parseXML(Map<String, ConfigurationVO> cfgMap,
			Set<ConfigurationVO> cfgSet, String path, XMLInputFactory xmlFac,
			boolean isNotFirst, boolean distributed) throws XMLStreamException {
		InputStream inStream = getResourceAsStream(path);
		XMLStreamReader reader = xmlFac.createXMLStreamReader(inStream);
		int event = reader.getEventType();
		while (reader.hasNext()) {
			if (event == XMLStreamConstants.START_ELEMENT) {
				if (!DOCUMENT_NAME.equals(reader.getLocalName())) {
					throw new ConnectionPoolXMLParseException(
							"xml's name should be " + DOCUMENT_NAME);
				}
				break;
			}
			event = reader.next();
		}
		// hasDistributed means XML has DISTRIBUTE_URL,noDistributed means XML
		// has other labels except DISTRIBUTE_URL.
		boolean hasDistributed = false, noDistributed = false;
		Set<String> urls = new HashSet<>();
		urls.add(path);
		ConfigurationVO cfgVO = new ConfigurationVO();
		while (reader.hasNext()) {
			event = reader.next();
			if (event != XMLStreamConstants.START_ELEMENT) {
				continue;
			}
			String parsing = reader.getLocalName();
			switch (parsing) {
			case ALIAS:
				checkDistributedLegal(hasDistributed);
				noDistributed = true;
				cfgVO.setAlias(reader.getElementText().trim());
				break;
			case CONSOLE:
				if (isNotFirst) {
					throw new ConnectionPoolXMLParseException(CONSOLE
							+ " should set in the first configuration");
				}
				if (ConfigurationVO.getConsole() != null) {
					// ignore the CONSOLE if it is already loaded.
					return;
				}
				Console console = new Console();
				console.parse(reader);
				ConfigurationVO.setConsole(console);
				break;
			case DISTRIBUTE_URL:
				checkDistributedLegal(noDistributed);
				if (!urls.add(reader.getElementText().trim())) {
					throw new ConnectionPoolXMLParseException(DISTRIBUTE_URL
							+ " repeat");
				}
				hasDistributed = true;
				distributed = true;
				break;
			case JDBC:
				checkDistributedLegal(hasDistributed);
				if (cfgVO.getDataSource() != null) {
					throw new ConnectionPoolXMLParseException(JDBC + " or "
							+ JNDI + " repeat");
				}
				noDistributed = true;
				JDBCDataSource jdbc = new JDBCDataSource();
				jdbc.parse(reader);
				cfgVO.setDataSource(jdbc);
				break;
			case JNDI:
				checkDistributedLegal(hasDistributed);
				if (cfgVO.getDataSource() != null) {
					throw new ConnectionPoolXMLParseException(JDBC + " or "
							+ JNDI + " repeat");
				}
				noDistributed = true;
				JndiDataSource jndi = new JndiDataSource();
				jndi.parse(reader);
				cfgVO.setDataSource(jndi);
				break;
			case CORE_POOL_SIZE:
				checkDistributedLegal(hasDistributed);
				noDistributed = true;
				cfgVO.setCorePoolSize(Integer.valueOf(reader.getElementText()
						.trim()));
				break;
			case MAX_POOL_SIZE:
				checkDistributedLegal(hasDistributed);
				noDistributed = true;
				cfgVO.setMaxPoolSize(Integer.valueOf(reader.getElementText()
						.trim()));
				break;
			case ACQUIRE_INCREMENT:
				checkDistributedLegal(hasDistributed);
				noDistributed = true;
				cfgVO.setAcquireIncrement(Integer.valueOf(reader
						.getElementText().trim()));
				break;
			case ACQUIRE_RETRY_TIMES:
				checkDistributedLegal(hasDistributed);
				noDistributed = true;
				cfgVO.setAcquireRetryTimes(Integer.valueOf(reader
						.getElementText().trim()));
				break;
			case LIMIT_IDLE_TIME:
				checkDistributedLegal(hasDistributed);
				noDistributed = true;
				cfgVO.setLimitIdleTime(Integer.valueOf(reader.getElementText()
						.trim()) * 1000L);
				break;
			case KEEP_TEST_PERIOD:
				checkDistributedLegal(hasDistributed);
				noDistributed = true;
				cfgVO.setKeepTestPeriod(Integer.valueOf(reader.getElementText()
						.trim()) * 1000L);
				break;
			case TEST_TABLE_NAME:
				checkDistributedLegal(hasDistributed);
				noDistributed = true;
				cfgVO.setTestTableName(reader.getElementText().trim());
				break;
			case SHOW_SQL:
				checkDistributedLegal(hasDistributed);
				noDistributed = true;
				cfgVO.setShowSql(Boolean
						.valueOf(reader.getElementText().trim()));
				break;
			default:
				throw new ConnectionPoolXMLParseException(DISTRIBUTE_URL
						+ " contains illegal elements");
			}
		}
		if (urls.size() > 1) {
			urls.remove(path);
			for (String url : urls) {
				// invoke parseXML recursive
				parseXML(cfgMap, cfgSet, url, xmlFac, true, distributed);
			}
		} else {
			cfgVO.init();
			if (!cfgSet.add(cfgVO)) {
				throw new ConnectionPoolXMLParseException(
						"configurations repeat");
			}
			if (cfgMap.put(cfgVO.getAlias(), cfgVO) != null) {
				throw new ConnectionPoolXMLParseException(
						"configurations have the same name");
			}
		}
	}

	/**
	 * check if XML have distributed and the other labels at the same time
	 */
	private static void checkDistributedLegal(boolean hasDistributed) {
		if (hasDistributed) {
			throw new ConnectionPoolXMLParseException("we shouldn't have "
					+ DISTRIBUTE_URL + " and other labels at the same time");
		}
	}
}
