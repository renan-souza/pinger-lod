package edu.stanford.slac.pinger.instantiator.physicallocation;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.bean.TownBean;
import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.general.ontology.GN_ONT;
import edu.stanford.slac.pinger.general.ontology.RDFS;
import edu.stanford.slac.pinger.instantiator.nodes.NodesInstantiator;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.pinger.rest.query.RDFReaderOpenRDF;
import edu.stanford.slac.pinger.rest.query.town.TownModelGeonamesQuery;
import edu.stanford.slac.pinger.rest.query.town.TownModelSparql;

public class TownInstantiator {


	public static void start() {

		JsonObject json = C.getJsonAsObject(C.JSON_NODES_FILE);
		if (json == null) {
			C.log(NodesInstantiator.class + "Json Null");
			return;
		}
		
		int attempt = 0;	
		for (Entry<String,JsonElement> entry : json.entrySet()) {
			
			if (!C.CONTINUE_TOWN) break; //this is set in: HttpGetter.getJsonArrayGeonames
			
			String key = entry.getKey();

			JsonObject j = json.get(key).getAsJsonObject();			

			double lat = Double.parseDouble(j.get("latitude").toString().replace("\"", ""));
			double lng = Double.parseDouble(j.get("longitude").toString().replace("\"", ""));

			try {

				System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
				System.out.println(j.get("SourceFullName") + "--- [lat,lng]= "+lat +", "+lng );
				TownBean mainTown = townInstantiator(lat,lng);
				System.out.println(mainTown);
				if (mainTown==null) {
					C.log(TownInstantiator.class + " key: " + key + "  Could not instantiate Town for these lat and long.");
					System.out.println("Could not instantiate Town for these lat and long");
					continue;
				}

				GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
				gm.begin();
				String townURI = P.BASE+"Town"+mainTown.getGeonamesId();

				//Datatype Properties
				gm.addTripleLiteral(townURI, P.MGC, "PingERLat", lat, P.TOWN_CONTEXT, true);
				gm.addTripleLiteral(townURI, P.MGC, "PingERLong", lng, P.TOWN_CONTEXT, true);
				gm.addTripleLiteral(townURI, P.POS, "lat", mainTown.getLatitude(), P.TOWN_CONTEXT, false);
				gm.addTripleLiteral(townURI, P.POS, "long", mainTown.getLongitude(), P.TOWN_CONTEXT, false);
				gm.addTripleLiteral(townURI, P.GN_ONT, "population", mainTown.getPopulation(), P.TOWN_CONTEXT, false);

				if (mainTown.getName() != null)
					gm.addTripleLiteral(townURI, P.GN_ONT, "name", mainTown.getName(), P.TOWN_CONTEXT, false);


				if (mainTown.getPostalCodes()!=null) {
					for (String postalCode : mainTown.getPostalCodes()) 
						gm.addTripleLiteral(townURI, P.GN_ONT, "postalCode", postalCode, P.TOWN_CONTEXT, false);
				}

				//MGC Datatype Properties
				if (mainTown.getNearestCity() != null) {
					gm.addTripleLiteral(townURI, P.MGC, "GeoNearestCity", mainTown.getNearestCity().getName(), P.TOWN_CONTEXT, false);
					gm.addTripleLiteral(townURI, P.MGC, "GeoNearestCityPopulation", mainTown.getNearestCity().getPopulation(), P.TOWN_CONTEXT, false);
				}
				if (mainTown.getCounty()!=null)
					gm.addTripleLiteral(townURI, P.MGC, "GeoCounty", mainTown.getCounty(), P.TOWN_CONTEXT, false);
				if (mainTown.getStateName()!=null)
					gm.addTripleLiteral(townURI, P.MGC, "GeoState", mainTown.getStateName(), P.TOWN_CONTEXT, false);
				if (mainTown.getCountryName()!=null)
					gm.addTripleLiteral(townURI, P.MGC, "GeoCountry", mainTown.getCountryName(), P.TOWN_CONTEXT, false);
				if (mainTown.getContinentName()!=null)
					gm.addTripleLiteral(townURI, P.MGC, "GeoContinent",  mainTown.getContinentName(), P.TOWN_CONTEXT, false);
				gm.addTripleLiteral(townURI, P.MGC, "GeoGMTOffset", mainTown.getGmtOffset(), P.TOWN_CONTEXT, false);

				//Object Properties
				gm.addTripleResource(townURI, P.RDF, "type", P.MGC, "Town", P.TOWN_CONTEXT, false);		

				if (mainTown.getGeonamesLink()!=null)
					gm.addTripleResource(townURI, P.MGC, "GeonamesLink", mainTown.getGeonamesLink(), P.TOWN_CONTEXT, false);
				if (mainTown.getDbpediaLink()!=null)
					gm.addTripleResource(townURI, P.MGC, "DBPediaLink", mainTown.getDbpediaLink(), P.TOWN_CONTEXT, false);
				if (mainTown.getFreebaseLink()!=null)
					gm.addTripleResource(townURI, P.MGC, "FreebaseLink", mainTown.getFreebaseLink(), P.TOWN_CONTEXT, false);
				if (mainTown.getWikiPediaLink()!=null)
					gm.addTripleResource(townURI, P.GN_ONT, "wikipediaArticle", mainTown.getWikiPediaLink(), P.TOWN_CONTEXT, false);
				if (mainTown.getCountryGeoId()!=null) {
					gm.addTripleResource(townURI, P.GN_ONT, "parentCountry", P.GN, mainTown.getCountryGeoId(), P.TOWN_CONTEXT, false);
					gm.addTripleResource(townURI, P.MGC, "isInCountry", P.BASE+"Country"+mainTown.getCountryGeoId().replace("http://sws.geonames.org/",""), P.TOWN_CONTEXT, false);
				}
				//Instantiating a State
				if (mainTown.getStateGeoId()!=null) {
					gm.addTripleResource(townURI, P.GN_ONT, "parentADM1", P.GN, mainTown.getStateGeoId(), P.TOWN_CONTEXT, false);
					
					String geoStateId = mainTown.getStateGeoId().replace("http://sws.geonames.org/","");
					String stateURI = P.BASE + "State"+geoStateId;
					gm.addTripleResource(stateURI, P.RDF, "type", P.MGC, "State", P.TOWN_CONTEXT, false);
					
					gm.addTripleResource(stateURI, P.MGC, "GeonamesLink",  P.GN, mainTown.getStateGeoId(), P.TOWN_CONTEXT, false);
					
					String url_rdf = "http://sws.geonames.org/"+geoStateId+"/about.rdf";
					RDFReaderOpenRDF rdfReader = new RDFReaderOpenRDF(url_rdf);

					String subj = "http://sws.geonames.org/"+geoStateId+"/";
					
					Model model = rdfReader.filterWeb(subj, RDFS.seeAlso, null);
					Set<Value> objs = model.objects();
					Iterator<Value> it = objs.iterator();
					while(it.hasNext()) {
						Value v = (Value) it.next();
						gm.addTripleResource(stateURI, P.MGC, "DBPediaLink", v.stringValue(), P.TOWN_CONTEXT, false);
					}
					
					
					model = rdfReader.filterWeb(subj, GN_ONT.wikipediaArticle, null);
					objs = model.objects();
					it = objs.iterator();
					while(it.hasNext()) {
						Value v = (Value) it.next();
						gm.addTripleResource(stateURI, GN_ONT.wikipediaArticle, v.stringValue(), P.TOWN_CONTEXT, false);
					}
					
					model = rdfReader.filterWeb(subj, GN_ONT.name, null);
					objs = model.objects();
					it = objs.iterator();
					while(it.hasNext()) {
						Value v = (Value) it.next();
						gm.addTripleLiteral(stateURI, GN_ONT.PREFIX, "name", v.stringValue(), P.TOWN_CONTEXT, false);
					}	
					
					model = rdfReader.filterWeb(subj, GN_ONT.population, null);
					objs = model.objects();
					it = objs.iterator();
					while(it.hasNext()) {
						Value v = (Value) it.next();
						gm.addTripleLiteral(stateURI, GN_ONT.PREFIX, "population", Integer.parseInt(v.stringValue()), P.TOWN_CONTEXT, false);
					}		
					
					model = rdfReader.filterWeb(subj, GN_ONT.parentCountry, null);
					objs = model.objects();
					it = objs.iterator();
					while(it.hasNext()) {
						Value v = (Value) it.next();
						String geoCountry = v.stringValue().replace("http://sws.geonames.org/", "").replace("/", "");
						gm.addTripleResource(stateURI, P.MGC, "isInCountry", P.BASE+"Country"+geoCountry, P.TOWN_CONTEXT, false);
					}	
					
					gm.addTripleResource(townURI, P.MGC, "isInState", stateURI, P.TOWN_CONTEXT, false);
					
				}
				//MGC Object Properties
				if (mainTown.getNearestCity() != null) {
					if (mainTown.getNearestCity().getGeonamesLink()!=null)
						gm.addTripleResource(townURI, P.MGC, "nearestCityGeonames", mainTown.getNearestCity().getGeonamesLink(), P.TOWN_CONTEXT, false);
					if (mainTown.getNearestCity().getDbpediaLink()!=null)
						gm.addTripleResource(townURI, P.MGC, "nearestCityDBPedia", mainTown.getNearestCity().getDbpediaLink(), P.TOWN_CONTEXT, false);
					if (mainTown.getNearestCity().getFreebaseLink()!=null)
						gm.addTripleResource(townURI, P.MGC, "nearestCityFreebase", mainTown.getNearestCity().getFreebaseLink(), P.TOWN_CONTEXT, false);
				}
				if (mainTown.getContinentGeoId()!=null) {
					gm.addTripleResource(townURI, P.MGC, "isInContinent", P.BASE+"Continent"+mainTown.getContinentGeoId().replace("http://sws.geonames.org/",""), P.TOWN_CONTEXT, false);				
				}

				gm.commit();
				
			} catch (Exception e) {
				C.log(TownInstantiator.class + "Key: " + key + " Exception: " + e);
				attempt++;
				if (attempt > C.MAX_ATTEMPT_INSTANTIATOR) {
					C.log(TownInstantiator.class + "maximum attempts reached. Exiting...");
					return;
				}
				continue;
			}

		}



	}

