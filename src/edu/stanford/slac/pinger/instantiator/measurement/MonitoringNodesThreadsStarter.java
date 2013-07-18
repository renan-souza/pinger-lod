package edu.stanford.slac.pinger.instantiator.measurement;

import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.general.C;

public class MonitoringNodesThreadsStarter {

	static JsonObject MonitoringGrouped = C.getJsonAsObject(C.MONITORING_NODES_GROUPED);
	public static void yearlyMonthlyThreadStarter(boolean allyearly,boolean allmonthly,String metric) {
		int nThreads = MonitoringGrouped.entrySet().size();
		Thread[] threads = new Thread[nThreads];
		int i = 0;
		for (Entry<String,JsonElement> entry : MonitoringGrouped.entrySet()) {
			JsonArray monitoringNodes = (JsonArray) entry.getValue();
			threads[i++] = new YearlyMonthlyThread(allyearly, allmonthly, monitoringNodes, metric);
		}
		for (i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		for (i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
				C.log("Thread " + threads[i].getId() + " from has finished its job.");
			} catch (Exception e) { System.out.println(e); }
		}
	}
	
	public static void last365DaysThreadStarter(String metric) {
		int nThreads = MonitoringGrouped.entrySet().size();
		Thread[] threads = new Thread[nThreads];
		
		int i = 0;
		for (Entry<String,JsonElement> entry : MonitoringGrouped.entrySet()) {
			JsonArray monitoringNodes = (JsonArray) entry.getValue();
			threads[i++] = new Last365DaysThread(metric, monitoringNodes);
		}
		for (i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		for (i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
				C.log("Thread " + threads[i].getId() + " has finished its job.");
			} catch (Exception e) { System.out.println(e); }
		}
	}
}
