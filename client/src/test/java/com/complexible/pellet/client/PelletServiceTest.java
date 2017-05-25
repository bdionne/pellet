package com.complexible.pellet.client;

import com.clarkparsia.owlapiv3.OWL;
import edu.stanford.protege.metaproject.ConfigurationManager;
import org.junit.Before;
import org.junit.Test;
import org.protege.editor.owl.client.api.Client;
import org.protege.editor.owl.client.util.ClientUtils;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.policy.CommitBundleImpl;
import org.protege.editor.owl.server.versioning.Commit;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class PelletServiceTest extends PelletClientTest {
	private UUID ID = UUID.randomUUID();

	private IRI agencyOntId;
	private IRI owl2OntId;

	@Before
	public void before() throws Exception {
		super.before();

		// create test ontology
		agencyOntId = createAgenciesOntology(managerClient);
		owl2OntId = createOwl2Ontology(managerClient);

		startPelletServer(AGENCIES_ONT);
	}

	@Test
	public void shouldUpdateWithEmptySets() throws Exception {
		PelletService aService = serviceProvider.get();

		ClientTools.executeCall(aService.insert(agencyOntId, ID, OWL.Ontology()));
		ClientTools.executeCall(aService.delete(agencyOntId, ID, OWL.Ontology()));
	}

	@Test
	public void shouldGetVersionFromClient() throws Exception {
		PelletService aService = serviceProvider.get();

		Call<Integer> aVersionCall = aService.version(agencyOntId, ID);

		int aVersion = ClientTools.executeCall(aVersionCall);

		assertEquals(0, aVersion);

		OWLOntology ont = OWL.manager.createOntology(agencyOntId);
		Commit commit = ClientUtils.createCommit(mClient, "comment", Arrays.<OWLOntologyChange>asList(new AddAxiom(ont, OWL.subClassOf(OWL.Nothing, OWL.Thing))));
		CommitBundle commitBundle = new CommitBundleImpl(DocumentRevision.START_REVISION, commit);
		mClient.commit(ConfigurationManager.getFactory().getProjectId(AGENCIES_ONT), commitBundle);

		pelletServer.getState().update();

		aVersionCall = aService.version(agencyOntId, UUID.randomUUID());

		aVersion = ClientTools.executeCall(aVersionCall);

		assertEquals(1, aVersion);
	}

	@Test
	public void restartPellet() {
		PelletService aService = serviceProvider.get();
		Integer v1 = ClientTools.executeCall(aService.version(agencyOntId, ID));
		ClientTools.executeCall(serviceProvider.get().restart());
		Integer v2 = ClientTools.executeCall(aService.version(agencyOntId, ID));
		assertEquals(v1, v2);
	}

	@Test
	public void ontologyAddDelete() throws Exception {
		PelletService aService = serviceProvider.get();

		ClientTools.executeCall(aService.load(OWL2_ONT));

		ClientTools.executeCall(aService.unload(owl2OntId));

		Response<Void> aResp = aService.unload(owl2OntId).execute();
		assertEquals(400, aResp.code());
		assertEquals("Ontology not found: " + owl2OntId, aResp.message());
	}
}
