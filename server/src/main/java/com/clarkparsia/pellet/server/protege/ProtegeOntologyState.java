// Copyright (c) 2006 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.pellet.server.protege;

import com.clarkparsia.modularity.IncrementalReasoner;
import com.clarkparsia.modularity.IncrementalReasonerConfiguration;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.io.Files;
import edu.stanford.protege.metaproject.api.ProjectId;
import org.mindswap.pellet.utils.progress.ConsoleProgressMonitor;
import org.protege.editor.owl.client.LocalHttpClient;
import org.protege.editor.owl.client.api.exception.AuthorizationException;
import org.protege.editor.owl.client.api.exception.ClientRequestException;
import org.protege.editor.owl.client.util.ClientUtils;
import org.protege.editor.owl.server.util.SnapShot;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.ServerDocument;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SetOntologyID;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Evren Sirin
 */
public class ProtegeOntologyState implements AutoCloseable {
	public static final Logger LOGGER = Logger.getLogger(ProtegeOntologyState.class.getName());

	private final LocalHttpClient client;

	private final ProjectId projectId;

	private final ServerDocument remoteOnt;

	private DocumentRevision revision;

	private boolean snapshotLoaded = false;

	protected final OWLOntologyManager manager;

	private final OWLOntology ontology;

	private final IncrementalReasoner reasoner;

	private final LoadingCache<UUID, ClientState> clients;

	public final Path path;

	public ProtegeOntologyState(final OWLOntologyManager manager,
															final LocalHttpClient client,
															final ProjectId projectId,
															final Path path) throws IOException, ClientRequestException, AuthorizationException {
		this.manager = manager;
		this.path = path;

		IncrementalReasonerConfiguration config = IncrementalReasoner.config().manager(manager);
		OWLOntology ont = null;
		if (java.nio.file.Files.exists(path)) {
			config.file(path.toFile());
		} else {
			try {
				if (!java.nio.file.Files.exists(path)) {
					java.nio.file.Files.createDirectories(path.getParent());
				}
				ont = manager.createOntology();
			} catch (Exception e) {
				throw new RuntimeException("Cannot initialize ontology state", e);
			}
		}
		reasoner = config.createIncrementalReasoner(ont);
		reasoner.getReasoner().getKB().setTaxonomyBuilderProgressMonitor(new ConsoleProgressMonitor());
		reasoner.classify();

		ontology = reasoner.getRootOntology();

		clients = initClientCache();

		this.client = client;
		this.projectId = projectId;
		this.remoteOnt = client.openProject(projectId).serverDocument;

		this.revision = readRevision();
		this.snapshotLoaded = revision.getRevisionNumber() > 0;
		writeRevision();
	}

	public File revisionFile() throws IOException {
		return path.resolveSibling("HEAD").toFile();
	}

	private DocumentRevision readRevision() throws IOException {
		final File aHeadFile = revisionFile();

		if (aHeadFile.exists()) {
			return DocumentRevision.create(Integer.parseInt(Files.toString(aHeadFile, Charsets.UTF_8)));
		}

		return DocumentRevision.START_REVISION;
	}

	private void writeRevision() throws IOException {
		final File aHeadFile = revisionFile();
		Files.write(String.valueOf(getVersion()), aHeadFile, Charsets.UTF_8);
	}

	public int getVersion() {
		return revision.getRevisionNumber();
	}

	public ClientState getClient(UUID clientID) {
		try {
			return clients.get(clientID);
		} catch (ExecutionException e) {
			LOGGER.log(Level.SEVERE, "Cannot create state for client " + clientID, e);
			throw new RuntimeException(e);
		}
	}

	public Optional<IRI> getIRI() {
		return ontology.getOntologyID().getOntologyIRI();
	}

	public boolean update() {
		boolean updated;
		try {
			boolean loadSnapshot = !snapshotLoaded;
			if (loadSnapshot) {
				SnapShot snapshot = client.getSnapShot(projectId);
				OWLOntology snapshotOnt = snapshot.getOntology();
				manager.addAxioms(ontology, snapshotOnt.getAxioms());
				manager.applyChange(new SetOntologyID(ontology, snapshotOnt.getOntologyID()));
				snapshotLoaded = true;
			}

			ChangeHistory history = client.getLatestChanges(remoteOnt, revision, projectId);
			boolean update = !history.isEmpty();
			if (update) {
				DocumentRevision headRevision = history.getHeadRevision();

				LOGGER.info("Updating " + this + " from " + revision + " to " + headRevision);

				ClientUtils.updateOntology(ontology, history, manager);

				revision = headRevision;
			}

			updated = loadSnapshot || update;
		} catch (Exception e) {
			// TODO dangerous swallowing of exceptions. should be addressed.
			LOGGER.warning("Cannot retrieve changes from the server");
			updated = false;
		}

		if (updated) {
			LOGGER.info("Classifying updated ontology " + ontology);

			reasoner.classify();

			save();
		}

		return updated;
	}

	public void save() {
		try {
			if (path != null) {
				reasoner.save(path.toFile());
			}
		} catch (IOException theE) {
			LOGGER.log(Level.SEVERE, "Couldn't save the OntologyState " + toString(), theE);
		}

		try {
			writeRevision();
		} catch (IOException theE) {
			LOGGER.log(Level.SEVERE, "Couldn't save the ontology state " + toString(), theE);
		}
	}

	@Override
	public void close() {
		clients.invalidateAll();
		ontology.getOWLOntologyManager().removeOntology(ontology);
		reasoner.dispose();
	}

	@Override
	public String toString() {
		return String.format("ProtegeOntologyState(projectId=%s, iri=%s)", projectId.get(), getIRI());
	}

	private LoadingCache<UUID, ClientState> initClientCache() {
		return CacheBuilder.newBuilder()
			.expireAfterAccess(30, TimeUnit.MINUTES)
			.removalListener(new RemovalListener<UUID, ClientState>() {
				@Override
				public void onRemoval(final RemovalNotification<UUID, ClientState> theRemovalNotification) {
					UUID user = theRemovalNotification.getKey();
					ClientState state = theRemovalNotification.getValue();
					LOGGER.info("Closing client for " + user);
					state.close();
				}
			})
			.build(new CacheLoader<UUID, ClientState>() {
				@Override
				public ClientState load(final UUID user) throws Exception {
					return newClientState(user);
				}
			});
	}

	private synchronized ClientState newClientState(final UUID user) {
		int version = getVersion();
		LOGGER.info("Creating new client for " + user + " with revision " + version);
		return new ClientState(reasoner, version);
	}

	public int hashCode() {
		return this.getIRI().hashCode();
	}

	@Override
	public boolean equals(final Object theOther) {
		if (this == theOther) {
			return true;
		}
		if (!(theOther instanceof ProtegeOntologyState)) {
			return false;
		}

		ProtegeOntologyState otherOntoState = (ProtegeOntologyState) theOther;

		// Just considering for now the ontology IRI to determine equality given
		// that there shouldn't more than one state per ontology.
		return Objects.equals(this.getIRI(), otherOntoState.getIRI());
	}

}