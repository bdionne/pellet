package com.clarkparsia.pellet.server;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class ConfigurationReaderTest {

	// Defaults
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final int PORT_DEFAULT = 5100;
	private static final String HOST_DEFAULT = "localhost";
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
		final Properties settings = minimumConfiguration();
		ProtegeSettings protegeSettings = new ProtegeSettings(settings);
		PelletSettings pelletSettings = new PelletSettings(settings);

		assertEquals(HOST_DEFAULT, protegeSettings.host());
		assertEquals(PORT_DEFAULT, protegeSettings.port());
		assertEquals(USERNAME, protegeSettings.username());
		assertEquals(PASSWORD, protegeSettings.password());

		assertEquals(0, protegeSettings.ontologies().size());

		assertEquals(PelletServer.DEFAULT_HOST, pelletSettings.host());
		assertEquals(PelletServer.DEFAULT_PORT, pelletSettings.port());
		assertEquals(UPDATE_INTERVAL_DEFAULT_IN_SECONDS, pelletSettings.updateIntervalInSeconds());
	}

	@Test
	public void shouldGetAllConfigs() {
		final Properties settings = allSettingsConfiguration();
		ProtegeSettings protegeSettings = new ProtegeSettings(settings);
		PelletSettings pelletSettings = new PelletSettings(settings);

		assertEquals("http://test-protege.com", protegeSettings.host());
		assertEquals(5000, protegeSettings.port());
		assertEquals("admin", protegeSettings.username());
		assertEquals("secret", protegeSettings.password());

		assertEquals(3, protegeSettings.ontologies().size());
		assertFalse(protegeSettings.ontologies().contains("invalid.txt"));

		assertEquals("home", pelletSettings.home());
		assertEquals("test-pellet.com", pelletSettings.host());
		assertEquals(9090, pelletSettings.port());
		assertEquals(30, pelletSettings.updateIntervalInSeconds());
	}

}
