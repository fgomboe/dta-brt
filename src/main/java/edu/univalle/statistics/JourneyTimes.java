package edu.univalle.statistics;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.EventsToLegs;

public class JourneyTimes implements BasicEventHandler{
	
	private String eventsFiles = "output20_BestScore70_Reroute30/ITERS/it.20";
	private Config config;
	private Scenario scenario;
	private EventsReaderXMLv1 eventsReader;
    private final EventsManager e = EventsUtils.createEventsManager();

	public static void main(String[] args) {

	}
	
	public void init() {
        this.config = ConfigUtils.loadConfig("input/config.xml");
        this.scenario = ScenarioUtils.createScenario(config);
        this.manager = new EventsToLegs(scenario);
        //TODO manage how to work with the event handler "e" 
        //hint: see file "EventsFileComparator.java" in package "org.matsim.utils.eventsfilecomparison"
        //      and file "Worker.java" in package "org.matsim.utils.eventsfilecomparison" 
        e.addHandler(this);
        this.eventsReader = new EventsReaderXMLv1(e);
        
	}
	
	public void calculate() {
	    //TODO use manager object to handle events file and compute statistics on legs
	}

}
