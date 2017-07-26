package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.owlapiv3.IRIUtils;
import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.server.PelletServer;
import com.clarkparsia.pellet.server.exceptions.ServerException;
import com.clarkparsia.pellet.server.protege.ClientState;
import com.clarkparsia.pellet.server.protege.ProtegeOntologyState;
import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.clarkparsia.pellet.service.PelletServiceConstants;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import okhttp3.HttpUrl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

	protected final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	public AbstractRoutingHandler(final String theMethod,
	                              final String thePath,
	                              final ProtegeServerState theServerState) {
		serverState = theServerState;
		mMethod = theMethod;
		mPath = REASONER_PATH + (Strings.isNullOrEmpty(thePath) ? "" : "/" + thePath);
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
		String rawIRI = theExchange.getQueryParameters().get(PelletServiceConstants.ONTOLOGY).getFirst();
		if (rawIRI == null) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "ontology query parameter not provided");
		}
		try {
			rawIRI = URLDecoder.decode(rawIRI, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "ontology query parameter not decodeable");
		}
		HttpUrl rawIRIUrl = HttpUrl.parse(rawIRI);
		if (rawIRIUrl.queryParameter(PelletServiceConstants.PROJECT_ID) != null) {
			return IRI.create(rawIRIUrl.uri());
		}

		final String projectId = theExchange.getRequestHeaders().getFirst(PelletServiceConstants.PROJECT_ID_HEADER);
		if (projectId == null) {
			throw new ServerException(StatusCodes.BAD_REQUEST, "Header "
				+ PelletServiceConstants.PROJECT_ID_HEADER + " not provided");
		}
		return IRIUtils.addProjectId(IRI.create(rawIRI), projectId);
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


	protected Set<OWLAxiom> readAxioms(final InputStream theInStream) throws ServerException {
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(theInStream);

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
				theInStream.close();
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
