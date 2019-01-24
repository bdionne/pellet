package com.clarkparsia.pellet.server.protege;

import com.clarkparsia.pellet.server.PelletSettings;
import com.clarkparsia.pellet.server.ProtegeSettings;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.impl.ProjectIdImpl;
import org.protege.editor.owl.client.LocalHttpClient;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.nio.file.Paths;
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
public final class ProtegeServerState implements AutoCloseable {

	private static final Logger LOGGER = Logger.getLogger(ProtegeServerState.class.getName());
	private final OntologyProvider ontologyProvider;
	private final ProtegeSettings protegeSettings;
	protected OWLOntologyManager manager;

	private LocalHttpClient client;

	/**
	 * Lock to control reloads of the state
	 */
	private ReentrantLock updateLock;
	private Map<IRI, ProtegeOntologyState> ontologies;
	private final PelletSettings pelletSettings;

	@Inject
	public ProtegeServerState(final PelletSettings pelletSettings,
														final ProtegeSettings protegeSettings,
														final OntologyProvider ontologyProvider) throws Exception {
		this.pelletSettings = pelletSettings;
		this.protegeSettings = protegeSettings;
		this.ontologyProvider = ontologyProvider;
		start();
	}

	public void start() {
		this.updateLock = new ReentrantLock();
		this.manager = OWLManager.createOWLOntologyManager();
		this.ontologies = Maps.newConcurrentMap();
		this.client = ProtegeServiceUtils.connect(protegeSettings);

		Set<String> onts = this.ontologyProvider.classifiableProjects();
		for (String ont : onts) {
			try {
				addOntology(ont);
			} catch (OWLOntologyCreationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean update() {
		try {
			if (updateLock.tryLock(1, TimeUnit.SECONDS)) {
				boolean updated = false;
				for (ProtegeOntologyState ontState : ontologies()) {
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

	public Optional<ProtegeOntologyState> getOntology(IRI ontology) {
		return Optional.fromNullable(ontologies.get(ontology));
	}

	public ProtegeOntologyState addOntology(final String ontologyPath) throws OWLOntologyCreationException {
		ProtegeOntologyState result;
		LOGGER.info("Loading ontology " + ontologyPath);

		try {
			ProjectId projectID = new ProjectIdImpl(ontologyPath);
			result = new ProtegeOntologyState(client, projectID,
				Paths.get(pelletSettings.home()).resolve(projectID.get()).resolve("reasoner_state.bin"));
			LOGGER.info("Loaded revision " + result.getVersion());
			if (result.isSnapShotLoaded()) {
				result.update();
			} else {
				result.update();
				ontologies.put(result.getIRI().get(), result);
				removeOntology(result.getIRI().get());
				
				result = new ProtegeOntologyState(client, projectID,
						Paths.get(pelletSettings.home()).resolve(projectID.get()).resolve("reasoner_state.bin"));
								
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			throw new OWLOntologyCreationException("Could not load ontology from Protege server: " + ontologyPath, e);
		}
		if (result.getIRI().isPresent()) {
			ontologies.put(result.getIRI().get(), result);
			return result;
		} else {
			throw new RuntimeException("Failed to get IRI for " + ontologyPath);
		}
	}

	public boolean removeOntology(final IRI ontology) {
		ProtegeOntologyState state = ontologies.remove(ontology);
		boolean removed = (state != null);
		if (removed) {
			state.close();
		}
		return removed;
	}

	public Collection<ProtegeOntologyState> ontologies() {
		return Collections.unmodifiableCollection(ontologies.values());
	}

	public void save() {
		for (ProtegeOntologyState aOntoState : ontologies()) {
			aOntoState.save();
		}
	}

	@Override
	public void close() {
		for (ProtegeOntologyState ontology : ontologies()) {
			ontology.close();
		}
	}
}
