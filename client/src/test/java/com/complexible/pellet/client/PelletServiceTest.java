package com.complexible.pellet.client;

import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.server.protege.model.ProjectIRI;
import com.google.common.collect.Lists;
import edu.stanford.protege.metaproject.ConfigurationManager;
import org.junit.Before;
import org.junit.Test;
import org.protege.editor.owl.client.util.ClientUtils;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.policy.CommitBundleImpl;
import org.protege.editor.owl.server.versioning.Commit;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.semanticweb.owlapi.model.AddAxiom;
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

	private ProjectIRI agency;
	private ProjectIRI owl;

	@Before
	public void before() throws Exception {
		super.before();

		// create test ontology
		agency = createAgenciesOntology(managerClient);
		owl = createOwl2Ontology(managerClient);

		startPelletServer(Lists.newArrayList(AGENCIES_ONT));
	}

	@Test
	public void shouldUpdateWithEmptySets() throws Exception {
		PelletService aService = serviceProvider.get();

		ClientTools.executeCall(aService.insert(agency.projectIRI(), ID, OWL.Ontology()));
		ClientTools.executeCall(aService.delete(agency.projectIRI(), ID, OWL.Ontology()));
	}

	@Test
	public void shouldGetVersionFromClient() throws Exception {
		PelletService aService = serviceProvider.get();

		Call<Integer> aVersionCall = aService.version(agency.projectIRI(), ID);

		int aVersion = ClientTools.executeCall(aVersionCall);

		assertEquals(0, aVersion);

		OWLOntology ont = OWL.manager.createOntology(agency.projectIRI());
		Commit commit = ClientUtils.createCommit(client, "comment", Arrays.<OWLOntologyChange>asList(new AddAxiom(ont, OWL.subClassOf(OWL.Nothing, OWL.Thing))));
		CommitBundle commitBundle = new CommitBundleImpl(DocumentRevision.START_REVISION, commit);
		client.commit(ConfigurationManager.getFactory().getProjectId(AGENCIES_ONT), commitBundle);

		pelletServer.getState().update();

		aVersionCall = aService.version(agency.projectIRI(), UUID.randomUUID());

		aVersion = ClientTools.executeCall(aVersionCall);

		assertEquals(1, aVersion);
	}

	@Test
	public void restartPellet() {
		PelletService aService = serviceProvider.get();
		Integer v1 = ClientTools.executeCall(aService.version(agency.projectIRI(), ID));
		ClientTools.executeCall(serviceProvider.get().restart());
		Integer v2 = ClientTools.executeCall(aService.version(agency.projectIRI(), ID));
		assertEquals(v1, v2);
	}

	@Test
	public void ontologyAddDelete() throws Exception {
		PelletService aService = serviceProvider.get();

		ClientTools.executeCall(aService.load(agency.projectIRI()));

		ClientTools.executeCall(aService.unload(owl.projectIRI()));

		Response<Void> aResp = aService.unload(owl.projectIRI()).execute();
		assertEquals(400, aResp.code());
		assertEquals("Ontology not found: " + owl.projectIRI(), aResp.message());
	}
}
