package edu.univalle.statistics;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.ScenarioUtils;

import edu.univalle.utils.CsvWriter;

public class WriteLinkVolumes
{

    private final static Logger log = Logger.getLogger(JourneyTimes.class);

    private final int startTime;
    private final int endTime;
    private final int timeBinSize;

    private String configFile;
    private String eventsFile;
    private String outputFile;
    private Config config;
    private Scenario scenario;
    private EventsManager manager;
    private LinkVolumesPax analyzer;
    private MatsimEventsReader eventsReader;
    private CsvWriter writer;

    public WriteLinkVolumes() {
        this(0, 24 * 3600 - 1, 3600);
    }

    public WriteLinkVolumes(int startTime, int endTime, int timeBinSize) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeBinSize = timeBinSize;
    }

    public void setConfigFile(String file) {
        this.configFile = file;
    }

    public void setEventsFile(String file) {
        this.eventsFile = file;
    }

    public void setOutputFile(String file) {
        outputFile = file;
    }

    public void init() {
        config = ConfigUtils.loadConfig(configFile);
        scenario = ScenarioUtils.loadScenario(config);

        manager = EventsUtils.createEventsManager();
        analyzer = new LinkVolumesPax(startTime, endTime, timeBinSize, scenario.getNetwork(),
                scenario.getTransitVehicles().getVehicles().size());
        manager.addHandler(analyzer);

        eventsReader = new MatsimEventsReader(manager);

    }

    public void readFile() {
        eventsReader.readFile(eventsFile);
        log.info("Read process finished!");

    }

    public void writeResults() {
        openWriter();
        writeVolumes();
        log.info("No. of passengers that did not board: " + analyzer.getNonBoardingPassengers().size());
        closeWriter();
        log.info("Results written to file: " + outputFile);
    }

    public void writeVolumes() {
        for (Id<Link> linkId : scenario.getNetwork().getLinks().keySet()) {
            int[] volumes = analyzer.getVolumesForLink(linkId);
            if (volumes != null) {
                Link link = scenario.getNetwork().getLinks().get(linkId);
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

    public static void main(String[] args) {
        // construct object this way: (analysis start time, analysis end time - 1, analysis bin time)
        WriteLinkVolumes lvp = new WriteLinkVolumes(61200, 68399, 3600);
        lvp.setConfigFile("input/config_dummy.xml");
        lvp.setEventsFile("temporal_Feli/0.events.xml.gz");
        lvp.setOutputFile("output/linkVolumes_17-19.csv");
        lvp.init();

        // FileAppender appender;
        // try {
        // appender = new FileAppender(new PatternLayout("[%d{MMM dd HH:mm:ss}] %-5p (%F:%L) - %m%n"),
        // "./output/volumes.log", false);
        // BasicConfigurator.configure(appender);
        // }
        // catch (IOException e) {
        // e.printStackTrace();
        // }

        lvp.readFile();
        lvp.writeResults();

    }
}
