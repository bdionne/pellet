package com.clarkparsia.pellet.server.protege.model;

import com.clarkparsia.pellet.server.ConfigurationReader;
import com.clarkparsia.pellet.server.protege.ProtegeServiceUtils;
import edu.stanford.protege.metaproject.api.Project;
import org.protege.editor.owl.client.api.Client;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by rgrinberg on 6/20/17.
 */
public class ProtegeServerOntologyProvider implements OntologyProvider {

	private final Client client;

	@Inject
	public ProtegeServerOntologyProvider(Properties config) {
		ConfigurationReader theConfigReader = ConfigurationReader.of(config);
		this.client = ProtegeServiceUtils.connect(theConfigReader);
	}

	@Override
	public Set<String> classifiableProjects() {
		Set<String> projects = new HashSet<>();
		for (Project project : client.classifiableProjects()) {
			projects.add(project.getId().get());
		}
		return projects;
	}
}
