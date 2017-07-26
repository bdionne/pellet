package com.clarkparsia.pellet.server;

import com.google.common.base.Optional;
import okio.ByteString;

/**
 * Created by rgrinberg on 7/26/17.
 */
public class AuthPair {
    public final String user;
    public final String password;

    public AuthPair(String user, String password) {
        this.user = user;
        this.password = password;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AuthPair authPair = (AuthPair) o;

		if (user != null ? !user.equals(authPair.user) : authPair.user != null) return false;
		if (password != null ? !password.equals(authPair.password) : authPair.password != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = user != null ? user.hashCode() : 0;
		result = 31 * result + (password != null ? password.hashCode() : 0);
		return result;
	}

	public static Optional<AuthPair> fromHeaderValue(String authHeader) {
        String decoded = ByteString.decodeBase64(authHeader.replaceFirst("^\\s*Basic ", "")).utf8();
        String[] parts = decoded.split(":", 2);
        if (parts.length == 2) {
            return Optional.of(new AuthPair(parts[0], parts[1]));
        } else {
            return Optional.absent();
        }
    }
}
