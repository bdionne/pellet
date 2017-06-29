package com.clarkparsia.pellet.server.protege;

import com.google.common.base.Throwables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Protege server configuration reader.
 *
 * @author Edgar Rodriguez-Diaz
 */
public class ProtegeServerConfiguration {

	private static final Logger LOGGER = Logger.getLogger(ProtegeServerConfiguration.class.getName());

	public static Properties protegeServerConfiguration(final File thePathToConfig) throws IOException {
		Properties mProps = new Properties();
		try {
			mProps.load(new FileInputStream(thePathToConfig));
			return mProps;
		}
		catch (FileNotFoundException fnfe) {
			LOGGER.log(Level.SEVERE, "Configuration file for the Protege Server Settings was not found", fnfe);
			throw fnfe;
		}
	}
}
