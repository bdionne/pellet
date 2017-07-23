package com.clarkparsia.pellet.server.protege;

import com.clarkparsia.pellet.server.ProtegeSettings;
import edu.stanford.protege.metaproject.api.Project;
import org.protege.editor.owl.client.api.Client;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rgrinberg on 6/20/17.
 */
public class ProtegeServerOntologyProvider implements OntologyProvider {

	private final Client client;

	@Inject
	public ProtegeServerOntologyProvider(ProtegeSettings protegeSettings) {
		this.client = ProtegeServiceUtils.connect(protegeSettings);
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
