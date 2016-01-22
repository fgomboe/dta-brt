package edu.univalle.population;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import edu.univalle.utils.CsvReader;
import edu.univalle.utils.CsvWriter;
import edu.univalle.utils.Map_Codes;

public class FillTrips
{

    public static int asignProb(ProbDictionary dict, int time, int origin_key) {

        HashMap<Integer, Double> probTable = dict.getProbability(dict.calcBin(time), origin_key);
        HashMap<Integer, Double> probTable_acum = probTable;
        Iterator<Integer> it = probTable.keySet().iterator();
        double acum = 0;
        double dize = Math.random();
        int destination = -1;

        if (probTable.size() == 0) {
            destination = -1;
        }
        else {
            while (it.hasNext()) {
                Integer key = it.next();
                acum = acum + probTable.get(key);
                probTable_acum.put(key, acum);
                // System.out.println("Clave: " + key + " -> Valor: " + probTable_acum.get(key));
                if (acum >= dize) {
                    destination = key;
                    break;
                }

            }
            // System.out.println("Dize= " + dize + " - Destination: " + destination);

        }

        return destination;
    }

    public static void main(String[] args) {
        HashMap<Integer, String> stations_code = Map_Codes.map_codes("input/std_code.csv");
        ProbDictionary dict = new ProbDictionary();
        dict.constructTripTable("C:/MATSim/workspace_mars/dta-brt/input/usos_xls/csv_closed/closed_trips_normal.csv");
        dict.constructProbTable();

        // asignProb(dict, 72000, 2);

        CsvReader usos_ready;

        String PRODUCTO;
        String VEH_ID;
        String QPAX;
        String CRD_SNR;
        String O_ESTACION;
        String D_ESTACION;
        String O_ID_ESTACION;
        String D_ID_ESTACION;
        String TRONCAL;
        String HORA_MATSIM;
        String USO;
        String START;
        String END;

        try {
            usos_ready = new CsvReader(
                    "C:/MATSim/workspace_mars/dta-brt/input/usos_xls/csv_ready/usos_univiaje_sin1uso.csv");
            usos_ready.readHeaders();

            String outputFile_coord = "C:/MATSim/workspace_mars/dta-brt/input/usos_xls/csv_closed/closed_trips_univiaje_sin1uso.csv";
            boolean alreadyExists_coord = new File(outputFile_coord).exists();
            try {
                CsvWriter new_closed_trips = new CsvWriter(new FileWriter(outputFile_coord, true), ',');
                if (!alreadyExists_coord) {
                    new_closed_trips.write("PRODUCTO");
                    new_closed_trips.write("VEH_ID");
                    new_closed_trips.write("QPAX");
                    new_closed_trips.write("CRD_SNR");
                    new_closed_trips.write("O_ESTACION");
                    new_closed_trips.write("D_ESTACION");
                    new_closed_trips.write("O_ID_ESTACION");
                    new_closed_trips.write("D_ID_ESTACION");
                    new_closed_trips.write("TRONCAL");
                    new_closed_trips.write("HORA_MATSIM");
                    new_closed_trips.write("1_USO");
                    new_closed_trips.write("START");
                    new_closed_trips.write("END");
                    new_closed_trips.endRecord();

                    while (usos_ready.readRecord()) {

                        PRODUCTO = usos_ready.get("PRODUCTO");
                        VEH_ID = usos_ready.get("VEH_ID");
                        QPAX = usos_ready.get("QPAX");
                        CRD_SNR = usos_ready.get("CRD_SNR");
                        O_ESTACION = usos_ready.get("O_ESTACION");
                        O_ID_ESTACION = usos_ready.get("O_ID_ESTACION");
                        TRONCAL = usos_ready.get("TRONCAL");
                        HORA_MATSIM = usos_ready.get("HORA_MATSIM");
                        USO = usos_ready.get("1_USO");
                        START = usos_ready.get("START");
                        END = usos_ready.get("END");

                        int destination_id = asignProb(dict, Integer.parseInt(HORA_MATSIM),
                                Integer.parseInt(O_ID_ESTACION));
                        String destination_name = stations_code.get(destination_id);
                        D_ESTACION = destination_name;
                        D_ID_ESTACION = Integer.toString(destination_id);

                        new_closed_trips.write(PRODUCTO);
                        new_closed_trips.write(VEH_ID);
                        new_closed_trips.write(QPAX);
                        new_closed_trips.write(CRD_SNR);
                        new_closed_trips.write(O_ESTACION);
                        new_closed_trips.write(D_ESTACION);
                        new_closed_trips.write(O_ID_ESTACION);
                        new_closed_trips.write(D_ID_ESTACION);
                        new_closed_trips.write(TRONCAL);
                        new_closed_trips.write(HORA_MATSIM);
                        new_closed_trips.write(USO);
                        new_closed_trips.write(START);
                        new_closed_trips.write(END);
                        new_closed_trips.endRecord();

                    }
                    new_closed_trips.close();

                }
                else {
                    System.out.println("File already exsists");
                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
