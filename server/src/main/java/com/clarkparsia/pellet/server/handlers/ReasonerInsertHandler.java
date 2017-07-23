package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.inject.Inject;

/**
 * @author Evren Sirin
 */
public class ReasonerInsertHandler extends ReasonerUpdateHandler {
	@Inject
	public ReasonerInsertHandler(final ProtegeServerState theServerState) {
		super(theServerState, true);
	}
}
