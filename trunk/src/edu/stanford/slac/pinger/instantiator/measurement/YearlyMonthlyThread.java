package edu.stanford.slac.pinger.instantiator.measurement;

import java.util.ArrayList;
import com.google.gson.JsonArray;
import edu.stanford.slac.pinger.general.C;

public class YearlyMonthlyThread extends Thread {

	private boolean allyearly, allmonthly;
	private JsonArray monitoringNodes;
	private String metric;
	public YearlyMonthlyThread(boolean allyearly,boolean allmonthly, JsonArray monitoringNodes, String metric) {
		this.allmonthly = allmonthly;
		this.allyearly = allyearly;
		this.monitoringNodes = monitoringNodes;
		this.metric = metric;
	}
	private void startYearlyMonthly() {
		ArrayList<String> tickParams = new ArrayList<String>();
		if (allyearly) 	tickParams.add("tick=allyearly");
		if (allmonthly) tickParams.add("tick=allmonthly");
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
			//C.log("It took " + (t2 - t1)/1000.0/60.0 + " minutes to instantiate for metric: "+metric + " " + tickParam + " for the monitoring nodes: " + this.monitoringNodes.toString());		
		}
	}



	public void run() {
		startYearlyMonthly();
	}
}
