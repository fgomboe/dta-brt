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
    private HashMap<Integer, HashMap<Integer, Double>> probTableDay;
    private HashMap<Integer, String> stationIds;

    private final int SECONDS_DAY = 86400;
    private final int SECONDS_HOUR = 3600;
    private final int TRUNK_STATIONS = 40;

    private int numberOfStations;
    private int initialTime, endTime, timeSpan;
    private int timeBins;

    private boolean tripTableDone = false;
    private boolean probTableDone = false;

    public ProbDictionary(String std_code) {
        this.numberOfStations = readStations(std_code);
        this.initialTime = 0;
        this.endTime = SECONDS_DAY;
        this.timeSpan = SECONDS_HOUR;
        this.timeBins = (int) Math.ceil((this.endTime - this.initialTime) / (float) this.timeSpan);

        tripTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>(this.timeBins, 1);
    }

    public ProbDictionary(String std_code, int initialTime, int endTime, int timeSpan) {
        this.numberOfStations = readStations(std_code);
        this.initialTime = initialTime;
        this.endTime = endTime;
        this.timeSpan = timeSpan;
        this.timeBins = (int) Math.ceil((this.endTime - this.initialTime) / (float) this.timeSpan);

        tripTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>(this.timeBins, 1);
    }

    private int readStations(String std_code) {
        try {
            stationIds = new HashMap<Integer, String>();
            CsvReader reader = new CsvReader(std_code);
            reader.readHeaders();
            while (reader.readRecord()) {
                if (reader.get("TRONCAL").equals("YES"))
                    stationIds.put(Integer.parseInt(reader.get("UV_CODES")), reader.get("STA_NAME"));
            }

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return stationIds.size();
    }

    /**
     * Must be called before constructProbTable() method. Once run, it is possible to
     * get the number of trips for a time bin, origin station and destination station
     * using method getNumberOfTrips(int bin, int orig, int dest); or for an origin
     * station and destination station for the entire analysis time period using method
     * getNumberOfTrips(int orig, int dest)
     * 
     * This method requires a csv file that MUST contain the following headers:
     * - "O_ID_ESTACION" Integer identifier of origin station
     * - "D_ID_ESTACION" Integer identifier of destination station
     * - "HORA_MATSIM" Integer number of seconds for the departure time
     * 
     * @param fileName csv file containing a list of public transport trips
     */
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

    /**
     * Method constructTripTable() must be called first.
     * 
     * Once run, it is possible to get the travel probability for a time bin, origin
     * station and destination station using method getProbability(int bin, int orig, int dest);
     * or for a time bin and origin station using method getProbability(int bin, int orig)
     */
    public void constructProbTable() {
        if (!tripTableDone) {
            System.out.println("Construct the trips table first!");
            return;
        }

        this.probTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>(this.timeBins, 1);
        this.probTableDay = new HashMap<Integer, HashMap<Integer, Double>>(this.numberOfStations, 1);

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

                    double probability = getNumberOfTrips(bin, orig, dest) / (double) totalTrips;
                    aux2.put(dest, probability);
                }
                aux1.put(orig, aux2);
            }
            probTable.put(bin, aux1);
        }

        for (int orig : stationIds.keySet()) {
            if (!probTableDay.containsKey(orig))
                probTableDay.put(orig, new HashMap<Integer, Double>(this.numberOfStations, 1));
            for (int dest : stationIds.keySet()) {
                if (!probTableDay.get(orig).containsKey(dest)) {
                    int totalTripsDay = 0;
                    for (int destination : stationIds.keySet()) {
                        totalTripsDay += getNumberOfTrips(orig, destination);
                    }
                    probTableDay.get(orig).put(dest, getNumberOfTrips(orig, dest) / (double) totalTripsDay);
                }
            }
        }

        probTableDone = true;
    }

    private void addTrip(int time, int orig, int dest) {
        HashMap<Integer, HashMap<Integer, Integer>> aux1 = null;
        HashMap<Integer, Integer> aux2 = null;

        int bin = calcBin(time);

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

    /**
     * Returns an integer indicating how many trips there is from origin station
     * 'orig' to destination station 'dest' at time bin 'bin'
     * 
     * @param bin The time bin for the number of trips
     * @param orig The origin station for which to get trips
     * @param dest The destination station for which to get trips
     * @return integer indicating number of trips for the time bin
     */
    public int getNumberOfTrips(int bin, int orig, int dest) {
        if (!tripTableDone) {
            System.out.println("Construct the trips table first!");
            return 0;
        }

        int numberOfTrips = 0;
        if (tripTable.containsKey(bin)) {
            if (tripTable.get(bin).containsKey(orig)) {
                if (tripTable.get(bin).get(orig).containsKey(dest)) {
                    numberOfTrips = tripTable.get(bin).get(orig).get(dest);
                }
            }
        }
        return numberOfTrips;
    }

    /**
     * Returns an integer indicating how many trips there are from origin station
     * 'orig' to destination station 'dest' during the entire analysis time period
     * 
     * @param orig The origin station for which to get trips
     * @param dest The destination station for which to get trips
     * @return integer indicating number of trips for whole analysis period
     */
    public int getNumberOfTrips(int orig, int dest) {
        if (!tripTableDone) {
            System.out.println("Construct the trips table first!");
            return 0;
        }

        int numberOfTrips = 0;
        for (int bin : tripTable.keySet()) {
            if (tripTable.get(bin).containsKey(orig))
                if (tripTable.get(bin).get(orig).containsKey(dest))
                    numberOfTrips += getNumberOfTrips(bin, orig, dest);
        }

        return numberOfTrips;
    }

    /**
     * Returns a double indicating the probability that a passenger travels from
     * origin station 'orig' to destination station 'dest' at time bin 'bin'
     * 
     * @param bin The time bin for the travel probability
     * @param orig The origin station for which to get probability
     * @param dest The destination station for which to get probability
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
     * from the input origin station during the time bin specified.
     * If, for the specified time bin, there are no trips from the specified origin
     * station, the method returns probabilities for the entire time period.
     * If, for the entire time period, there are no trips from the specified origin
     * station, the method returns null.
     * 
     * @param bin The time bin for the travel probabilities
     * @param orig The origin station for which to get probabilities
     * @return HashMap<Integer, Double> of probabilities for origin station at time bin
     */
    public HashMap<Integer, Double> getProbability(int bin, int orig) {
        if (!probTableDone) {
            System.out.println("Construct the probabilities table first!");
            return null;
        }

        HashMap<Integer, Double> probs = null;
        if (probTable.containsKey(bin)) {
            if (probTable.get(bin).containsKey(orig)) {
                probs = probTable.get(bin).get(orig);
            }
            else {
                // if for the specified time bin there is no trip from the specified origin
                // station, take probability for all day
                probs = probTableDay.get(orig);
            }
        }

        return probs;
    }

    /**
     * Computes the time bin which contains the specified time, taking into account
     * the set initial time (default 0 sec), end time (default 86400 sec) and the
     * time span to divide the analyzed time period (default 3600 sec)
     * 
     * @param time The time for which to compute time bin
     * @return integer indicating the time bin the specified time belongs to
     */
    public int calcBin(int time) {
        double bin = Math.floor((time - initialTime) / (float) timeSpan);
        return (int) bin;
    }

    /**
     * Writes a csv file in the directory and filename specified in the argument
     * containing the travel probabilities for every origin, to every destination
     * happened during every one of the time bins computed.
     * If there were no travels either for an entire time bin, an origin station,
     * or to a destination station, the corresponding entry will not be present
     * in the file.
     * The specified directory must exist before calling this method. The file
     * must not exist.
     * 
     * @param fileName "Directory" + "Filename" + ".csv" to save the results to
     */
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

    /**
     * Writes a scv file in the directory and filename specified in the argument
     * containing the travel probabilities for every destination in the specified
     * time bin and origin station.
     * If there were no travels in the specified time bin from the specified origin
     * station to some destination station, the corresponding entry will not be
     * present in the file.
     * The specified directory must exist before calling this method. The file
     * must not exist.
     * 
     * @param fileName "Directory" + "Filename" + ".csv" to save the results to
     * @param bin Time bin for which to get the probabilities to write
     * @param orig Origin station for which to get the probabilities to write
     */
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
}
