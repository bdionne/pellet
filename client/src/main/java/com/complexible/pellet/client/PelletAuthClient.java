package com.complexible.pellet.client;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by rgrinberg on 6/15/17.
 */
public class PelletAuthClient {
	public static final String AUTH_HEADER = "Authorization";
	public static final String USERNAME = "pellet";

	public static String baseHeader(String name, String password) {
		String toenc = name + ":" + password;
		return "Basic " + new String(Base64.encodeBase64(toenc.getBytes()));
	}

	static class AuthBasicInterceptor implements Interceptor {
		private final String password;

		public AuthBasicInterceptor(@Nonnull String password) {
			this.password = password;
		}

		@Override
		public Response intercept(Chain chain) throws IOException {
			final String authHeader = baseHeader(USERNAME, password);
			Request request = chain.request().newBuilder().addHeader(AUTH_HEADER, authHeader).build();
			return chain.proceed(request);
		}
	}
}
