package edu.stanford.slac.pinger.instantiator.physicallocation;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.bean.SchoolBean;
import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.pinger.model.NodesModel;
import edu.stanford.slac.pinger.rest.query.SchoolModelSparql;

public class SchoolInstantiator {


	public static void start() {

		int contSchools = 0;
		JsonObject json = C.getJsonAsObject(C.JSON_NODES_FILE);
		int total = json.entrySet().size();

		for (Entry<String,JsonElement> entry : json.entrySet()) {
			String key = entry.getKey();
			System.out.println("*******************#######################==="+key);
			JsonObject j = json.get(key).getAsJsonObject();

			SchoolBean sb = new SchoolBean();
			sb.setSchoolPingerName(j.get("SourceFullName").toString().replace("\"", ""));
			sb.setPingERLat(j.get("latitude").toString().replace("\"", ""));
			sb.setPingERLong(j.get("longitude").toString().replace("\"", ""));


			SchoolModelSparql.getDBPediaSchool(sb);
			if (sb.getSchoolDBPediaLink()!=null) {

				try {
					contSchools++;
					SchoolModelSparql.getInfoFromDBPedia(sb);

					System.out.println(sb);
					System.out.println("||||||||||||||||||||||||||||||||");


					String schoolURI = P.BASE + sb.getSchoolDBPediaLink().replace("http://dbpedia.org/resource/", "");
					GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
					gm.begin();
					gm.addTripleResource(schoolURI, P.RDF, "type", P.MGC, "School", P.SCHOOLS_CONTEXT, false);
					gm.addTripleResource(schoolURI, P.MGC, "DBPediaLink", sb.getSchoolDBPediaLink(), P.SCHOOLS_CONTEXT, false);
					gm.addTripleResource(schoolURI, P.MGC, "WikipediaLInk", sb.getSchoolWikipediaLink(), P.SCHOOLS_CONTEXT, false);
					if(sb.getSchoolFreebaseLink()!=null)
						gm.addTripleResource(schoolURI, P.MGC, "FreebaseLink", sb.getSchoolFreebaseLink(), P.SCHOOLS_CONTEXT, false);

					if(sb.getSchoolType()!=null) {
						for (String t : sb.getSchoolType()) {
							gm.addTripleResource(schoolURI, P.MGC, "SchoolType", t, P.SCHOOLS_CONTEXT, false);
						}
					}
					try {
						if(sb.getSchoolNumberOfStudents()!=null)
							gm.addTripleLiteral(schoolURI, P.MGC, "SchoolNumberOfStudents", Integer.parseInt(sb.getSchoolNumberOfStudents()), P.SCHOOLS_CONTEXT, false);
					} catch (Exception e) {
						System.out.println(e);
						C.log(SchoolInstantiator.class + " " + entry.getKey());
					}
					try {
						if(sb.getSchoolNumberOfUgradStudents()!=null)
							gm.addTripleLiteral(schoolURI, P.MGC, "SchoolNumberOfUgradStudents", Integer.parseInt(sb.getSchoolNumberOfUgradStudents()), P.SCHOOLS_CONTEXT, false);
					} catch (Exception e) {
						System.out.println(e);
						C.log(SchoolInstantiator.class + " " + entry.getKey() + " " + e);
					}
					try {
						if(sb.getSchoolNumberOfGradStudents()!=null)
							gm.addTripleLiteral(schoolURI, P.MGC, "SchoolNumberOfGradStudents", Integer.parseInt(sb.getSchoolNumberOfGradStudents()), P.SCHOOLS_CONTEXT, false);
					} catch (Exception e) {
						System.out.println(e);
						C.log(SchoolInstantiator.class + " " + entry.getKey() + " " + e);
					}	
					try {
						if(sb.getSchoolEndowment()!=null)
							gm.addTripleLiteral(schoolURI, P.MGC, "SchoolEndowment", Float.parseFloat(sb.getSchoolEndowment()), P.SCHOOLS_CONTEXT, false);
					} catch (Exception e) {
						System.out.println(e);
						C.log(SchoolInstantiator.class + " " + entry.getKey() + " " + e);
					}		
					try {
						if(sb.getSchoolFacultySize()!=null)
							gm.addTripleLiteral(schoolURI, P.MGC, "SchoolFacultySize", Integer.parseInt(sb.getSchoolFacultySize()), P.SCHOOLS_CONTEXT, false);
					} catch (Exception e) {
						System.out.println(e);
						C.log(SchoolInstantiator.class + " " + entry.getKey() + " " + e);
					}	
					try {
						if(sb.getSchoolName()!=null)
							gm.addTripleLiteral(schoolURI, P.MGC, "SchoolName", sb.getSchoolName(), P.SCHOOLS_CONTEXT, false);
					} catch (Exception e) {
						System.out.println(e);
						C.log(SchoolInstantiator.class + " " + entry.getKey() + " " + e);
					}		
					try {
						if(sb.getSchoolPingerName()!=null)
							gm.addTripleLiteral(schoolURI, P.MGC, "SchoolPingerName", sb.getSchoolPingerName(), P.SCHOOLS_CONTEXT, false);
					} catch (Exception e) {
						System.out.println(e);
						C.log(SchoolInstantiator.class + " " + entry.getKey() + " " + e);
					}		
					if(sb.getPingERLat()!=null)
						gm.addTripleLiteral(schoolURI, P.MGC, "PingERLat", Double.parseDouble(sb.getPingERLat()), P.SCHOOLS_CONTEXT, true);		
					if(sb.getPingERLong()!=null)
						gm.addTripleLiteral(schoolURI, P.MGC, "PingERLong", Double.parseDouble(sb.getPingERLong()), P.SCHOOLS_CONTEXT, true);
					try {
						if(sb.getGeoLatitude()!=null)
							gm.addTripleLiteral(schoolURI, P.POS, "lat", Double.parseDouble(sb.getGeoLatitude()), P.SCHOOLS_CONTEXT, false);
					} catch (Exception e) {
						System.out.println(e);
						C.log(SchoolInstantiator.class + " " + entry.getKey() + " " + e);
					}	
					try {
						if(sb.getGeoLongitude()!=null)
							gm.addTripleLiteral(schoolURI, P.POS, "long", Double.parseDouble(sb.getGeoLongitude()), P.SCHOOLS_CONTEXT, false);
					} catch (Exception e) {
						System.out.println(e);
						C.log(SchoolInstantiator.class + " " + entry.getKey() + " " + e);
					}		
					String townURI = NodesModel.getTownResourceFromPingerLatLong(sb.getPingERLat(), sb.getPingERLong());
					if (townURI != null)
						gm.addTripleResource(schoolURI, P.MGC, "isInTown", townURI, P.SCHOOLS_CONTEXT, false);
					gm.commit();
				} catch (Exception e) {
					System.out.println(e);
					C.log(SchoolInstantiator.class + " " + entry.getKey());
					continue;
				}
			}	
		}

		System.out.println("Number of schools="+contSchools + ", out of: "+total);
		C.log("Number of schools="+contSchools + ", out of: "+total);
	}

}
