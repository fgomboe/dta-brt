package edu.univalle.statistics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import edu.univalle.utils.CsvWriter;

public class ControllerRouteVolumesListener implements StartupListener, IterationEndsListener
{
    // TODO Change class, because this is a copy of 'ControllerLinkVolumesListener.java'

    private final static Logger log = Logger.getLogger(JourneyTimes.class);

    private final int startTime;
    private final int endTime;
    private RouteVolumesPax analyzer;
    private CsvWriter writer;
    Controler controler;
    private static final String volumeVariables[] = { "id", "entering", "leaving", "passthrough", "totalVolume" };

    public ControllerRouteVolumesListener(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
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
            writeVolumes(lineRoute);
            closeWriter();
            log.info("Results written to file: stats/Volumes-" + lineRoute + "_"
                    + (int) Math.ceil((double) startTime / 3600) + "-" + (int) Math.ceil((double) endTime / 3600)
                    + ".csv");
        }
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

    private void writeVolumes(String lineRoute) {
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
}
