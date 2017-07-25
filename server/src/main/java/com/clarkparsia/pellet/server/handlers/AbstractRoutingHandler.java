package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.server.PelletServer;
import com.clarkparsia.pellet.server.exceptions.ServerException;
import com.clarkparsia.pellet.server.protege.ClientState;
import com.clarkparsia.pellet.server.protege.ProtegeOntologyState;
import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.clarkparsia.pellet.service.PelletServiceConstants;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.impl.ProjectIdImpl;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import io.undertow.util.StatusCodes;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract handler with tools for wrapping and setting up HttpHandlers implementing reasoner's functionality.
 *
 * @author Edgar Rodriguez-Diaz
 */
public abstract class AbstractRoutingHandler implements RoutingHandler {
	public static String REASONER_PATH = PelletServer.ROOT_PATH + "reasoner";

	private final String path;
	private final String method;
	private final ProtegeServerState serverState;

	protected final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	public AbstractRoutingHandler(final String method,
	                              final String path,
	                              final ProtegeServerState serverState) {
		this.serverState = serverState;
		this.method = method;
		this.path = REASONER_PATH + "/" + path;
	}

	@Override
	public final String getMethod() {
		return method;
	}

	@Override
	public final String getPath() {
		return path;
	}


	protected ProtegeServerState getServerState() {
		return serverState;
	}

	protected ClientState getClientState(ProjectId projectId, final UUID clientId) throws ServerException {
		Optional<ProtegeOntologyState> aOntoState = getServerState().getOntology(projectId);
		if (!aOntoState.isPresent()) {
			throw new ServerException(StatusCodes.NOT_FOUND, "Project not found: " + projectId);
		}

		return aOntoState.get().getClient(clientId);
	}

	protected static ProjectId getProjectId(final HttpServerExchange exchange) throws ServerException {
		final String projectId = exchange.getRequestHeaders().getFirst(PelletServiceConstants.PROJECT_ID_HEADER);
		if (projectId == null) {
			throw new ServerException(StatusCodes.BAD_REQUEST,
				"Header " + PelletServiceConstants.PROJECT_ID_HEADER + " doesn't exist");
		} else {
			return new ProjectIdImpl(projectId);
		}
	}

	protected static UUID getClientID(final HttpServerExchange exchange) throws ServerException {
		try {
			return UUID.fromString(getQueryParameter(exchange, "client"));
		}
		catch (IllegalArgumentException e) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "Error parsing Client ID - must be a UUID", e);
		}
	}

	protected static String getQueryParameter(final HttpServerExchange exchange,
																						final String paramName) throws ServerException {
		final Map<String, Deque<String>> queryParams = exchange.getQueryParameters();

		if (!queryParams.containsKey(paramName) || queryParams.get(paramName).isEmpty()) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "Missing required parameter: "+ paramName);
		}

		final String paramVal = queryParams.get(paramName).getFirst();
		if (Strings.isNullOrEmpty(paramVal)) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "Missing required parameter: " + paramName);
		}

		return paramVal;
	}


	protected Set<OWLAxiom> readAxioms(final InputStream inputStream) throws ServerException {
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(inputStream);

			return ontology.getAxioms();
		}
		catch (OWLOntologyCreationException e) {
			throw new ServerException(400, "There was an error parsing axioms", e);
		}
		catch (Exception e) {
			throw new ServerException(500, "There was an IO error while reading input stream", e);
		}
		finally {
			try {
				inputStream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			if (ontology != null) {
				OWL.manager.removeOntology(ontology);
			}
		}
	}
}
