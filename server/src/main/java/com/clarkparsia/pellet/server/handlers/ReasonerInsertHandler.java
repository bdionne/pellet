package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.inject.Inject;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Evren Sirin
 */
public class ReasonerInsertHandler extends ReasonerUpdateHandler {
	@Inject
	public ReasonerInsertHandler(final OWLOntologyManager manager, final ProtegeServerState theServerState) {
		super(manager, theServerState, true);
	}
}
