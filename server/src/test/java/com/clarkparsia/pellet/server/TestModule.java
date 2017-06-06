package com.clarkparsia.pellet.server;

import com.clarkparsia.pellet.server.protege.TestProtegeServerConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import java.util.List;

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
		binder().bind(Configuration.class).toInstance(new TestProtegeServerConfiguration(ontologies));
	}
}
