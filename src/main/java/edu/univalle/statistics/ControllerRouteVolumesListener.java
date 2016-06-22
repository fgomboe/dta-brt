package edu.univalle.statistics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import edu.univalle.utils.CsvReader;
import edu.univalle.utils.CsvWriter;

public class ControllerRouteVolumesListener implements StartupListener, IterationEndsListener
{
    // TODO Change class, because this is a copy of 'ControllerLinkVolumesListener.java'

    private final static Logger log = Logger.getLogger(JourneyTimes.class);

    private final int startTime;
    private final int endTime;
    private RouteVolumesPax analyzer;
    private CsvWriter writer;
    private CsvReader reader;
    Controler controler;
    private static final String volumeVariables[] = { "id", "entering", "leaving", "passthrough", "totalVolume" };
    private Map<String, List<FacilityGEH>> lineRouteFacilityGEH = new HashMap<>();

    public ControllerRouteVolumesListener(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private class FacilityGEH
    {
        Id<TransitStopFacility> facilityId;
        double entering;
        double leaving;
        double passthrough;
        double totalVolume;

        FacilityGEH(Id<TransitStopFacility> id) {
            this.facilityId = id;
            this.entering = 0.0;
            this.leaving = 0.0;
            this.passthrough = 0.0;
            this.totalVolume = 0.0;
        }
    }

    @Override
    public void notifyStartup(StartupEvent event) {
        controler = event.getControler();
        analyzer = new RouteVolumesPax(startTime, endTime);
        controler.getEvents().addHandler(analyzer);

    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        String[] lineRoutes = analyzer.getLineRoutes();
        for (String lineRoute : lineRoutes) {
            // write results to external csv file
            openWriter("stats/Volumes-" + lineRoute + "_" + (int) Math.ceil((double) startTime / 3600) + "-"
                    + (int) Math.ceil((double) endTime / 3600) + ".csv");
            writeSimVolumes(lineRoute);
            closeWriter();
            log.info("Results written to file: stats/Volumes-" + lineRoute + "_"
                    + (int) Math.ceil((double) startTime / 3600) + "-" + (int) Math.ceil((double) endTime / 3600)
                    + ".csv");

            // compute GEH index for every measurement
            openReader("stats/input/Volumes-" + lineRoute + "_" + (int) Math.ceil((double) startTime / 3600) + "-"
                    + (int) Math.ceil((double) endTime / 3600) + ".csv");
            List<Id<TransitStopFacility>> facilities = analyzer.getFacilities(lineRoute);
            for (Id<TransitStopFacility> facility : facilities) {
                int[] simVolumes = analyzer.getVolumesForRouteAndFacility(lineRoute, facility);
                int[] realVolumes = readRealVolumes();
                mapGEHForLinerouteAndFacility(simVolumes, realVolumes, lineRoute, facility);
            }
        }
        writeGEHs();
    }

    private int checkIdInFacilityVolumeList(Id<TransitStopFacility> id, List<FacilityGEH> list) {
        if (id == null)
            return -1;

        int pos = 0;
        for (FacilityGEH facGEH : list) {
            if (facGEH.facilityId.toString().equals(id.toString()))
                return pos;
            pos++;
        }
        return -1;
    }

    private void openWriter(String outputFile) {
        try {
            writer = new CsvWriter(outputFile);
            writer.writeRecord(volumeVariables);
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

    private void writeSimVolumes(String lineRoute) {
        List<Id<TransitStopFacility>> facilities = analyzer.getFacilities(lineRoute);
        for (Id<TransitStopFacility> facility : facilities) {
            try {
                writer.write(facility.toString());
                int[] volumes = analyzer.getVolumesForRouteAndFacility(lineRoute, facility);
                String[] a = Arrays.toString(volumes).split("[\\[\\]]")[1].split(", ");
                writer.writeRecord(a);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeGEHs() {
        for (Map.Entry<String, List<FacilityGEH>> entry : lineRouteFacilityGEH.entrySet()) {
            openWriter("stats/GEH-" + entry.getKey() + "_" + (int) Math.ceil((double) startTime / 3600) + "-"
                    + (int) Math.ceil((double) endTime / 3600) + ".csv");
            for (FacilityGEH facGEH : entry.getValue()) {
                try {
                    writer.write(facGEH.facilityId.toString());
                    writer.write(String.valueOf(facGEH.entering));
                    writer.write(String.valueOf(facGEH.leaving));
                    writer.write(String.valueOf(facGEH.passthrough));
                    writer.write(String.valueOf(facGEH.totalVolume));
                    writer.endRecord();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            closeWriter();
        }
    }

    private void openReader(String inputFile) {
        try {
            reader = new CsvReader(inputFile);
            reader.readHeaders();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] readRealVolumes() {
        int[] volumes = new int[4];
        try {
            reader.readRecord();
            volumes[0] = Integer.parseInt(reader.get(volumeVariables[1]));
            volumes[1] = Integer.parseInt(reader.get(volumeVariables[2]));
            volumes[2] = Integer.parseInt(reader.get(volumeVariables[3]));
            volumes[3] = Integer.parseInt(reader.get(volumeVariables[4]));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return volumes;
    }

    private void mapGEHForLinerouteAndFacility(int[] simVolumes, int[] realVolumes, String lineRoute,
            Id<TransitStopFacility> facility) {
        if (!lineRouteFacilityGEH.containsKey(lineRoute)) {
            List<FacilityGEH> facilityGEH = new ArrayList<>();
            FacilityGEH facGEH = new FacilityGEH(facility);
            facGEH.entering = calcGEH(simVolumes[0], realVolumes[0]);
            facGEH.leaving = calcGEH(simVolumes[1], realVolumes[1]);
            facGEH.passthrough = calcGEH(simVolumes[2], realVolumes[2]);
            facGEH.totalVolume = calcGEH(simVolumes[3], realVolumes[3]);
            facilityGEH.add(facGEH);
            lineRouteFacilityGEH.put(lineRoute, facilityGEH);
        }
        else if (checkIdInFacilityVolumeList(facility, lineRouteFacilityGEH.get(lineRoute)) == -1) {
            FacilityGEH facGEH = new FacilityGEH(facility);
            facGEH.entering = calcGEH(simVolumes[0], realVolumes[0]);
            facGEH.leaving = calcGEH(simVolumes[1], realVolumes[1]);
            facGEH.passthrough = calcGEH(simVolumes[2], realVolumes[2]);
            facGEH.totalVolume = calcGEH(simVolumes[3], realVolumes[3]);
            lineRouteFacilityGEH.get(lineRoute).add(facGEH);
        }
    }

    private double calcGEH(double sim, double real) {
        if (sim == real)
            return 0.0;

        double sqDiff = Math.pow(sim - real, 2.0);
        double sum = sim + real;
        return Math.sqrt(2 * (sqDiff / sum));
    }
}
