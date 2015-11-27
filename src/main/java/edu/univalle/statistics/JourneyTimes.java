package edu.univalle.statistics;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.EventsToLegs;

public class JourneyTimes {
	
	private String eventsFiles = "output20_BestScore70_Reroute30/ITERS/it.20";
	private Config config;
	private Scenario scenario;
	private EventsReaderXMLv1 eventsReader;
	private EventsToLegs manager;

	public static void main(String[] args) {

	}
	
	public void init() {
        this.config = ConfigUtils.loadConfig("input/config.xml");
        this.scenario = ScenarioUtils.createScenario(config);
        this.manager = new EventsToLegs(scenario);
        this.eventsReader = new EventsReaderXMLv1();
        
	}
	
	public void calculate() {
	    //TODO use manager object to handle events file and compute statistics on legs
	}

}
