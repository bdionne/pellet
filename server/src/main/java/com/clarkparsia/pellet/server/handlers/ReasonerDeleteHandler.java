package com.clarkparsia.pellet.server.handlers;

import com.clarkparsia.pellet.server.protege.ProtegeServerState;
import com.google.inject.Inject;

/**
 * @author Evren Sirin
 */
public class ReasonerDeleteHandler extends ReasonerUpdateHandler {
	@Inject
	public ReasonerDeleteHandler(final ProtegeServerState theServerState) {
		super(theServerState, false);
	}
}
