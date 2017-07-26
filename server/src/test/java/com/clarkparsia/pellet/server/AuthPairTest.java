package com.clarkparsia.pellet.server;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.core.Is.is;

/**
 * Created by rgrinberg on 7/26/17.
 */
public class AuthPairTest {

	@Test
	public void testAuthHeader() {
		String user = "pellet";
		String password = "abc123";
		String header = "Basic cGVsbGV0OmFiYzEyMw==";

		final com.google.common.base.Optional<AuthPair> actual = AuthPair.fromHeaderValue(header);
		Assert.assertTrue(actual.isPresent());

		Assert.assertThat(actual.get(), is(new AuthPair(user, password)));
	}
}