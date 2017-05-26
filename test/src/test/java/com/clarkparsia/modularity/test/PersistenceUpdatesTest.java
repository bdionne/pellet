// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.modularity.test;

import com.clarkparsia.modularity.GraphBasedModuleExtractor;
import com.clarkparsia.modularity.IncremantalReasonerFactory;
import com.clarkparsia.modularity.IncrementalReasoner;
import com.clarkparsia.modularity.ModuleExtractor;
import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.owlapiv3.OntologyUtils;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.junit.Test;
import org.mindswap.pellet.test.PelletTestSuite;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.clarkparsia.modularity.test.TestUtils.assertClassificationEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Blazej Bulka
 */
public class PersistenceUpdatesTest {
	public static final String base = PelletTestSuite.base + "modularity/";

	private static final String TEST_FILE = "test-persistence-classification.zip";

	public ModuleExtractor createModuleExtractor() {
		return new GraphBasedModuleExtractor();
	}

	@Test
	public void miniTambisPersistenceAddsTest() throws IOException {
		String common = "file:" + base + "miniTambis";
		File testFile = new File(TEST_FILE);
		OWLOntology ontology = OntologyUtils.loadOntology(common + ".owl");

		try {
			ModuleExtractor moduleExtractor = createModuleExtractor();

			IncrementalReasoner modular = IncremantalReasonerFactory.getInstance()
				.createReasoner(ontology, IncrementalReasoner.config().extractor(moduleExtractor));

			// first remove a random axiom
			Set<OWLAxiom> owlAxioms = TestUtils.selectRandomAxioms(ontology, 1, System.currentTimeMillis());
			List<OWLAxiom> axiomsToRemove = new ArrayList<OWLAxiom>(owlAxioms);

			for (OWLAxiom axiomToRemove : axiomsToRemove) {
				OWL.manager.applyChange(new RemoveAxiom(ontology, axiomToRemove));
			}

			// classify (i.e., update)
			modular.classify();

			// add the axiom back but do not classify (do not cause an update)

			for (OWLAxiom axiomToAdd : axiomsToRemove) {
				OWL.manager.applyChange(new AddAxiom(ontology, axiomToAdd));
			}

			// at this point there should be a change to the ontology that is not applied yet to the classifier
			// this should cause the save operation to fail

			try {
				modular.save(testFile);
				fail("The incremental classifer must not allow itself to be persisted if there are any unapplied changes to the ontology");
			} catch (IllegalStateException e) {
				assertTrue(testFile.delete());
				// correct behavior
			}

			modular.dispose();
		} finally {
			if (ontology != null) {
				OWL.manager.removeOntology(ontology);
			}
		}
	}

	@Test
	public void miniTambisPersistenceRemovesTest() throws IOException {
		String common = "file:" + base + "miniTambis";
		File testFile = new File(TEST_FILE);
		OWLOntology ontology = OntologyUtils.loadOntology(common + ".owl");

		try {
			ModuleExtractor moduleExtractor = createModuleExtractor();

			IncrementalReasoner modular =
				IncremantalReasonerFactory.getInstance().createReasoner(
					ontology, IncrementalReasoner.config().extractor(moduleExtractor));
			modular.classify();

			List<OWLAxiom> axiomsToRemove = new ArrayList<OWLAxiom>(
				TestUtils.selectRandomAxioms(ontology, 1, System.currentTimeMillis()));

			for (OWLAxiom axiomToRemove : axiomsToRemove) {
				OWL.manager.applyChange(new RemoveAxiom(ontology, axiomToRemove));
			}

			// at this point there should be a change to the ontology that is not applied yet to the classifier
			// this should cause the save operation to fail

			try {
				modular.save(testFile);
				fail("The incremental classifer must not allow itself to be persisted if there are any unapplied changes to the ontology");
			} catch (IllegalStateException e) {
				assertTrue(testFile.delete());
				// correct behavior
			}

		} finally {
			if (ontology != null) {
				OWL.manager.removeOntology(ontology);
			}
		}
	}

