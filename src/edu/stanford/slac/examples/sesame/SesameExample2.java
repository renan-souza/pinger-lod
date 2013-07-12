package edu.stanford.slac.examples.sesame;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

import edu.stanford.slac.pinger.general.C;

public class SesameExample2 {

	public static void main(String[] args) throws Exception {

		RepositoryConnection con;
		Repository repo;	
		ValueFactory factory;		
		repo = new HTTPRepository(C.SESAME_SERVER, "tests");
		repo.initialize();
		con = repo.getConnection();
		factory = repo.getValueFactory();
		
		
		
		
		URI bob = factory.createURI("http://example.org/bob");
		URI name = factory.createURI("http://example.org/name");

		long t1 = System.currentTimeMillis();
		con.begin();
		for (int i = 0; i < 1000; i++) {
		
			con.add(bob, name, factory.createURI("http://example.org/name"+i));
			
		}
		con.commit();
		long t2 = System.currentTimeMillis();	
		System.out.println(t2-t1);
		
		repo = null;
		con.close();
		
	}
}
