package edu.stanford.slac.pinger.instantiator.measurement.NOT_USED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.instantiator.measurement.MonitoredNodesThreadsStarter;
import edu.stanford.slac.pinger.instantiator.measurement.MeasurementUtils;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.pinger.rest.pingtable.GetPingTableTSV;

public class CopyOfMeasurementInstantiatorFromCSV {

	private static void TEST(String metric, String packetSize, String tickParameter) {
		JsonObject MonitoringMonitred;
		try {
			MonitoringMonitred = C.getJsonAsObject(C.MONITORING_MONITORED_JSON_FILE);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			return;
		}

		for (Entry<String,JsonElement> monitoringEntry : MonitoringMonitred.entrySet()) {
			
		}
	}
	
	private static void xx() {
		
	}
	
	private static void start(String metric, String packetSize, String tickParameter) {

		JsonObject MonitoringMonitred;
		try {
			MonitoringMonitred = C.getJsonAsObject(C.MONITORING_MONITORED_JSON_FILE);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			return;
		}

		for (Entry<String,JsonElement> monitoringEntry : MonitoringMonitred.entrySet()) {
			String monitoring = monitoringEntry.getKey();
			JsonObject monitoringNodeDetails = C.getNodeDetails().get(monitoring).getAsJsonObject();
			String fromSourceName =  monitoringNodeDetails.get("SourceName").getAsString();
			String fromNickName =  monitoringNodeDetails.get("SourceNickName").getAsString();

			HashMap<String,HashMap<String, String>> map = GetPingTableTSV.getMeasurementMap(fromNickName, metric, packetSize, tickParameter);
			if (map == null) {
				System.out.println("Error on getting the map for " + fromNickName);
				C.log("Error on getting the map..."+fromNickName + " " + metric + " " + packetSize + " " + tickParameter);
				continue;
			}
			GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
			long t1 = System.currentTimeMillis();
			gm.begin();
			for (String monitored : map.keySet()) {
				JsonObject monitoredNodeDetails = null;
				try {
					monitoredNodeDetails = C.getNodeDetails().get(monitored).getAsJsonObject();
				} catch (Exception e) {
					System.out.println(e);
					System.out.println("Tried to get details about " + monitored);
					continue;
				}
				String toSourceName =  monitoredNodeDetails.get("SourceName").getAsString();
				String toNickName =  monitoredNodeDetails.get("SourceNickName").getAsString();


				//Begin Metric
				String metricURI = P.BASE+"MetricFROM-"+fromNickName+"-TO-"+toNickName;
				{
					gm.addTripleResource(metricURI, P.RDF, "type", P.MD, "Metric", P.MEASUREMENTS_CONTEXT, false);

					String sourceNode =  P.BASE+"Node-"+fromSourceName;	
					gm.addTripleResource(metricURI, P.MD, "hasSourceNodeInformation", sourceNode, P.MEASUREMENTS_CONTEXT, false);

					String destinationNode =  P.BASE+"Node-"+toSourceName;	
					gm.addTripleResource(metricURI, P.MD, "hasDestinationNodeInformation", destinationNode, P.MEASUREMENTS_CONTEXT, false);

				}
				//End Metric
				System.out.println("a");
				long tt1 = System.currentTimeMillis();
				MonitoredNodesThreadsStarter.instantiate(map.get(monitored), metric, metricURI, packetSize, tickParameter);
				long tt2 = System.currentTimeMillis();
				System.out.println((tt2-tt1)/1000.0);

			}
			gm.commit();
			long t2 = System.currentTimeMillis();
			C.log("It took " + (t2-t1) + " to instantiate measurements from " + monitoring );
			System.out.println("It took " + (t2-t1) + " to instantiate measurements from " + monitoring );
		}
	}




	





		
	

	/**
	 * The parameters to be included in the search.
	 * @param allyearly
	 * @param allmonthly
	 * @param last365days
	 * @param hourly
	 */
	public static void start(boolean allyearly,boolean allmonthly) {
		ArrayList<String> tickParams = new ArrayList<String>();
		if (allyearly) 	tickParams.add("tick=allyearly");
		if (allmonthly) tickParams.add("tick=allmonthly");
	

		for (String metric : MeasurementUtils.getMapMetric().keySet()) {
			for (String tickParam : tickParams) {
				long t1 = System.currentTimeMillis();
				for (String packetSize : MeasurementUtils.packetSizes) {
					try {
						start(metric, packetSize, tickParam);
					} catch (Exception e) {
						System.out.println(e);
						C.log(CopyOfMeasurementInstantiatorFromCSV.class + " " + e);
						continue;
					}
				}
				long t2 = System.currentTimeMillis();
				System.out.println("It took " + (t2 - t1)/1000.0 + " seconds to instantiate for metric: "+metric + " and tick: "+tickParam);
				C.log("It took " + (t2 - t1)/1000.0 + " seconds to instantiate for metric: "+metric + " and tick: "+tickParam);
			}
		}
	}

	


}
