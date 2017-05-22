// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.modularity.test;

import com.clarkparsia.modularity.ModuleExtractor;
import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.owlapiv3.OntologyUtils;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: Test modular classification for correctness against unified
 * classification
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Mike Smith
 */
public class RandomizedIncrementalClassifierTest extends AbstractModularityTest {
	public RandomizedIncrementalClassifierTest(final Supplier<ModuleExtractor> theModExtractorSupplier) {
		super(theModExtractorSupplier);
	}

	private void classifyCorrectnessTest(String file, boolean copy) throws OWLException {
		OWLOntology ontology = OntologyUtils.loadOntology( "file:" + base + file, false );

		// We are setting the seed to constant here to always get the same failures
//		long seed = System.currentTimeMillis();
		long seed = 1182;

		Set<OWLAxiom> owlAxioms = TestUtils.selectRandomAxioms(ontology, 10, seed);
		List<OWLAxiom> axioms = new ArrayList<OWLAxiom>(owlAxioms);

		// Delete 5 axioms before the test
		OntologyUtils.removeAxioms( ontology, axioms.subList( 0, 5 ) );

		// Update test will add 5 axioms and remove 5 axioms
		List<OWLAxiom> additions = axioms.subList( 0, 5 );
		List<OWLAxiom> deletions = axioms.subList( 5, 10 );
		try {
			TestUtils.runComparisonUpdateTest(ontology, modExtractor, (Collection<OWLAxiom>) additions,
				(Collection<OWLAxiom>) deletions, copy, new TestUtils.ReasonerComparisonMethod() {
				public void compare(OWLReasoner expected, OWLReasoner actual) {
					TestUtils.assertClassificationEquals(expected, actual);
				}
			});
		} catch(Throwable t) {
			System.err.println("Seed = " + seed);
			System.err.println("Additions: " + additions);
			System.err.println("Deletions: " + deletions);
			Throwables.propagate(t);
		}
		finally {
			OWL.manager.removeOntology( ontology );
		}
	}

	@Test
	public void galenTest() throws OWLException {
		classifyCorrectnessTest( "galen.owl", false );
	}

	@Test
	public void koalaTest() throws OWLException {
		classifyCorrectnessTest( "koala.owl", false );
	}

	@Test
	public void sumoTest() throws OWLException {
		classifyCorrectnessTest("SUMO.owl", false);
	}

	@Test
	public void sweetTest() throws OWLException {
		classifyCorrectnessTest( "SWEET.owl", false );
	}

	@Test
	public void galenTestCopy() throws OWLException {
		classifyCorrectnessTest( "galen.owl", true );
	}

	@Test
	public void koalaTestCopy() throws OWLException {
		classifyCorrectnessTest("koala.owl", true);
	}

	@Test
	public void sumoTestCopy() throws OWLException {
		classifyCorrectnessTest( "SUMO.owl", true );
	}

	@Test
	public void sweetTestCopy() throws OWLException {
		classifyCorrectnessTest( "SWEET.owl", true );
	}
}