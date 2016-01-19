package edu.univalle.population;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import edu.univalle.utils.CsvReader;
import edu.univalle.utils.CsvWriter;

public class ProbDictionary
{
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> tripTable;
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> probTable;

    private final int SECONDS_DAY = 86400;
    private final int SECONDS_HOUR = 3600;
    private final int TRUNK_STATIONS = 40;

    private int numberOfStations;
    private int initialTime, endTime, timeSpan;
    private int timeBins;

    private boolean tripTableDone = false;
    private boolean probTableDone = false;

    public ProbDictionary() {
        this.numberOfStations = TRUNK_STATIONS;
        this.initialTime = 0;
        this.endTime = SECONDS_DAY;
        this.timeSpan = SECONDS_HOUR;
        this.timeBins = (int) Math.ceil((this.endTime - this.initialTime) / (float) this.timeSpan);

        tripTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>(this.timeBins, 1);
    }

    public ProbDictionary(int numberOfStations, int initialTime, int endTime, int timeSpan) {
        this.numberOfStations = numberOfStations;
        this.initialTime = initialTime;
        this.endTime = endTime;
        this.timeSpan = timeSpan;
        this.timeBins = (int) Math.ceil((this.endTime - this.initialTime) / (float) this.timeSpan);

        tripTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>(this.timeBins, 1);
    }

