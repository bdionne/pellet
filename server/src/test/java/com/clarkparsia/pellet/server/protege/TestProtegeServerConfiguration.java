package com.clarkparsia.pellet.server.protege;

import java.util.Properties;

import com.clarkparsia.pellet.server.Configuration;
import com.google.common.base.Joiner;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class TestProtegeServerConfiguration implements Configuration {

	private final Properties mProperties;

	public TestProtegeServerConfiguration() {
		this(new String[0]);
	}

	public TestProtegeServerConfiguration(String... ontologies) {
		mProperties = new Properties();
		mProperties.setProperty(Configuration.PROTEGE_HOST, TestUtilities.PROTEGE_HOST);
		mProperties.setProperty(Configuration.PROTEGE_PORT, TestUtilities.PROTEGE_PORT);
		mProperties.setProperty(Configuration.PROTEGE_USERNAME, TestUtilities.PROTEGE_USERNAME);
		mProperties.setProperty(Configuration.PROTEGE_PASSWORD, TestUtilities.PROTEGE_PASSWORD);
		mProperties.setProperty(Configuration.PROTEGE_ONTOLOGIES, Joiner.on(",").join(ontologies));

		mProperties.setProperty(Configuration.PELLET_HOME, PelletClientTest.TEST_HOME.toString());
		mProperties.setProperty(Configuration.PELLET_HOST, "http://127.0.0.1");
	}

	@Override
	public Properties getSettings() {
		return mProperties;
	}
}
