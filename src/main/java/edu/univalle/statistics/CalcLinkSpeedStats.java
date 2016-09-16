package edu.univalle.statistics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

import edu.univalle.network.NetworkCreatorMIO;
import edu.univalle.utils.CsvReader;

public class CalcLinkSpeedStats
{

    private final static Logger log = Logger.getLogger(NetworkCreatorMIO.class);

    private class AvgStdDev

    {
        double average;
        double stdDev;
        double sum;
        double sqrSum;
        int numberOfValues;

        AvgStdDev(double avg, double dev) {
            average = avg;
            stdDev = dev;
            numberOfValues = 1;
            sum = avg;
            sqrSum = Math.pow(avg, 2);
        }
    }

    public ObjectAttributes nodeAttributes;
    Map<String, String> long2ShortNames;
    Map<String, Map<Integer, AvgStdDev>> name2AvgStdDev;

    public static void main(String[] args) {
        CalcLinkSpeedStats calculator = new CalcLinkSpeedStats();
        calculator.readInput("input/specialNodeAttributes.xml", "input/networkMIO/stops.csv",
                "input/linkSpeeds/semana_abril.csv");
        System.out.println("5\t" + calculator.getAverage("39-2", 5) + "\t" + calculator.getStdDev("39-2", 5));
        System.out.println("6\t" + calculator.getAverage("39-2", 6) + "\t" + calculator.getStdDev("39-2", 6));
        System.out.println("7\t" + calculator.getAverage("39-2", 7) + "\t" + calculator.getStdDev("39-2", 7));
        System.out.println("8\t" + calculator.getAverage("39-2", 8) + "\t" + calculator.getStdDev("39-2", 8));
        System.out.println("9\t" + calculator.getAverage("39-2", 9) + "\t" + calculator.getStdDev("39-2", 9));
        System.out.println("10\t" + calculator.getAverage("39-2", 10) + "\t" + calculator.getStdDev("39-2", 10));
        System.out.println("11\t" + calculator.getAverage("39-2", 11) + "\t" + calculator.getStdDev("39-2", 11));
        System.out.println("12\t" + calculator.getAverage("39-2", 12) + "\t" + calculator.getStdDev("39-2", 12));
        System.out.println("13\t" + calculator.getAverage("39-2", 13) + "\t" + calculator.getStdDev("39-2", 13));
        System.out.println("14\t" + calculator.getAverage("39-2", 14) + "\t" + calculator.getStdDev("39-2", 14));
        System.out.println("15\t" + calculator.getAverage("39-2", 15) + "\t" + calculator.getStdDev("39-2", 15));
        System.out.println("16\t" + calculator.getAverage("39-2", 16) + "\t" + calculator.getStdDev("39-2", 16));
        System.out.println("17\t" + calculator.getAverage("39-2", 17) + "\t" + calculator.getStdDev("39-2", 17));
        System.out.println("18\t" + calculator.getAverage("39-2", 18) + "\t" + calculator.getStdDev("39-2", 18));
        System.out.println("19\t" + calculator.getAverage("39-2", 19) + "\t" + calculator.getStdDev("39-2", 19));
        System.out.println("20\t" + calculator.getAverage("39-2", 20) + "\t" + calculator.getStdDev("39-2", 20));
        System.out.println("21\t" + calculator.getAverage("39-2", 21) + "\t" + calculator.getStdDev("39-2", 21));
        System.out.println("22\t" + calculator.getAverage("39-2", 22) + "\t" + calculator.getStdDev("39-2", 22));
        System.out.println("23\t" + calculator.getAverage("39-2", 23) + "\t" + calculator.getStdDev("39-2", 23));

    }

    public double getAverage(String link, double time) {
        return getAverage(link, (int) Math.floor(time / 3600));
    }

    public double getAverage(String link, int time) {
        // There is almost no data for time > 22, although there is indeed some data
        // MAX_VALUE of Integer because speed calculator takes minimum speed between this, vehicle max speed
        // and link free speed
        if (time > 22)
            return name2AvgStdDev.get(link).containsKey(time) ? name2AvgStdDev.get(link).get(time).average
                    : Integer.MAX_VALUE;
        else
            return name2AvgStdDev.get(link).get(time).average;
    }

