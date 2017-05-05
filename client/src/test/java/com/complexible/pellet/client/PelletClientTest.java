package com.complexible.pellet.client;

import com.clarkparsia.pellet.server.PelletServerTest;
import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.PlainPassword;
import edu.stanford.protege.metaproject.api.PolicyFactory;
import edu.stanford.protege.metaproject.api.UserId;
import org.junit.Before;
import org.protege.editor.owl.client.LocalHttpClient;

/**
 * @author Edgar Rodriguez-Diaz
 */
public abstract class PelletClientTest extends PelletServerTest {
	protected PelletServiceProvider serviceProvider = new PelletServiceProvider(PelletService.DEFAULT_LOCAL_ENDPOINT, 0, 0, 0); // disable all timeouts for tests

	protected LocalHttpClient mClient;
	LocalHttpClient managerClient;

	@Before
	public void before() throws Exception {
		super.before();

		mClient = new LocalHttpClient(PROTEGE_USERNAME, PROTEGE_PASSWORD, "http://" + PROTEGE_HOST + ":" + PROTEGE_PORT);
		PolicyFactory f = ConfigurationManager.getFactory();
		UserId managerId = f.getUserId("bob");
		PlainPassword managerPassword = f.getPlainPassword("bob");
		managerClient = new LocalHttpClient(managerId.get(), managerPassword.getPassword(), "http://localhost:8081");
	}
}
