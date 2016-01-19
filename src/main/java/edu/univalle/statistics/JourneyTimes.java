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

import edu.univalle.utils.CsvWriter;

public class JourneyTimes
{

    private final static Logger log = Logger.getLogger(JourneyTimes.class);

    private String configFile;
    private String eventsFile;

    private String outputFile;

    private double startTime;
    private double endTime;

    private CsvWriter writer;
    private Config config;
    private Scenario scenario;
    private MatsimEventsReader eventsReader;
    private EventsManager manager;
    private EventsToTrips handler;
    private MyLegHandler legHandler;

    private class MyLegHandler implements EventsToTrips.LegHandler
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

                // excludes transit walkers and bus drivers
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

        this.configFile = "input/config.xml";
        this.eventsFile = "output0_NormalControler_ChangeExpBeta80_ReRoute10_TimeAllocatorMutator10/ITERS/it.0/0.events.xml.gz";
        this.outputFile = "output/legStats.csv";

    }

    public JourneyTimes(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;

        this.configFile = "input/config.xml";
        this.eventsFile = "output0_NormalControler_ChangeExpBeta80_ReRoute10_TimeAllocatorMutator10/ITERS/it.0/0.events.xml.gz";
        this.outputFile = "output/legStats.csv";

    }

    public void init() {
        config = ConfigUtils.loadConfig(configFile);
        scenario = ScenarioUtils.loadScenario(config);

        legHandler = new MyLegHandler();
        handler = new EventsToTrips(scenario);
        handler.setLegHandler(legHandler);

        manager = EventsUtils.createEventsManager();
        manager.addHandler(handler);

        eventsReader = new MatsimEventsReader(manager);

    }

    public void setConfigFile(String file) {
        configFile = file;
    }

    public void setEventsFile(String file) {
        eventsFile = file;
    }

    public void setOutputFile(String file) {
        outputFile = file;
    }

    public void readFile() {
        openWriter();
        eventsReader.readFile(eventsFile);
        closeWriter();
        log.info("process finished!");

    }

    private void openWriter() {
        try {
            writer = new CsvWriter(outputFile);
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

    private void closeWriter() {
        writer.close();
    }

    public static void main(String[] args) {
        JourneyTimes calc = new JourneyTimes();
        calc.init();
        calc.readFile();

    }
}
