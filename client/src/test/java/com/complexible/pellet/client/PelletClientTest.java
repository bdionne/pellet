package com.complexible.pellet.client;

import com.clarkparsia.pellet.server.PelletServer;
import com.clarkparsia.pellet.server.PelletServerModule;
import com.clarkparsia.pellet.server.TestModule;
import com.clarkparsia.pellet.server.protege.ProtegeServerTest;
import com.clarkparsia.pellet.server.protege.TestUtilities;
import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.PlainPassword;
import edu.stanford.protege.metaproject.api.PolicyFactory;
import edu.stanford.protege.metaproject.api.UserId;
import org.junit.After;
import org.junit.Before;
import org.protege.editor.owl.client.LocalHttpClient;

import java.util.List;

import static com.clarkparsia.pellet.server.protege.TestUtilities.*;

/**
 * @author Edgar Rodriguez-Diaz
 */
public abstract class PelletClientTest extends ProtegeServerTest {
	protected static PelletServer pelletServer;
	protected PelletServiceProvider serviceProvider =
		new PelletServiceProvider(PelletService.DEFAULT_LOCAL_ENDPOINT,
			0, 0, 0, Optional.of(TestUtilities.PELLET_MANAGEMENT_PASSWORD));

	protected LocalHttpClient client;
	LocalHttpClient managerClient;

	@Before
	public void before() throws Exception {
		super.before();

		client = new LocalHttpClient(PROTEGE_USERNAME, PROTEGE_PASSWORD, PROTEGE_HOST + ":" + PROTEGE_PORT);
		PolicyFactory f = ConfigurationManager.getFactory();
		UserId managerId = f.getUserId("bob");
		PlainPassword managerPassword = f.getPlainPassword("bob");
		managerClient = new LocalHttpClient(managerId.get(), managerPassword.getPassword(), "http://localhost:8081");
	}

	public void startPelletServer(List<String> ontologies) throws Exception {
		pelletServer = new PelletServer(Guice.createInjector(Modules.override(new PelletServerModule())
			.with(new TestModule(ontologies))));
		pelletServer.start();
	}

	public void stopPelletServer() {
		if (pelletServer != null) {
			pelletServer.stop();
			pelletServer = null;
		}
	}

	@After
	public void after() throws Exception {
		stopPelletServer();
		super.after();
	}
}
