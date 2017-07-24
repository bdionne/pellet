package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.clarkparsia.pellet.service.reasoner.SchemaQuery;
import com.clarkparsia.pellet.service.reasoner.SchemaReasoner;
import com.google.inject.Inject;
import edu.stanford.protege.metaproject.api.ProjectId;
import io.undertow.server.HttpServerExchange;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.util.UUID;

import static com.clarkparsia.pellet.service.messages.JsonMessage.readQuery;
import static com.clarkparsia.pellet.service.messages.JsonMessage.writeNodeSet;

/**
 * Specification for {@link SchemaReasoner#query(SchemaQuery)} functionality within
 * the Pellet Server.
 *
 * @author Edgar Rodriguez-Diaz
 */
public class ReasonerQueryHandler extends AbstractRoutingHandler {
	@Inject
	public ReasonerQueryHandler(final ProtegeServerState theServerState) {
		super("POST", "{ontology}/query", theServerState);
	}

	@Override
	public void handleRequest(final HttpServerExchange theExchange) throws Exception {
		final ProjectId projectId = getProjectId(theExchange);
		final UUID clientId = getClientID(theExchange);
		final SchemaQuery query = readQuery(theExchange.getInputStream());
		final SchemaReasoner aReasoner = getClientState(projectId, clientId).getReasoner();
		final NodeSet<? extends OWLObject> result = aReasoner.query(query);

		writeNodeSet(result, theExchange.getOutputStream());

		theExchange.endExchange();
	}
}
