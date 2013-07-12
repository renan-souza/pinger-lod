package edu.stanford.slac.pinger.model;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.general.ontology.XSD;


/**
 * This class is to be used to manipulate the RDF Storage.
 * Please, remember to close() the object when you finish using.
 * Attention: close() the connection only once.
 * @author Renan
 *
 */
public class GeneralModelSingleton {


	private RepositoryConnection con;

	private Repository repo;	

	private ValueFactory factory;
	public ValueFactory getFactory() {
		return factory;
	}
	public void setFactory(ValueFactory factory) {
		this.factory = factory;
	}
	private static GeneralModelSingleton instance = null;

	private static final int TYPE_CVS = 1;
	private static final int TYPE_RDF = 2;
	private static final int TYPE_JSON = 3;


	protected GeneralModelSingleton() {
	}
	public static GeneralModelSingleton getInstance() {
		if (instance == null) {			
			instance = new GeneralModelSingleton();
			instance.connect();
		}
		return instance;
	}
	private void connect() {
		try {
			repo = new HTTPRepository(C.SESAME_SERVER, C.REPOSITORY_ID);
			repo.initialize();
			con = repo.getConnection();
			factory = repo.getValueFactory();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	public void close() {
		try {
			repo.shutDown();
			con.close();
			instance = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void begin() {
		try {
			con.begin();
			System.out.println("Connected to the repository.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void commit() {
		try {
			con.commit();
			System.out.println("Transactions commited successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addTripleLiteral(String resourceURI, 
			String propertyNS, String dataProperty,
			Literal obj, String context, boolean allowDuplicate) {
		URI subj = factory.createURI(resourceURI);
		URI pred = factory.createURI(P.MAP_PREFIXES.get(propertyNS)+dataProperty);
		URI ctxt = (context!=null)?factory.createURI(P.MAP_PREFIXES.get("MD")+context):null;
		try {
			if (allowDuplicate)
				con.add(subj, pred, obj, ctxt);
			else if (!propertyExists(subj, pred))
				con.add(subj, pred, obj, ctxt);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}

	}

	/**
	 * Adds a triple of the form [Resource, Datatype Property, Data Value]
	 * @param resourceURI - The absolute URI for a new Resource
	 * @param propertyNS
	 * @param dataProperty
	 * @param String value
	 */
	public void addTripleLiteral(String resourceURI, 
			String propertyNS, String dataProperty,
			String value, String context, boolean allowDuplicate) {
		URI uri = factory.createURI(XSD.string);
		addTripleLiteral(resourceURI, propertyNS, dataProperty, factory.createLiteral(value, uri), context, allowDuplicate);
	}
	/**
	 * Adds a triple of the form [Resource, Datatype Property, Data Value]
	 * @param resourceURI - The absolute URI for a new Resource
	 * @param propertyNS
	 * @param dataProperty
	 * @param float value
	 */
	public void addTripleLiteral(String resourceURI, 
			String propertyNS, String dataProperty,
			float value, String context, boolean allowDuplicate) {
		URI uri = factory.createURI(XSD.Float);
		addTripleLiteral(resourceURI, propertyNS, dataProperty, factory.createLiteral(String.valueOf(value),uri), context, allowDuplicate);
	}
	/**
	 * Adds a triple of the form [Resource, Datatype Property, Data Value]
	 * @param resourceURI - The absolute URI for a new Resource
	 * @param propertyNS
	 * @param dataProperty
	 * @param double value
	 */
	public void addTripleLiteral(String resourceURI, 
			String propertyNS, String dataProperty,
			double value, String context, boolean allowDuplicate) {
		URI uri = factory.createURI(XSD.Double);
		addTripleLiteral(resourceURI, propertyNS, dataProperty, factory.createLiteral(String.valueOf(value),uri), context, allowDuplicate);
	}
	/**
	 * Adds a triple of the form [Resource, Datatype Property, Data Value]
	 * @param resourceURI - The absolute URI for a new Resource
	 * @param propertyNS
	 * @param dataProperty
	 * @param int value
	 */
	public void addTripleLiteral(String resourceURI, 
			String propertyNS, String dataProperty,
			int value, String context, boolean allowDuplicate) {
		URI uri = factory.createURI(XSD.integer);
		addTripleLiteral(resourceURI, propertyNS, dataProperty, factory.createLiteral(String.valueOf(value),uri), context, allowDuplicate);
	}

	/**
	 * Adds a triple of the form [Resource, Datatype Property, Data Value]
	 * @param resourceURI - The absolute URI for a new Resource
	 * @param propertyNS
	 * @param dataProperty
	 * @param NonNegativeInteger value
	 */
	public void addTripleLiteralNonNegativeInteger(String resourceURI, 
			String propertyNS, String dataProperty,
			int value, String context, boolean allowDuplicate) {
		URI uri = factory.createURI(XSD.nonNegativeInteger);
		addTripleLiteral(resourceURI, propertyNS, dataProperty, factory.createLiteral(String.valueOf(value),uri), context, allowDuplicate);
	}

	private void addTripleResource(URI subj, URI pred, URI obj, String context, boolean allowDuplicate) {
		try {
			URI ctxt = (context!=null)?factory.createURI(P.MAP_PREFIXES.get("MD")+context):null;
			if (allowDuplicate)
				con.add(subj, pred, obj, ctxt);
			else if (!propertyExists(subj, pred))
				con.add(subj, pred, obj, ctxt);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Adds a triple of the form [Absolute URI, NS:Property, NS:Resource]
	 * @param resourceURI Absolute URI
	 * @param propertyNS
	 * @param ojectProperty
	 * @param resourceBns
	 * @param resourceB
	 */
	public void addTripleResource(String resourceURI, 
			String propertyNS, String ojectProperty,
			String resourceBns, String resourceB, String context, boolean allowDuplicate) {
		URI subj = factory.createURI(resourceURI);
		URI pred = factory.createURI(P.MAP_PREFIXES.get(propertyNS)+ojectProperty);
		URI obj = factory.createURI(P.MAP_PREFIXES.get(resourceBns)+resourceB);
		addTripleResource(subj, pred, obj, context, allowDuplicate);
	}


	/**
	 * Adds a triple of the form [Absolute URI, NS:Property, Resource]
	 * @param resourceURI
	 * @param propertyNS
	 * @param ojectProperty
	 * @param resourceB
	 */
	public void addTripleResource(String resourceURI, 
			String propertyNS, String ojectProperty,
			String resourceB, String context, boolean allowDuplicate) {
		URI subj = factory.createURI(resourceURI);
		URI prop = factory.createURI(P.MAP_PREFIXES.get(propertyNS)+ojectProperty);
		URI obj = factory.createURI(resourceB);
		addTripleResource(subj, prop, obj, context, allowDuplicate);
	}

	/**
	 * Adds a triple of the form [Absolute URI, Absolute URI Property, Absolute URI Resource]
	 * @param resourceAns
	 * @param resourceA
	 * @param propertyNS
	 * @param ojectProperty
	 * @param resourceBns
	 * @param resourceB
	 */
	public void addTripleResource(String resourceURI, 
			String property,
			String resourceB, String context, boolean allowDuplicate) {
		URI subj = factory.createURI(resourceURI);
		URI prop = factory.createURI(property);
		URI obj = factory.createURI(resourceB);
		addTripleResource(subj, prop, obj, context, allowDuplicate);
	}


	private String queryResult(String sparqlQuery, int outputType) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TupleQueryResult result = query(sparqlQuery);
		try {
			TupleQueryResultFormat format = null;
			if (outputType==TYPE_CVS) format = TupleQueryResultFormat.CSV;
			else if (outputType==TYPE_JSON) format = TupleQueryResultFormat.JSON;
			else if (outputType==TYPE_RDF) format = TupleQueryResultFormat.SPARQL;
			QueryResultIO.write(result, format, baos);
			return baos.toString("UTF-8");
		} catch (Exception e) {
			System.out.println(e);
			return null;
		} finally {
			try {
				result.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	public TupleQueryResult query(String sparqlQuery) {
		TupleQueryResult result = null;
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, P.PREFIXES + sparqlQuery);
			result = tupleQuery.evaluate();

			return result;
		}
		catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public String queryResultAsCVS(String sparqlQuery) {
		return queryResult(sparqlQuery, TYPE_CVS);
	}
	public String queryResultAsRDF(String sparqlQuery) {
		return queryResult(sparqlQuery, TYPE_RDF);
	}

	public String queryResultAsJSON(String sparqlQuery) {
		return queryResult(sparqlQuery, TYPE_JSON);
	}

	private boolean propertyExists(URI subj, URI prop) {
		try {
			RepositoryResult<Statement> res = con.getStatements(subj, prop, null, true, (Resource) null);
			Boolean b = res.hasNext();
			res.close();
			return b;
		} catch (RepositoryException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void addRDFXMLFile(String filePath, String baseURI) {
		try {
			File file = new File(filePath);
			con.add(file, baseURI, RDFFormat.RDFXML);
		} catch (Exception e) {
			System.out.println(e);
		}
	}


	@Deprecated
	private static void DeleteAllNodes() {

	}
	public static void start(String[] args) {
		//DeleteAllNodes();
		//SetUpPrefixes();
	}
}
