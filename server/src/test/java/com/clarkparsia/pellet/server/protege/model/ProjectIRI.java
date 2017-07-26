package com.clarkparsia.pellet.server.protege.model;

import com.clarkparsia.owlapiv3.IRIUtils;
import edu.stanford.protege.metaproject.api.ProjectId;
import org.semanticweb.owlapi.model.IRI;

/**
 * Created by rgrinberg on 7/26/17.
 */
public class ProjectIRI {
	private final IRI iri;
	public final ProjectId projectId;

	public ProjectIRI(IRI iri, ProjectId projectId) {
		this.iri = iri;
		this.projectId = projectId;
	}

	public IRI rawIRI() {
		return iri;
	}

	public IRI projectIRI() {
		return IRIUtils.addProjectId(this.iri, this.projectId.get());
	}
}
