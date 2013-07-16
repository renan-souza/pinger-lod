package edu.stanford.slac.pinger.instantiator.measurement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.pinger.rest.pingtable.GetPingTableCSV;

public class MeasurementInstantiatorFromCSV {

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

			HashMap<String,HashMap<String, String>> map = GetPingTableCSV.getMeasurementMap(fromNickName, metric, packetSize, tickParameter);
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
				instantiate(map.get(monitored), metric, metricURI, packetSize, tickParameter);


			}
			gm.commit();
			long t2 = System.currentTimeMillis();
			C.log("It took " + (t2-t1) + " to instantiate measurements from " + monitoring );
			System.out.println("It took " + (t2-t1) + " to instantiate measurements from " + monitoring );
		}
	}



	private static void instantiate(HashMap<String, String> timeValue,  String metric, String metricURI, String packetSize, String tickParameter) {
		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		//Begin SimpleMeasurement
		String simpleMeasurementURI = P.BASE + "SimpleMeasurement-"+ mapMetric.get(metric);
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
	private static String timeStamp(String time, String tickParameter) {
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
				gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "month", getMonthNumberByMonthInitials(month), P.MEASUREMENTS_CONTEXT, false);
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
			gm.addTripleResource(timeURI, P.TIME, "dayOfWeek", P.TIME, getDayOfWeek(dayOfWeek), P.MEASUREMENTS_CONTEXT, false);
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
			timeURI = P.BASE + "Time"+time+"-"+year+getInitialsByMonthNumber(month)+day;
			gm.addTripleResource(timeURI, P.TIME, "unitType", P.TIME, "unitHour", P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "day", Integer.parseInt(day), P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "month", getMonthNumberByMonthInitials(month), P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "year", Integer.parseInt(year), P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "hour", Integer.parseInt(time), P.MEASUREMENTS_CONTEXT, false);
			Calendar cal = Calendar.getInstance();
			cal.set(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day));
			int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
			gm.addTripleLiteralNonNegativeInteger(timeURI, P.TIME, "dayOfYear", dayOfYear, P.MEASUREMENTS_CONTEXT, false);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			gm.addTripleResource(timeURI, P.TIME, "dayOfWeek", P.TIME, getDayOfWeek(dayOfWeek), P.MEASUREMENTS_CONTEXT, false);

		}
		gm.addTripleResource(timeURI, P.RDF, "type", P.MGC, "TimeStamp", P.MEASUREMENTS_CONTEXT, false);
		return timeURI;
	}

	private static void instatiatePacketSize(String packetSizes[]) {
		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		for (String s : packetSizes) {
			gm.begin();
			String packetSizeURI = P.BASE + "PacketSize"+s;
			gm.addTripleResource(packetSizeURI, P.RDF, "type", P.MD, "PacketSize", P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteral(packetSizeURI, P.MD, "PacketSizeValue", Float.parseFloat(s), P.MEASUREMENTS_CONTEXT, false);
			gm.commit();
		}
	}

	static HashMap<String,String> mapMetric = new HashMap<String, String>();
	private static void instantiateSimpleMeasurement() {
		mapMetric.put("MOS", "MOSMeasurement");
		mapMetric.put("alpha", "DirectivityMeasurement");
		mapMetric.put("average_rtt", "AverageRoundTripDelayMeasurement");
		mapMetric.put("conditional_loss_probability", "ConditionalLossProabilityMeasurement");
		mapMetric.put("duplicate_packets", "DuplicatePacketsMeasurement");
		mapMetric.put("ipdv", "InterPacketDelayVariationMeasurement");
		//mapMetric.put("iqr", "SimpleMeasurementIQRMeasurement");
		//mapMetric.put("maximum_rtt", "MaximunRoundTripDelayMeasurement");
		//mapMetric.put("minimum_packet_loss", "MinimunPacketLossMeasurement");
		mapMetric.put("minimum_rtt", "MinimunRoundTripDelayMeasurement");
		//mapMetric.put("out_of_order_packets", "OutOfOrderPacketsMeasurement");
		mapMetric.put("packet_loss", "PacketLossMeasurement");
		mapMetric.put("throughput", "TCPThroughputMeasurement");
		//mapMetric.put("unpredictability", "PingUnpredictabilityMeasurement");
		mapMetric.put("unreachability", "PingUnreachabilityMeasurement");
		mapMetric.put("zero_packet_loss_frequency", "ZeroPacketLossFrequencyMeasurement");

		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		gm.begin();
		for (String key : mapMetric.keySet()) {
			String simpleMeasurementURI = P.BASE + "SimpleMeasurement-"+mapMetric.get(key);
			gm.addTripleResource(simpleMeasurementURI, P.RDF, "type", P.MD, mapMetric.get(key), P.MEASUREMENTS_CONTEXT, false);
		}
		gm.commit();
	}

	/**
	 * The parameters to be included in the search.
	 * @param allyearly
	 * @param allmonthly
	 * @param last365days
	 * @param hourly
	 */
	public static void start(boolean allyearly,boolean allmonthly,boolean last365days,boolean hourly) {
		String packetSizes[] = {
				"100",
				//"1000"
		};
		ArrayList<String> tickParams = new ArrayList<String>();
		if (allyearly) 	tickParams.add("tick=allyearly");
		if (allmonthly) tickParams.add("tick=allmonthly");
		if (last365days) tickParams.add("tick=last365days");
		if (hourly) includeHourly(tickParams);

		instatiatePacketSize(packetSizes);
		instantiateSimpleMeasurement();

		for (String metric : mapMetric.keySet()) {
			for (String tickParam : tickParams) {
				long t1 = System.currentTimeMillis();
				for (String packetSize : packetSizes) {
					try {
						start(metric, packetSize, tickParam);
					} catch (Exception e) {
						System.out.println(e);
						C.log(MeasurementInstantiatorFromCSV.class + " " + e);
						continue;
					}
				}
				long t2 = System.currentTimeMillis();
				System.out.println("It took " + (t2 - t1)/1000.0 + " seconds to instantiate for metric: "+metric + " and tick: "+tickParam);
				C.log("It took " + (t2 - t1)/1000.0 + " seconds to instantiate for metric: "+metric + " and tick: "+tickParam);
			}
		}
	}

	private static void includeHourly(ArrayList<String> tickParams) {
		ArrayList<String> days = new ArrayList<String>();
		ArrayList<String> months = new ArrayList<String>();
		for (int i = 1; i <= 9; i++) {
			days.add("0"+i);
			months.add("0"+i);
		}
		for (int i = 10; i <= 31; i++) {
			days.add(i+"");
			if (i<=12)months.add(i+"");
		}

		ArrayList<String> years = new ArrayList<String>();
		GregorianCalendar gc = new GregorianCalendar();
		int year = gc.get(Calendar.YEAR) + 1;
		for (int i = 1998; i <= year; i++) {
			years.add(i+"");
		}

		for (String d : days) {
			for (String m : months) {
				for (String y : years) {
					String date = "year="+y+"&month="+m+"&day="+d;
					tickParams.add("tick=hourly&"+date);
				}
			}
		}
	}

	private static String getInitialsByMonthNumber(String monthNumber) {
		if (monthNumber.equals("01")) return "Jan";
		else if (monthNumber.equals("02")) return "Feb";
		else if (monthNumber.equals("03")) return "Mar";
		else if (monthNumber.equals("04")) return "Apr";
		else if (monthNumber.equals("05")) return "May";
		else if (monthNumber.equals("06")) return "Jun";
		else if (monthNumber.equals("07")) return "Jul";
		else if (monthNumber.equals("08")) return "Aug";
		else if (monthNumber.equals("09")) return "Sep";
		else if (monthNumber.equals("10")) return "Oct";
		else if (monthNumber.equals("11")) return "Nov";
		else if (monthNumber.equals("12")) return "Dec";
		else return null;
	}

	private static int getMonthNumberByMonthInitials(String monthInitials) {
		if (monthInitials.equals("Jan")) return 1;
		else if (monthInitials.equals("Feb")) return 2;
		else if (monthInitials.equals("Mar")) return 3;
		else if (monthInitials.equals("Apr")) return 4;
		else if (monthInitials.equals("May")) return 5;
		else if (monthInitials.equals("Jun")) return 6;
		else if (monthInitials.equals("Jul")) return 7;
		else if (monthInitials.equals("Aug")) return 8;
		else if (monthInitials.equals("Sep")) return 9;
		else if (monthInitials.equals("Oct")) return 10;
		else if (monthInitials.equals("Nov")) return 11;
		else if (monthInitials.equals("Dec")) return 12;
		else return -1;
	}
	private static String getDayOfWeek(int day) {
		switch (day) {
		case 1: return "Sunday";
		case 2: return "Monday";
		case 3: return "Tuesday";
		case 4: return "Wednesday";
		case 5: return "Thursday";
		case 6: return "Friday";
		case 7: return "Saturday";
		default: return null;
		}
	}


}