	@Test
	public void miniTambisPersistenceAllowedUpdatesTest() throws IOException {
		String common = "file:" + base + "miniTambis";
		File testFile = new File(TEST_FILE);
		OWLOntology ontology = OntologyUtils.loadOntology(common + ".owl");

		try {
			ModuleExtractor moduleExtractor = createModuleExtractor();

			IncrementalReasoner modular = IncremantalReasonerFactory.getInstance()
				.createReasoner(ontology, IncrementalReasoner.config().extractor(moduleExtractor));
			modular.classify();

			// first remove a random axiom
			Set<OWLAxiom> owlAxioms = TestUtils.selectRandomAxioms(ontology, 2, System.currentTimeMillis());
			List<OWLAxiom> axiomsToRemove = new ArrayList<OWLAxiom>(owlAxioms);

			OWL.manager.applyChange(new RemoveAxiom(ontology, axiomsToRemove.get(0)));
			OWL.manager.applyChange(new AddAxiom(ontology, axiomsToRemove.get(0)));
			OWL.manager.applyChange(new RemoveAxiom(ontology, axiomsToRemove.get(1)));

			// classify (i.e., update)
			modular.classify();

			// at this point, the ontology should be updated (despite the changes), and the save should succeed.
			modular.save(testFile);

			assertTrue(testFile.delete());
		} finally {
			OWL.manager.removeOntology(ontology);
		}
	}

	@Test
	public void miniTambisUpdatesAfterPersistenceTest() throws IOException {
		String common = "file:" + base + "miniTambis";
		File testFile = new File(TEST_FILE);
		OWLOntology ontology = OntologyUtils.loadOntology(common + ".owl");

		try {
			ModuleExtractor moduleExtractor = createModuleExtractor();

			IncrementalReasoner modular = IncremantalReasonerFactory.getInstance().createReasoner(
				ontology, IncrementalReasoner.config().extractor(moduleExtractor));
			modular.classify();

			modular.save(testFile);
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			modular = IncrementalReasoner.config().file(testFile).manager(manager).createIncrementalReasoner();

			// first remove a random axiom
			Set<OWLAxiom> owlAxioms = TestUtils.selectRandomAxioms(ontology, 1, System.currentTimeMillis());
			List<OWLAxiom> axiomsToRemove = new ArrayList<OWLAxiom>(owlAxioms);

			for (OWLAxiom axiomToRemove : axiomsToRemove) {
				manager.applyChange(new RemoveAxiom(modular.getRootOntology(), axiomToRemove));
			}

			modular.classify();
			PelletReasoner expected = PelletReasonerFactory.getInstance().createReasoner(modular.getRootOntology());

			assertClassificationEquals(expected, modular);
		} finally {
			OWL.manager.removeOntology(ontology);
		}
	}

	@Test
	public void miniTambisUpdatesAfterPersistence2Test() throws IOException {
		String common = "file:" + base + "miniTambis";
		File testFile = new File(TEST_FILE);
		OWLOntology ontology = OntologyUtils.loadOntology(common + ".owl");

		try {
			IncrementalReasoner modular = IncremantalReasonerFactory.getInstance()
				.createReasoner(ontology, IncrementalReasoner.config().extractor(createModuleExtractor()));
			modular.classify();

			modular.save(testFile);
			modular = IncrementalReasoner.config().file(testFile).createIncrementalReasoner(ontology);

			// first remove a random axiom
			Set<OWLAxiom> owlAxioms = TestUtils.selectRandomAxioms(ontology, 1, System.currentTimeMillis());
			List<OWLAxiom> axiomsToRemove = new ArrayList<OWLAxiom>(owlAxioms);

			for (OWLAxiom axiomToRemove : axiomsToRemove) {
				OWL.manager.applyChange(new RemoveAxiom(ontology, axiomToRemove));
			}

			modular.classify();

			PelletReasoner expected = PelletReasonerFactory.getInstance().createReasoner(ontology);

			assertClassificationEquals(expected, modular);
		} finally {
			OWL.manager.removeOntology(ontology);
		}
	}

	@Test
	public void miniTambisUpdatesWhenPersistedTest() throws IOException {
		String common = "file:" + base + "miniTambis";
		File testFile = new File(TEST_FILE);
		OWLOntology ontology = OntologyUtils.loadOntology(common + ".owl");

		try {
			IncrementalReasoner modular = IncrementalReasoner.config().extractor(createModuleExtractor()).createIncrementalReasoner(ontology);
			modular.classify();
			modular.save(testFile);

			// perform changes while the classifier is stored on disk
			// first remove a random axiom
			Set<OWLAxiom> owlAxioms = TestUtils.selectRandomAxioms(ontology, 1, System.currentTimeMillis());
			List<OWLAxiom> axiomsToRemove = new ArrayList<OWLAxiom>(owlAxioms);

			for (OWLAxiom axiomToRemove : axiomsToRemove) {
				OWL.manager.applyChange(new RemoveAxiom(ontology, axiomToRemove));
			}

			modular = IncrementalReasoner.config().file(testFile).createIncrementalReasoner(ontology);
			PelletReasoner expected = PelletReasonerFactory.getInstance().createReasoner(ontology);

			assertClassificationEquals(expected, modular);
		} finally {
			OWL.manager.removeOntology(ontology);
		}
	}

}
