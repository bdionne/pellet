package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.inject.Inject;
import edu.stanford.protege.metaproject.api.ProjectId;
import io.undertow.server.HttpServerExchange;

import java.util.UUID;

import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static io.undertow.util.Headers.CONTENT_TYPE;
import static io.undertow.util.StatusCodes.OK;
import static java.lang.String.valueOf;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class ReasonerVersionHandler extends AbstractRoutingHandler {

	@Inject
	public ReasonerVersionHandler(final ProtegeServerState theServerState) {
		super("GET", "{ontology}/version", theServerState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleRequest(final HttpServerExchange theExchange) throws Exception {
		final ProjectId projectId = getProjectId(theExchange);
		final UUID clientId = getClientID(theExchange);

		// Get local client reasoner's version
		int version = getClientState(projectId, clientId).version();

		theExchange.setStatusCode(OK);
		theExchange.getResponseHeaders().put(CONTENT_TYPE, PLAIN_TEXT_UTF_8.toString());
		theExchange.getResponseSender().send(valueOf(version));
		theExchange.endExchange();
	}
}
