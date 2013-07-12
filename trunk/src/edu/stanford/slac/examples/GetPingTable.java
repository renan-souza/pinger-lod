package edu.stanford.slac.examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class GetPingTable {

	public static final int MAX_ATTEMPT = 3;
	public static final int TIMEOUT = 200;

	public static void main(String[] args) {
		String uri_from = "EDU.SLAC.STANFORD.N3";
		String uri_to = "BR.UFRJ.N1";

		String url =     	
				"http://www-wanmon.slac.stanford.edu/cgi-wrap/pingtable.pl?"
						+"file=average_rtt&"
						+"by=by-node&size=100&tick=allyearly&"
						+"from="+uri_from+"&"
						+"to="+uri_to+"&"
						+"ex=none&only=all&dataset=hep&percentage=any&dnode=on";
		System.out.println(url);
		long startTime = System.currentTimeMillis();
		String htmlContent = readPage(url);
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println(estimatedTime);		
		System.out.println(htmlContent);

		System.out.println("\n\n\n");


		String metrics[] = htmlContent.split(",");
		for (String ln :  metrics) System.out.println(ln);

		System.out.println("\n\n\n");

		/*
		 * This HashMap maps each metric to the pair (time, value). For example,
		 * avg -->  (2003, 0.000) -  (1998, 0.000) -  (2012, 221.782) -  (2011, 227.693) -  (2010, 238.845) 
		
		//metric -> (time, value)
		 */

		HashMap<String, HashMap<String,String>> hmMetrics = new HashMap<String, HashMap<String,String>>();
		String times[] = metrics[0].split("[+]");
		String metricLabels[] = {"tick","min","twentyfifth","avg","median","seventyfifth","ninetieth","ninetyfifth","max","iqr","std dev","#pairs"};

		for (int i = 1; i < metrics.length; i++) {

			HashMap<String,String> timeValue = new HashMap<String, String>();
			String values[] = metrics[i].split("[+]");
			for (int j = 0; j < times.length; j++) {
				timeValue.put(times[j],values[j]);
			}	
			hmMetrics.put(metricLabels[i], timeValue);		

		}

		//printHashMap
		for (String metric : hmMetrics.keySet()) {
			String s = "";
			HashMap<String, String> timeVal = hmMetrics.get(metric);
			for (String time : timeVal.keySet()) {
				s += " (" + time + ", " + timeVal.get(time) + ") - ";
			}
			System.out.println(metric + " --> " + s);
		}







	}

	public static String readPage(String URL) {
		int attempt = 0;
		while (attempt < MAX_ATTEMPT) {
			try {
				BufferedReader in = null;
				URL url = new URL(URL);
				URLConnection con = url.openConnection();
				con.setConnectTimeout(TIMEOUT);
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));

				//in = new BufferedReader(new InputStreamReader(url.openStream()));			
				StringBuffer sb = new StringBuffer();
				int read;
				char[] cbuf = new char[1024];
				while ((read = in.read(cbuf)) != -1)
					sb.append(cbuf, 0, read);
				String s = sb.toString();
				if (in != null) in.close();

				//<!--?+Oct2007+Sep2007...+Nov2005,+0.141+0.000...+737.923+734.756,,-->
				//The format of the line in @contents (contains pingtable.pl web page) that we want is:
				if (s.indexOf("<!--")!=-1)
					return s.split("<!--")[1].split("-->")[0];

			} catch (Exception e) {
				attempt++;
				e.printStackTrace();			
			}
		}
		return null;
	}
}
