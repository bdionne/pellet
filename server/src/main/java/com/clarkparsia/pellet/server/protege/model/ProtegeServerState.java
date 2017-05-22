package com.clarkparsia.pellet.server.protege.model;

import com.clarkparsia.pellet.server.Configuration;
import com.clarkparsia.pellet.server.ConfigurationReader;
import com.clarkparsia.pellet.server.model.OntologyState;
import com.clarkparsia.pellet.server.model.ServerState;
import com.clarkparsia.pellet.server.model.impl.OntologyStateImpl;
import com.clarkparsia.pellet.server.protege.ProtegeServiceUtils;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.PlainPassword;
import edu.stanford.protege.metaproject.api.PolicyFactory;
import edu.stanford.protege.metaproject.api.UserId;
import org.protege.editor.owl.client.LocalHttpClient;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Edgar Rodriguez-Diaz
 */
@Singleton
public final class ProtegeServerState implements ServerState {

	private static final Logger LOGGER = Logger.getLogger(ProtegeServerState.class.getName());
	protected final OWLOntologyManager manager;

	private final LocalHttpClient mClient;
	final LocalHttpClient managerClient;

	/**
	 * Lock to control reloads of the state
	 */
	private final ReentrantLock updateLock = new ReentrantLock();
	private final Map<IRI, OntologyState> ontologies;

	@Inject
	public ProtegeServerState(final Configuration theConfig) throws Exception {
		this(ConfigurationReader.of(theConfig));
	}

	ProtegeServerState(final ConfigurationReader theConfigReader) throws Exception {
		this.manager = OWLManager.createOWLOntologyManager();

		this.ontologies = Maps.newConcurrentMap();
		for (OntologyState ontoState : ImmutableSet.<OntologyState>of()) {
			this.ontologies.put(ontoState.getIRI(), ontoState);
		}

		mClient = ProtegeServiceUtils.connect(theConfigReader);

		PolicyFactory f = ConfigurationManager.getFactory();
		UserId managerId = f.getUserId("bob");
		PlainPassword managerPassword = f.getPlainPassword("bob");
		managerClient = new LocalHttpClient(managerId.get(), managerPassword.getPassword(), "http://localhost:8081");

		Set<String> onts = theConfigReader.protegeSettings().ontologies();
		for (String ont : onts) {
			addOntology(ont);
		}
	}

	@Override
	public boolean update() {
		try {
			if (updateLock.tryLock(1, TimeUnit.SECONDS)) {
				boolean updated = false;
				for (OntologyState ontState : ontologies()) {
					updated |= ontState.update();
				}
				return updated;
			}
			else {
				LOGGER.info("Skipping update, there's another state update still happening");
			}
		}
		catch (InterruptedException ie) {
			LOGGER.log(Level.SEVERE, "Something interrupted a Server State update", ie);
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not refresh Server State from Protege", e);
		}
		finally {
			if (updateLock.isHeldByCurrentThread()) {
				updateLock.unlock();
			}
		}

		return false;
	}

	public LocalHttpClient getClient() {
		return mClient;
	}

	@Override
	public Optional<OntologyState> getOntology(IRI ontology) {
		return Optional.fromNullable(ontologies.get(ontology));
	}

	@Override
	public OntologyState addOntology(final String ontologyPath) throws OWLOntologyCreationException {
		OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(ontologyPath));
		OntologyState state = new OntologyStateImpl(ont);
		ontologies.put(state.getIRI(), state);
		return state;
	}

	@Override
	public boolean removeOntology(final IRI ontology) {
		OntologyState state = ontologies.remove(ontology);
		boolean removed = (state != null);
		if (removed) {
			state.close();
		}
		return removed;
	}

	@Override
	public Collection<OntologyState> ontologies() {
		return Collections.unmodifiableCollection(ontologies.values());
	}

	@Override
	public void save() {
		for (OntologyState aOntoState : ontologies()) {
			aOntoState.save();
		}
	}

	@Override
	public void close() throws Exception {
		for (OntologyState ontology : ontologies()) {
			ontology.close();
		}
	}
}
