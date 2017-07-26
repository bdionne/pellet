package com.clarkparsia.pellet.server;

import java.util.Properties;
import java.util.Set;

import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.server.handlers.InferredAxiomsHandler;
import com.clarkparsia.pellet.server.handlers.OntologyAddHandler;
import com.clarkparsia.pellet.server.handlers.OntologyRemoveHandler;
import com.clarkparsia.pellet.server.handlers.ReasonerClassifyHandler;
import com.clarkparsia.pellet.server.handlers.ReasonerDeleteHandler;
import com.clarkparsia.pellet.server.handlers.ReasonerExplainHandler;
import com.clarkparsia.pellet.server.handlers.ReasonerInsertHandler;
import com.clarkparsia.pellet.server.handlers.ReasonerQueryHandler;
import com.clarkparsia.pellet.server.handlers.ReasonerVersionHandler;
import com.clarkparsia.pellet.server.handlers.RoutingHandler;
import com.clarkparsia.pellet.server.protege.OntologyProvider;
import com.clarkparsia.pellet.server.protege.ProtegeServerOntologyProvider;
import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class PelletServerModule extends AbstractModule implements Module {

	private final Properties mSettings;

	public static TypeLiteral<Set<RoutingHandler>> HANDLERS = new TypeLiteral<Set<RoutingHandler>>() {};

	public PelletServerModule() {
		mSettings = null;
	}

	public PelletServerModule(final Properties theSettings) {
		mSettings = theSettings;
	}

	@Override
	protected void configure() {
		Multibinder<RoutingHandler> pathsBinder = Multibinder.newSetBinder(binder(), RoutingHandler.class);
		pathsBinder.addBinding().to(InferredAxiomsHandler.class);
		pathsBinder.addBinding().to(ReasonerQueryHandler.class);
		pathsBinder.addBinding().to(ReasonerExplainHandler.class);
		pathsBinder.addBinding().to(ReasonerInsertHandler.class);
		pathsBinder.addBinding().to(ReasonerDeleteHandler.class);
		pathsBinder.addBinding().to(ReasonerClassifyHandler.class);
		pathsBinder.addBinding().to(ReasonerVersionHandler.class);
		pathsBinder.addBinding().to(OntologyAddHandler.class);
		pathsBinder.addBinding().to(OntologyRemoveHandler.class);

		if (mSettings != null) {
			binder().bind(Properties.class).toInstance(mSettings);
		}

		binder().bind(ProtegeServerState.class).in(Singleton.class);
		binder().bind(OntologyProvider.class).to(ProtegeServerOntologyProvider.class).in(Singleton.class);
	}
}
