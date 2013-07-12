package edu.stanford.slac.pinger.rest;


import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;

import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.rest.query.QuerySparqlEndpoints;
import edu.stanford.slac.pinger.rest.query.RDFReaderOpenRDF;

public class CheckEndpoints {

	public static final long MAX = 2000;
	public static final long MAX_FACTFORGE = 2700;
	
	public static void main(String[] args) {

		System.out.println(DBPediaIsUP());
	}

	public static boolean GeoNamesIsUp() {
		String url = "http://api.geonames.org/findNearbyPlaceNameJSON?&style=FULL&username=pinger&lat=37.448&lng=-122.1745&cities=cities15000";
		long t1 = System.currentTimeMillis();
		String s = HttpGetter.readPage(url);
		long t2 = System.currentTimeMillis();
		if (s == null || s.contains("sorry")) {
			System.out.println(s);
			return false;
		}
		if ((t2-t1) > MAX) {
			System.out.println(s);
			System.out.println(url);
			System.out.println("It took " + (t2-t1) + " to run the test GeoNames.");				
			return false;
		}
		return true;
	}
	
	public static boolean GeoNamesRDFIsUp() {
		String url_rdf = "http://sws.geonames.org/"+"5332921"+"/about.rdf";
		long t1 = System.currentTimeMillis();
		RDFReaderOpenRDF rdfReader = new RDFReaderOpenRDF(url_rdf);
		long t2 = System.currentTimeMillis();
		if ((t2-t1) > MAX) {
			System.out.println(url_rdf);
			System.out.println("It took " + (t2-t1) + " to run the test query GeoNamesRDF.");				
			return false;
		}
		Model model = rdfReader.filterWeb(null, null, null);
		Set<Resource> subjs = model.subjects();
		Iterator<Resource> it = subjs.iterator();
		return (it.hasNext());
	}
	
	public static boolean FactForgeIsUP() {
		try {
			long t1 = System.currentTimeMillis();
			JsonObject json = QuerySparqlEndpoints.getResultAsJsonUsingHttpGetFactForge(C.STANDARD_SPARQLQUERY);			
			long t2 = System.currentTimeMillis();
			if ((t2-t1) > MAX_FACTFORGE) {
				System.out.println(json);
				System.out.println("It took " + (t2-t1) + " to run a standard query FactForge.");				
				return false;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	}
	
	public static boolean DBPediaIsUP() {
		try {
			long t1 = System.currentTimeMillis();
			QuerySparqlEndpoints.getResultAsText(C.STANDARD_SPARQLQUERY, C.DBPEDIA_ENDPOINT);
			long t2 = System.currentTimeMillis();
			if ((t2-t1) > MAX) {
				System.out.println("It took " + (t2-t1) + " to run a standard query DBPedia.");
				return false;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	}


}
