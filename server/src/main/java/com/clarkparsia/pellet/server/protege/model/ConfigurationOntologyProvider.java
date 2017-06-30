package com.clarkparsia.pellet.server.protege.model;

import com.clarkparsia.pellet.server.ConfigurationReader;

import javax.inject.Inject;
import java.util.Properties;
import java.util.Set;

/**
 * Created by rgrinberg on 6/20/17.
 */
public class ConfigurationOntologyProvider implements OntologyProvider {

	private final Properties config;

	@Inject
	public ConfigurationOntologyProvider(final Properties config) {
		this.config = config;
	}

	@Override
	public Set<String> classifiableProjects() {
		return new ConfigurationReader(config).protegeSettings().ontologies();
	}
}
