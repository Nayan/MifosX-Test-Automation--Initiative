/*
 * 
 */
package cz.mallat.uasparser;

import cz.mallat.uasparser.fileparser.Entry;
import cz.mallat.uasparser.fileparser.PHPFileParser;
import cz.mallat.uasparser.fileparser.Section;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * User agent parser.
 * 
 * @author oli
 * 
 */
public class UASparser {

	/** The lock. */
	private ReentrantLock lock = new ReentrantLock();

	/** The Constant INFO_URL. */
	static final String INFO_URL = "http://user-agent-string.info";

	/** The robots map. */
	private Map<String, RobotEntry> robotsMap;

	/** The os map. */
	private Map<Long, OsEntry> osMap;

	/** The browser map. */
	private Map<Long, BrowserEntry> browserMap;

	/** The browser type map. */
	private Map<Long, String> browserTypeMap;

	/** The browser reg map. */
	private Map<String, Long> browserRegMap;

	/** The browser os map. */
	private Map<Long, Long> browserOsMap;

	/** The os reg map. */
	private Map<Pattern, Long> osRegMap;

	/**
	 * Use the given filename to load the definition file from the local
	 * filesystem.
	 * 
	 * @param localDefinitionFilename
	 *            the local definition filename
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public UASparser(String localDefinitionFilename) throws IOException {
		loadDataFromFile(new File(localDefinitionFilename));
	}

	/**
	 * Use the given inputstream to load the definition file from the local
	 * filesystem.
	 * 
	 * @param inputStreamToDefinitionFile
	 *            the input stream to definition file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public UASparser(InputStream inputStreamToDefinitionFile)
			throws IOException {
		loadDataFromFile(inputStreamToDefinitionFile);
	}

	/**
	 * Constructor for inherented classes.
	 */
	public UASparser() {
		// empty
	}

	/**
	 * When a class inherents from this class, it probably has to override this
	 * method.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void checkDataMaps() throws IOException {
		// empty for this base class
	}

	/**
	 * Parse the given user agent string and returns a UserAgentInfo object with
	 * the related data.
	 * 
	 * @param useragent
	 *            the useragent
	 * @return the user agent info
	 * @throws IOException
	 *             may happen when the retrieval of the data file fails
	 */
	public UserAgentInfo parse(String useragent) throws IOException {
		UserAgentInfo retObj = new UserAgentInfo();

		if (useragent == null) {
			return retObj;
		}
		useragent = useragent.trim();

		// check that the data maps are up-to-date
		checkDataMaps();

		// first check if it's a robot
		if (!processRobot(useragent, retObj)) {
			// search for a browser on the browser regex patterns
			boolean osFound = processBrowserRegex(useragent, retObj);

			if (!osFound) {
				// search the OS regex patterns for the used OS
				processOsRegex(useragent, retObj);
			}
		}
		return retObj;
	}

