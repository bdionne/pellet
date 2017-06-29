package com.clarkparsia.pellet.server;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class ConfigurationReaderTest {

	// Defaults
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final int PORT_DEFAULT = 5100;
	private static final String HOST_DEFAULT = "localhost";
	private static final boolean STRICT_DEFAULT = false;
	private static final int UPDATE_INTERVAL_DEFAULT_IN_SECONDS = 300;

	public static Properties minimumConfiguration() {
		Properties p = new Properties();
		p.setProperty(Configuration.PROTEGE_USERNAME, USERNAME);
		p.setProperty(Configuration.PROTEGE_PASSWORD, PASSWORD);
		return p;
	}

	public static Properties allSettingsConfiguration() {
			Properties p = new Properties();
			p.setProperty(Configuration.PROTEGE_HOST, "http://test-protege.com");
			p.setProperty(Configuration.PROTEGE_PORT, "5000");
			p.setProperty(Configuration.PROTEGE_USERNAME, "admin");
			p.setProperty(Configuration.PROTEGE_PASSWORD, "secret");
			p.setProperty(Configuration.PROTEGE_ONTOLOGIES, "owl2.history, owl3.history, agencies.history");

			p.setProperty(Configuration.PELLET_HOME, "home");
			p.setProperty(Configuration.PELLET_HOST, "test-pellet.com");
			p.setProperty(Configuration.PELLET_PORT, "9090");
			p.setProperty(Configuration.PELLET_UPDATE_INTERVAL, "30");
			return p;
	}

	@Test
	public void shouldGetDefaults() {
		final ConfigurationReader configReader = ConfigurationReader.of(minimumConfiguration());

		assertEquals(HOST_DEFAULT, configReader.protegeSettings().host());
		assertEquals(PORT_DEFAULT, configReader.protegeSettings().port());
		assertEquals(USERNAME, configReader.protegeSettings().username());
		assertEquals(PASSWORD, configReader.protegeSettings().password());

		assertEquals(0, configReader.protegeSettings().ontologies().size());

		assertEquals(PelletServer.DEFAULT_HOST, configReader.pelletSettings().host());
		assertEquals(PelletServer.DEFAULT_PORT, configReader.pelletSettings().port());
		assertEquals(UPDATE_INTERVAL_DEFAULT_IN_SECONDS, configReader.pelletSettings().updateIntervalInSeconds());
	}

	@Test
	public void shouldGetAllConfigs() {
		final ConfigurationReader configReader = ConfigurationReader.of(allSettingsConfiguration());

		assertEquals("http://test-protege.com", configReader.protegeSettings().host());
		assertEquals(5000, configReader.protegeSettings().port());
		assertEquals("admin", configReader.protegeSettings().username());
		assertEquals("secret", configReader.protegeSettings().password());

		assertEquals(3, configReader.protegeSettings().ontologies().size());
		assertFalse(configReader.protegeSettings().ontologies().contains("invalid.txt"));

		assertEquals("home", configReader.pelletSettings().home());
		assertEquals("test-pellet.com", configReader.pelletSettings().host());
		assertEquals(9090, configReader.pelletSettings().port());
		assertEquals(30, configReader.pelletSettings().updateIntervalInSeconds());
	}

}
