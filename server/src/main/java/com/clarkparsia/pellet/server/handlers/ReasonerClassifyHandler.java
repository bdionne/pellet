package com.clarkparsia.pellet.server.handlers;

import java.util.UUID;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.inject.Inject;
import edu.stanford.protege.metaproject.api.ProjectId;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Evren Sirin
 */
public class ReasonerClassifyHandler extends AbstractRoutingHandler {

	@Inject
	public ReasonerClassifyHandler(final ProtegeServerState theServerState) {
		super("GET", "{ontology}/classify", theServerState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleRequest(final HttpServerExchange theExchange) throws Exception {
		final ProjectId projectId = getProjectId(theExchange);
		final UUID clientId = getClientID(theExchange);

		// Get local client reasoner's version
		getClientState(projectId, clientId).getReasoner().classify();

		theExchange.setStatusCode(StatusCodes.OK);
		theExchange.endExchange();
	}
}
