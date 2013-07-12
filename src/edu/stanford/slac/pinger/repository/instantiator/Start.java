package edu.stanford.slac.pinger.repository.instantiator;

import edu.stanford.slac.pinger.general.C;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;
import edu.stanford.slac.pinger.repository.instantiator.measurement.MeasurementInstantiatorFromCSV;
import edu.stanford.slac.pinger.repository.instantiator.nodes.NodesInstantiator;
import edu.stanford.slac.pinger.repository.instantiator.physicallocation.ContinentInstantiator;
import edu.stanford.slac.pinger.repository.instantiator.physicallocation.CountryInstantiator;
import edu.stanford.slac.pinger.repository.instantiator.physicallocation.SchoolInstantiator;
import edu.stanford.slac.pinger.repository.instantiator.physicallocation.TownInstantiator;
import edu.stanford.slac.pinger.rest.CheckEndpoints;
import edu.stanford.slac.pinger.rest.GenerateMonitoringMonitoredJson;
import edu.stanford.slac.pinger.rest.GenerateNodeDetailsJSON;

public class Start {

	public static void main(String[] args) {

		String ags[] = {
				"setupprefixes=0,generate=0",
				"continents=0",
				"countries=0,file=1,setdbpedia=1",
				"towns=0",
				"schools=0",
				"nodes=0",
				"generateMonitoringMonitored=0",
				"generateNodeDetails=1,generateNodesCF=1,all=0",
				"measurements=0,allyearly=1,allmonthly=0,last365days=0,hourly=0"
		};

		start(ags);

	}

	public static void start(String[] args) {
		GeneralModelSingleton gm = null;
		try {
			gm = GeneralModelSingleton.getInstance();

			long t1 = System.currentTimeMillis();
			for (String arg : args) {
				if (arg.contains("setupprefixes=1")) {
					boolean isToGenerate = arg.contains("generate=1");
					SetUpPrefixes.start(isToGenerate);
				}else if (arg.contains("towns=1")) {
					System.out.println("Instantiating Towns...");
					if (CheckEndpoints.DBPediaIsUP() && CheckEndpoints.FactForgeIsUP() && CheckEndpoints.GeoNamesIsUp() && CheckEndpoints.GeoNamesRDFIsUp())
						TownInstantiator.start();
				}else if (arg.contains("schools=1")) {
					System.out.println("Instantiating Schools...");
					if (CheckEndpoints.DBPediaIsUP() && CheckEndpoints.FactForgeIsUP())
						SchoolInstantiator.start();
				} 
				else if (arg.contains("nodes=1")) {
					System.out.println("Instantiating Nodes...");
					NodesInstantiator.start();
				} else if (arg.contains("continents=1")) {
					System.out.println("Instantiating Continents...");
					ContinentInstantiator.start();
				} else if (arg.contains("countries=1")) {
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
				} else if (arg.contains("generateMonitoringMonitored=1")) {
					System.out.println("Generating JSON Monitoring->Monitored Hosts...");
					GenerateMonitoringMonitoredJson.start();
				} else if (arg.contains("generateNodeDetails=1")) {
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
				}else if (arg.contains("measurements=1")) {
					System.out.println("Instantiating Measurements...");
					boolean allyearly=false, allmonthly=false, hourly=false, last365days=false;
					String ags[] = arg.split(",");
					for (String ag : ags) {
						if (ag.contains("allyearly"))
							allyearly = ag.contains("1");
						else if (ag.contains("allmonthly"))
							allmonthly = ag.contains("1");
						else if (ag.contains("last365days"))
							last365days = ag.contains("1");	
						else if (ag.contains("hourly"))
							hourly = ag.contains("1");	
					}
					MeasurementInstantiatorFromCSV.start(allyearly,allmonthly,last365days,hourly);
				}
			}


			long t2 = System.currentTimeMillis();
			System.out.println("It took " + ((t2-t1)/1000.0/60.0) + " minutes.");
			C.log("Done! It took " + ((t2-t1)/1000.0/60.0) + " minutes.");

		} catch (Exception e) {
			System.out.println(e);
		}
		finally {
			gm.close();
		}

	}

}
