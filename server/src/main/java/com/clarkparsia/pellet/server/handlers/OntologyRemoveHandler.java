package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.inject.Inject;
import edu.stanford.protege.metaproject.api.ProjectId;
import io.undertow.server.HttpServerExchange;

import static io.undertow.util.StatusCodes.BAD_REQUEST;
import static io.undertow.util.StatusCodes.OK;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class OntologyRemoveHandler extends AbstractRoutingHandler {

	@Inject
	public OntologyRemoveHandler(final ProtegeServerState theServerState) {
		super("DELETE", "{ontology}", theServerState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleRequest(final HttpServerExchange theExchange) throws Exception {
		ProjectId projectId = getProjectId(theExchange);
		boolean removed = getServerState().removeProject(projectId);

		if (removed) {
			theExchange.setStatusCode(OK);
		} else {
			theExchange.setStatusCode(BAD_REQUEST);
			theExchange.setReasonPhrase("Ontology not found: " + projectId);
		}
		theExchange.endExchange();
	}
}
