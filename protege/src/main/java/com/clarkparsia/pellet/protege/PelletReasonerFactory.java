package com.clarkparsia.pellet.protege;

import com.clarkparsia.modularity.IncremantalReasonerFactory;
import com.clarkparsia.pellet.service.reasoner.SchemaReasonerFactory;
import com.complexible.pellet.client.ClientModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.common.base.Optional;
import com.google.inject.Provider;
import org.mindswap.pellet.PelletOptions;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.client.ClientSession;
import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.List;


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
		PelletReasonerMode reasonerMode = prefs.getReasonerMode();
		switch (reasonerMode) {
			case REGULAR:
				factory = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance();
				break;
			case INCREMENTAL:
				factory = IncremantalReasonerFactory.getInstance();
				break;
			case REMOTE: {
				final String serverURL = PelletReasonerPreferences.getInstance().getServerURL();
				final Injector aInjector = Guice.createInjector(new ClientModule(serverURL, Optional.<String>absent(),
					new Provider<String>() {
						@Override
						public String get() {
							final List<EditorKit> editorKits = ProtegeManager.getInstance().getEditorKitManager().getEditorKits();
							if (editorKits.size() > 0) {
								// cast is safe b/c there's only one implementaiton of EditorKit
								return ClientSession.getInstance((OWLEditorKit) editorKits.get(0)).getActiveProject().get();
							} else {
								throw new RuntimeException("Project id not available becasuse there are no editor kits");
							}
						}
					}));
				factory = new RemotePelletReasonerFactory(aInjector.getInstance(SchemaReasonerFactory.class), getOWLModelManager());
				break;
			}
			default: {
				throw new UnsupportedOperationException("Unrecognized reasoner type: " + reasonerMode);
			}
		}
		return factory;
	}

	@Override
	public BufferingMode getRecommendedBuffering() {
		PelletReasonerMode reasonerMode = prefs.getReasonerMode();
		switch (reasonerMode) {
			case REGULAR:
			case INCREMENTAL:
				return BufferingMode.NON_BUFFERING;
			case REMOTE:
				return BufferingMode.BUFFERING;
			default:
				throw new UnsupportedOperationException("Unrecognized reasoner type: " + reasonerMode);
		}
	}

	public void preferencesUpdated() {
		factory = null;
	}
}
