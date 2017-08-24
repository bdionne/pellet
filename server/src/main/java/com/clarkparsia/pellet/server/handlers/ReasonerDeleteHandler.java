package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.inject.Inject;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Evren Sirin
 */
public class ReasonerDeleteHandler extends ReasonerUpdateHandler {
	@Inject
	public ReasonerDeleteHandler(final OWLOntologyManager manager,
															 final ProtegeServerState theServerState) {
		super(manager, theServerState, false);
	}
}
