package edu.univalle.population;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import edu.univalle.utils.CsvReader;

public class ProbDictionary
{
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> tripTable;

    private final int SECONDS_DAY = 86400;
    private final int SECONDS_HOUR = 3600;
    private final int TRUNK_STATIONS = 40;

    private int numberOfStations;
    private int initialTime, endTime, timeSpan;
    private int timeBins;
    int debug = 1;

    public ProbDictionary() {
        this.numberOfStations = TRUNK_STATIONS;
        this.initialTime = 0;
        this.endTime = SECONDS_DAY;
        this.timeSpan = SECONDS_HOUR;
        this.timeBins = (int) Math.ceil((this.endTime - this.initialTime) / (float) this.timeSpan);

        // TODO prove this division for correctness (I need a float value as result)
        tripTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>(this.timeBins, 1);
    }

    public ProbDictionary(int numberOfStations, int initialTime, int endTime, int timeSpan) {
        this.numberOfStations = numberOfStations;
        this.initialTime = initialTime;
        this.endTime = endTime;
        this.timeSpan = timeSpan;
        this.timeBins = (int) Math.ceil((this.endTime - this.initialTime) / (float) this.timeSpan);

        // TODO prove this division for correctness (I need a float value as result)
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
                addTrip(tripTime, originStation, destinationStation);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTrip(int time, int orig, int dest) {
        HashMap<Integer, HashMap<Integer, Integer>> aux1 = null;
        HashMap<Integer, Integer> aux2 = null;
        int bin = calcBin(time);
        // System.out.println("for the time: " + time + " - calculated bin: " + bin);
        if (tripTable.containsKey(bin)) {
            if (tripTable.get(bin).containsKey(orig)) {
                if (tripTable.get(bin).get(orig).containsKey(dest)) {
                    // if it contains the mapping <time, orig, dest>, then add 1 to that value
                    int prevValue = tripTable.get(bin).get(orig).get(dest);
                    tripTable.get(bin).get(orig).put(dest, prevValue + 1);
                }
                else {
                    // if it contains the mapping <time, orig> but not the specified destination
                    // from that origin, then initialize <dest> value to 1
                    tripTable.get(bin).get(orig).put(dest, 1);
                }
            }
            else {
                // if it contains the mapping <time> but no origin or destination for that time,
                // then initialize <orig, dest> value to 1
                aux2 = new HashMap<Integer, Integer>(numberOfStations, 1);
                aux2.put(dest, 1);
                tripTable.get(bin).put(orig, aux2);
            }
        }
        else {
            // if it does not contain any entry for the time
            aux2 = new HashMap<Integer, Integer>(numberOfStations, 1);
            aux2.put(dest, 1);
            aux1 = new HashMap<Integer, HashMap<Integer, Integer>>(numberOfStations, 1);
            aux1.put(orig, aux2);
            tripTable.put(bin, aux1);
        }
    }

    public int getNumberOfTrips(int bin, int orig, int dest) {
        if (tripTable.containsKey(bin)) {
            if (tripTable.containsKey(orig)) {
                if (tripTable.containsKey(dest)) {
                    return tripTable.get(bin).get(orig).get(dest);
                }
            }
        }
        return 0;
    }

    private int calcBin(int time) {
        // TODO prove this division for correctness (I need a float value as result)
        double bin = Math.floor((float) (time - initialTime) / timeSpan);
        return (int) bin;
    }

    public static void main(String[] args) {
        ProbDictionary dict = new ProbDictionary();
        dict.constructTripTable("./temporal_Feli/ready_closed_trips_noUniviaje.csv");
        int noTrips = dict.getNumberOfTrips(14, 9, 13);
        System.out.println("Number of Trips: " + noTrips + "\nFinished!");
    }
}
