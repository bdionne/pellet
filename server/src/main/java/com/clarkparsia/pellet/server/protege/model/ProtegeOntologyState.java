// Copyright (c) 2006 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.pellet.server.protege.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.clarkparsia.owlapiv3.OntologyUtils;
import com.clarkparsia.pellet.server.model.impl.OntologyStateImpl;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.impl.ProjectIdImpl;
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
import org.semanticweb.owlapi.model.SetOntologyID;

/**
 * @author Evren Sirin
 */
public class ProtegeOntologyState extends OntologyStateImpl {
	public static final Logger LOGGER = Logger.getLogger(ProtegeOntologyState.class.getName());

	private final LocalHttpClient client;

	private final ProjectId projectId;
	
	private final ServerDocument remoteOnt;

	private DocumentRevision revision;

	private boolean snapshotLoaded = false;

	public ProtegeOntologyState(final LocalHttpClient client,
	                            final ProjectId projectId,
	                            final Path path) throws IOException, ClientRequestException, AuthorizationException {
		super(path);

		this.client = client;
		this.projectId = projectId;
		this.remoteOnt = client.openProject(projectId);;
		this.revision = readRevision(path);
		this.snapshotLoaded = revision.getRevisionNumber() > 0;
		writeRevision();
	}

	private static File revisionFile(final Path path) throws IOException {
		return path.resolveSibling("HEAD").toFile();
	}

	private static DocumentRevision readRevision(final Path path) throws IOException {
		final File aHeadFile = revisionFile(path);

		if (aHeadFile.exists()) {
			return DocumentRevision.create(Integer.parseInt(Files.toString(aHeadFile, Charsets.UTF_8)));
		}

		return DocumentRevision.START_REVISION;
	}

	private void writeRevision() throws IOException {
		final File aHeadFile = revisionFile(getPath());
		Files.write(String.valueOf(getVersion()), aHeadFile, Charsets.UTF_8);
	}

	@Override
	protected boolean updateOntology(OWLOntology ontology) {
		try {
			boolean loadSnapshot = !snapshotLoaded;
			if (loadSnapshot) {
				SnapShot snapshot = client.getSnapShot(projectId);
				OWLOntology snapshotOnt = snapshot.getOntology();
				MANAGER.addAxioms(ontology, snapshotOnt.getAxioms());
				MANAGER.applyChange(new SetOntologyID(ontology, snapshotOnt.getOntologyID()));
				snapshotLoaded = true;
			}

			ChangeHistory history = client.getLatestChanges(remoteOnt, revision);
			boolean update = !history.isEmpty();
			if (update) {
				DocumentRevision headRevision = history.getHeadRevision();

				LOGGER.info("Updating " + this + " from " + revision + " to " + headRevision);

				ClientUtils.updateOntology(ontology, history, ontology.getOWLOntologyManager());

				revision = headRevision;
			}

			return loadSnapshot || update;
		}
		catch (Exception e) {
			LOGGER.warning("Cannot retrieve changes from the server");
			return false;
		}
	}

	@Override
	protected int getVersion() {
		return revision.getRevisionNumber();
	}

	@Override
	public void save() {
		super.save();

		try {
			writeRevision();
		}
		catch (IOException theE) {
			LOGGER.log(Level.SEVERE, "Couldn't save the ontology state " + getIRI().toQuotedString(), theE);
		}
	}

	@Override
	public String toString() {
		IRI iri = getIRI();
		return "State(" + (iri == null ? "Anonymous ontology" : iri) + ")";
	}
}