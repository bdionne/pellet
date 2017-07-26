package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.model.ServerState;
import com.clarkparsia.pellet.service.messages.JsonMessage;
import com.clarkparsia.pellet.service.reasoner.SchemaReasoner;
import com.google.inject.Inject;
import io.undertow.server.HttpServerExchange;
import org.semanticweb.owlapi.model.IRI;

import java.util.UUID;

public class InferredAxiomsHandler extends AbstractRoutingHandler {
	@Inject
	public InferredAxiomsHandler(final ServerState serverState) {
		super("GET", "{ontology}/inferred_axioms", serverState);
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		final IRI ontology = getOntology(exchange);
		final UUID clientId = getClientID(exchange);

		final SchemaReasoner reasoner = getReasoner(ontology, clientId);

		JsonMessage.writeSubclassSet(reasoner.getInferredAxioms(), exchange.getOutputStream());

		exchange.endExchange();
	}
}
