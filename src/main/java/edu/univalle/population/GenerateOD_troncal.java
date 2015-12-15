package edu.univalle.population;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import edu.univalle.utils.CsvReader;
import edu.univalle.utils.CsvWriter;

public class GenerateOD_troncal
{

    public static void main(String[] args) {

        CsvReader matriz_usos;
        CsvReader estaciones;
        String st_ID_ESTACION = null;
        String st_N_ESTACION;
        String st_HORA = null;
        String troncal;
        String estacion_matriz;
        String st_ID_ESTACION_D = null;
        // String st_ID_pass = null;
        String st_N_ESTACION_D;
        String[] estaciones_troncal = new String[42];
        String[] estaciones_troncal_d = new String[42];
        int count_estaciones;
        int hora_inicio = 21600;
        int hora_fin = 28800;
        String[][] matriz_od = new String[1764][5];

        try {
            // Para sacar la lista de las estaciones de la troncal
            estaciones = new CsvReader("input/estaciones_matrizUsos2.csv");
            System.out.println("### extracting data from " + estaciones.toString() + " ###");
            estaciones.readHeaders();
            count_estaciones = 0;
            while (estaciones.readRecord()) {
                troncal = estaciones.get("TRONCAL");
                estacion_matriz = estaciones.get("MATRIZ USOS");
                if (troncal.equals("YES")) {
                    estaciones_troncal[count_estaciones] = estacion_matriz;
                    count_estaciones++;
                }
            }
            estaciones_troncal_d = estaciones_troncal;
            System.out.print(Arrays.toString(estaciones_troncal));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        int contador = 0;
        int od_index = 0;

        for (String est_o : estaciones_troncal) {
            for (String est_d : estaciones_troncal_d) {
                contador = 0;
                try {

                    matriz_usos = new CsvReader("data_filtered_troncal_od4.csv");
                    matriz_usos.readHeaders();
                    while (matriz_usos.readRecord())

                    {
                        st_N_ESTACION = matriz_usos.get("O_ESTACION");
                        st_N_ESTACION_D = matriz_usos.get("D_ESTACION");
                        st_HORA = matriz_usos.get("HORA");
                        // st_ID_pass=matriz_usos.get("ID_PAS");
                        if (Integer.parseInt(st_HORA) >= hora_inicio && Integer.parseInt(st_HORA) <= hora_fin
                                && st_N_ESTACION.equals(est_o) && st_N_ESTACION_D.equals(est_d)) {
                            contador++;
                            st_ID_ESTACION = matriz_usos.get("ID_ESTACION_O");
                            st_ID_ESTACION_D = matriz_usos.get("ID_ESTACION_D");
                        }
                    }
                    matriz_od[od_index][0] = st_ID_ESTACION;
                    matriz_od[od_index][1] = est_o;
                    matriz_od[od_index][2] = st_ID_ESTACION_D;
                    matriz_od[od_index][3] = est_d;
                    matriz_od[od_index][4] = Integer.toString(contador);
                    matriz_usos.close();
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                // System.out.println(matriz_od[od_index][0]+"-"+matriz_od[od_index][1]+"-"+matriz_od[od_index][2]+"-"+matriz_od[od_index][3]+"-"+matriz_od[od_index][4]);
                System.out.println("INDEX=" + od_index);
                od_index++;
            }

        }
        // aqui escribir el archivo
        String outputFile_coord = "matriz_od_6_8.csv";
        boolean alreadyExists_coord = new File(outputFile_coord).exists();

        try {
            CsvWriter csvOutput_coord = new CsvWriter(new FileWriter(outputFile_coord, true), ',');
            if (!alreadyExists_coord) {
                csvOutput_coord.write("ID_ORIGEN");
                csvOutput_coord.write("NOMBRE_ORIGEN");
                csvOutput_coord.write("ID_DESTINO");
                csvOutput_coord.write("NOMBRE_DESTINO");
                csvOutput_coord.write("CONTEO");
                csvOutput_coord.endRecord();

                for (int i = 0; i < matriz_od.length; i++) {
                    csvOutput_coord.write(matriz_od[i][0]);
                    csvOutput_coord.write(matriz_od[i][1]);
                    csvOutput_coord.write(matriz_od[i][2]);
                    csvOutput_coord.write(matriz_od[i][3]);
                    csvOutput_coord.write(matriz_od[i][4]);
                    csvOutput_coord.endRecord();
                }
                csvOutput_coord.close();
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
