package edu.stanford.slac.pinger.rest.query;

import java.io.ByteArrayOutputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.rest.HttpGetter;

public final class QuerySparqlEndpoints {
	
	
	public static String getResultAsText(String querySparql, String sparqlEndPoint) {
	    Query query = QueryFactory.create(P.PREFIXES + querySparql);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, query);
	    ResultSet resultSet = qexec.execSelect();
	    String result = ResultSetFormatter.asText(resultSet);
	    qexec.close();
		return result;		
	}
	
	public static JsonObject getResultAsJson(String querySparql, String sparqlEndpoint) {
	    Query query = QueryFactory.create(P.PREFIXES + querySparql);	    
		QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint,query );
	    ResultSet resultSet = qexec.execSelect();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ResultSetFormatter.outputAsJSON(baos, resultSet);
	    qexec.close();
	    return (JsonObject) new JsonParser().parse(baos.toString());    
	}
	
	public static JsonObject getResultAsJsonUsingHttpGet(String querySparql, String sparqlEndpoint) {
	    Query query = QueryFactory.create(P.PREFIXES + querySparql);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		String url = qexec.toString().replace("GET ", "");
		JsonObject js = HttpGetter.readPageJson(url);
	    qexec.close();	    
	    return js;	    
	}
	
	public static String getResultAsTextUsingHttpGet(String querySparql, String sparqlEndpoint) {
	    Query query = QueryFactory.create(P.PREFIXES + querySparql);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		String url = qexec.toString().replace("GET ", "");
		String ret = HttpGetter.readPage(url);
	    qexec.close();	    
	    return ret;	    
	}
	
	/**
	 * This function is necessary because some results only show if the option "Expand results over equivalent URIs" is true.
	 * @param querySparql
	 * @return
	 */
	public static JsonObject getResultAsJsonUsingHttpGetFactForge(String querySparql) {
	    Query query = QueryFactory.create(P.PREFIXES + querySparql);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(C.FACTFORGE_ENDPOINT_JSON, query);
		String url = qexec.toString().replace("GET ", "");
		url = url.replace("LIMIT+++", "LIMIT+"); //Factforge does not like LIMIT +++
		url += "&_implicit=false&implicit=true&_equivalent=false&equivalent=true&_form=%2Fsparql";
		JsonObject js = HttpGetter.readPageJson(url);
	    qexec.close();	    
	    return js;	    
	}
	
}
