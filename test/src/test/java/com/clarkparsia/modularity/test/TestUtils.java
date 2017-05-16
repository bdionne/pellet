// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.modularity.test;

import com.clarkparsia.modularity.IncremantalReasonerFactory;
import com.clarkparsia.modularity.IncrementalReasoner;
import com.clarkparsia.modularity.IncrementalReasonerConfiguration;
import com.clarkparsia.modularity.ModuleExtractor;
import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.owlapiv3.OntologyUtils;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.utils.Comparators;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class TestUtils {
	/**
	 * Checks if there is a difference between two array of elements and prints
	 * a sorted, easy to read message showing the differences between two
	 * arrays. The elements of the array are compared with toString() values
	 * so this function is suitable only if the array elements have a unique
	 * string representation. For example, if the array element is a set then
	 * this function cannot be used reliabily.
	 *
	 * @param expected
	 *            expected values
	 * @param computed
	 *            computed values
	 * @return <code>true</code> if there is a difference between the modules
	 */
	public static <T> void assertToStringEquals(String msg, T[] expected, T[] computed) {
		Comparator<Object> comparator = Comparators.stringComparator;

		List<T> onlyInComputed = new ArrayList<T>();
		List<T> onlyInExpected = new ArrayList<T>();
		List<T> both = new ArrayList<T>();

		Arrays.sort( expected, comparator );

		Arrays.sort( computed, comparator );

		int i = 0, j = 0;
		while( i < computed.length && j < expected.length ) {
			if( computed[i].equals( expected[j] ) ) {
				both.add( computed[i] );
				i++;
				j++;
			}
			else if( comparator.compare( computed[i], expected[j] ) < 0 ) {
				onlyInComputed.add( computed[i] );
				i++;
			}
			else {
				onlyInExpected.add( expected[j] );
				j++;
			}
		}

		while( i < computed.length ) {
			onlyInComputed.add( computed[i++] );
		}

		while( j < expected.length ) {
			onlyInExpected.add( expected[j++] );
		}

		if( !onlyInComputed.isEmpty() || !onlyInExpected.isEmpty() ) {
			System.err.println( msg );
			System.err.println( "Both " + both.size() + " " + both );
			System.err.println( "Computed " + onlyInComputed.size() + " " + onlyInComputed );
			System.err.println( "Expected " + onlyInExpected.size() + " " + onlyInExpected );
			System.err.println();

			fail( msg );
		}
	}

	public static List<OWLOntologyChange> createChanges(OWLOntology ontology,
			Collection<? extends OWLAxiom> axioms, boolean add) {
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for( OWLAxiom axiom : axioms ) {
			OWLOntologyChange change = add
				? new AddAxiom( ontology, axiom )
				: new RemoveAxiom( ontology, axiom );
			changes.add( change );
		}

		return changes;
	}

	public static <E> Set<E> flatten(Set<Set<E>> setOfSets) {
		Set<E> result = new HashSet<E>();
		for( Set<E> set : setOfSets ) {
			result.addAll(set);
		}
		return result;
	}

	public static double[] getSizes(Collection<? extends Collection<?>> collections) {
		double[] sizes = new double[collections.size()];

		int i = 0;
		for( Collection<?> collection : collections ) {
			sizes[i++] = collection.size();
		}

		return sizes;
	}

	/**
	 * Selects a random axiom from an ontology
	 */
	public static OWLAxiom selectRandomAxiom(OWLOntology ontology, final long seed) throws OWLException {
		Set<OWLAxiom> selectedAxioms = selectRandomAxioms( ontology, 1 , seed);

		return selectedAxioms.iterator().next();
	}

	/**
	 * Selects a set of random axioms from an ontology
	 */
	public static Set<OWLAxiom> selectRandomAxioms(OWLOntology ontology, int count, final long seed) {
		Set<OWLAxiom> axioms = ontology.getAxioms();

		return selectRandomElements(axioms, count, seed);
	}

	public static <T> Set<T> selectRandomElements(Collection<T> coll, int K, final long seed) {
		// get the size
		int N = coll.size();

		if( K > N )
			throw new IllegalArgumentException( K + " >= " + N );

		List<T> list = (coll instanceof RandomAccess)
			? (List<T>) coll
			: new ArrayList<T>( coll );

		Random rand = new Random(seed);

		for( int k = 0; k < K; k++ ) {
			int j = rand.nextInt( N - k ) + k;
			Collections.swap( list, k, j );
		}

		return new HashSet<T>( list.subList( 0, K ) );
	}

	public static void assertClassificationEquals(OWLReasoner expected, OWLReasoner actual) {
//		classificationResults( expected, actual, OWL.Nothing );

		Multimap<OWLClass, ClassificationResult> expectedResult = ArrayListMultimap.create();
		Multimap<OWLClass, ClassificationResult> actualResult = ArrayListMultimap.create();
		for( OWLClass cls : expected.getRootOntology().getClassesInSignature() ) {
			ComparisonResult result = classificationResults( expected, actual, cls );
			expectedResult.put(cls, (ClassificationResult) result.expected);
			actualResult.put(cls, (ClassificationResult) result.actual);
		}

		assertThat(actualResult.asMap(), is(equalTo(expectedResult.asMap())));
	}

	private static class ClassificationResult {
		Set<OWLClass> supers;
		Set<OWLClass> equivalents;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ClassificationResult that = (ClassificationResult) o;

			if (!supers.equals(that.supers)) return false;
			if (!equivalents.equals(that.equivalents)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = supers.hashCode();
			result = 31 * result + equivalents.hashCode();
			return result;
		}

		public ClassificationResult(Set<OWLClass> supers, Set<OWLClass> equivalents) {
			this.supers = supers;
			this.equivalents = equivalents;
		}
	}

	public static ComparisonResult classificationResults(OWLReasoner expected, OWLReasoner actual, OWLClass cls) {
		Set<OWLClass> expectedEquivalents = expected.getEquivalentClasses( cls ).getEntities();
		Set<OWLClass> actualEquivalents = actual.getEquivalentClasses( cls ).getEntities();

		Set<OWLClass> expectedSupers = expected.getSuperClasses( cls, true ).getFlattened();
		Set<OWLClass> actualSupers = actual.getSuperClasses( cls, true ).getFlattened();

		ClassificationResult expectedResult = new ClassificationResult(expectedSupers, expectedEquivalents);
		ClassificationResult actualResult = new ClassificationResult(actualSupers, actualEquivalents);

		return new ComparisonResult(expectedResult, actualResult);
	}

	public static void assertDisjointnessEquals(OWLReasoner expected, OWLReasoner actual) {
		Multimap<OWLClass, Set<OWLClass>> expectedResult = ArrayListMultimap.create();
		Multimap<OWLClass, Set<OWLClass>> actualResult = ArrayListMultimap.create();
		for( OWLClass cls : actual.getRootOntology().getClassesInSignature() ) {
			Set<OWLClass> expectedDisjoints = expected.getDisjointClasses(cls).getFlattened();
			Set<OWLClass> actualDisjoints = actual.getDisjointClasses(cls).getFlattened();
			expectedResult.put(cls, expectedDisjoints);
			actualResult.put(cls, actualDisjoints);
		}
		assertEquals(expectedResult, actualResult);
	}

	public static void assertInstancesEquals(OWLReasoner expected, OWLReasoner actual) {
		Multimap<OWLClass, Set<OWLNamedIndividual>> expectedResult = ArrayListMultimap.create();
		Multimap<OWLClass, Set<OWLNamedIndividual>> actualResult = ArrayListMultimap.create();
		for( OWLClass cls : actual.getRootOntology().getClassesInSignature() ) {
			Set<OWLNamedIndividual> expectedIndividuals = expected.getInstances(cls, true ).getFlattened();
			Set<OWLNamedIndividual> actualIndividuals = actual.getInstances(cls, true ).getFlattened();
			expectedResult.put(cls, expectedIndividuals);
			actualResult.put(cls, actualIndividuals);
		}
		assertEquals(expectedResult, actualResult);
	}

	public static void assertTypesEquals(OWLReasoner expected, OWLReasoner actual) {
		Multimap<OWLNamedIndividual, Set<OWLClass>> expectedResult = ArrayListMultimap.create();
		Multimap<OWLNamedIndividual, Set<OWLClass>> actualResult = ArrayListMultimap.create();
		for( OWLNamedIndividual ind : actual.getInstances( OWL.Thing, false ).getFlattened() ) {
			Set<OWLClass> expectedTypes = expected.getTypes(ind, true ).getFlattened();
			Set<OWLClass> actualTypes = actual.getTypes(ind, true ).getFlattened();
			expectedResult.put(ind, expectedTypes);
			actualResult.put(ind, actualTypes);
		}
		assertEquals(expectedResult, actualResult);
	}


	public static void runComparisonTest(OWLOntology ontology, ModuleExtractor modExtractor, ReasonerComparisonMethod comparisonMethod) {
		PelletReasoner unified = PelletReasonerFactory.getInstance().createNonBufferingReasoner( ontology );
		IncrementalReasoner modular = IncremantalReasonerFactory.getInstance().createReasoner(
			ontology, new IncrementalReasonerConfiguration().extractor(modExtractor));

		PelletOptions.USE_CLASSIFICATION_MONITOR = PelletOptions.MonitorType.CONSOLE;
		modular.classify();
		unified.getKB().classify();

		comparisonMethod.compare( unified, modular );

		modular.dispose();
	}

	public static void runComparisonUpdateTest(OWLOntology ontology, ModuleExtractor modExtractor, Collection<OWLAxiom> additions, Collection<OWLAxiom> deletions, boolean updateCopy, ReasonerComparisonMethod comparisonMethod) {
		PelletReasoner regular = PelletReasonerFactory.getInstance().createNonBufferingReasoner( ontology );
		IncrementalReasoner incremental = IncremantalReasonerFactory.getInstance().createReasoner(ontology, new IncrementalReasonerConfiguration().extractor(modExtractor));

		PelletOptions.USE_CLASSIFICATION_MONITOR = PelletOptions.MonitorType.CONSOLE;
		incremental.classify();

		comparisonMethod.compare(regular, incremental);


		IncrementalReasoner updateIncremental;
		OWLOntology updateOntology;

		if (updateCopy) {
			updateIncremental = incremental.copy();
			updateOntology = updateIncremental.getRootOntology();
		}
		else {
			updateIncremental = incremental;
			updateOntology = ontology;
		}

		OntologyUtils.addAxioms(updateOntology, additions);
		OntologyUtils.removeAxioms(updateOntology, deletions);

		updateIncremental.classify();

		PelletReasoner updateRegular = PelletReasonerFactory.getInstance().createNonBufferingReasoner(updateOntology);
		updateRegular.getKB().classify();

		updateIncremental.timers.print();

		comparisonMethod.compare(updateRegular, updateIncremental);

		incremental.dispose();
		if (updateCopy) {
			updateIncremental.dispose();
			OWL.manager.removeOntology(updateOntology);
		}
	}

	public static <T> Set<T> set(T... elements) {
		switch ( elements.length ) {
		case 0:
			return emptySet();
		case 1:
			return singleton( elements[0] );
		default:
			return new HashSet<T>( Arrays.asList( elements ) );
		}
	}

	public static class ComparisonResult {
		Object expected;
		Object actual;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ComparisonResult that = (ComparisonResult) o;

			if (expected != null ? !expected.equals(that.expected) : that.expected != null) return false;
			if (actual != null ? !actual.equals(that.actual) : that.actual != null) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = expected != null ? expected.hashCode() : 0;
			result = 31 * result + (actual != null ? actual.hashCode() : 0);
			return result;
		}

		public ComparisonResult(Object expected, Object actual) {
			this.expected = expected;
			this.actual = actual;
		}
	}

	public interface ReasonerComparisonMethod {
		public void compare(OWLReasoner expected, OWLReasoner actual);
	}
}
