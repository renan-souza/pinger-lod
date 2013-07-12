package edu.stanford.slac.pinger.repository.instantiator.physicallocation;

import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Value;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.bean.ContinentBean;
import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.general.ontology.RDFS;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.pinger.rest.HttpGetter;
import edu.stanford.slac.pinger.rest.query.RDFReaderOpenRDF;

public class CountryInstantiator {

	/**
	 * 
	 * @param getJsonFrom Possible values: "file" or anything else. If "file", the instantiator will work with the Json stored file.
	 *  Otherwise, it will generate a Json file from Geonames. Using "file" is recommended since the list of countries does not change often, unless you want to update the file.
	 * 
	 */
	/**
	 * 
	 * @param file Set to true to work with the Json stored file.  Otherwise, it will generate a Json file from Geonames. Using "file" is recommended since the list of countries does not change often, unless you want to update the file.
	 * @param setdbpedia Set to true to access geonames rdfs to get Dbpedia
	 */
	public static void start(boolean file, boolean setdbpedia) {
		JsonArray jArr = null; 
		if (file) {
			jArr = C.getJsonAsArray(C.COUNTRIES_JSON);
		} else {
			String url = "http://api.geonames.org/countryInfoJSON?username="+C.GEONAMES_USERNAME[0];
			jArr = HttpGetter.getJsonArrayGeonames(url);
			if (jArr != null){
				C.writeIntoFile(jArr.toString(), C.COUNTRIES_JSON);
			}
		}
		if (jArr==null) {
			System.out.println("Could not instantiate countries");
			return;
		} 
		
		
		for (int i = 0; i < jArr.size(); i++) {
			JsonObject json = jArr.get(i).getAsJsonObject();

			if (!C.CONTINUE_COUNTRY) break; //this is set in: HttpGetter.getJsonArrayGeonames
			
			String gnName = json.get("countryName").toString().replace("\"", "");
			String gnPopulation = json.get("population").toString().replace("\"", "");
			String geonamesId = json.get("geonameId").toString().replace("\"", "");
			String languages = json.get("languages").toString().replace("\"", "");
			String currencyCode = json.get("currencyCode").toString().replace("\"", "");
			String areaInSqKm = json.get("areaInSqKm").toString().replace("\"", "");
			String continentCode = json.get("continent").toString().replace("\"", "");
			String countryCode = json.get("countryCode").toString().replace("\"", "");
			String capital = json.get("capital").toString().replace("\"", "");
			ContinentBean cb = ContinentBean.MAP.get(continentCode);

			GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
			gm.begin();
			String countryURI = P.BASE+"Country"+geonamesId;
			gm.addTripleResource(countryURI, P.RDF, "type", P.MGC, "Country", P.COUNTRIES_CONTEXT, false);
			try {
				gm.addTripleLiteral(countryURI, P.GN_ONT, "name", gnName, P.COUNTRIES_CONTEXT, false);
				gm.addTripleLiteral(countryURI, P.GN_ONT, "population", Integer.parseInt(gnPopulation), P.COUNTRIES_CONTEXT, false);

				gm.addTripleLiteral(countryURI, P.MGC, "areaInSqKm", Double.parseDouble(areaInSqKm), P.COUNTRIES_CONTEXT, false);
				gm.addTripleLiteral(countryURI, P.MGC, "currency", currencyCode, P.COUNTRIES_CONTEXT, false);
				gm.addTripleLiteral(countryURI, P.MGC, "languages", languages, P.COUNTRIES_CONTEXT, false);
				gm.addTripleLiteral(countryURI, P.MGC, "countryCode", countryCode, P.COUNTRIES_CONTEXT, false);
				gm.addTripleLiteral(countryURI, P.MGC, "capital", capital, P.COUNTRIES_CONTEXT, false);
				gm.addTripleLiteral(countryURI, P.MGC, "ContinentName", cb.getGnName(), P.COUNTRIES_CONTEXT, false);
			} catch (Exception e) {
				System.out.println(CountryInstantiator.class);
				System.out.println(gnName);
				System.out.println(e);
			}
			gm.addTripleLiteral(countryURI, P.MGC, "GeonamesLink", "http://sws.geonames.org/"+geonamesId+"/", P.COUNTRIES_CONTEXT, false);
			if (setdbpedia) {
				String url_rdf = "http://sws.geonames.org/"+geonamesId+"/about.rdf";
				String subj = "http://sws.geonames.org/"+geonamesId+"/";
				String pred = RDFS.seeAlso;
				RDFReaderOpenRDF rdfReader = new RDFReaderOpenRDF(url_rdf);
				Model model = rdfReader.filterWeb(subj, pred, null);
				Set<Value> objs = model.objects();
				Iterator<Value> it = objs.iterator();
				while(it.hasNext()) {
					Value v = (Value) it.next();
					gm.addTripleLiteral(countryURI, P.MD, "DBPediaLink", v.stringValue(), P.COUNTRIES_CONTEXT, false);
				}
			}
			gm.addTripleLiteral(countryURI, P.MGC, "isInContinent", P.BASE + "Continent"+cb.getGeoNamesId(), P.COUNTRIES_CONTEXT, false);

			gm.commit();

		}



	}

}


