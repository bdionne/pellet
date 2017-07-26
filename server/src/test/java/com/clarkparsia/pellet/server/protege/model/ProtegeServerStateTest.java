package com.clarkparsia.pellet.server.protege.model;

import com.clarkparsia.pellet.server.PelletServerModule;
import com.clarkparsia.pellet.server.ProtegeSettings;
import com.clarkparsia.pellet.server.TestModule;
import com.clarkparsia.pellet.server.protege.ProtegeOntologyState;
import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.clarkparsia.pellet.server.protege.ProtegeServerTest;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.PlainPassword;
import edu.stanford.protege.metaproject.api.PolicyFactory;
import edu.stanford.protege.metaproject.api.UserId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.protege.editor.owl.client.LocalHttpClient;
import org.semanticweb.owlapi.model.IRI;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class ProtegeServerStateTest extends ProtegeServerTest {

	public static final List<String> ONTOLOGIES = Lists.<String>newArrayList(OWL2_ONT, AGENCIES_ONT);
	public String agencies;
	public String owl2;
	ProtegeServerState mServerState;

	@Before
	public void before() throws Exception {
		super.before();

		Injector injector = Guice.createInjector(Modules.override(new PelletServerModule())
			.with(new TestModule(Lists.<String>newArrayList())));

		mServerState = (ProtegeServerState) injector.getInstance(ProtegeServerState.class);
		assertTrue(mServerState.ontologies().isEmpty());

		PolicyFactory f = ConfigurationManager.getFactory();
		UserId managerId = f.getUserId("bob");
		PlainPassword managerPassword = f.getPlainPassword("bob");
		ProtegeSettings protegeSettings = injector.getInstance(ProtegeSettings.class);
		LocalHttpClient managerClient = new LocalHttpClient(managerId.get(),
			managerPassword.getPassword(),
			protegeSettings.host() + ":8081");

		owl2 = createOwl2Ontology(managerClient).projectIRI().toString();
		agencies = createAgenciesOntology(managerClient).projectIRI().toString();

		mServerState.close();

		injector = Guice.createInjector(Modules.override(new PelletServerModule())
			.with(new TestModule(ONTOLOGIES)));
		mServerState = (ProtegeServerState) injector.getInstance(ProtegeServerState.class);
	}

	@After
	public void after() throws Exception {
		super.after();
		mServerState.close();
	}

	private void assertOntologies(List<String> ontologies) {
		assertEquals(ontologies.size(), mServerState.ontologies().size());
		for (String ontology : ontologies) {
			IRI ontologyIRI = IRI.create(ontology);
			Optional<ProtegeOntologyState> state = mServerState.getOntology(ontologyIRI);
			assertTrue(state.isPresent());
			assertEquals(ontologyIRI, state.get().getIRI());
		}
	}

	@Test
	public void shouldHaveOntologies() throws Exception {
		assertOntologies(Lists.newArrayList(owl2, agencies));
	}

	@Test
	public void addRemoveOntologies() throws Exception {
		for (String s : Lists.newArrayList(owl2, agencies)) {
			assertTrue(mServerState.removeOntology(IRI.create(s)));
		}
		assertOntologies(Lists.<String>newArrayList());
	}

	@Test
	public void removeNotExists() throws Exception {
		assertFalse(mServerState.removeOntology(IRI.create("http://www.example.com/does-not-exist")));
	}

	@Test
	public void shouldSaveOntologyStates() throws Exception {
		mServerState.save();
		assertFalse(mServerState.ontologies().isEmpty());
		assertOntologyFilesExist(mServerState);
	}

	@Test
	public void shouldSaveAndLoadOntologyStates() throws Exception {
		assertFalse(mServerState.ontologies().isEmpty());
		assertOntologyFilesExist(mServerState);
		assertEquals(2, mServerState.ontologies().size());
	}

	private void assertOntologyFilesExist(ProtegeServerState state) throws IOException {
		for (ProtegeOntologyState aState : state.ontologies()) {
			assertTrue(((ProtegeOntologyState) aState).revisionFile().exists());
			assertTrue(Files.exists(((ProtegeOntologyState) aState).path));
		}
	}
}
