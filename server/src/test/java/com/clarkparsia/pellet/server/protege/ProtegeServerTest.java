package com.clarkparsia.pellet.server.protege;

import com.clarkparsia.owlapiv3.OntologyUtils;
import com.clarkparsia.pellet.server.protege.model.ProjectIRI;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.PolicyFactory;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectOptions;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.protege.editor.owl.client.LocalHttpClient;
import org.protege.editor.owl.server.http.HTTPServer;
import org.semanticweb.owlapi.model.IRI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Edgar Rodriguez-Diaz
 */
public abstract class ProtegeServerTest {
	private static HTTPServer mServer;

	public static File TEST_HOME;
	protected static File CONFIG_FILE;
	protected static String CONFIG;

	protected final static String OWL2_ONT = "owl2";
	protected final static String AGENCIES_ONT = "agencies";

	protected static File OWL2_FILE;
	protected static File AGENCIES_FILE;

	public ProtegeServerTest() {
		super();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		TEST_HOME = Files.createTempDir();
		System.out.println(TEST_HOME.getAbsolutePath());
		CONFIG_FILE = new File(TEST_HOME, "server-configuration.json");
		CONFIG = Resources.toString(ProtegeServerTest.class.getResource("/server-configuration.json"), Charsets.UTF_8)
		                  .replace("_ROOT_", TEST_HOME.getAbsolutePath());
		System.setProperty(HTTPServer.SERVER_CONFIGURATION_PROPERTY, CONFIG_FILE.getAbsolutePath());

		OWL2_FILE = createFile("owl2");
		AGENCIES_FILE = createFile("agencies");
	}

	private static File createFile(String resourceName) throws IOException {
		File f = File.createTempFile(resourceName, "owl");
		f.deleteOnExit();
		try (OutputStream out = new FileOutputStream(f)) {
			Resources.copy(Resources.getResource(resourceName + ".owl"), out);
		}
		return f;
	}

	@AfterClass
	public static void afterClass() throws Exception {
		FileUtils.deleteDirectory(TEST_HOME);
		OWL2_FILE.delete();
		AGENCIES_FILE.delete();
	}

	@Before
	public void before() throws Exception {
		BasicConfigurator.configure();
		Files.write(CONFIG, CONFIG_FILE, Charsets.UTF_8);
		mServer = new HTTPServer(CONFIG_FILE.getAbsolutePath());
		mServer.start();
	}

	@After
	public void after() throws Exception {
		mServer.stop();
		FileUtils.cleanDirectory(TEST_HOME);
		OntologyUtils.clearOWLOntologyManager();
	}

	protected static ProjectIRI createOwl2Ontology(final LocalHttpClient client) throws Exception {
		final ProjectId projectId = createOntology(OWL2_ONT, OWL2_FILE, client);
		return new ProjectIRI(IRI.create("http://www.example.org/test"), projectId);
	}

	protected static ProjectIRI createAgenciesOntology(final LocalHttpClient client) throws Exception {
		final ProjectId projectId = createOntology(AGENCIES_ONT, AGENCIES_FILE, client);
		return new ProjectIRI(IRI.create("http://www.owl-ontologies.com/unnamed.owl"), projectId);
	}

	protected static ProjectId createOntology(final String resourceName, final File ont, final LocalHttpClient client) throws Exception {
		PolicyFactory f = ConfigurationManager.getFactory();
		final ProjectId projectId = f.getProjectId(resourceName);
		Project p = f.getProject(projectId,
		                         f.getName(resourceName),
		                         f.getDescription(resourceName),
		                         f.getUserId("admin"),
		                         Optional.<ProjectOptions>absent());
		client.createProject(p, ont);
		return projectId;
	}
}
