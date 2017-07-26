package com.clarkparsia.pellet.server.handlers;

import java.util.UUID;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.common.net.MediaType;
import com.google.inject.Inject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class ReasonerVersionHandler extends AbstractRoutingHandler {

	@Inject
	public ReasonerVersionHandler(final ProtegeServerState theServerState) {
		super("GET", "version", theServerState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleRequest(final HttpServerExchange theExchange) throws Exception {
		final IRI ontology = getOntology(theExchange);
		final UUID clientId = getClientID(theExchange);

		// Get local client reasoner's version
		int version = getClientState(ontology, clientId).version();

		theExchange.setStatusCode(StatusCodes.OK);
		theExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MediaType.PLAIN_TEXT_UTF_8.toString());
		theExchange.getResponseSender().send(String.valueOf(version));
		theExchange.endExchange();
	}
}
