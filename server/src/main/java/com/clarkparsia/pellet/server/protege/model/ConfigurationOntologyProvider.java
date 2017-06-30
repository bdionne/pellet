package com.clarkparsia.pellet.server.protege.model;

import com.clarkparsia.pellet.server.ProtegeSettings;

import javax.inject.Inject;
import java.util.Set;

/**
 * Created by rgrinberg on 6/20/17.
 */
public class ConfigurationOntologyProvider implements OntologyProvider {

	private final ProtegeSettings protegeSettings;

	@Inject
	public ConfigurationOntologyProvider(final ProtegeSettings protegeSettings) {
		this.protegeSettings = protegeSettings;
	}

	@Override
	public Set<String> classifiableProjects() {
		return this.protegeSettings.ontologies();
	}
}
