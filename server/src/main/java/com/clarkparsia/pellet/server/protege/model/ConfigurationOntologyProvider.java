package com.clarkparsia.pellet.server.protege.model;

import com.clarkparsia.pellet.server.Configuration;
import com.clarkparsia.pellet.server.ConfigurationReader;

import javax.inject.Inject;
import java.util.Set;

/**
 * Created by rgrinberg on 6/20/17.
 */
public class ConfigurationOntologyProvider implements OntologyProvider {

	private final Configuration config;

	@Inject
	public ConfigurationOntologyProvider(final Configuration config) {
		this.config = config;
	}

	@Override
	public Set<String> classifiableProjects() {
		return ConfigurationReader.of(config).protegeSettings().ontologies();
	}
}
