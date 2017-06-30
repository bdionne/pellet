package com.clarkparsia.pellet.server;

import java.util.Properties;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class Configuration {

	public static String PROTEGE_HOST = "protege.host";
	public static String PROTEGE_PORT = "protege.port";
	public static String PROTEGE_USERNAME = "protege.username";
	public static String PROTEGE_PASSWORD = "protege.password";
	public static String PROTEGE_ONTOLOGIES = "protege.ontologies";

	public static String PELLET_HOME = "pellet.home";
	public static String PELLET_HOST = "pellet.host";
	public static String PELLET_PORT = "pellet.port";
	public static String PELLET_UPDATE_INTERVAL = "pellet.update.interval.sec";
	public static String PELLET_MANAGEMENT_PASSWORD = "pellet.password";

	public static String getProperty(Properties properties, String key, String defaultValue) {
		String val = properties.getProperty(key, defaultValue);
		if (val == null) {
			throw new IllegalArgumentException("Value of configuration property " + key + " is missing");
		}
		return val;
	}

	public static int getPropertyAsInteger(Properties properties, String key, int defaultValue) {
		String val = properties.getProperty(key);
		if (val == null) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(val);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Value of configuration property " + key + " is not a valid integer: " + defaultValue);
		}
	}
}
