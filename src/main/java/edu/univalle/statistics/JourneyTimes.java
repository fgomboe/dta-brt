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
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.ScenarioUtils;

import edu.univalle.statistics.EventsToTrips.LegPlusWait;
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
    private MyTripHandler tripHandler;

    // Class LegPlusWait defined inside 'EventsToTrips.java'
    private HashMap<Id<Person>, List<List<LegPlusWait>>> trips = new HashMap<Id<Person>, List<List<LegPlusWait>>>();

    private class MyTripHandler implements EventsToTrips.TripHandler
    {

        @Override
        public void handleTrip(Id<Person> agentId, List<LegPlusWait> trip) {
            if (!trips.containsKey(agentId)) {
                List<List<LegPlusWait>> tripList = new ArrayList<List<LegPlusWait>>();
                tripList.add(trip);
                trips.put(agentId, tripList);
            }
            else {
                trips.get(agentId).add(trip);
            }

        }

    }

    public JourneyTimes() {
        this.startTime = 0;
        this.endTime = 86400 - 1; // Because in constructor of class org.matsim.analysis.VolumesAnalyzer they use it like that;

        this.setConfigFile("input/config_dummy.xml");
        this.setEventsFile(
                "output0_DummyPopulation40%_NormalControler_ChangeExpBeta80_ReRoute10_TimeAllocationMutator10_ParamsAsInBitacora_VehCap40%Time/ITERS/it.0/0.events.xml.gz");
        this.setOutputFile(outputFile = "output/legStats.csv");

    }

    public JourneyTimes(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;

        this.setConfigFile("input/config_dummy.xml");
        this.setEventsFile(
                "output0_DummyPopulation40%_NormalControler_ChangeExpBeta80_ReRoute10_TimeAllocationMutator10_ParamsAsInBitacora_VehCap40%Time/ITERS/it.0/0.events.xml.gz");
        this.setOutputFile(outputFile = "output/legStats.csv");

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
        eventsReader.readFile(eventsFile);
        log.info("Read process finished!");

    }

    public void writeResults() {
        openWriter();
        writeTrips();
        closeWriter();
        log.info("Results written to file: " + outputFile);

    }

    public void writeTrips() {
        for (Id<Person> agentId : trips.keySet()) {
            List<List<LegPlusWait>> tripList = trips.get(agentId);
            Iterator<List<LegPlusWait>> itTrips = tripList.iterator();
            while (itTrips.hasNext()) {
                List<LegPlusWait> trip = itTrips.next();
                String s_tripId = Integer.toString(tripList.indexOf(trip));

                Iterator<LegPlusWait> itLegs = trip.iterator();
                while (itLegs.hasNext()) {
                    LegPlusWait legPlusWait = itLegs.next();
                    // exclude bus drivers
                    if (legPlusWait.leg.getDepartureTime() >= startTime && legPlusWait.leg.getDepartureTime() < endTime
                            && !legPlusWait.leg.getMode().equals("car")) {
                        String s_agentId = agentId.toString();
                        String s_mode = legPlusWait.leg.getMode();
                        String s_departureTime = Double.toString(legPlusWait.leg.getDepartureTime());
                        String s_waitingTime = Double.toString(legPlusWait.waitingTime);
                        String s_travelTime = Double.toString(legPlusWait.leg.getTravelTime());
                        String s_travelDistance = Double.toString(legPlusWait.leg.getRoute().getDistance());
                        String s_origin = "";
                        String s_dest = "";
                        String s_line = "";
                        String s_route = "";
                        if (legPlusWait.leg.getMode().equals("pt")) {
                            s_origin = legPlusWait.leg.getRoute().toString().split(" ")[1].split("=")[1];
                            s_dest = legPlusWait.leg.getRoute().toString().split(" ")[2].split("=")[1];
                            s_line = legPlusWait.leg.getRoute().toString().split(" ")[3].split("=")[1] + "_";
                            s_route = legPlusWait.leg.getRoute().toString().split(" ")[4].split("=")[1];
                        }
                        else if (legPlusWait.leg.getMode().equals("transit_walk")) {
                            s_origin = legPlusWait.leg.getRoute().getStartLinkId().toString();
                            s_dest = legPlusWait.leg.getRoute().getEndLinkId().toString();
                        }

                        try {
                            writer.writeRecord(
                                    new String[] { s_agentId, s_tripId, s_mode, s_departureTime, s_waitingTime,
                                            s_travelTime, s_travelDistance, s_origin, s_dest, s_line + s_route });
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    private void openWriter() {
        try {
            writer = new CsvWriter(outputFile);
            writer.writeRecord(new String[] { "AgentId", "TripId", "Mode", "DepartureTime", "WaitingTime", "TravelTime",
                    "TravelDistance", "Origin", "Destination", "Route_Line" });
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
