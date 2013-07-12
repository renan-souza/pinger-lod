package edu.stanford.slac.pinger.rest;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.slac.pinger.general.C;

public class GenerateMonitoringMonitoredJson {

	public static void main(String[] args) {
		start();
	}

	public static void start() {
		JsonObject MonitoringNodes = new JsonObject();
		ArrayList<String> monitoringLst = getMonitoring();
		if (monitoringLst!=null) {
			for (String monitoringHost : monitoringLst) {
				ArrayList<String> monitoredLst = getMonitored(monitoringHost);
				if (monitoredLst!=null) {
					JsonArray monitoredArr = (JsonArray) new JsonParser().parse(monitoredLst.toString());
					MonitoringNodes.add(monitoringHost, monitoredArr);
				}
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(MonitoringNodes);
		C.writeIntoFile(json, C.MONITORING_MONITORED_JSON_FILE);
	}

	public static ArrayList<String> getMonitoring() {
		return getHosts("http://www-wanmon.slac.stanford.edu/cgi-wrap/dbprac.pl?monalias=all", "\\s+([0-9])+\\s+");
	}

	public static ArrayList<String> getMonitored(String monitoringHost) {
		try {
			String nickname = C.getNodeDetails().get(monitoringHost).getAsJsonObject().get("SourceNickName").getAsString();
			String URLMonitoredHosts = "http://www-wanmon.slac.stanford.edu/cgi-wrap/dbprac.pl?monalias="+nickname+"&find=1";
			return getHosts(URLMonitoredHosts, monitoringHost);
		} catch (Exception e) {
			System.out.println(e + " " + monitoringHost);
			return null;
		}
	}

	private static ArrayList<String> getHosts(String URL, String regexPattern) {
		String content = HttpGetter.readPage(URL);		
		ArrayList<String> hosts = new ArrayList<String>();

		if (content.indexOf("<pre>")!=-1) {
			content = content.split("<pre>")[1].split("</pre>")[0];
			String lines[] = content.split("\n");
			for (String host : lines) {
				String arr[] = host.split(regexPattern);
				if (arr.length > 1) {
					String h = arr[1].replaceAll("\\s", "");
					hosts.add(h);
				}
			}
			return hosts;
		} else
			return null;
	}


}
