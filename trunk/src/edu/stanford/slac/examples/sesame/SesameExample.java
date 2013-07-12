package edu.stanford.slac.examples.sesame;

import java.io.File;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import edu.stanford.slac.pinger.general.P;

/**
 * http://openrdf.callimachus.net/sesame/2.7/docs/users.docbook?view
 * Set up a Native RDF Schema Repository
 * @author Renan
 *
 */

public class SesameExample {

	/**
	 * @param args
	 * @throws QueryEvaluationException 
	 */
	public static void main(String[] args) throws QueryEvaluationException {
		connect();
		ValueFactory factory = repo.getValueFactory();//ValueFactoryImpl.getInstance();
		URI bob = factory.createURI("http://example.org/bob");
		URI name = factory.createURI("http://example.org/name");
		URI age = factory.createURI("http://example.org/age");
		
		Literal bobsName = factory.createLiteral(new String("Bob"));
		Literal bobsAge = factory.createLiteral(Integer.parseInt("10"));
		
		//Statement nameStatement = factory.createStatement(bob, name, bobsName);
		try {
			con.begin();
			con.add(bob, name, bobsName);
			con.add(bob, age, bobsAge);
			con.commit();
		} catch (Exception e){
			
		}
		
		TupleQueryResult r = query("SELECT ?b ?c  WHERE { <http://example.org/bob> ?b ?c } limit 100");
		while (r.hasNext()) {
				
				BindingSet bindingSet = r.next();
				Value valueOfX = bindingSet.getValue("b");
				Value valueOfP = bindingSet.getValue("c");

				
				System.out.println(valueOfX.stringValue() + " " + valueOfP.stringValue()  + " " );
		}
		r.close();
		//query();
		System.out.println("end");

	}
	public static TupleQueryResult query(String sparqlQuery) {
		TupleQueryResult result = null;
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, P.PREFIXES + sparqlQuery);
			result = tupleQuery.evaluate();
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}  finally {
			try {
				//result.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void query() {

		TupleQueryResult result = null;
		try {
	
			String queryString = "SELECT ?b ?c  WHERE { <http://example.org/bob> ?b ?c } limit 100";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, P.PREFIXES + queryString);

			result = tupleQuery.evaluate();
			while (result.hasNext()) {
				
				BindingSet bindingSet = result.next();
				Value valueOfX = bindingSet.getValue("b");
				Value valueOfP = bindingSet.getValue("c");
				
				System.out.println(valueOfX + " " + valueOfP + " " );
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}  finally {
			try {
				result.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	static RepositoryConnection con;
	static Repository repo;
	public static void connect() {
		String sesameServer = "http://localhost:8080/openrdf-sesame/";
		String repositoryID = "pingernativesesame";
		try {
			repo = new HTTPRepository(sesameServer, repositoryID);
			repo.initialize();
			con = repo.getConnection();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public static void connect2() {
		File dataDir = new File("data/tmp/repository/");
		Repository repo = new SailRepository( new MemoryStore(dataDir) );
		try {
			repo.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}


}