    public void constructTripTable(String fileName) {
        try {
            CsvReader reader = new CsvReader(fileName);
            reader.readHeaders();

            while (reader.readRecord()) {
                int originStation = Integer.parseInt(reader.get("O_ID_ESTACION"));
                int destinationStation = Integer.parseInt(reader.get("D_ID_ESTACION"));
                int tripTime = Integer.parseInt(reader.get("HORA_MATSIM"));
                // Trips done exactly at the end time are not included
                if (tripTime >= initialTime && tripTime < endTime)
                    addTrip(tripTime, originStation, destinationStation);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        tripTableDone = true;
    }

    public void constructProbTable() {
        if (!tripTableDone) {
            System.out.println("Construct the trips table first!");
            return;
        }

        this.probTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>(this.timeBins, 1);

        HashMap<Integer, HashMap<Integer, Double>> aux1;
        HashMap<Integer, Double> aux2;

        for (int bin : tripTable.keySet()) {
            aux1 = new HashMap<Integer, HashMap<Integer, Double>>(numberOfStations, 1);
            for (int orig : tripTable.get(bin).keySet()) {
                aux2 = new HashMap<Integer, Double>(numberOfStations, 1);
                for (int dest : tripTable.get(bin).get(orig).keySet()) {
                    int totalTrips = 0;
                    for (int destination : tripTable.get(bin).get(orig).keySet()) {
                        totalTrips += getNumberOfTrips(bin, orig, destination);
                    }
                    double probability = tripTable.get(bin).get(orig).get(dest) / (double) totalTrips;
                    aux2.put(dest, probability);
                    aux1.put(orig, aux2);
                    probTable.put(bin, aux1);
                }
            }
        }

        probTableDone = true;
    }

    private void addTrip(int time, int orig, int dest) {
        HashMap<Integer, HashMap<Integer, Integer>> aux1 = null;
        HashMap<Integer, Integer> aux2 = null;

        int bin = calcBin(time);
        // System.out.println("for the time: " + time + " - calculated bin: " + bin);

        if (tripTable.containsKey(bin)) {
            if (tripTable.get(bin).containsKey(orig)) {
                if (tripTable.get(bin).get(orig).containsKey(dest)) {
                    // if it contains the mapping <bin, orig, dest>, then add 1 to that value
                    int prevValue = tripTable.get(bin).get(orig).get(dest);
                    tripTable.get(bin).get(orig).put(dest, prevValue + 1);
                }
                else {
                    // if it contains the mapping <bin, orig> but NOT the specified destination
                    // from that origin, then initialize <dest> value to 1
                    tripTable.get(bin).get(orig).put(dest, 1);
                }
            }
            else {
                // if it contains the mapping <bin> but NOT origin NOR destination for that time,
                // then initialize <orig, dest> value to 1
                aux2 = new HashMap<Integer, Integer>(numberOfStations, 1);
                aux2.put(dest, 1);
                tripTable.get(bin).put(orig, aux2);
            }
        }
        else {
            // if it does NOT contain any entry for the bin
            aux2 = new HashMap<Integer, Integer>(numberOfStations, 1);
            aux2.put(dest, 1);
            aux1 = new HashMap<Integer, HashMap<Integer, Integer>>(numberOfStations, 1);
            aux1.put(orig, aux2);
            tripTable.put(bin, aux1);
        }
    }

    public int getNumberOfTrips(int bin, int orig, int dest) {
        if (!tripTableDone) {
            System.out.println("Construct the trips table first!");
            return 0;
        }

        if (tripTable.containsKey(bin)) {
            if (tripTable.get(bin).containsKey(orig)) {
                if (tripTable.get(bin).get(orig).containsKey(dest)) {
                    return tripTable.get(bin).get(orig).get(dest);
                }
            }
        }
        return 0;
    }

    /**
     * Returns a double indicating the probability that a passenger travels from
     * origin station 'orig' to destination station 'dest' at time bin 'bin'
     * 
     * @param bin The time bin for the travel probability
     * @param orig The origin station for which to compute probability
     * @param dest The destination station for which to compute probability
     * @return double value of probability
     */
    public double getProbability(int bin, int orig, int dest) {
        if (!probTableDone) {
            System.out.println("Construct the probabilities table first!");
            return 0.0;
        }

        if (probTable.containsKey(bin)) {
            if (probTable.get(bin).containsKey(orig)) {
                if (probTable.get(bin).get(orig).containsKey(dest)) {
                    return probTable.get(bin).get(orig).get(dest);
                }
            }
        }
        return 0.0;
    }

    /**
     * Returns a HashMap containing the probabilities for every possible destination
     * from the input origin station during the time bin specified
     * 
     * @param bin The time bin for the travel probabilities
     * @param orig The origin station for which to compute probabilities
     * @return HashMap<Integer, Double> of probabilities for origin station at time bin
     */
    public HashMap<Integer, Double> getProbability(int bin, int orig) {
        if (!probTableDone) {
            System.out.println("Construct the probabilities table first!");
            return null;
        }

        HashMap<Integer, Double> probs = new HashMap<Integer, Double>(numberOfStations, 1);
        if (probTable.containsKey(bin)) {
            if (probTable.get(bin).containsKey(orig)) {
                return probTable.get(bin).get(orig);
            }
        }

        return probs;
    }

    public int calcBin(int time) {
        double bin = Math.floor((time - initialTime) / (float) timeSpan);
        return (int) bin;
    }

    public void writeProbabilities(String fileName) {
        try {
            CsvWriter writer = new CsvWriter(fileName);
            writer.writeRecord(new String[] { "START_TIME", "END_TIME", "ORIGIN", "DESTINATION", "PROBABILITY" });

            for (int bin : probTable.keySet()) {
                for (int orig : probTable.get(bin).keySet()) {
                    for (int dest : probTable.get(bin).get(orig).keySet()) {
                        int initTime = bin * this.timeSpan + initialTime;
                        String s_initTime = Integer.toString(initTime);
                        int endingTime = initTime + this.timeSpan;
                        String s_endingTime = Integer.toString(endingTime);
                        String s_originStation = Integer.toString(orig);
                        String s_destStation = Integer.toString(dest);
                        String s_prob = Double.toString(probTable.get(bin).get(orig).get(dest));
                        writer.writeRecord(
                                new String[] { s_initTime, s_endingTime, s_originStation, s_destStation, s_prob });
                    }
                }
            }

            writer.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeProbabilities(String fileName, int bin, int orig) {
        try {
            CsvWriter writer = new CsvWriter(fileName);
            writer.writeRecord(new String[] { "ORIGIN", "DESTINATION", "PROBABILITY" });

            if (probTable.containsKey(bin)) {
                if (probTable.get(bin).containsKey(orig)) {
                    for (int dest : probTable.get(bin).get(orig).keySet()) {
                        String s_originStation = Integer.toString(orig);
                        String s_destStation = Integer.toString(dest);
                        String s_prob = Double.toString(probTable.get(bin).get(orig).get(dest));
                        writer.writeRecord(new String[] { s_originStation, s_destStation, s_prob });
                    }
                }
            }

            writer.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ProbDictionary dict = new ProbDictionary();
        dict.constructTripTable("./temporal_Feli/ready_closed_trips_noUniviaje.csv");
        dict.constructProbTable();
        dict.writeProbabilities("./output/probabilities.csv", 8, 12);

        System.out.println("Finished!");
    }
}
