package edu.stanford.slac.pinger.instantiator.measurement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.tests.t;

public class MeasurementParametersInstantiator {
	
	public static void start(boolean timestamp) {
		instatiatePacketSize();
		instantiateSimpleMeasurement();
		if (timestamp)
			instantiateTimeStamp();
	}
	private static void instantiateSimpleMeasurement() {
		HashMap<String,String> mapMetric = MeasurementUtils.getMapMetric();
		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		gm.begin();
		for (String key : mapMetric.keySet()) {
			String simpleMeasurementURI = P.BASE + "SimpleMeasurement-"+mapMetric.get(key);
			gm.addTripleResource(simpleMeasurementURI, P.RDF, "type", P.MD, mapMetric.get(key), P.MEASUREMENTS_CONTEXT, false);
		}
		gm.commit();
	}
	private static void instatiatePacketSize() {
		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		for (String s : MeasurementUtils.packetSizes) {
			gm.begin();
			String packetSizeURI = P.BASE + "PacketSize"+s;
			gm.addTripleResource(packetSizeURI, P.RDF, "type", P.MD, "PacketSize", P.MEASUREMENTS_CONTEXT, false);
			gm.addTripleLiteral(packetSizeURI, P.MD, "PacketSizeValue", Float.parseFloat(s), P.MEASUREMENTS_CONTEXT, false);
			gm.commit();
		}
	}	
	
	private static void instantiateTimeStamp() {
		ArrayList<String> years = t.getYears();
		ArrayList<String> allmonthly = t.generateMonthly();
		ArrayList<String> alldaily = t.generateDaily();
		
		GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
		gm.begin();
		System.out.println("Instantiating years...");
		for (String year : years) {
			timeStamp(year, "tick=allyearly");
		}
		gm.close();
		gm.begin();
		System.out.println("Instantiating allmonthly...");
		for (String monthly : allmonthly) {
			timeStamp(monthly, "tick=allmonthly");
		}
		gm.close();
		gm.begin();
		System.out.println("Instantiating alldaily...");
		for (String daily : alldaily) {
			timeStamp(daily, "tick=last365days");
		}
		gm.close();
		
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
