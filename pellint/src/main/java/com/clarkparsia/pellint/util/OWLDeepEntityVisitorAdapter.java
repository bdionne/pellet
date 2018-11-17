// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.pellint.util;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.util.DeprecatedOWLEntityCollector;
import org.semanticweb.owlapi.util.OWLEntityCollector;

/**
 * <p>
 * Title: OWL Deep-Entity-Visitor Adapter
 * </p>
 * <p>
 * Description: A visitor that visits the entire structure of any OWL entity -
 * a workaround to OWLAPI since the visitor pattern was adapted such that the
 * visitors are responsible for the traversals instead of the object structures. 
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
public abstract class OWLDeepEntityVisitorAdapter extends
        OWLEntityCollector {

	public OWLDeepEntityVisitorAdapter(Collection<OWLEntity> toReturn) {
		super(toReturn);
		// TODO Auto-generated constructor stub
	}
	@Override
    public Collection<OWLEntity> visit(OWLClass desc) {
		return objects;}
	@Override
    public Collection<OWLEntity> visit(OWLObjectProperty property) {
		return objects;}
	@Override
    public Collection<OWLEntity> visit(OWLDataProperty property) {
		return objects;}
	public void visit(OWLIndividual individual) {}
	@Override
    public Collection<OWLEntity> visit(OWLDatatype datatype) {
		return objects;}
	
}
