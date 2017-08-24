package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.exceptions.ServerException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by rgrinberg on 7/26/17.
 */
public class HandlerUtils {
	protected static Set<OWLAxiom> readAxioms(
		final OWLOntologyManager manager, final InputStream inputStream) throws ServerException {
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(inputStream);
			return ontology.getAxioms();
		} catch (OWLOntologyCreationException e) {
			throw new ServerException(400, "There was an error parsing axioms", e);
		} catch (Exception e) {
			throw new ServerException(500, "There was an IO error while reading input stream", e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (ontology != null) {
				manager.removeOntology(ontology);
			}
		}
	}
}