	/**
	 * Routine to fill the fields of a TownBean
	 * @param lat
	 * @param lng
	 * @return
	 */
	public static TownBean townInstantiator(double lat, double lng) {
		System.out.println("Getting city1000");
		TownBean mainTown = TownModelGeonamesQuery.getCity1000(lat,lng);		
		if (mainTown==null) {
			mainTown = TownModelGeonamesQuery.getCity15000(lat, lng);
			if (mainTown==null) return null;
		}
		System.out.println("Getting nearest city");
		mainTown.setNearestCity(TownModelGeonamesQuery.getCity15000(lat, lng));

		mainTown.setPingerLat(lat);
		mainTown.setPingerLong(lng);

		//Try to set up DbpediaId, FreebaseId
		System.out.println("Accessing dbpedia to get DBPedia and Freebase resources");
		TownModelSparql.getDBPediaAndFreebaseResourcesFromGeonamesId(mainTown);

		//If we don't have the FreebaseID, but we got the DBPediaID from either one of the functions above (both of them try to set DBPedia ID)
		//Try to set up FreebaseID from DBPedia
		if (mainTown.getFreebaseLink()==null && mainTown.getDbpediaLink() != null) {
			System.out.println("Freebase not set yet... Trying to get it from DBPedia...");
			TownModelSparql.getFreebaseResourceFromDBPedia(mainTown);
		}
		//If after these queries, we still do not have DBPedia link or Population=0, it is likely that the town is very small.
		//Then the town becomes the nearest city.
		if ((mainTown.getDbpediaLink() == null || mainTown.getPopulation() == 0) && mainTown.getNearestCity() != null) {
			System.out.println("Town not found in DBPedia or its population is 0... The town becomes the nearest city and another attempt to get the DBPedia ID");
			mainTown = mainTown.getNearestCity();
			mainTown.setNearestCity(mainTown);
			TownModelSparql.getDBPediaAndFreebaseResourcesFromGeonamesId(mainTown);
			if (mainTown.getFreebaseLink()==null && mainTown.getDbpediaLink() != null) {
				System.out.println("Another attempt to get Freebase id...");
				TownModelSparql.getFreebaseResourceFromDBPedia(mainTown);
			}
		}

		//Try to set up StateDBPediaId, countryDBPediaID, continentName, continentGeoId continentDBPediaId
		TownModelSparql.getStateCountryContinentFromGeonamesId(mainTown);
		if (mainTown.getStateDBPediaId()==null)
			TownModelSparql.getCountryContinentFromGeonamesId(mainTown);

		return mainTown;
	}




}


