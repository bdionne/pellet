// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.pellint.lintpattern.axiom;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.clarkparsia.pellint.format.LintFormat;
import com.clarkparsia.pellint.format.SimpleLintFormat;
import com.clarkparsia.pellint.model.Lint;
import com.clarkparsia.pellint.model.Severity;
import com.clarkparsia.pellint.util.OWLDeepEntityVisitorAdapter;

/**
 * <p>
 * Title: 
 * </p>
 * <p>
 * Description: 
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Harris Lin
 */
public class LargeDisjunctionPattern extends AxiomLintPattern {
	public LargeDisjunctionPattern(Collection<OWLEntity> toReturn) {		
		super(toReturn);
		m_Visitor = new DisjunctionSizeCollector(toReturn);
		// TODO Auto-generated constructor stub
	}

	private static final LintFormat DEFAULT_LINT_FORMAT = new SimpleLintFormat();
	
	private int m_MaxAllowed = 10;
	private DisjunctionSizeCollector m_Visitor;
	
	//public LargeDisjunctionPattern() {
		//m_Visitor = new DisjunctionSizeCollector();
	//}
	
	public String getName() {
		return getClass().getSimpleName() + " (MaxAllowed = " + m_MaxAllowed + ")";
	}
	
	public String getDescription() {
		return "Too many disjuncts in a disjunction - maximum recommended is " + m_MaxAllowed;
	}
	
	public boolean isFixable() {
		return false;
	}
	
	public LintFormat getDefaultLintFormat() {
		return DEFAULT_LINT_FORMAT;
	}

	public void setMaxAllowed(int value) {
		m_MaxAllowed = value;
	}
	
	public Collection<OWLEntity> visit(OWLDisjointClassesAxiom axiom) {
		visitNaryClassAxiom(axiom);
		return objects;
	}
	
	public Collection<OWLEntity> visit(OWLDisjointUnionAxiom axiom) {
		visitNaryClassAxiom(axiom);
		return objects;
	}
	
	public Collection<OWLEntity> visit(OWLEquivalentClassesAxiom axiom) {
		visitNaryClassAxiom(axiom);
		return objects;
	}
	
	public Collection<OWLEntity> visit(OWLSubClassOfAxiom axiom) {
		visitNaryClassAxiom(axiom);
		return objects;
	}
	
	private void visitNaryClassAxiom(OWLClassAxiom axiom) {
		m_Visitor.reset();
		axiom.accept(m_Visitor);
		int disjunctionSize = m_Visitor.getDisjunctionSize();
		if (disjunctionSize > m_MaxAllowed) {
			Lint lint = makeLint();
			lint.addParticipatingAxiom(axiom);
			lint.setSeverity(new Severity(disjunctionSize));
			setLint(lint);
		}
	}
}

class DisjunctionSizeCollector extends OWLDeepEntityVisitorAdapter {
	public DisjunctionSizeCollector(Collection<OWLEntity> toReturn) {
		super(toReturn);
		// TODO Auto-generated constructor stub
	}

	private int m_Size;
	
	public void reset() {
		m_Size = 0;
	}
	
	public int getDisjunctionSize() {
		return m_Size;
	}
	
	public Collection<OWLEntity> visit(OWLObjectUnionOf union) {
		int size = union.getOperands().size();
		if (size > m_Size) {
			m_Size = size;
		}
		return super.visit(union);
	}
}