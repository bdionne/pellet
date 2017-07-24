package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.clarkparsia.pellet.service.reasoner.SchemaReasoner;
import edu.stanford.protege.metaproject.api.ProjectId;
import io.undertow.server.HttpServerExchange;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static io.undertow.util.StatusCodes.OK;

/**
 * @author Evren Sirin
 */
public class ReasonerUpdateHandler extends AbstractRoutingHandler {
	private static final Logger LOGGER = Logger.getLogger(ReasonerUpdateHandler.class.getName());
	private final boolean insert;

	public ReasonerUpdateHandler(final ProtegeServerState theServerState,
	                             final boolean insert) {
		super("POST", "{ontology}/" + (insert ? "insert" : "delete"), theServerState);

		this.insert = insert;
	}

	@Override
	public void handleRequest(final HttpServerExchange theExchange) throws Exception {
		final ProjectId projectId = getProjectId(theExchange);
		final UUID clientId = getClientID(theExchange);

		final SchemaReasoner aReasoner = getClientState(projectId, clientId).getReasoner();

		final Set<OWLAxiom> axioms = readAxioms(theExchange.getInputStream());

		LOGGER.info("Updating client " + clientId + " (+" + axioms.size() + ")");

		if (insert) {
			aReasoner.insert(axioms);
		} else {
			aReasoner.delete(axioms);
		}

		LOGGER.info("Updating client " + clientId + " Success!");

		theExchange.setStatusCode(OK);
		theExchange.endExchange();
	}
}
