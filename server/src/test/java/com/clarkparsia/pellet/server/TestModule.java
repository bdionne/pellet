package com.clarkparsia.pellet.server;

import com.clarkparsia.pellet.server.protege.TestProtegeServerConfiguration;
import com.clarkparsia.pellet.server.protege.model.ConfigurationOntologyProvider;
import com.clarkparsia.pellet.server.protege.model.OntologyProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Properties;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class TestModule extends AbstractModule implements Module {
	private final List<String> ontologies;

	public TestModule(final List<String> theOntologies) {
		ontologies = theOntologies;
	}

	@Override
	protected void configure() {
		binder().bind(Properties.class).toInstance(
			TestProtegeServerConfiguration.testProtegeServerConfiguration(ontologies));
		binder().bind(OntologyProvider.class).to(ConfigurationOntologyProvider.class).in(Singleton.class);
	}
}
