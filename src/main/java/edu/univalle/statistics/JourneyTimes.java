package edu.univalle.statistics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import org.matsim.core.population.LegImpl;
import org.matsim.core.scenario.ScenarioUtils;

import edu.univalle.utils.CsvWriter;

public class JourneyTimes
{

    private final static Logger log = Logger.getLogger(JourneyTimes.class);

    private String configFile;
    private String eventsFile;

    private String outputFile;

    @SuppressWarnings("unused")
    private double startTime;
    @SuppressWarnings("unused")
    private double endTime;

    private CsvWriter writer;
    private Config config;
    private Scenario scenario;
    private MatsimEventsReader eventsReader;
    private EventsManager manager;
    private EventsToTrips handler;
    private MyTripHandler tripHandler;

    @SuppressWarnings("unused")
    private HashMap<Integer, ArrayList<Leg>> trips = new HashMap<Integer, ArrayList<Leg>>();

    private class MyTripHandler implements EventsToTrips.TripHandler
    {

        @Override
        public void handleTrip(Id<Person> agentId, List<LegImpl> leg) {
            if (agentId.toString().equals("3")) {
                System.out.println(agentId.toString());
                Iterator<LegImpl> it = leg.iterator();
                while (it.hasNext()) {
                    System.out.println(it.next().toString());
                }
            }

            /*
            // exclude bus drivers
            if (leg.getDepartureTime() >= startTime && leg.getDepartureTime() < endTime
                    && !leg.getMode().equals("car")) {
            
                int i_agentId = Integer.parseInt(agentId.toString());
                String s_agentId = agentId.toString();
                String s_mode = leg.getMode();
                String s_departureTime = Double.toString(leg.getDepartureTime());
                String s_travelTime = Double.toString(leg.getTravelTime());
                String s_travelDistance = Double.toString(leg.getRoute().getDistance());
                String s_origin = "";
                String s_dest = "";
                String s_line = "";
                String s_route = "";
                if (leg.getMode().equals("pt")) {
                    s_origin = leg.getRoute().toString().split(" ")[1].split("=")[1];
                    s_dest = leg.getRoute().toString().split(" ")[2].split("=")[1];
                    s_line = leg.getRoute().toString().split(" ")[3].split("=")[1] + "_";
                    s_route = leg.getRoute().toString().split(" ")[4].split("=")[1];
                }
                else if (leg.getMode().equals("transit_walk")) {
                    s_origin = leg.getRoute().getStartLinkId().toString();
                    s_dest = leg.getRoute().getEndLinkId().toString();
                }
            
                if (!trips.containsKey(i_agentId)) {
                    ArrayList<Leg> legList = new ArrayList<Leg>();
                    legList.add(leg);
                    trips.put(i_agentId, legList);
                }
                else {
                    trips.get(i_agentId).add(leg);
                }
            
                try {
                    writer.writeRecord(new String[] { s_agentId, s_mode, s_departureTime, s_travelTime,
                            s_travelDistance, s_origin, s_dest, s_line + s_route });
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            */

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

        tripHandler = new MyTripHandler();
        handler = new EventsToTrips(scenario);
        handler.setTripHandler(tripHandler);

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
        log.info("Process finished!");

    }

    private void openWriter() {
        try {
            writer = new CsvWriter(outputFile);
            writer.writeRecord(new String[] { "AgentId", "Mode", "DepartureTime", "TravelTime", "TravelDistance",
                    "Origin", "Destination", "Route_Line" });
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
        // calc.exportFile();

    }
}
