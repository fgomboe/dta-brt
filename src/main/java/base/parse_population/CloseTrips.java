package base.parse_population;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import edu.univalle.utils.CsvReader;
import edu.univalle.utils.CsvWriter;

public class CloseTrips
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
        String O_COORD_X;
        String D_COORD_X;
        String O_COORD_Y;
        String D_COORD_Y;
        String TRONCAL;
        String HORA_MATSIM;
        String USO;
        String START;
        String END;

        String t_D_ESTACION = null;
        String t_D_ID_ESTACION = null;
        String t_D_COORD_X = null;
        String t_D_COORD_Y = null;

        try {
            usos_ready = new CsvReader("input/usos_ready_noUniviaje.csv");
            usos_ready.readHeaders();

            String outputFile_coord = "ready_closed_trips_noUniviaje.csv";
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
                    new_closed_trips.write("O_COORD_X");
                    new_closed_trips.write("D_COORD_X");
                    new_closed_trips.write("O_COORD_Y");
                    new_closed_trips.write("D_COORD_Y");
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
                        D_ESTACION = usos_ready.get("D_ESTACION");
                        O_ID_ESTACION = usos_ready.get("O_ID_ESTACION");
                        D_ID_ESTACION = usos_ready.get("D_ID_ESTACION");
                        O_COORD_X = usos_ready.get("O_COORD_X");
                        D_COORD_X = usos_ready.get("D_COORD_X");
                        O_COORD_Y = usos_ready.get("O_COORD_Y");
                        D_COORD_Y = usos_ready.get("D_COORD_Y");
                        TRONCAL = usos_ready.get("TRONCAL");
                        HORA_MATSIM = usos_ready.get("HORA_MATSIM");
                        USO = usos_ready.get("1_USO");
                        START = usos_ready.get("START");
                        END = usos_ready.get("END");

                        if (START.equals("YES")) {
                            t_D_ESTACION = O_ESTACION;
                            t_D_ID_ESTACION = O_ID_ESTACION;
                            t_D_COORD_X = O_COORD_X;
                            t_D_COORD_Y = O_COORD_Y;
                        }

                        if (END.equals("YES")) {
                            D_ESTACION = t_D_ESTACION;
                            D_ID_ESTACION = t_D_ID_ESTACION;
                            D_COORD_X = t_D_COORD_X;
                            D_COORD_Y = t_D_COORD_Y;
                        }

                        new_closed_trips.write(PRODUCTO);
                        new_closed_trips.write(VEH_ID);
                        new_closed_trips.write(QPAX);
                        new_closed_trips.write(CRD_SNR);
                        new_closed_trips.write(O_ESTACION);
                        new_closed_trips.write(D_ESTACION);
                        new_closed_trips.write(O_ID_ESTACION);
                        new_closed_trips.write(D_ID_ESTACION);
                        new_closed_trips.write(O_COORD_X);
                        new_closed_trips.write(D_COORD_X);
                        new_closed_trips.write(O_COORD_Y);
                        new_closed_trips.write(D_COORD_Y);
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
