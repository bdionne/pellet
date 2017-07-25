package com.complexible.pellet.client;

import com.clarkparsia.pellet.service.reasoner.SchemaReasoner;
import com.clarkparsia.pellet.service.reasoner.SchemaReasonerFactory;
import com.complexible.pellet.client.reasoner.RemoteSchemaReasoner;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.common.base.Optional;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import javax.inject.Provider;


/**
 * @author Edgar Rodriguez-Diaz
 */
public class ClientModule extends AbstractModule {

	public static final int TIMEOUT_MIN = 3;
	private final String endpoint;

	private final long connTimeoutMin;

	private final long readTimeoutMin;

	private final long writeTimeoutMin;
	private final Optional<String> managementPassword;
	// Should really be Provider<ProjectId> but we don't have the correct dependency on hand
	private final Provider<String> projectIdProvider;

	public ClientModule(final String endpoint,
											final Optional<String> managementPassword,
											final Provider<String> projectIdProvider) {
		this.endpoint = endpoint;
		this.connTimeoutMin = TIMEOUT_MIN;
		this.readTimeoutMin = TIMEOUT_MIN;
		this.writeTimeoutMin = TIMEOUT_MIN;
		this.managementPassword = managementPassword;
		this.projectIdProvider = projectIdProvider;
	}

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder()
			        .implement(SchemaReasoner.class, RemoteSchemaReasoner.class)
			        .build(SchemaReasonerFactory.class));

		bind(String.class).annotatedWith(Names.named("endpoint"))
		                  .toInstance(endpoint);
		bind(Long.class).annotatedWith(Names.named("conn_timeout"))
		                   .toInstance(connTimeoutMin);
		bind(Long.class).annotatedWith(Names.named("read_timeout"))
		                   .toInstance(readTimeoutMin);
		bind(Long.class).annotatedWith(Names.named("write_timeout"))
		                   .toInstance(writeTimeoutMin);
		bind(new TypeLiteral<Optional<String>>(){ }).annotatedWith(Names.named("management_password"))
			.toInstance(managementPassword);

		bind(new TypeLiteral<Provider<String>>() {})
			.annotatedWith(Names.named("project_id_provider"))
			.toInstance(projectIdProvider);
		bind(PelletService.class).toProvider(PelletServiceProvider.class).in(Singleton.class);
	}
}
