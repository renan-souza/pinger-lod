package edu.stanford.slac.examples.sesame;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

public class SesameExample3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		URL url = new URL("http://sws.geonames.org/2077456/about.rdf");

		InputStream inputStream = url.openStream();

		RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);

		Model myGraph = new LinkedHashModel();
		rdfParser.setRDFHandler(new StatementCollector(myGraph));
		rdfParser.parse(inputStream, url.toString());

		ValueFactory factory = ValueFactoryImpl.getInstance();
		URI subj = factory.createURI("http://sws.geonames.org/2077456/");
		//URI pred = factory.createURI(RDFS.seeAlso);
		URI pred = factory.createURI("http://www.geonames.org/ontology#alternateName");
		
		URI x = null;
		Model res = myGraph.filter(subj, pred, x);
		Set<Value> objs = res.objects();
		Iterator<Value> it = objs.iterator();
		while(it.hasNext()) {
			Value v = (Value) it.next();
			String s1 = v.toString();
			String s2 = v.stringValue();
			
			System.out.println(s1 + " " + s2);
		}
		
		
		//System.out.println(res.objectValue());

		//System.out.println(res.objectLiteral().toString());	
		
		
		//Model resultModel = QueryResults.asModel(graphQueryResult);
		
		/*
			 RDFFormat format = Rio.getParserFormatForMIMEType("application/rdf+json");
			if(format == null) {
			    format = Rio.getParserFormatForFileName("http://sws.geonames.org/2077456/about.rdf");
			}

			Model results = Rio.parse(inputStream, documentURL.toString(), format);
		 */
	}	

}
