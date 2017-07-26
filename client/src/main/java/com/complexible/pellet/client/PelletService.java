package com.complexible.pellet.client;

import java.util.UUID;

import com.clarkparsia.pellet.service.PelletServiceConstants;
import com.clarkparsia.pellet.service.reasoner.SchemaQuery;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.NodeSet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface definition for Pellet Service.
 *
 * @author Edgar Rodriguez-Diaz
 */
public interface PelletService {

	String DEFAULT_LOCAL_ENDPOINT = "http://localhost:18080";

	@GET("/admin/shutdown")
	Call<Void> shutdown();

	@GET("/admin/restart")
	Call<Void> restart();

	@POST("/reasoner/query")
	Call<NodeSet> query(@Query(PelletServiceConstants.ONTOLOGY) IRI theOntology,
											@Query("client") UUID theClientID,
											@Body SchemaQuery query);

	@POST("/reasoner/explain")
	Call<OWLOntology> explain(@Query(PelletServiceConstants.ONTOLOGY) IRI theOntology,
														@Query("client") UUID theClientID,
														@Query("limit") int limit,
														@Body OWLAxiom inference);

	@POST("/reasoner/insert")
	Call<Void> insert(@Query(PelletServiceConstants.ONTOLOGY) IRI theOntology,
										@Query("client") UUID theClientID,
										@Body OWLOntology axioms);

	@POST("/reasoner/delete")
	Call<Void> delete(@Query(PelletServiceConstants.ONTOLOGY) IRI theOntology,
										@Query("client") UUID theClientID,
										@Body OWLOntology axioms);

	@GET("/reasoner/classify")
	Call<Void> classify(@Query(PelletServiceConstants.ONTOLOGY) IRI theOntology,
											@Query("client") UUID theClientID);

	@GET("/reasoner/version")
	Call<Integer> version(@Query(PelletServiceConstants.ONTOLOGY) IRI theOntology,
												@Query("client") UUID theClientID);

	@PUT("/reasoner")
	Call<Void> load(@Query(PelletServiceConstants.ONTOLOGY) IRI theOntologyPath);

	@DELETE("/reasoner")
	Call<Void> unload(@Query(PelletServiceConstants.ONTOLOGY) IRI theOntology);
}
