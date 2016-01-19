package edu.univalle.population;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**This class takes data from Usos_Detallados 01-04-2014 and add stations' ID and coordinates taken
 * from std_code**/

import edu.univalle.utils.CsvReader;
import edu.univalle.utils.CsvWriter;

public class Usos_Det_CodigoYCoord
{
    // declare name for output file
    private static String outputFile_coord = "usos_det_codigo_coord.csv";

    public static void main(String[] args) {

        try {
            // Open file with smart card data
            CsvReader matriz_usos = new CsvReader("input/Usos_Detallados 01-04-2014.csv");
            System.out.println("### extracting data from " + matriz_usos.toString() + " ###");

            // Read file header
            matriz_usos.readHeaders();
            boolean alreadyExists_coord = new File(outputFile_coord).exists();

            try {
                // use FileWriter constructor that specifies open for appending
                // Create output file
                CsvWriter csvOutput_coord = new CsvWriter(new FileWriter(outputFile_coord, true), ',');

                // if the file didn't already exist then we need to write out the header line
                if (!alreadyExists_coord) {
                    csvOutput_coord.write("ESTACION");
                    csvOutput_coord.write("PRODUCTO");
                    csvOutput_coord.write("INTEGRACION");
                    csvOutput_coord.write("HORA");
                    csvOutput_coord.write("VEH_ID");
                    csvOutput_coord.write("QPAX");
                    csvOutput_coord.write("CRD_SNR");
                    csvOutput_coord.write("ID_ESTACION");
                    csvOutput_coord.write("COORD_X");
                    csvOutput_coord.write("COORD_Y");
                    csvOutput_coord.write("TRONCAL");
                    csvOutput_coord.endRecord();
                }
                // else assume that the file already has the correct header line
                // we sweep the file with smart card data
                while (matriz_usos.readRecord())

                {
                    String id_estacion = matriz_usos.get("VEH_ID");
                    // Specification says that veh_id<1000 correspond to stations, otherwise is a secondary bus
                    if (Integer.parseInt(id_estacion) < 10000) { // Saca solo los registros de estaciones

                        // If we got an station input, we extract data

                        String name_estacion = matriz_usos.get("ESTACION");
                        String name_tipo = matriz_usos.get("PRODUCTO");
                        String integracion = matriz_usos.get("INTEGRACION");
                        String time_arrival = matriz_usos.get("HORA");
                        String cantidad_pasajeros = matriz_usos.get("QPAX");
                        String id_passenger = matriz_usos.get("CRD_SNR");

                        csvOutput_coord.write(name_estacion);
                        csvOutput_coord.write(name_tipo);
                        csvOutput_coord.write(integracion);
                        csvOutput_coord.write(time_arrival);
                        csvOutput_coord.write(id_estacion);
                        csvOutput_coord.write(cantidad_pasajeros);
                        csvOutput_coord.write(id_passenger);

                        try {

                            /*for each "station" register, we search in "estaciones_matrizUsos.csv"
                             * which contains coordenates in adequate system for each station
                             * according to its name
                             */

                            CsvReader matriz_usos_coord = new CsvReader("input/std_code.csv");
                            matriz_usos_coord.readHeaders();
                            while (matriz_usos_coord.readRecord()) {
                                String name_estacion_coord = matriz_usos_coord.get("STA_NAME");
                                if (name_estacion.equals(name_estacion_coord)) {
                                    // when we find the right station name, we extract its coordenates
                                    String codigo_uv = matriz_usos_coord.get("UV_CODES");
                                    String coord_x = matriz_usos_coord.get("COORD_X");
                                    String coord_y = matriz_usos_coord.get("COORD_Y");
                                    String troncal = matriz_usos_coord.get("TRONCAL");

                                    csvOutput_coord.write(codigo_uv);
                                    csvOutput_coord.write(coord_x);
                                    csvOutput_coord.write(coord_y);
                                    csvOutput_coord.write(troncal);
                                    break;
                                }

                            }
                            matriz_usos_coord.close();
                        }
                        catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        csvOutput_coord.endRecord();
                    }
                }

                csvOutput_coord.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            matriz_usos.close();
            System.out.println("### File construction finished #####");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