	/**
	 * Searches in the os regex table. if found a match copies the os data
	 * 
	 * @param useragent
	 *            the useragent
	 * @param retObj
	 *            the ret obj
	 */
	private void processOsRegex(String useragent, UserAgentInfo retObj) {
		try {
			lock.lock();

			for (Map.Entry<Pattern, Long> entry : osRegMap.entrySet()) {
				Matcher matcher = entry.getKey().matcher(useragent);
				if (matcher.find()) {
					// simply copy the OS data into the result object
					Long idOs = entry.getValue();
					OsEntry os = osMap.get(idOs);
					if (os != null) {
						os.copyTo(retObj);
					}
					break;
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Searchs in the browser regex table. if found a match copies the browser
	 * data and if possible os data
	 * 
	 * @param useragent
	 *            the useragent
	 * @param retObj
	 *            the ret obj
	 * @return true, if successful
	 */
	private boolean processBrowserRegex(String useragent, UserAgentInfo retObj) {
		try {
			lock.lock();
			boolean osFound = false;
			for (Map.Entry<String, Long> entry : browserRegMap.entrySet()) {
				Pattern pattern = Pattern.compile(entry.getKey(),
						Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				Matcher matcher = pattern.matcher(useragent);
				if (matcher.find()) {
					// if a browse was found...
					Long idBrowser = entry.getValue();
					// ... but the browser type from browser type map into the
					// typ
					copyType(retObj, idBrowser);
					// get all the browser data from the browser map
					BrowserEntry be = browserMap.get(idBrowser);
					if (be != null) {
						// first try to get the browser version from the first
						// subgroup of the regex
						String browserVersionInfo = null;
						if (matcher.groupCount() > 0) {
							browserVersionInfo = matcher.group(1);
						}
						// copy the browser data into the result
						be.copyTo(retObj, browserVersionInfo);
					}
					// check if this browser has exactly one OS mapped
					Long idOs = browserOsMap.get(idBrowser);
					if (idOs != null) {
						osFound = true;
						OsEntry os = osMap.get(idOs);
						if (os != null) {
							os.copyTo(retObj);
						}
					}
					break;
				}
			}
			return osFound;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Sets the source type, if possible.
	 * 
	 * @param retObj
	 *            the ret obj
	 * @param idBrowser
	 *            the id browser
	 */
	private void copyType(UserAgentInfo retObj, Long idBrowser) {
		try {
			lock.lock();

			BrowserEntry be = browserMap.get(idBrowser);
			if (be != null) {
				Long type = be.getType();
				if (type != null) {
					String typeString = browserTypeMap.get(type);
					if (typeString != null) {
						retObj.setTyp(typeString);
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Checks if the useragent comes from a robot. if yes copies all the data to
	 * the result object
	 * 
	 * @param useragent
	 *            the useragent
	 * @param retObj
	 *            the ret obj
	 * @return true if the useragent belongs to a robot, else false
	 */
	private boolean processRobot(String useragent, UserAgentInfo retObj) {
		try {
			lock.lock();

			if (robotsMap.containsKey(useragent)) {
				retObj.setTyp("Robot");
				RobotEntry robotEntry = robotsMap.get(useragent);
				robotEntry.copyTo(retObj);
				if (robotEntry.getOsId() != null) {
					OsEntry os = osMap.get(robotEntry.getOsId());
					if (os != null) {
						os.copyTo(retObj);
					}
				}
				return true;
			}
		} finally {
			lock.unlock();
		}
		return false;
	}

	/**
	 * loads the data file and creates all internal data structs.
	 * 
	 * @param definitionFile
	 *            the definition file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void loadDataFromFile(File definitionFile) throws IOException {
		PHPFileParser fp = new PHPFileParser(definitionFile);
		createInternalDataStructre(fp.getSections());
	}

	/**
	 * loads the data file and creates all internal data structs.
	 * 
	 * @param is
	 *            the is
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void loadDataFromFile(InputStream is) throws IOException {
		PHPFileParser fp = new PHPFileParser(is);
		createInternalDataStructre(fp.getSections());
	}

	/**
	 * Creates the internal data structes from the seciontList.
	 * 
	 * @param sectionList
	 *            the section list
	 */
	protected void createInternalDataStructre(List<Section> sectionList) {
		try {
			lock.lock();

			for (Section sec : sectionList) {
				if ("robots".equals(sec.getName())) {
					Map<String, RobotEntry> robotsMapTmp = new HashMap<String, RobotEntry>();
					for (Entry en : sec.getEntries()) {
						RobotEntry re = new RobotEntry(en.getData());
						robotsMapTmp.put(re.getUserAgentString(), re);
					}
					robotsMap = robotsMapTmp;
				} else if ("os".equals(sec.getName())) {
					Map<Long, OsEntry> osMapTmp = new HashMap<Long, OsEntry>();
					for (Entry en : sec.getEntries()) {
						OsEntry oe = new OsEntry(en.getData());
						osMapTmp.put(Long.parseLong(en.getKey()), oe);
					}
					osMap = osMapTmp;
				} else if ("browser".equals(sec.getName())) {
					Map<Long, BrowserEntry> browserMapTmp = new HashMap<Long, BrowserEntry>();
					for (Entry en : sec.getEntries()) {
						BrowserEntry be = new BrowserEntry(en.getData());
						browserMapTmp.put(Long.parseLong(en.getKey()), be);
					}
					browserMap = browserMapTmp;
				} else if ("browser_type".equals(sec.getName())) {
					Map<Long, String> browserTypeMapTmp = new HashMap<Long, String>();
					for (Entry en : sec.getEntries()) {
						browserTypeMapTmp.put(Long.parseLong(en.getKey()), en
								.getData().iterator().next());
					}
					browserTypeMap = browserTypeMapTmp;
				} else if ("browser_reg".equals(sec.getName())) {
					Map<String, Long> browserRegMapTmp = new LinkedHashMap<String, Long>();
					for (Entry en : sec.getEntries()) {
						Iterator<String> it = en.getData().iterator();
						browserRegMapTmp.put(convertPerlToJavaRegex(it.next()),
								Long.parseLong(it.next()));
					}
					browserRegMap = browserRegMapTmp;
				} else if ("browser_os".equals(sec.getName())) {
					Map<Long, Long> browserOsMapTmp = new HashMap<Long, Long>();
					for (Entry en : sec.getEntries()) {
						browserOsMapTmp.put(Long.parseLong(en.getKey()),
								Long.parseLong(en.getData().iterator().next()));
					}
					browserOsMap = browserOsMapTmp;
				} else if ("os_reg".equals(sec.getName())) {
					Map<Pattern, Long> osRegMapTmp = new LinkedHashMap<Pattern, Long>();
					for (Entry en : sec.getEntries()) {
						Iterator<String> it = en.getData().iterator();
						Pattern pattern = Pattern.compile(
								convertPerlToJavaRegex(it.next()),
								Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
						osRegMapTmp.put(pattern, Long.parseLong(it.next()));
					}
					osRegMap = osRegMapTmp;
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Converts a PERL style regex into the Java style. That means in removes
	 * the leading and the last / and removes the modifiers
	 * 
	 * @param regex
	 *            the regex
	 * @return the string
	 */
	private String convertPerlToJavaRegex(String regex) {
		regex = regex.substring(1);
		int lastIndex = regex.lastIndexOf('/');
		regex = regex.substring(0, lastIndex);
		return regex;
	}

}
