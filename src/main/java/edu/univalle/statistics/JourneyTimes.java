package edu.univalle.statistics;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.EventsToLegs;

import edu.univalle.utils.CsvWriter;

public class JourneyTimes
{

    private final static Logger log = Logger.getLogger(JourneyTimes.class);

    private String configFile = "input/config.xml";
    private String testFile = "output/legStats.csv";
    private String eventsFile = "output0_NormalControler_ChangeExpBeta80_ReRoute10_TimeAllocatorMutator10/ITERS/it.0/0.events.xml.gz";

    private double startTime;
    private double endTime;

    private CsvWriter writer;
    private Config config;
    private Scenario scenario;
    private MatsimEventsReader eventsReader;
    private final EventsManager manager = EventsUtils.createEventsManager();
    private EventsToLegs handler;
    private MyLegHandler legHandler;

    private class MyLegHandler implements EventsToLegs.LegHandler
    {

        @Override
        public void handleLeg(Id<Person> agentId, Leg leg) {
            try {
                String s_agentId = agentId.toString();
                String s_travelTime = Double.toString(leg.getTravelTime());
                String s_mode = leg.getMode();
                String s_departureTime = Double.toString(leg.getDepartureTime());
                String s_origin = leg.getRoute().toString().split(" ")[1].split("=")[1];
                String s_destination = leg.getRoute().toString().split(" ")[2].split("=")[1];
                String s_routeDescription = leg.getRoute().getRouteDescription();

                if (leg.getDepartureTime() >= startTime && leg.getDepartureTime() <= endTime
                        && !leg.getMode().equals("transit_walk") && !leg.getMode().equals("car"))
                    writer.writeRecord(new String[] { s_agentId, s_travelTime, s_mode, s_departureTime, s_origin,
                            s_destination, s_routeDescription });

            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public JourneyTimes() {
        this.startTime = 0;
        this.endTime = 86400;

    }

    public JourneyTimes(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;

    }

    public void init() {
        this.config = ConfigUtils.loadConfig(configFile);
        this.scenario = ScenarioUtils.loadScenario(config);
        this.handler = new EventsToLegs(scenario);
        this.legHandler = new MyLegHandler();
        handler.setLegHandler(legHandler);
        manager.addHandler(handler);
        this.eventsReader = new MatsimEventsReader(manager);

    }

    public void readFile() {
        this.eventsReader.readFile(eventsFile);
        log.info("process finished!");

    }

    public void openWriter() {
        try {
            writer = new CsvWriter(testFile);
            writer.writeRecord(new String[] { "AgentId", "TravelTime", "Mode", "DepartureTime", "Origin", "Destination",
                    "RouteDescription" });
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWriter() {
        writer.close();
    }

    public static void main(String[] args) {
        JourneyTimes calc = new JourneyTimes(21600, 28800);
        calc.init();
        calc.openWriter();
        calc.readFile();
        calc.closeWriter();

    }
}
