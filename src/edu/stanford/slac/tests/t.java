package edu.stanford.slac.tests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.stanford.slac.pinger.general.C;

public class t {

	public static void main(String[] args) throws Exception {
		s();
	}
	
	private static void s() {
		ArrayList<String> years = t.getYears();
		ArrayList<String> allmonthly = t.generateMonthly();
		ArrayList<String> alldaily = t.generateDaily();
		int c = 0;
		for (String year : years) {
			System.out.println(year);
			c++;
			//timeStamp(year, "tick=allyearly");
		}
		for (String monthly : allmonthly) {
			System.out.println(monthly);
			c++;
			//timeStamp(monthly, "tick=allmonthly");
		}
		for (String daily : alldaily) {
			System.out.println(daily);
			c++;
			//timeStamp(daily, "tick=last365days");
		}
		System.out.println(c);
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
		for (int i = 2005; i <= year; i++) {
			years.add(i+"");
		}
		return years;
	}
	

	public static void main2(String[] args) throws Exception {
		int nThreads = 1000;
		Thread[] threads = new Thread[nThreads];
		int i = 0;
		for (; i < nThreads; ) {
			threads[i++] = new t2("oi, td bem?");
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


class t2 extends Thread {
	String msg;
	public t2(String msg) {
		this.msg = msg;
	}
	public void run(){
		System.out.println("I am thread " + this.getId() + " and I have this message: " + msg);
	}	
}
