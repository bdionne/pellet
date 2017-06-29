package com.clarkparsia.pellet.server.protege;

import java.util.List;
import java.util.Properties;

import com.clarkparsia.pellet.server.Configuration;
import com.google.common.base.Joiner;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class TestProtegeServerConfiguration {

	public static Properties testProtegeServerConfiguration(List<String> ontologies) {
		Properties p = new Properties();
		p.setProperty(Configuration.PROTEGE_HOST, TestUtilities.PROTEGE_HOST);
		p.setProperty(Configuration.PROTEGE_PORT, TestUtilities.PROTEGE_PORT);
		p.setProperty(Configuration.PROTEGE_USERNAME, TestUtilities.PROTEGE_USERNAME);
		p.setProperty(Configuration.PROTEGE_PASSWORD, TestUtilities.PROTEGE_PASSWORD);
		p.setProperty(Configuration.PROTEGE_ONTOLOGIES, Joiner.on(",").join(ontologies));

		p.setProperty(Configuration.PELLET_HOME, ProtegeServerTest.TEST_HOME.toString());
		p.setProperty(Configuration.PELLET_HOST, "http://127.0.0.1");
		p.setProperty(Configuration.PELLET_MANAGEMENT_PASSWORD, TestUtilities.PELLET_MANAGEMENT_PASSWORD);
		return p;
	}
}
