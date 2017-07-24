package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.inject.Inject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class OntologyAddHandler extends AbstractRoutingHandler {

	@Inject
	public OntologyAddHandler(final ProtegeServerState theServerState) {
		super("PUT", "{ontology}", theServerState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleRequest(final HttpServerExchange theExchange) throws Exception {
		getServerState().addOntology(getProjectId(theExchange).toString());

		theExchange.setStatusCode(StatusCodes.OK);
		theExchange.endExchange();
	}
}
