package edu.stanford.slac.pinger;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.instantiator.SetUpPrefixes;
import edu.stanford.slac.pinger.instantiator.measurement.MeasurementParametersInstantiator;
import edu.stanford.slac.pinger.instantiator.measurement.MonitoringNodesThreadsStarter;
import edu.stanford.slac.pinger.instantiator.nodes.NodesInstantiator;
import edu.stanford.slac.pinger.instantiator.physicallocation.ContinentInstantiator;
import edu.stanford.slac.pinger.instantiator.physicallocation.CountryInstantiator;
import edu.stanford.slac.pinger.instantiator.physicallocation.SchoolInstantiator;
import edu.stanford.slac.pinger.instantiator.physicallocation.TownInstantiator;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.pinger.rest.CheckEndpoints;
import edu.stanford.slac.pinger.rest.GenerateMonitoringJSON;
import edu.stanford.slac.pinger.rest.GenerateNodeDetailsJSON;

public class Start {

	public static void main(String[] args) {

		String ags[] = {
				"setupprefixes=0,generate=1",
				"continents=0",
				"countries=0,file=1,setdbpedia=1",
				"towns=0",
				"schools=0",
				"nodes=0",
				"generateMonitoring=0,generateGrouped=0,generateMonitoringMonitored=0,monitoringMonitoredGrouped=1", //generateMonitoringMonitored is never used.
				"generateNodeDetails=0,generateNodesCF=1,all=0",
				"measurementParameters=0,timestamp=0",
				"allyearly=0 metric=throughput",
				"allmonthly=0  metric=throughput",
				"last365days=1 metric=throughput"
		};
		start(ags);
	}

	public static void start(String[] args) {
		C.getNodeDetails();
		C.getMonitoringMonitoredGroupedJSON();		//REDUNDANT CODE....
		GeneralModelSingleton gm = null;
		try {
			gm = GeneralModelSingleton.getInstance();

			long t1 = System.currentTimeMillis();
			for (String arg : args) {
				if (arg.contains("setupprefixes=1")) {
					setupprefixes(arg);
				} else if (arg.contains("towns=1")) {
					towns(arg);
				} else if (arg.contains("schools=1")) {
					schools(arg);
				} else if (arg.contains("nodes=1")) {
					nodes(arg);
				} else if (arg.contains("continents=1")) {
					continents(arg);
				} else if (arg.contains("countries=1")) {
					countries(arg);
				} else if (arg.contains("generateMonitoring=1")) {
					generateMonitoring(arg);
				} else if (arg.contains("generateNodeDetails=1")) {
					generateNodeDetails(arg);
				} else if (arg.contains("measurementParameters=1")) {
					measurementParameters(arg);
				} else if (arg.contains("allyearly=1")) {
					allYearly(arg);
				} else if (arg.contains("allmonthly=1")) {
					allMonthly(arg);
				} else if (arg.contains("last365days=1")) {
					last365Days(arg);
				}
			}
			long t2 = System.currentTimeMillis();
			C.log("Done! It took " + ((t2-t1)/1000.0/60.0) + " minutes.");

		} catch (Exception e) {
			System.out.println(e);
		}
		finally {
			gm.close();
		}
	}

	public static void setupprefixes(String arg) {
		boolean isToGenerate = arg.contains("generate=1");
		SetUpPrefixes.start(isToGenerate);
	}
	public static void towns(String arg) {
		System.out.println("Instantiating Towns...");
		if (CheckEndpoints.DBPediaIsUP() && CheckEndpoints.FactForgeIsUP() && CheckEndpoints.GeoNamesIsUp() && CheckEndpoints.GeoNamesRDFIsUp())
			TownInstantiator.start();
	}	
	public static void schools(String arg) {
		System.out.println("Instantiating Schools...");
		if (CheckEndpoints.DBPediaIsUP() && CheckEndpoints.FactForgeIsUP())
			SchoolInstantiator.start();
	}
	public static void nodes(String arg) {
		System.out.println("Instantiating Nodes...");
		NodesInstantiator.start();
	}
	public static void continents(String arg) {
		System.out.println("Instantiating Continents...");
		ContinentInstantiator.start();
	}
	public static void countries(String arg) {
		System.out.println("Instantiating Countries...");
		boolean file=true,setdbpedia=false;
		String ags[] = arg.split(",");
		for (String ag : ags) {
			if (ag.contains("file"))
				file = ag.contains("1");
			else if (ag.contains("setdbpedia")) 
				setdbpedia = ag.contains("1");
		}
		if (setdbpedia) {
			if (CheckEndpoints.GeoNamesRDFIsUp()) 
				CountryInstantiator.start(file,setdbpedia);
		}
		else
			CountryInstantiator.start(file,setdbpedia);
	}
	public static void generateMonitoring(String arg) {
		System.out.println("Generating JSON Monitoring Hosts...");
		boolean grouped = arg.contains("generateGrouped=1");
		boolean monitoringMonitored = arg.contains("generateMonitoringMonitored=1");
		boolean monitoringMonitoredGrouped = arg.contains("monitoringMonitoredGrouped=1");
		GenerateMonitoringJSON.start(monitoringMonitored, grouped, monitoringMonitoredGrouped);
	}
	public static void generateNodeDetails(String arg) {
		boolean generateNodesFile=false, all=false;
		String ags[] = arg.split(",");
		for (String ag : ags) {
			if (ag.contains("generateNodesCF"))
				generateNodesFile = ag.contains("1");
			else if (ag.contains("all"))
				all = ag.contains("1");
		}
		System.out.println("Generating NodeDetails JSON. 'Getting Nodes.cf from web'="+generateNodesFile + ", 'all nodes'="+all);
		GenerateNodeDetailsJSON.start(generateNodesFile, all);
	}
	public static void measurementParameters(String arg) {
		System.out.println("Instantiating Measurement Parameters...");
		boolean timestamp =  arg.contains("timestamp=0");
		MeasurementParametersInstantiator.start(timestamp);
	}
	public static void allYearly(String arg) {
		String ags[] = arg.split("\\s");
		String metrics[] = ags[1].replace("metric=", "").split(",");
		for (String metric : metrics) {
			System.out.println("Generating Yearly Measurements for Metric " + metric);
			MonitoringNodesThreadsStarter.yearlyMonthlyThreadStarter(true,false,metric);
		}
	}
	public static void allMonthly(String arg) {
		String ags[] = arg.split("\\s");
		String metrics[] = ags[1].replace("metric=", "").split(",");
		for (String metric : metrics) {
			System.out.println("Generating Monthly Measurements for Metric " + metric);
			MonitoringNodesThreadsStarter.yearlyMonthlyThreadStarter(false,true,metric);
		}
	}
	public static void last365Days(String arg) {
		String ags[] = arg.split("\\s");
		String metrics[] = ags[1].replace("metric=", "").split(",");
		for (String metric : metrics) {
			System.out.println("Generating Last365 Days Measurements for Metric " + metric);
			MonitoringNodesThreadsStarter.last365DaysThreadStarter(metric);
		}
	}

}
