package edu.stanford.slac.pinger.repository.instantiator.physicallocation;

import edu.stanford.slac.pinger.bean.ContinentBean;
import edu.stanford.slac.pinger.general.P;
import edu.stanford.slac.pinger.model.GeneralModelSingleton;

public class ContinentInstantiator {

	public static void start() {
		for (ContinentBean cb : ContinentBean.MAP.values()) {
			GeneralModelSingleton gm = GeneralModelSingleton.getInstance();
			gm.begin();
			String continentURI = P.BASE+"Continent"+cb.getGeoNamesId();
			gm.addTripleResource(continentURI, P.RDF, "type", P.MGC, "Continent", P.CONTINENTS_CONTEXT, false);
			gm.addTripleLiteral(continentURI, P.GN_ONT, "name", cb.getGnName(), P.CONTINENTS_CONTEXT, false);
			gm.addTripleResource(continentURI, P.MGC, "DBPediaLink", cb.getDBPediaLink(), P.CONTINENTS_CONTEXT, false);
			gm.addTripleResource(continentURI, P.MGC, "GeonamesLink",cb.getGeonamesLink(), P.CONTINENTS_CONTEXT, false);
			gm.addTripleLiteral(continentURI, P.MGC, "continentCode",cb.getContinentCode(), P.CONTINENTS_CONTEXT, false);
			gm.commit();
		}
	}

}
