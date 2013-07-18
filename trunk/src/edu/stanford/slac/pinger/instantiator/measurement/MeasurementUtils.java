package edu.stanford.slac.pinger.instantiator.measurement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public final class MeasurementUtils {
	
	public static final String packetSizes[] = {
			"100",
			//"1000"
	};
	
	private static HashMap<String,String> mapMetric = null;
	public static  HashMap<String,String> getMapMetric() {
		if (mapMetric == null) {
			mapMetric = new HashMap<String, String>();
			mapMetric.put("MOS", "MOSMeasurement");
			mapMetric.put("alpha", "DirectivityMeasurement");
			mapMetric.put("average_rtt", "AverageRoundTripDelayMeasurement");
			mapMetric.put("conditional_loss_probability", "ConditionalLossProabilityMeasurement");
			mapMetric.put("duplicate_packets", "DuplicatePacketsMeasurement");
			mapMetric.put("ipdv", "InterPacketDelayVariationMeasurement");
			//mapMetric.put("iqr", "SimpleMeasurementIQRMeasurement");
			//mapMetric.put("maximum_rtt", "MaximumRoundTripDelayMeasurement");
			//mapMetric.put("minimum_packet_loss", "MinimumPacketLossMeasurement");
			mapMetric.put("minimum_rtt", "MinimumRoundTripDelayMeasurement");
			//mapMetric.put("out_of_order_packets", "OutOfOrderPacketsMeasurement");
			mapMetric.put("packet_loss", "PacketLossMeasurement");
			mapMetric.put("throughput", "TCPThroughputMeasurement");
			//mapMetric.put("unpredictability", "PingUnpredictabilityMeasurement");
			mapMetric.put("unreachability", "PingUnreachabilityMeasurement");
			mapMetric.put("zero_packet_loss_frequency", "ZeroPacketLossFrequencyMeasurement");
		}
		return mapMetric;
	}
	
	public static void includeHourly(ArrayList<String> tickParams) {
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
	public static String getInitialsByMonthNumber(String monthNumber) {
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

	public static int getMonthNumberByMonthInitials(String monthInitials) {
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
	public static String getDayOfWeek(int day) {
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
	
	public static ArrayList<String> generateDaily() {
		ArrayList<String> months = getMonthNames();
		ArrayList<String> years = getYears();
		ArrayList<String> days = getDays();
		ArrayList<String> alldaily = new ArrayList<String>();
		for (String year : years)
			for (String month : months)
				for (String day : days)
					alldaily.add(year.substring(2, 4)+month+day);
		return alldaily;
	}
	public static ArrayList<String> generateMonthly() {
		ArrayList<String> months = getMonthNames();
		ArrayList<String> years = getYears();
		ArrayList<String> allmonthly = new ArrayList<String>();
		for (String year : years) {
			for (String month : months) {
				allmonthly.add(month+year);
			}
		}
		return allmonthly;
	}
	public static ArrayList<String> getDays() {
		ArrayList<String> days = new ArrayList<String>();
		for (int i = 1; i <= 9; i++) {
			days.add("0"+i);
		}
		for (int i = 10; i <= 31; i++) {
			days.add(i+"");
		}
		return days;
	}
	public static ArrayList<String> getMonthNames() {
		ArrayList<String> monthNames = new ArrayList<String>();
		monthNames.add("Jan");
		monthNames.add("Feb");
		monthNames.add("Mar");
		monthNames.add("Apr");
		monthNames.add("May");
		monthNames.add("Jun");
		monthNames.add("Jul");
		monthNames.add("Aug");
		monthNames.add("Sep");
		monthNames.add("Oct");
		monthNames.add("Nov");
		monthNames.add("Dec");
		return monthNames;
	}
	public static ArrayList<String>  getYears() {
		ArrayList<String> years = new ArrayList<String>();
		GregorianCalendar gc = new GregorianCalendar();
		int year = gc.get(Calendar.YEAR) + 1;
		for (int i = 1998; i <= year; i++) {
			years.add(i+"");
		}
		return years;
	}
}
