package com.clarkparsia.pellet.server.protege;

import java.io.File;

/**
 * @author Edgar Rodriguez-Diaz
 */
public class TestUtilities {
	public static final String BASE = "file:test/data/";

	public static final String PROTEGE_HOST = "localhost";
	public static final String PROTEGE_PORT = "8080";
	public static final String PROTEGE_USERNAME = "root";
	public static final String PROTEGE_PASSWORD = "rootpwd";

//	public static final String PREFIX;
//	static {
//		StringBuffer sb = new StringBuffer();
//		sb.append("src");
//		sb.append(File.separator);
//		sb.append("test");
//		sb.append(File.separator);
//		sb.append("resources");
//		sb.append(File.separator);
//		PREFIX = sb.toString();
//	}
//
//	TestUtilities() {
//	}
//
//	public static File initializeServerRoot() {
//		delete(ROOT_DIRECTORY);
//		delete(CONFIGURATION_DIRECTORY);
//		delete(PELLET_DIRECTORY);
//		ROOT_DIRECTORY.mkdirs();
//		CONFIGURATION_DIRECTORY.mkdirs();
//		PELLET_DIRECTORY.mkdirs();
//
//		return ROOT_DIRECTORY;
//	}

	protected static void delete(File f) {
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				delete(child);
			}
		}
		f.delete();
	}
}
