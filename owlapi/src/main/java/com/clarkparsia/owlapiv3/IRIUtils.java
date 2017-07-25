package com.clarkparsia.owlapiv3;

import okhttp3.HttpUrl;
import org.semanticweb.owlapi.model.IRI;

/**
 * Created by rgrinberg on 7/25/17.
 */
public class IRIUtils {
	public static final String PROJECT_ID = "projectId";

	public static IRI addProjectId(IRI iri, String projectId) {
		HttpUrl url = HttpUrl.parse(iri.toString()).newBuilder().addQueryParameter(PROJECT_ID, projectId).build();
		return IRI.create(url.uri());
	}
}
