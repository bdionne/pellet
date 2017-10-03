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



/**
 * @author Edgar Rodriguez-Diaz
 */
public class ClientModule extends AbstractModule {

	private final String mEndpoint;

	private final long mConnTimeoutMin;

	private final long mReadTimeoutMin;

	private final long mWriteTimeoutMin;
	private final Optional<String> managementPassword;

	public ClientModule(final String theEndpoint, final long theConnTimeoutMin, final long theReadTimeoutMin,
											final long theWriteTimeoutMin, final Optional<String> managementPassword) {
		mEndpoint = theEndpoint;
		mConnTimeoutMin = theConnTimeoutMin;
		mReadTimeoutMin = theReadTimeoutMin;
		mWriteTimeoutMin = theWriteTimeoutMin;
		this.managementPassword = managementPassword;
	}

	public ClientModule(final String theEndpoint, final Optional<String> managementPassword) {
		this(theEndpoint, 30, 30, 30, managementPassword);
	}

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder()
			        .implement(SchemaReasoner.class, RemoteSchemaReasoner.class)
			        .build(SchemaReasonerFactory.class));

		bind(String.class).annotatedWith(Names.named("endpoint"))
		                  .toInstance(mEndpoint);
		bind(Long.class).annotatedWith(Names.named("conn_timeout"))
		                   .toInstance(mConnTimeoutMin);
		bind(Long.class).annotatedWith(Names.named("read_timeout"))
		                   .toInstance(mReadTimeoutMin);
		bind(Long.class).annotatedWith(Names.named("write_timeout"))
		                   .toInstance(mWriteTimeoutMin);
		bind(new TypeLiteral<Optional<String>>(){ }).annotatedWith(Names.named("management_password"))
			.toInstance(managementPassword);

		bind(PelletService.class).toProvider(PelletServiceProvider.class).in(Singleton.class);
	}
}
