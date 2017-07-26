package com.clarkparsia.pellet.server.protege;

import com.clarkparsia.pellet.server.PelletServerModule;
import com.clarkparsia.pellet.server.TestModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import org.semanticweb.owlapi.OWLAPIParsersModule;
import org.semanticweb.owlapi.OWLAPIServiceLoaderModule;
import uk.ac.manchester.cs.owl.owlapi.OWLAPIImplModule;
import uk.ac.manchester.cs.owl.owlapi.concurrent.Concurrency;

import java.util.List;

/**
 * Created by rgrinberg on 7/26/17.
 */
public class PelletTestModule {

	public static Injector testInjector(List<String> ontologies) {
		return Guice.createInjector(
			new OWLAPIParsersModule(),
			new OWLAPIServiceLoaderModule(),
			new OWLAPIImplModule(Concurrency.NON_CONCURRENT),
			Modules.override(new PelletServerModule()).with(new TestModule(ontologies))
		);
	}
}
