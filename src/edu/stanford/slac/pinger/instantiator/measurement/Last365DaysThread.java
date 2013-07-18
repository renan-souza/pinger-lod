package edu.stanford.slac.pinger.instantiator.measurement;

import java.util.ArrayList;
import com.google.gson.JsonArray;
import edu.stanford.slac.pinger.general.C;

public class Last365DaysThread extends Thread {
	private String metric;
	private JsonArray monitoringNodes;
	public Last365DaysThread(String metric, JsonArray monitoringNodes) {
		this.metric = metric;
		this.monitoringNodes = monitoringNodes;
	}
	private void startLast365Days() {
		ArrayList<String> tickParams = new ArrayList<String>();
		tickParams.add("tick=last365days");
		for (String tickParam : tickParams) {
			long t1 = System.currentTimeMillis();
			for (String packetSize : MeasurementUtils.packetSizes) {
				try {
					MonitoredNodesThreadsStarter.start(monitoringNodes, metric, packetSize, tickParam, this.getId());
				} catch (Exception e) {
					System.out.println(e);
					C.log(YearlyMonthlyThread.class + " " + e);
					continue;
				}
			}
			long t2 = System.currentTimeMillis();
			C.log("It took " + (t2 - t1)/1000.0 + " seconds to instantiate for metric: "+metric + " " + tickParam + " for the monitoring nodes: " + this.monitoringNodes.toString());
		}
	}
	public void run() {
		startLast365Days();
	}
}