    public double getStdDev(String link, double time) {
        return getStdDev(link, (int) Math.floor(time / 3600));
    }

    public double getStdDev(String link, int time) {
        // There is almost no data for time > 22, although there is indeed some data
        // 0.0 because if there is no data for time > 22 the value for speed must be exactly the average (Integer.MAX_VALUE)
        if (time > 22)
            return name2AvgStdDev.get(link).containsKey(time) ? name2AvgStdDev.get(link).get(time).stdDev : 0.0;
        else
            return name2AvgStdDev.get(link).get(time).stdDev;
    }

    public void readInput(String s_nodeAttributes, String s_stops, String s_speeds) {
        nodeAttributes = new ObjectAttributes();
        new ObjectAttributesXmlReader(nodeAttributes).parse(s_nodeAttributes);
        readStops(s_stops);
        readSpeeds(s_speeds);
    }

    private void readStops(String stops) {
        log.info("Starting to read stops");

        try {
            long2ShortNames = new HashMap<String, String>();
            CsvReader reader = new CsvReader(stops);
            reader.readHeaders();

            while (reader.readRecord()) {
                String longName = reader.get("LONGNAME");
                String shortName = reader.get("SHORTNAME");
                long2ShortNames.put(longName, shortName);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Finished reading stops");
        log.info("Size of map long2ShortNames is " + long2ShortNames.size());
    }

    private void readSpeeds(String speeds) {
        log.info("Starting to read speeds");

        try {
            name2AvgStdDev = new HashMap<String, Map<Integer, AvgStdDev>>();
            CsvReader reader = new CsvReader(speeds);
            reader.readHeaders();

            while (reader.readRecord()) {
                String fromNode = long2ShortNames.get(reader.get("Nombre parada De"));
                String toNode = long2ShortNames.get(reader.get("Nombre parada Hacia"));

                if (nodeAttributes.getAttribute(fromNode, "ASSOCIATE_ID") != null)
                    fromNode = nodeAttributes.getAttribute(fromNode, "ASSOCIATE_ID").toString();
                if (nodeAttributes.getAttribute(toNode, "ASSOCIATE_ID") != null)
                    toNode = nodeAttributes.getAttribute(toNode, "ASSOCIATE_ID").toString();

                String linkName = fromNode + "-" + toNode;

                if (!name2AvgStdDev.containsKey(linkName)) {
                    double speed = Double.parseDouble(reader.get("Velocidad")) * 1000 / 3600; // To store speeds in m/s
                    String longTime = reader.get("HORA");
                    int time = Integer.parseInt(longTime.substring(0, longTime.length() - 3));

                    Map<Integer, AvgStdDev> map = new TreeMap<Integer, AvgStdDev>();
                    map.put(time, new AvgStdDev(speed, 0.0));
                    name2AvgStdDev.put(linkName, map);
                }
                else {
                    Map<Integer, AvgStdDev> data = name2AvgStdDev.get(linkName);
                    double speed = Double.parseDouble(reader.get("Velocidad")) * 1000 / 3600; // To store speeds in m/s
                    String longTime = reader.get("HORA");
                    int time = Integer.parseInt(longTime.substring(0, longTime.length() - 3));

                    if (data.containsKey(time)) {
                        AvgStdDev datum = data.get(time);
                        datum.sum = datum.sum + speed;
                        datum.sqrSum = datum.sqrSum + Math.pow(speed, 2);
                        datum.numberOfValues++;

                        datum.average = datum.sum / datum.numberOfValues;
                        double variance = (datum.numberOfValues * Math.pow(datum.average, 2)
                                - (2 * datum.average * datum.sum) + datum.sqrSum) / datum.numberOfValues;
                        datum.stdDev = Math.sqrt(variance);
                    }
                    else {
                        data.put(time, new AvgStdDev(speed, 0.0));
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Finished reading speeds");
        log.info("Size of map name2AvgStdDev is " + name2AvgStdDev.size());
    }

}
