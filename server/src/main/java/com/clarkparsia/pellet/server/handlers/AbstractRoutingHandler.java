package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeOntologyState;
import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.server.PelletServer;
import com.clarkparsia.pellet.server.exceptions.ServerException;
import com.clarkparsia.pellet.server.protege.ClientState;
import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.common.base.Strings;
import com.google.common.base.Optional;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import io.undertow.util.StatusCodes;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

	private final String mPath;
	private final String mMethod;
	private final ProtegeServerState serverState;

	public AbstractRoutingHandler(final String theMethod,
	                              final String thePath,
	                              final ProtegeServerState theServerState) {
		serverState = theServerState;
		mMethod = theMethod;
		mPath = REASONER_PATH + "/" + thePath;
	}

	@Override
	public final String getMethod() {
		return mMethod;
	}

	@Override
	public final String getPath() {
		return mPath;
	}


	protected ProtegeServerState getServerState() {
		return serverState;
	}

	protected ClientState getClientState(final IRI theOntology, final UUID theClientId) throws ServerException {
		Optional<ProtegeOntologyState> aOntoState = getServerState().getOntology(theOntology);
		if (!aOntoState.isPresent()) {
			throw new ServerException(StatusCodes.NOT_FOUND, "Ontology not found: " + theOntology);
		}

		return aOntoState.get().getClient(theClientId);
	}

	protected static IRI getOntology(final HttpServerExchange theExchange) throws ServerException {
		try {
			return IRI.create(URLDecoder.decode(theExchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY)
					.getParameters().get("ontology"), StandardCharsets.UTF_8.name()));
		}
		catch (Exception theE) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "Error parsing Ontology IRI", theE);
		}
	}

	protected static UUID getClientID(final HttpServerExchange theExchange) throws ServerException {
		try {
			return UUID.fromString(getQueryParameter(theExchange, "client"));
		}
		catch (IllegalArgumentException theE) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "Error parsing Client ID - must be a UUID", theE);
		}
	}

	protected static String getQueryParameter(final HttpServerExchange theExchange,
																						final String paramName) throws ServerException {
		final Map<String, Deque<String>> queryParams = theExchange.getQueryParameters();

		if (!queryParams.containsKey(paramName) || queryParams.get(paramName).isEmpty()) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "Missing required parameter: "+ paramName);
		}

		final String paramVal = queryParams.get(paramName).getFirst();
		if (Strings.isNullOrEmpty(paramVal)) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "Missing required parameter: " + paramName);
		}

		return paramVal;
	}
}
