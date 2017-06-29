package com.clarkparsia.pellet.server;

import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import okio.ByteString;


/**
 * Created by rgrinberg on 6/19/17.
 */
public class PelletAuthHandler implements HttpHandler {
	private static final String PELLET_MANAGEMENT_USER = "pellet";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private final HttpHandler subHandler;
	private final String managementPassword;

	PelletAuthHandler(HttpHandler subHandler, String managementPassword) {
		this.subHandler = subHandler;
		this.managementPassword = managementPassword;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		final String authorization = exchange.getRequestHeaders().getFirst(AUTHORIZATION_HEADER);
		final Sender sender = exchange.getResponseSender();
		if (authorization == null) {
			exchange.setStatusCode(400);
			sender.send("Authorization token isn't present");
			exchange.endExchange();
			return;
		}

		String decoded = ByteString.decodeBase64(authorization).utf8();
		String[] parts = decoded.split(":", 2);
		if (parts.length != 2) {
			sender.send("Authorization token isn't present");
			exchange.setStatusCode(400);
			exchange.endExchange();
			return;
		}

		if (!parts[0].equals(PELLET_MANAGEMENT_USER) || !parts[1].equals(this.managementPassword)) {
			sender.send("Incorrect username or password.");
			exchange.setStatusCode(401);
			exchange.endExchange();
			return;
		}

		subHandler.handleRequest(exchange);
	}
}
