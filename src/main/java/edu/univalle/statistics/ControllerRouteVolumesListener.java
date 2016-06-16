package edu.univalle.statistics;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.StartupListener;

import edu.univalle.utils.CsvWriter;

public class ControllerRouteVolumesListener implements StartupListener, IterationEndsListener
{
    // TODO Change class, because this is a copy of 'ControllerLinkVolumesListener.java'

    private final static Logger log = Logger.getLogger(JourneyTimes.class);

    private final int startTime;
    private final int endTime;
    private final int timeBinSize;
    private RouteVolumesPax analyzer;
    private CsvWriter writer;
    Controler controler;
    private String outputFile = "output/linkVolumes_17-19.csv";

    public ControllerRouteVolumesListener(int startTime, int endTime, int timeBinSize) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeBinSize = timeBinSize;
    }

    @Override
    public void notifyStartup(StartupEvent event) {
        controler = event.getControler();
        analyzer = new RouteVolumesPax(startTime, endTime);
        controler.getEvents().addHandler(analyzer);

    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        openWriter();
        writeVolumes();
        closeWriter();
        log.info("Results written to file: " + outputFile);

    }

    private void openWriter() {
        try {
            writer = new CsvWriter(outputFile);
            writer.writeRecord(new String[] { "LinkId", "FromNode", "ToNode", "Length", "Vol" });
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

    public void writeVolumes() {
        for (Id<Link> linkId : controler.getScenario().getNetwork().getLinks().keySet()) {
            int[] volumes = analyzer.getVolumesForLink(linkId);
            if (volumes != null) {
                Link link = controler.getScenario().getNetwork().getLinks().get(linkId);
                try {
                    writer.write(linkId.toString());
                    writer.write(link.getFromNode().getId().toString());
                    writer.write(link.getToNode().getId().toString());
                    writer.write(Double.toString(link.getLength()));
                    int volume = 0;
                    for (int slot = 0; slot < volumes.length; ++slot) {
                        volume += volumes[slot];
                    }
                    writer.write(Integer.toString(volume));
                    writer.endRecord();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            writer.writeRecord(new String[] {});
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

}
