package edu.univalle.population;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.univalle.utils.CsvReader;
import edu.univalle.utils.CsvWriter;

public class MergeFiles
{

    public static void main(String[] args) {
        // TODO Auto-generated method stub

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

        List<String> FileList = new ArrayList<String>();

        FileList.add("C:/MATSim/workspace_mars/dta-brt/input/usos_xls/csv_closed/closed_trips_1uso.csv");
        FileList.add("C:/MATSim/workspace_mars/dta-brt/input/usos_xls/csv_closed/closed_trips_normal.csv");
        FileList.add("C:/MATSim/workspace_mars/dta-brt/input/usos_xls/csv_closed/closed_trips_univiaje_sin1uso.csv");

        String outputFile_coord = "C:/MATSim/workspace_mars/dta-brt/input/usos_xls/csv_closed/general.csv";
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

                for (String temp : FileList) {

                    try {
                        usos_ready = new CsvReader(temp);
                        usos_ready.readHeaders();

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
                            D_ESTACION = usos_ready.get("D_ESTACION");
                            D_ID_ESTACION = usos_ready.get("D_ID_ESTACION");

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

                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

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

}
