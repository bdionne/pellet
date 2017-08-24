package com.clarkparsia.pellet.server.protege.model;

import com.clarkparsia.pellet.server.PelletServerModule;
import com.clarkparsia.pellet.server.ProtegeSettings;
import com.clarkparsia.pellet.server.TestModule;
import com.clarkparsia.pellet.server.protege.PelletTestModule;
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
import uk.ac.manchester.cs.owl.owlapi.OWLAPIImplModule;
import uk.ac.manchester.cs.owl.owlapi.concurrent.Concurrency;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class ProtegeServerStateTest extends ProtegeServerTest {

	public static final ArrayList<String> ONTOLOGIES = Lists.<String>newArrayList(OWL2_ONT, AGENCIES_ONT);
	public static final String HTTP_WWW_OWL_ONTOLOGIES_COM_UNNAMED_OWL = "http://www.owl-ontologies.com/unnamed.owl";
	public static final String HTTP_WWW_EXAMPLE_ORG_TEST = "http://www.example.org/test";
	public static final ArrayList<String> ONTOLOGIES1 = Lists.newArrayList(
		HTTP_WWW_EXAMPLE_ORG_TEST, HTTP_WWW_OWL_ONTOLOGIES_COM_UNNAMED_OWL);
	ProtegeServerState mServerState;

	@Before
	public void before() throws Exception {
		super.before();

		Injector injector = PelletTestModule.testInjector(Lists.newArrayList());

		mServerState = (ProtegeServerState) injector.getInstance(ProtegeServerState.class);
		assertTrue(mServerState.ontologies().isEmpty());

		PolicyFactory f = ConfigurationManager.getFactory();
		UserId managerId = f.getUserId("bob");
		PlainPassword managerPassword = f.getPlainPassword("bob");
		ProtegeSettings protegeSettings = injector.getInstance(ProtegeSettings.class);
		LocalHttpClient managerClient = new LocalHttpClient(managerId.get(),
			managerPassword.getPassword(),
			protegeSettings.host() + ":8081");

		createOwl2Ontology(managerClient);
		createAgenciesOntology(managerClient);
		mServerState.close();

		injector = PelletTestModule.testInjector(ONTOLOGIES);
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
			assertEquals(ontologyIRI, state.get().getIRI().get());
		}
	}

	@Test
	public void shouldHaveOntologies() throws Exception {
		assertOntologies(ONTOLOGIES1);
	}

	@Test
	public void addRemoveOntologies() throws Exception {
		for (String s : ONTOLOGIES1) {
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
