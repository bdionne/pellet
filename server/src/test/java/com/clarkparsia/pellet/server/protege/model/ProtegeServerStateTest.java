package com.clarkparsia.pellet.server.protege.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.clarkparsia.pellet.server.model.OntologyState;
import com.clarkparsia.pellet.server.protege.ProtegeServerTest;
import com.clarkparsia.pellet.server.protege.TestProtegeServerConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.protege.editor.owl.client.LocalHttpClient;
import org.semanticweb.owlapi.model.IRI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class ProtegeServerStateTest extends ProtegeServerTest {

	ProtegeServerState mServerState;

	public ProtegeServerStateTest() {
		super();
	}

	@Before
	public void before() throws Exception {
		super.before();
		recreateServerState(Lists.<String>newArrayList());
	}

	@After
	public void after() throws Exception {
		super.after();
		mServerState.close();
	}

	private void recreateServerState(List<String> ontologies) throws Exception {
		if (mServerState != null) {
			mServerState.close();
		}
		mServerState = new ProtegeServerState(new TestProtegeServerConfiguration(ontologies));
	}

	@Test
	public void shouldBeEmpty() throws Exception {
		assertTrue(mServerState.ontologies().isEmpty());
	}

	private void loadOntologies(final LocalHttpClient theClient) throws Exception {
		// create ontologies
		createOwl2Ontology(theClient);
		createAgenciesOntology(theClient);
	}

	private Path getOntologyHEAD(final OntologyState theState) throws IOException {
		return ((ProtegeOntologyState) theState).getPath().resolveSibling("HEAD");
	}

	private Path getOntologyReasoner(final OntologyState theState) throws IOException {
		return ((ProtegeOntologyState) theState).getPath();
	}

	private void assertOntologies(List<String> ontologies) {
		assertEquals(ontologies.size(), mServerState.ontologies().size());

		for (String ontology : ontologies) {
			IRI ontologyIRI = IRI.create(ontology);
			Optional<OntologyState> state = mServerState.getOntology(ontologyIRI);
			assertTrue(state.isPresent());
			assertEquals(ontologyIRI, state.get().getIRI());
		}
	}

	@Test
	public void shouldHaveOntologies() throws Exception {
		// create ontologies
		loadOntologies(mServerState.managerClient);
		recreateServerState(Lists.<String>newArrayList(OWL2_ONT, AGENCIES_ONT));
		assertOntologies(Lists.newArrayList("http://www.example.org/test", "http://www.owl-ontologies.com/unnamed.owl"));
	}

	@Test
	public void addRemoveOntologies() throws Exception {
		// create ontologies
		loadOntologies(mServerState.managerClient);

		OntologyState s = mServerState.addOntology(OWL2_ONT);
		assertOntologies(Lists.newArrayList("http://www.example.org/test"));
		assertNotNull(s);

		s = mServerState.addOntology(AGENCIES_ONT);
		assertOntologies(Lists.newArrayList("http://www.example.org/test", "http://www.owl-ontologies.com/unnamed.owl"));
		assertNotNull(s);

		boolean removed = mServerState.removeOntology(IRI.create("http://www.example.org/test"));
		assertOntologies(Lists.newArrayList("http://www.owl-ontologies.com/unnamed.owl"));
		assertTrue(removed);

		removed = mServerState.removeOntology(IRI.create("http://www.example.com/does-not-exist"));
		assertOntologies(Lists.newArrayList("http://www.owl-ontologies.com/unnamed.owl"));
		assertFalse(removed);

		removed = mServerState.removeOntology(IRI.create("http://www.owl-ontologies.com/unnamed.owl"));
		assertOntologies(Lists.<String>newArrayList());
		assertTrue(removed);
	}

	@Test
	public void shouldSaveOntologyStates() throws Exception {
		loadOntologies(mServerState.managerClient);
		recreateServerState(Lists.<String>newArrayList(OWL2_ONT, AGENCIES_ONT));
		mServerState.save();

		assertFalse(mServerState.ontologies().isEmpty());

		for (OntologyState aState : mServerState.ontologies()) {
			assertTrue(Files.exists(getOntologyHEAD(aState)));
			assertTrue(Files.exists(getOntologyReasoner(aState)));
		}
	}

	@Test
	public void shouldSaveAndLoadOntologyStates() throws Exception {
		loadOntologies(mServerState.managerClient);
		recreateServerState(Lists.<String>newArrayList(OWL2_ONT, AGENCIES_ONT));
		mServerState.save();

		assertFalse(mServerState.ontologies().isEmpty());

		for (OntologyState aState : mServerState.ontologies()) {
			assertTrue(Files.exists(getOntologyHEAD(aState)));
			assertTrue(Files.exists(getOntologyReasoner(aState)));
		}

		recreateServerState(Lists.<String>newArrayList(OWL2_ONT, AGENCIES_ONT));

		assertFalse(mServerState.ontologies().isEmpty());

		int requiredChecks = 0;
		for (OntologyState aState : mServerState.ontologies()) {
			assertTrue(Files.exists(getOntologyHEAD(aState)));
			assertTrue(Files.exists(getOntologyReasoner(aState)));
			requiredChecks++;
		}

		// check that the 2 loaded ontologies exist
		assertEquals(2, requiredChecks);
	}

}
