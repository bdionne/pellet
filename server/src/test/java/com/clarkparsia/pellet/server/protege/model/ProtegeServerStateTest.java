package com.clarkparsia.pellet.server.protege.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.clarkparsia.pellet.server.model.OntologyState;
import com.clarkparsia.pellet.server.protege.ProtegeServerTest;
import com.clarkparsia.pellet.server.protege.TestProtegeServerConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
		recreateServerState(Lists.<String>newArrayList());
		createOwl2Ontology(mServerState.managerClient);
		createAgenciesOntology(mServerState.managerClient);
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
		recreateServerState(ONTOLOGIES);
		assertOntologies(ONTOLOGIES1);
	}

	@Test
	public void addRemoveOntologies() throws Exception {
		for (String ontology : ONTOLOGIES) {
			assertNotNull(mServerState.addOntology(ontology));
		}
		assertOntologies(ONTOLOGIES1);

		for (String s : ONTOLOGIES1) {
			assertTrue(mServerState.removeOntology(IRI.create(s)));
		}
		assertOntologies(Lists.<String>newArrayList());
	}

	@Test
	public void removeNotExists() throws Exception {
		recreateServerState(ONTOLOGIES);
		assertFalse(mServerState.removeOntology(IRI.create("http://www.example.com/does-not-exist")));
	}

	@Test
	public void shouldSaveOntologyStates() throws Exception {
		recreateServerState(ONTOLOGIES);
		mServerState.save();

		assertFalse(mServerState.ontologies().isEmpty());
		assertOntologyFilesExist(mServerState);
	}

	@Test
	public void shouldSaveAndLoadOntologyStates() throws Exception {
		recreateServerState(ONTOLOGIES);
		assertFalse(mServerState.ontologies().isEmpty());

		assertOntologyFilesExist(mServerState);
		assertEquals(2, mServerState.ontologies().size());
	}

	private void assertOntologyFilesExist(ProtegeServerState state) throws IOException {
		for (OntologyState aState : state.ontologies()) {
			assertTrue(Files.exists(getOntologyHEAD(aState)));
			assertTrue(Files.exists(getOntologyReasoner(aState)));
		}
	}
}
