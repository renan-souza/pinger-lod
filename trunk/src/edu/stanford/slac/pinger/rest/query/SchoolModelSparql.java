package edu.stanford.slac.pinger.rest.query;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.bean.SchoolBean;
import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;

public class SchoolModelSparql {

	public static void main(String args[]) {

	}

	public static SchoolBean getInfoFromDBPedia(SchoolBean sb) {
		if (sb.getSchoolDBPediaLink() == null) return sb;
		sb.setSchoolWikipediaLink(sb.getSchoolDBPediaLink().replace("http://dbpedia.org/resource/","http://wikipedia.org/wiki/"));
		String query =
				"SELECT distinct * WHERE {" +
						"<resource> ?prop ?value . " +
						"}";
		query = query.replace("resource", sb.getSchoolDBPediaLink());
		try {
			JsonObject json = QuerySparqlEndpoints.getResultAsJson(query, C.DBPEDIA_ENDPOINT);
			String owl = P.MAP_PREFIXES.get(P.OWL);
			String rdf = P.MAP_PREFIXES.get(P.RDF);
			String dbpowl = P.MAP_PREFIXES.get(P.DBP_OWL);
			String dbpprop = P.MAP_PREFIXES.get(P.DBPPROP);
			String foaf = P.MAP_PREFIXES.get(P.FOAF);
			String geo = P.MAP_PREFIXES.get(P.POS);

			HashSet<String> props = new HashSet<String>();
			props.add(owl+"sameAs");
			props.add(dbpowl+"type");
			props.add(rdf+"label");
			props.add(foaf+"name");
			props.add(dbpprop+"name");
			props.add(dbpowl+"endowment");
			props.add(dbpowl+"facultySize");
			props.add(dbpowl+"numberOfPostgraduateStudents");	
			props.add(dbpowl+"numberOfStudents");
			props.add(dbpowl+"numberOfUndergraduateStudents");	
			props.add(dbpowl+"wikiPageExternalLink");
			props.add(dbpprop+"undergrad");
			props.add(dbpprop+"students");
			props.add(dbpprop+"postgrad");
			props.add(geo+"lat");
			props.add(geo+"long");

			JsonObject j = C.getValues(json, props);

			
			//SchoolName
			try {
				if (j.get(dbpprop+"name") != null) {
					JsonArray jarr = j.get(dbpprop+"name").getAsJsonArray();
					if (jarr.size()>0){
						sb.setSchoolName(jarr.get(0).getAsString());
					}
				}

			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "name -- " + e);
			}
			//SchoolEndowment
			try {
				if (j.get(dbpowl+"endowment") != null) {
					JsonArray jarr = j.get(dbpowl+"endowment").getAsJsonArray();
					if (jarr.size()>0){
						sb.setSchoolEndowment(jarr.get(0).getAsString());
					}
				}
			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "endowment -- " + e);
			}
			//SchoolType
			try {
				if (j.get(dbpowl+"type") != null) {
					JsonArray jarr = j.get(dbpowl+"type").getAsJsonArray();
					if (jarr.size()>0){
						ArrayList<String> lst = new ArrayList<String>();
						for (int i = 0; i < jarr.size(); i++) {
							lst.add(jarr.get(i).getAsString());
						}
						sb.setSchoolType(lst);
					}
				}
			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "type -- " + e);
			}
			//SchoolFacultySize
			try {
				if (j.get(dbpowl+"facultySize") != null) {
					JsonArray jarr = j.get(dbpowl+"facultySize").getAsJsonArray();
					if (jarr.size()>0){
						sb.setSchoolFacultySize(jarr.get(0).getAsString());
					}
				}
			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "facultySize -- " + e);
			}
			//GeoLatitude
			try {
				if (j.get(geo+"lat") != null) {
					JsonArray jarr = j.get(geo+"lat").getAsJsonArray();
					if (jarr.size()>0){
						sb.setGeoLatitude(jarr.get(0).getAsString());
					}
				}
			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "GeoLatitude -- " + e);
			}
			//GeoLongitude
			try {
				if (j.get(geo+"long") != null) {
					JsonArray jarr = j.get(geo+"long").getAsJsonArray();
					if (jarr.size()>0){
						sb.setGeoLongitude(jarr.get(0).getAsString());
					}
				}
			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "GeoLongitude -- " + e);
			}
			//NumberOfStudents
			try {
				if (j.get(dbpowl+"numberOfStudents") != null) {
					JsonArray jarr = j.get(dbpowl+"numberOfStudents").getAsJsonArray();
					if (jarr.size()>0){
						sb.setSchoolNumberOfStudents(jarr.get(0).getAsString());
					} 
				} else if (j.get(dbpprop+"students") != null){
					JsonArray jarr = j.get(dbpprop+"students").getAsJsonArray();
					if (jarr.size()>0){
						sb.setSchoolNumberOfStudents(jarr.get(0).getAsString());
					} 
				}
			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "facultySize -- " + e);
			}
			//NumberOfGradStudents
			try {
				if (j.get(dbpowl+"numberOfPostgraduateStudents") != null) {
					JsonArray jarr = j.get(dbpowl+"numberOfPostgraduateStudents").getAsJsonArray();
					if (jarr.size()>0){
						sb.setSchoolNumberOfGradStudents(jarr.get(0).getAsString());
					} 
				} else if (j.get(dbpprop+"postgrad") != null){
					JsonArray jarr = j.get(dbpprop+"postgrad").getAsJsonArray();
					if (jarr.size()>0){
						sb.setSchoolNumberOfGradStudents(jarr.get(0).getAsString());
					} 
				}
			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "facultySize -- " + e);
			}
			//NumberOfUgradStudents
			try {
				if (j.get(dbpowl+"numberOfUndergraduateStudents") != null) {
					JsonArray jarr = j.get(dbpowl+"numberOfUndergraduateStudents").getAsJsonArray();
					if (jarr.size()>0){
						sb.setSchoolNumberOfUgradStudents(jarr.get(0).getAsString());
					} 
				} else if (j.get(dbpprop+"undergrad") != null){
					JsonArray jarr = j.get(dbpprop+"undergrad").getAsJsonArray();
					if (jarr.size()>0){
						sb.setSchoolNumberOfUgradStudents(jarr.get(0).getAsString());
					} 
				}
			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "facultySize -- " + e);
			}
			//SchoolFreebaseLink
			try {
				if (j.get(owl+"sameAs") != null) {
					JsonArray jarr = j.get(owl+"sameAs").getAsJsonArray();
					if (jarr.size()>0){
						for (int i = 0; i < jarr.size(); i++) {
							String s = jarr.get(i).getAsString();
							if (s.contains("freebase")) {
								sb.setSchoolFreebaseLink(s);
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println(SchoolModelSparql.class + ".getInfoFromDBPedia." + "type -- " + e);
			}

			/*
			 * SchoolFreebaseLink
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb;
	}


	public static SchoolBean getDBPediaSchool(SchoolBean sb) {
		if (sb.getSchoolPingerName().contains("'")) sb.setSchoolPingerName(sb.getSchoolPingerName().replace("'", "")); //Jena queries dont like " ' " in strings		
		String query =
				"SELECT distinct * WHERE {" +
						"?school rdfs:label ?label . " +
						"{?school dbp-owl:type dbp-rsrc:Private_university.}" +
						"union" +
						"{?school dbp-owl:type dbp-rsrc:Public_university.}" +
						"union" +
						"{?school rdf:type dbp-owl:University.}" +
						"union" +
						"{?school rdf:type <http://schema.org/EducationalOrganization>.} " +
						"union" +
						"{?school rdf:type <http://schema.org/CollegeOrUniversity>.} " +
						"union" +
						"{?school rdf:type dbp-owl:EducationalInstitution.} " +							
						"filter ( contains( '"+sb.getSchoolPingerName()+"', str(?label) ) ||" +
						" contains( str(?label), '"+sb.getSchoolPingerName()+"' ) )" +
						"}";
		query = query.replace("dbp-owl", P.DBP_OWL);
		query = query.replace("dbp-rsrc", P.DBPRSRC);
		try {
			JsonObject json = QuerySparqlEndpoints.getResultAsJson(query, C.DBPEDIA_ENDPOINT);

			sb.setSchoolDBPediaLink(C.getValue(json, "school"));

		} catch (Exception e) {
			e.printStackTrace();
			sb.setSchoolDBPediaLink(null);
		}
		return sb;
	}

}


