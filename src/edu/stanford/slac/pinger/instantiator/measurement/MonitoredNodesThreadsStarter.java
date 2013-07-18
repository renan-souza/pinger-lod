package edu.stanford.slac.pinger.instantiator.measurement;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.rest.pingtable.GetPingTableTSV;

public class MonitoredNodesThreadsStarter {


	public static void start(JsonArray monitoringNodes, String metric, String packetSize, String tickParameter, long tid) {

		for (JsonElement monitoringEl : monitoringNodes) {
			String monitoring = monitoringEl.getAsString();
			JsonObject monitoringNodeDetails = C.getNodeDetails().get(monitoring).getAsJsonObject();
			String fromNickName =  monitoringNodeDetails.get("SourceNickName").getAsString();

			HashMap<String,HashMap<String, String>> map = GetPingTableTSV.getMeasurementMap(fromNickName, metric, packetSize, tickParameter);
			if (map == null) {
				continue;
			}
			JsonObject monitoringObj = C.getMonitoringMonitoredGroupedJSON().get(monitoring).getAsJsonObject();
			int nThreads = monitoringObj.entrySet().size();
			Thread[] threads = new Thread[nThreads];
			int i = 0;
			for (Entry<String,JsonElement> entry : monitoringObj.entrySet()) {
				JsonArray monitoredNodes = (JsonArray) entry.getValue();
				threads[i++] = new MonitoredNodesThread(monitoring, map, monitoredNodes, metric, packetSize, tickParameter);
			}
			for (i = 0; i < threads.length; i++) {
				threads[i].start();
			}
			for (i = 0; i < threads.length; i++) {
				try {
					threads[i].join();
				} catch (Exception e) { System.out.println(e); }
			}			
		}
	}
}
