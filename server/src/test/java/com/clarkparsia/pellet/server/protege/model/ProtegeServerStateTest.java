package com.clarkparsia.pellet.server.protege.model;

import com.clarkparsia.pellet.server.model.OntologyState;
import com.clarkparsia.pellet.server.protege.ProtegeServerTest;
import com.clarkparsia.pellet.server.protege.TestProtegeServerConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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
		mServerState = new ProtegeServerState(new TestProtegeServerConfiguration(Lists.<String>newArrayList()));
		assertTrue(mServerState.ontologies().isEmpty());
		createOwl2Ontology(mServerState.managerClient);
		createAgenciesOntology(mServerState.managerClient);
		mServerState.close();
		mServerState = new ProtegeServerState(new TestProtegeServerConfiguration(ONTOLOGIES));
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
			Optional<OntologyState> state = mServerState.getOntology(ontologyIRI);
			assertTrue(state.isPresent());
			assertEquals(ontologyIRI, state.get().getIRI());
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
		for (OntologyState aState : state.ontologies()) {
			assertTrue(Files.exists(((ProtegeOntologyState) aState).getPath().resolveSibling("HEAD")));
			assertTrue(Files.exists(((ProtegeOntologyState) aState).getPath()));
		}
	}
}
