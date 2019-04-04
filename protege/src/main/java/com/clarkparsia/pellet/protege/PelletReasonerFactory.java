package com.clarkparsia.pellet.protege;

import com.clarkparsia.modularity.IncremantalReasonerFactory;
import com.clarkparsia.pellet.service.reasoner.SchemaReasonerFactory;
import com.complexible.pellet.client.ClientModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.common.base.Optional;
import org.mindswap.pellet.PelletOptions;
import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;


/**
 * 
 * @author Evren Sirin
 */
public class PelletReasonerFactory extends AbstractProtegeOWLReasonerInfo {
	static {
		// true = (default) Non DL axioms will be ignored (eg as use of complex
		// roles in cardinality restrictions)
		// false = pellet will throw an exception if non DL axioms are included
		PelletOptions.IGNORE_UNSUPPORTED_AXIOMS = false;
		PelletOptions.SILENT_UNDEFINED_ENTITY_HANDLING = true;
	}

	private final PelletReasonerPreferences prefs = PelletReasonerPreferences.getInstance();
	private OWLReasonerFactory factory = null;

	@Override
	public OWLReasonerFactory getReasonerFactory() {
		if (factory != null) {
			return factory;
		}
		// enable/disable tracing based on the preference
		PelletOptions.USE_TRACING = prefs.getExplanationCount() != 0;
		
		factory = IncremantalReasonerFactory.getInstance();
		return factory;
	}

	@Override
	public BufferingMode getRecommendedBuffering() {
				
		// TODO: Make this buffered
		
		return BufferingMode.BUFFERING;
	}

	public void preferencesUpdated() {
		factory = null;
	}
}
