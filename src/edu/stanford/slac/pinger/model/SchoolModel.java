package edu.stanford.slac.pinger.model;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import edu.stanford.slac.pinger.general.P;

public class SchoolModel {

	public static String getSchoolResourceFromPingerLatLong(String pingerLat, String pingerLong) {
		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		String q = 
				"select ?school where {" +
				"?school "+P.RDF+":type " + P.MGC+":School . " +
				"?school "+P.MGC+":PingERLat ?lat . " +
				"?school "+P.MGC+":PingERLong ?lng . " +
				"filter (xsd:double("+ pingerLat + ") = xsd:double(?lat) && xsd:double(" + pingerLong + ") = xsd:double(?lng) ) . " +
				"}";
		TupleQueryResult result = gm.query(q);
		try {
			if (result.hasNext()) {
				BindingSet bindingSet;
				bindingSet = result.next();
				Value school = bindingSet.getValue("school");
				return school.stringValue();
			}
		} catch (QueryEvaluationException e) {
			return null;
		} finally {
			try {
				result.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return null;
	}
	
}
