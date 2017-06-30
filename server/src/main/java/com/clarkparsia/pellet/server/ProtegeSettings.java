package com.clarkparsia.pellet.server;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import java.util.Properties;
import java.util.Set;

/**
 * Created by rgrinberg on 6/5/17.
 */
public class ProtegeSettings {

    private static final String DEFAULT_PROTEGE_HOST = "localhost";
    private static final int DEFAULT_PROTEGE_PORT = 5100;

    private Properties settings;

    @Inject
    public ProtegeSettings(final Properties theSettings) {
        settings = theSettings;
    }

    public String host() {
        return ConfigurationReader.getProperty(settings, Configuration.PROTEGE_HOST, DEFAULT_PROTEGE_HOST);
    }

    public int port() {
        return ConfigurationReader.getPropertyAsInteger(settings, Configuration.PROTEGE_PORT, DEFAULT_PROTEGE_PORT);
    }

    public String username() {
        return ConfigurationReader.getProperty(settings, Configuration.PROTEGE_USERNAME, null);
    }

    public String password() {
        return ConfigurationReader.getProperty(settings, Configuration.PROTEGE_PASSWORD, null);
    }

    public Set<String> ontologies() {
        final String ontologiesList = ConfigurationReader.getProperty(settings, Configuration.PROTEGE_ONTOLOGIES, "");

        // try parsing the ontology names list
        return ImmutableSet.copyOf(Splitter.on(',').omitEmptyStrings().trimResults().split(ontologiesList));
    }
}
