package com.clarkparsia.pellet.server;

import java.util.Properties;

/**
 * @author Edgar Rodriguez-Diaz
 */
public final class ConfigurationReader {

	private final ProtegeSettings protegeSettings;
	private final PelletSettings pelletSettings;

	public ConfigurationReader(final Properties theConfig) {
		protegeSettings = new ProtegeSettings(theConfig);
		pelletSettings = new PelletSettings(theConfig);
	}

	public ProtegeSettings protegeSettings() {
		return protegeSettings;
	}

	public PelletSettings pelletSettings() {
		return pelletSettings;
	}

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
