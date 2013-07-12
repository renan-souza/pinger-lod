package edu.stanford.slac.pinger.general;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;



public final class C {
	
	/* *******************************************************
	 * ************* Sesame Configuration ********************
	 ********************************************************* */ 
	public static final String SESAME_SERVER = "http://localhost:8181/openrdf-sesame/";
	public static final String REPOSITORY_ID = "pinger";


	/* *******************************************************
	 * ***************** JSON Files **************************
	 ********************************************************* */ 
	//public static final String JSON_NODES_FILE = "json/NodesData2.json";
	public static final String JSON_NODES_FILE = "data/json/NodesDataComplete.json";
	public static final String MONITORING_MONITORED_JSON_FILE = "data/json/MonitoringMoniredNodes.json";
	public static final String COUNTRIES_JSON = "data/json/countries.json";
	
	/* *******************************************************
	 * ***************** RDF Files **************************
	 ********************************************************* */ 
	public static final String PREFIXES_FILE = "data/rdf/prefixes.rdf";
	
	
	/* *****************************************************************
	 * ***************** Web Sparql Endpoints **************************
	 ******************************************************************* */ 	
	public static final String DBPEDIA_ENDPOINT = "http://dbpedia.org/sparql/";
	public static final String FACTFORGE_ENDPOINT = "http://factforge.net/sparql";
	public static final String FACTFORGE_ENDPOINT_JSON = FACTFORGE_ENDPOINT+".json";

	public static final String[] GEONAMES_USERNAME = {"pinger", "renansouza", "renan2", "renan3", "demo"};
	//http://www.geonames.org/manageaccount


	/* *******************************************************************
	 * ************* Other General Public Constants *******************
	 ******************************************************************* */ 	
	public static final String PROJECT_HOME = System.getProperty("user.dir");
	public static final String PERL_HOME = "C:\\strawberry\\perl\\bin";

	public static final String STANDARD_SPARQLQUERY = "select * where { ?a ?b ?c } limit 10";
	public static final int MAX_ATTEMPT_INSTANTIATOR = 5;

	public static boolean CONTINUE_TOWN = true;
	public static boolean CONTINUE_COUNTRY = true;


	
	
	/* *******************************************************************
	 * ************* General Functions ***********************************
	 ******************************************************************* */ 
	
	private static JsonObject NODE_DETAILS = null;
	public static JsonObject getNodeDetails() {
		if (NODE_DETAILS==null) {
			NODE_DETAILS = getJsonAsObject(JSON_NODES_FILE);
		} 
		return NODE_DETAILS;
	}

	private static JsonElement getJsonElement(String jsonFilePath) {
		BufferedReader br = null;		 
		try {
			String jsonStr = ""; 
			String sCurrentLine;
			br = new BufferedReader(new FileReader(jsonFilePath)); 
			while ((sCurrentLine = br.readLine()) != null) {
				jsonStr += sCurrentLine+"\n";
			}		
			return new JsonParser().parse(jsonStr);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}


	public static JsonObject getJsonAsObject(String jsonFilePath) {
		try {
			return (JsonObject) getJsonElement(jsonFilePath);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public static JsonArray getJsonAsArray(String jsonFilePath) {
		try {
			return (JsonArray) getJsonElement(jsonFilePath);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}	

	/**
	 * This function is used to get the values of the properties you need from a resource.
	 * It is to be used with a sparql query of the format " select * where { :resource ?property ?value } " 
	 * @param json The json Result (It is expected a RDF Json format).
	 * @param properties A HashSet with the variables to be searched for.  
	 * @return The JsonObject with the variables and their values.
	 */
	public static JsonObject  getValues(JsonObject json, HashSet<String> properties) {
		JsonArray head = json.get("head").getAsJsonObject().get("vars").getAsJsonArray();
		JsonObject propertiesAndValues = new JsonObject();
		if (head.size()==2) {
			String prop = head.get(0).getAsString().toString().replace("\"", "");
			String value = head.get(1).getAsString().toString().replace("\"", "");			
			JsonArray jArr = json.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
			if (jArr.size()>0) {
				for (int i = 0; i < jArr.size(); i++ ) {
					JsonObject j = jArr.get(i).getAsJsonObject();
					String p = j.get(prop).getAsJsonObject().get("value").toString().replace("\"", "");
					if (properties.contains(p)) {
						String v = j.get(value).getAsJsonObject().get("value").toString().replace("\"", "");
						JsonArray j1;
						if (propertiesAndValues.get(p) == null) { 
							j1 = new JsonArray(); 
							j1.add(new JsonPrimitive(v));
							propertiesAndValues.add(p, j1);
						}
						else {
							j1 = propertiesAndValues.get(p).getAsJsonArray();
							j1.add(new JsonPrimitive(v));
						}						
					}
				}
			}			
		} 
		return propertiesAndValues;
	}




	/**
	 * @param json The json Result (It is expected a Jena Api Json format).
	 * @param variable The head variable you want the result from.
	 * @return The value of a variable in a Jena Json Query Result
	 */
	public static String getValue(JsonObject json, String variable) {
		return getValue(json, variable, 0);
	}

	/**
	 * @param json The json Result (It is expected a Jena Api Json format).
	 * @param variable The head variable you want the result from.
	 * @param index The index in the result table.
	 * @return The value of a variable in a Jena Json Query Result
	 */
	public static String getValue(JsonObject json, String variable, int index) {
		try {
			JsonArray jArr = json.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
			if (jArr.size() > 0) {
				JsonObject j = jArr.get(index).getAsJsonObject();
				return j.get(variable).getAsJsonObject().get("value").toString().replace("\"", ""); 
			} else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeIntoFile(String content, String filePath) {
		try {
			PrintWriter out = new PrintWriter(filePath);
			out.println(content);
			out.close();
			System.out.println("Written into file " + filePath);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void log(Object msg) {
		try {
			String logFile = "data/log/log.txt";
			msg = msg.toString();
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(System.currentTimeMillis()); int day = c.get(Calendar.DAY_OF_MONTH); int month = c.get(Calendar.MONTH); int year = c.get(Calendar.YEAR);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int min = c.get(Calendar.MINUTE);
			int sec = c.get(Calendar.SECOND);
			String date = month+"/"+day+"/"+year + " " +hour+":"+min+":"+sec;
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
			out.println(date + " -- " + msg);
			out.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}




