package edu.stanford.slac.pinger.instantiator.measurement;

import java.util.HashMap;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;

public class MonitoredNodesThread extends Thread {

	private String monitoring, metric, packetSize, tickParameter;
	private JsonArray monitoredNodes;
	private HashMap<String,HashMap<String, String>> map;
	public MonitoredNodesThread(String monitoring, HashMap<String,HashMap<String, String>> map, JsonArray monitoredNodes, String metric, String packetSize, String tickParameter){
		this.map = map;
		this.monitoredNodes = monitoredNodes;
		this.monitoring = monitoring;
		this.metric = metric;
		this.packetSize = packetSize;
		this.tickParameter = tickParameter;
	}

	public void run() {

		JsonObject monitoringNodeDetails = C.getNodeDetails().get(monitoring).getAsJsonObject();
		String fromSourceName =  monitoringNodeDetails.get("SourceName").getAsString();
		String fromNickName =  monitoringNodeDetails.get("SourceNickName").getAsString();

		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();

		for (JsonElement monitoriedEl : monitoredNodes) {
			String monitored = monitoriedEl.getAsString();
			HashMap<String, String> timeVal = map.get(monitored);
			if (timeVal==null) continue; //Some nodes that are said to be monitored by the monitoring host do not have data on the map gotten from Pintable TSV.

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
			instantiate(timeVal, metric, metricURI, packetSize, tickParameter);
		}
	}

	/**
	 * It was verified that this is the function that takes the longest. For each monitored host, this function currently takes approximately 9 seconds to run (last365days). 
	 * This function should be especially parallelized.
	 * @param timeValue
	 * @param metric
	 * @param metricURI
	 * @param packetSize
	 * @param tickParameter
	 */
	private static void instantiate(HashMap<String, String> timeValue,  String metric, String metricURI, String packetSize, String tickParameter) {
		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		String simpleMeasurementURI = P.BASE + "SimpleMeasurement-"+ MeasurementUtils.getMapMetric().get(metric);
		for (String time : timeValue.keySet()) {
			String timeURI = P.BASE + "Time"+time;
			{
				String statisticalAnalysisURI = P.BASE + "StatisticalAnalysis-"+UUID.randomUUID();
				gm.addTripleResource(statisticalAnalysisURI, P.RDF, "type", P.MD, "StatisticalAnalysis", P.MEASUREMENTS_CONTEXT, false);
				gm.addTripleResource(statisticalAnalysisURI, P.MD, "measurementsAnalyzed", simpleMeasurementURI, P.MEASUREMENTS_CONTEXT, false);
				gm.addTripleResource(statisticalAnalysisURI, P.MD, "measuresMetric", metricURI, P.MEASUREMENTS_CONTEXT, false);
				gm.addTripleResource(statisticalAnalysisURI, P.MD, "timestamp", timeURI, P.MEASUREMENTS_CONTEXT, false);						
				//Linking PacketSize
				String packetSizeURI = P.BASE + "PacketSize"+packetSize;
				gm.addTripleResource(statisticalAnalysisURI, P.MD, "hasMeasurementParameters", packetSizeURI, P.MEASUREMENTS_CONTEXT, false);
				gm.addTripleLiteral(statisticalAnalysisURI, P.MD, "StatisticalAnalysisValue", Float.parseFloat(timeValue.get(time)), P.MEASUREMENTS_CONTEXT, false);

			}
		}

	}


}
