package com.complexible.pellet.client;


import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.Set;

public class InferredAxiomsResponse {
	public Set<OWLSubClassOfAxiom> data;

	public InferredAxiomsResponse(Set<OWLSubClassOfAxiom> data) {
		this.data = data;
	}
}
