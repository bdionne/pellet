package com.clarkparsia.pellet.server;

import javax.inject.Inject;
import java.util.Properties;

/**
 * Created by rgrinberg on 6/5/17.
 */
public class PelletSettings {
    private Properties settings;

    private static final int UPDATE_INTERVAL_DEFAULT_IN_SECONDS = 300;

    @Inject
    public PelletSettings(final Properties theSettings) {
        settings = theSettings;
    }

    public String home() {
        return ConfigurationReader.getProperty(settings, Configuration.PELLET_HOME, System.getProperty("user.dir"));
    }

    public String host() {
        return ConfigurationReader.getProperty(settings, Configuration.PELLET_HOST, PelletServer.DEFAULT_HOST);
    }

    public String endpoint() {
        return host() + ":" + port();
    }

    public int port() {
        return ConfigurationReader.getPropertyAsInteger(settings, Configuration.PELLET_PORT, PelletServer.DEFAULT_PORT);
    }

    public int updateIntervalInSeconds() {
        return ConfigurationReader.getPropertyAsInteger(settings, Configuration.PELLET_UPDATE_INTERVAL, UPDATE_INTERVAL_DEFAULT_IN_SECONDS);
    }

	public String managementPassword() {
		final String pass = ConfigurationReader.getProperty(settings, Configuration.PELLET_MANAGEMENT_PASSWORD, null);
		if (pass == null) {
			throw new RuntimeException("Unavilable management password");
		}
		return pass;
	}
}
