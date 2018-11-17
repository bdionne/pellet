package com.clarkparsia.pellet.owlapiv3;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;


public class SupFindVisitor implements OWLClassExpressionVisitor {
	private OWLClass cls = null;
	private OWLOntology ont = null;

	public Set<OWLClass> sups = new HashSet<>();

	public SupFindVisitor(OWLClass entity, OWLOntology ontology) {
		cls = entity;
		ont = ontology;
	}

	@Override
	public void visit(OWLClass ce) {
		if (!ce.equals(cls)) {
			sups.add(ce);
			return;
		}
		for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(cls)) {
			OWLClassExpression exp = ax.getSuperClass();
			if (!exp.isAnonymous()) {
				sups.add(exp.asOWLClass());
			}
		}
		for (OWLEquivalentClassesAxiom eax : ont.getEquivalentClassesAxioms(cls)) {
			for (OWLClassExpression exp : eax.getClassExpressions()) {
				if (!exp.isAnonymous()) {
					sups.add(exp.asOWLClass());
				} else {
					exp.accept(this);
				}
			}
		}
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		for (OWLClassExpression exp : ce.getOperands()) {
			exp.accept(this);
		}
	}

	@Override
	public void visit(@Nonnull OWLObjectUnionOf ce) { }

	@Override
	public void visit(@Nonnull OWLObjectComplementOf ce) { }

	@Override
	public void visit(@Nonnull OWLObjectSomeValuesFrom ce) { }

	@Override
	public void visit(@Nonnull OWLObjectAllValuesFrom ce) { }

	@Override
	public void visit(@Nonnull OWLObjectHasValue ce) { }

	@Override
	public void visit(@Nonnull OWLObjectMinCardinality ce) { }

	@Override
	public void visit(@Nonnull OWLObjectExactCardinality ce) { }

	@Override
	public void visit(@Nonnull OWLObjectMaxCardinality ce) { }

	@Override
	public void visit(@Nonnull OWLObjectHasSelf ce) { }

	@Override
	public void visit(@Nonnull OWLObjectOneOf ce) { }

	@Override
	public void visit(@Nonnull OWLDataSomeValuesFrom ce) { }

	@Override
	public void visit(@Nonnull OWLDataAllValuesFrom ce) { }

	@Override
	public void visit(@Nonnull OWLDataHasValue ce) { }

	@Override
	public void visit(@Nonnull OWLDataMinCardinality ce) { }

	@Override
	public void visit(@Nonnull OWLDataExactCardinality ce) { }

	@Override
	public void visit(@Nonnull OWLDataMaxCardinality ce) { }
}
