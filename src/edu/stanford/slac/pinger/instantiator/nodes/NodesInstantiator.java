package edu.stanford.slac.pinger.instantiator.nodes;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.pinger.model.NodesModel;
import edu.stanford.slac.pinger.model.SchoolModel;

public class NodesInstantiator {


	public static void start() {
		JsonObject json = C.getNodeDetails();
		if (json == null) {
			C.log(NodesInstantiator.class + "Json Null");
			return;
		}
		
		int attempt = 0;		
		for (Entry<String,JsonElement> entry : json.entrySet()) {
			try {
				String key = entry.getKey();
				System.out.println(key);
				JsonObject j = json.get(key).getAsJsonObject();			

				String sourceName =  j.get("SourceName").toString().replace("\"", "");

				String latitude = j.get("latitude").toString().replace("\"", "");
				String longitude = j.get("longitude").toString().replace("\"", "");
				String continent = j.get("continent").toString().replace("\"", "");
				String country = j.get("country").toString().replace("\"", "");
				String group =j.get("country").toString().replace("\"", "");

				GeneralModelSingleton gm = GeneralModelSingleton.getInstance();			
				gm.begin();
				//Instantiating a PhysicalLocation
				String plURI = P.BASE+"PL-"+sourceName;
				gm.addTripleResource(plURI, P.RDF, "type", P.MGC, "PhysicalLocation", P.NODES_CONTEXT, false);

				gm.addTripleLiteral(plURI, P.MGC, "latitude", Double.parseDouble(latitude), P.NODES_CONTEXT, false);
					j.remove("latitude");
				gm.addTripleLiteral(plURI, P.MGC, "longitude",  Double.parseDouble(longitude), P.NODES_CONTEXT, false);
					j.remove("longitude");
				gm.addTripleLiteral(plURI, P.MGC, "country", country, P.NODES_CONTEXT, false);
					j.remove("country");
				gm.addTripleLiteral(plURI, P.MGC, "continent", continent, P.NODES_CONTEXT, false); 
					j.remove("continent");
				gm.addTripleLiteral(plURI, P.MGC, "group", group, P.NODES_CONTEXT, false);
					j.remove("group");

				//Instantiating a NodeInformation
				String nodeURI = P.BASE+"Node-"+sourceName;		
				
				gm.addTripleResource(nodeURI, P.RDF, "type", P.MD, "NodeInformation", P.NODES_CONTEXT, false);
				gm.addTripleResource(nodeURI, P.MD, "isInPhysicalLocation", plURI, P.NODES_CONTEXT, false);			

				String townURI = NodesModel.getTownResourceFromPingerLatLong(latitude, longitude);
				if (townURI != null)
					gm.addTripleResource(nodeURI, P.MD, "isInPhysicalLocation", townURI, P.NODES_CONTEXT, false);
				else
					C.log("Could not find a town with PingERLat="+latitude+", PingERLong="+longitude);
				String schoolURI = SchoolModel.getSchoolResourceFromPingerLatLong(latitude, longitude);
				if (schoolURI != null)
					gm.addTripleResource(nodeURI, P.MD, "isInPhysicalLocation", schoolURI, P.NODES_CONTEXT, false);
				

				//Instantiating information about a node
				for (Entry<String,JsonElement> e : j.entrySet()) {
					String k = e.getKey();
					String property = k+"Value";
					String value = j.get(k).toString().replace("\"", "");
					if (value.equals("")) value = "undefined";
					String uri = P.BASE+k+"-"+sourceName;
					gm.addTripleResource(uri, P.RDF, "type", P.MD, k, P.NODES_CONTEXT, false);
					gm.addTripleLiteral(uri, P.MD, property, value, P.NODES_CONTEXT, false);

					gm.addTripleResource(nodeURI, P.MD, "hasNodeInformation", uri, P.NODES_CONTEXT, false);
				}
				
				gm.commit();
				
			} catch (Exception e) {
				System.out.println(e);
				C.log(NodesInstantiator.class + "Key: " + entry.getKey() + " Exception: " + e);
				attempt++;
				if (attempt > C.MAX_ATTEMPT_INSTANTIATOR) {
					C.log(NodesInstantiator.class + "maximum attempts reached. Exiting...");
					return;
				}
				continue;
			}
		}
	}

}
