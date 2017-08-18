// Copyright (c) 2006 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.pellet.protege;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.clarkparsia.pellet.service.reasoner.SchemaReasonerFactory;
import com.complexible.pellet.client.ClientModule;
import com.complexible.pellet.client.ClientTools;
import com.complexible.pellet.client.PelletService;
import com.complexible.pellet.client.reasoner.SchemaOWLReasoner;
import com.google.common.base.Optional;
import com.google.inject.Guice;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.owl.client.ClientSession;
import org.protege.editor.owl.client.LocalHttpClient;
import org.protege.editor.owl.client.util.ClientUtils;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.server.http.ServerProperties;
import org.protege.editor.owl.server.versioning.api.VersionedOWLOntology;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * @author Evren Sirin
 */
public class RemotePelletReasonerFactory implements OWLReasonerFactory {
	public static final Logger LOGGER = Logger.getLogger(RemotePelletReasonerFactory.class.getName());

	private final SchemaReasonerFactory factory;
	private final OWLModelManager modelManager;
	private ClientSession connectionManager;

	public RemotePelletReasonerFactory(final SchemaReasonerFactory factory, final OWLModelManager modelManager) {
		this.factory = factory;
		this.modelManager = modelManager;
	}

	@Override
	public String getReasonerName() {
		return "Pellet Schema Reasoner";
	}

	private SchemaOWLReasoner createReasoner(final OWLOntology ontology, final BufferingMode bufferingMode) {
		SchemaOWLReasoner reasoner = new SchemaOWLReasoner(ontology, factory, bufferingMode);

		if (connectionManager == null) {
			connectionManager = modelManager.get(ClientSession.ID);

			System.out.println("No connection manager can be found");
		}
		else {
			VersionedOWLOntology vont = connectionManager.getActiveVersionOntology();
			if (vont != null) {
				try {
					// FIXME also compare the vont version with the remote version
					List<OWLOntologyChange> uncommitted = ClientUtils.getUncommittedChanges(modelManager.getHistoryManager(), ontology, vont.getChangeHistory());
					LOGGER.info("There are " + uncommitted.size() + " uncommitted change(s)");
					if (!uncommitted.isEmpty()) {
						LOGGER.info("Sending " + uncommitted.size() + " uncommitted changes to the remote server");

						reasoner.getListener().ontologiesChanged(uncommitted);
						reasoner.flush();
					}
				}
				catch (Exception e) {
					LOGGER.log(Level.WARNING, "Cannot synchronize remote reasoner with uncommitted changes", e);
				}
			}
		}

		final List<EditorKit> editorKits = ProtegeManager.getInstance().getEditorKitManager().getEditorKits();
		if (editorKits.isEmpty()) {
			throw new RuntimeException("no editor kits");
		} else {
			final OWLWorkspace workspace = (OWLWorkspace) editorKits.get(0).getWorkspace();
			workspace.pelletRestartCallback = Optional.of(() -> {
				LocalHttpClient client = (LocalHttpClient) connectionManager.getActiveClient();
				if (!client.isWorkFlowManager(connectionManager.getActiveProject())) {
					throw new RuntimeException("Not a workflow manager");
				}
				final String serverURL = PelletReasonerPreferences.getInstance().getServerURL();
				ServerConfiguration serverConfiguration = client.getConfig().getCurrentConfig();
				String password = serverConfiguration.getProperty(ServerProperties.PELLET_ADMIN_PASSWORD);
				ClientModule clientModule = new ClientModule(serverURL, Optional.of(password));
				PelletService pelletService = Guice.createInjector(clientModule).getInstance(PelletService.class);
				ClientTools.executeCall(pelletService.restart());
				return null;
			});
		}
		return reasoner;
	}
	
	@Override
	public OWLReasoner createNonBufferingReasoner(final OWLOntology ontology) {
		return createReasoner(ontology, BufferingMode.NON_BUFFERING);
	}

	@Override
	public OWLReasoner createReasoner(final OWLOntology ontology) {
		return createReasoner(ontology, BufferingMode.BUFFERING);
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(final OWLOntology ontology,  final OWLReasonerConfiguration owlReasonerConfiguration) {
		return createNonBufferingReasoner(ontology);
	}

	@Override
	public OWLReasoner createReasoner(final OWLOntology ontology, final OWLReasonerConfiguration theOWLReasonerConfiguration) {
		return createReasoner(ontology);
	}

	@Override
	public String toString() {
		return getReasonerName();
	}
}