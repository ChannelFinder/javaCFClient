package gov.bnl.channelfinder.api;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.prefs.Preferences;

class CFProperties {
	
	private static Properties defaultProperties;
	private static Properties userCFProperties;
	private static Properties userHomeCFProperties;
	private static Properties systemCFProperties;
	
	public CFProperties() {

		try {
			File userCFPropertiesFile = new File(System.getProperty(
					"channelfinder.properties", ""));
			File userHomeCFPropertiesFile = new File(
					System.getProperty("user.home")
							+ "/channelfinder.properties");
			File systemCFPropertiesFile = null;
			if (System.getProperty("os.name").startsWith("Windows")) {
				systemCFPropertiesFile = new File("/channelfinder.properties");
			} else if (System.getProperty("os.name").startsWith("Linux")) {
				systemCFPropertiesFile = new File(
						"/etc/channelfinder.properties");
			} else {
				systemCFPropertiesFile = new File(
						"/etc/channelfinder.properties");
			}

			// File defaultPropertiesFile = new
			// File(this.getClass().getResource(
			// "/config/channelfinder.properties").getPath());

			defaultProperties = new Properties();
			try {
				defaultProperties.load(this.getClass().getResourceAsStream(
						"/config/channelfinder.properties"));
			} catch (Exception e) {
				// The jar has been modified and the default packaged properties
				// file has been moved
				defaultProperties = null;
			}

			// Not using to new Properties(default Properties) constructor to
			// make the hierarchy clear.
			systemCFProperties = new Properties(defaultProperties);
			if (systemCFPropertiesFile.exists()) {
				systemCFProperties.load(new FileInputStream(
						systemCFPropertiesFile));
			}
			userHomeCFProperties = new Properties(systemCFProperties);
			if (userHomeCFPropertiesFile.exists()) {
				userHomeCFProperties.load(new FileInputStream(
						userHomeCFPropertiesFile));
			}
			userCFProperties = new Properties(userHomeCFProperties);
			if (userCFPropertiesFile.exists()) {
				userCFProperties
						.load(new FileInputStream(userCFPropertiesFile));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * check java preferences for the requested key - then checks the various
	 * default properties files.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getPreferenceValue(String key, String defaultValue) {
		if (userCFProperties.containsKey(key))
			return userCFProperties.getProperty(key);
		else if (userHomeCFProperties.containsKey(key))
			return userHomeCFProperties.getProperty(key);
		else if (systemCFProperties.containsKey(key))
			return systemCFProperties.getProperty(key);
		else if (defaultProperties.containsKey(key))
			return defaultProperties.getProperty(key);
		else
			return defaultValue;
	}

}
