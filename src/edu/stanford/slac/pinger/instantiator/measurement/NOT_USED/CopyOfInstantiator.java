package edu.stanford.slac.pinger.instantiator.measurement.NOT_USED;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.instantiator.measurement.MeasurementUtils;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.pinger.rest.pingtable.GetPingTableTSV;

public class CopyOfInstantiator {
	
	
	public static void start(JsonArray monitoringNodes, String metric, String packetSize, String tickParameter, long tid) {

		for (JsonElement monitoringEl : monitoringNodes) {
			String monitoring = monitoringEl.getAsString();
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
			//gm.begin();
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
				System.out.println("Thread " + tid + " started istantiating from " + monitoring + " to " + monitored + "...");
				//long tt1 = System.currentTimeMillis();
				instantiate(map.get(monitored), metric, metricURI, packetSize, tickParameter);
				System.out.println("..." + tid + " says: done!");
				//long tt2 = System.currentTimeMillis();
				//System.out.println((tt2-tt1)/1000.0);

			}
			//gm.commit();
			long t2 = System.currentTimeMillis();
			C.log("I am thread " + tid + " and I took " + (t2-t1)/1000.0/60.0 + " minutes to instantiate measurements from " + monitoring + " " + metric + " " + packetSize);
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
	public static void instantiate(HashMap<String, String> timeValue,  String metric, String metricURI, String packetSize, String tickParameter) {
		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		//Begin SimpleMeasurement
		String simpleMeasurementURI = P.BASE + "SimpleMeasurement-"+ MeasurementUtils.getMapMetric().get(metric);
		//End SimpleMeasurement
		for (String time : timeValue.keySet()) {
			//Begin TimeStamp
			String timeURI = timeStamp(time, tickParameter);
			//End TimeStamp
			//Begin StatisticalAnalysis
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
			//End StatisticalAnalysis
		}

	}
	public static String timeStamp(String time, String tickParameter) {
		String timeURI = null;
		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		if (tickParameter.equals("tick=allyearly")) {
			timeURI = P.BASE + "Time"+time;
			gm.addTripleResource(timeURI, P.TIME, "unitType", P.TIME, "unitYear", P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "year", Integer.parseInt(time), P.MEASUREMENTS_CONTEXT, false);
		} else if (tickParameter.equals("tick=allmonthly")) {
			try {
				timeURI = P.BASE + "Time"+time;
				gm.addTripleResource(timeURI, P.TIME, "unitType", P.TIME, "unitMonth", P.MEASUREMENTS_CONTEXT, false);
				String month = time.substring(0, 3);
				String year = time.substring(3, 7);
				gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "month", MeasurementUtils.getMonthNumberByMonthInitials(month), P.MEASUREMENTS_CONTEXT, false);
				gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "year", Integer.parseInt(year), P.MEASUREMENTS_CONTEXT, false);
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		} else if (tickParameter.equals("tick=last365days")) {
			timeURI = P.BASE + "Time"+time;
			gm.addTripleResource(timeURI, P.TIME, "unitType", P.TIME, "unitDay", P.MEASUREMENTS_CONTEXT, false);
			SimpleDateFormat df = new SimpleDateFormat("yyMMMdd", Locale.ENGLISH);
			Date date = null;
			try {
				 date = df.parse(time);
			} catch (Exception e) {
				System.out.println(e);
				return null;
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH) + 1;
			int year = cal.get(Calendar.YEAR);
			int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "day", day, P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "month", month, P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "year", year, P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "dayOfYear", dayOfYear, P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleResource(timeURI, P.TIME, "dayOfWeek", P.TIME, MeasurementUtils.getDayOfWeek(dayOfWeek), P.MEASUREMENTS_CONTEXT, false);
		} else { //Hourly
			String params[] = tickParameter.replace("tick=hourly&", "").split("&");
			String day="",month="",year="";
			for (String s : params) {
				if (s.contains("day"))
					day = s.replace("day=", "");
				else if (s.contains("month"))
					month = s.replace("month=", "");
				else if (s.contains("year"))
					year = s.replace("year=", "");
			}
			timeURI = P.BASE + "Time"+time+"-"+year+MeasurementUtils.getInitialsByMonthNumber(month)+day;
			gm.addTripleResource(timeURI, P.TIME, "unitType", P.TIME, "unitHour", P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "day", Integer.parseInt(day), P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "month", MeasurementUtils.getMonthNumberByMonthInitials(month), P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "year", Integer.parseInt(year), P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "hour", Integer.parseInt(time), P.MEASUREMENTS_CONTEXT, false);
			Calendar cal = Calendar.getInstance();
			cal.set(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day));
			int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "dayOfYear", dayOfYear, P.MEASUREMENTS_CONTEXT, false);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			gm.addTripleResource(timeURI, P.TIME, "dayOfWeek", P.TIME, MeasurementUtils.getDayOfWeek(dayOfWeek), P.MEASUREMENTS_CONTEXT, false);

		}
		gm.addTripleResource(timeURI, P.RDF, "type", P.MGC, "TimeStamp", P.MEASUREMENTS_CONTEXT, false);
		return timeURI;
	}
}
