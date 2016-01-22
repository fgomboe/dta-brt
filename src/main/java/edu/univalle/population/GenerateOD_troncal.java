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
        String st_ID_ESTACION_D = null;
        String st_HORA = null;
        String troncal;
        String[][] matriz_od = new String[1600][5];
        String[] estaciones_troncal_names = new String[40];
        String[] estaciones_troncal_codes = new String[40];
        int count_estaciones;
        int hora_inicio = 0 * 3600;
        int hora_fin = 24 * 3600;

        try {
            // Para sacar la lista de las estaciones de la troncal
            estaciones = new CsvReader("input/std_code.csv");
            System.out.println("### extracting data from " + estaciones.toString() + " ###");
            estaciones.readHeaders();
            count_estaciones = 0;

            while (estaciones.readRecord()) {
                troncal = estaciones.get("TRONCAL");

                if (troncal.equals("YES")) {
                    estaciones_troncal_codes[count_estaciones] = estaciones.get("UV_CODES");

                    estaciones_troncal_names[count_estaciones] = estaciones.get("STA_NAME");
                    count_estaciones++;
                }
            }
            System.out.print(Arrays.toString(estaciones_troncal_names));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        int contador = 0;
        int od_index = 0;
        int count_o = 0;
        for (String est_o : estaciones_troncal_codes) {
            int count_d = 0;
            for (String est_d : estaciones_troncal_codes) {
                try {
                    contador = 0;
                    matriz_usos = new CsvReader("input/usos_xls/csv_closed/general.csv");
                    matriz_usos.readHeaders();
                    while (matriz_usos.readRecord())

                    {
                        st_ID_ESTACION = matriz_usos.get("O_ID_ESTACION");
                        st_ID_ESTACION_D = matriz_usos.get("D_ID_ESTACION");
                        st_HORA = matriz_usos.get("HORA_MATSIM");
                        if (Integer.parseInt(st_HORA) >= hora_inicio && Integer.parseInt(st_HORA) <= hora_fin
                                && st_ID_ESTACION.equals(est_o) && st_ID_ESTACION_D.equals(est_d)) {
                            contador++;
                        }

                    }
                    matriz_usos.close();
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("INDEX=" + od_index);
                matriz_od[od_index][0] = est_o;
                matriz_od[od_index][1] = estaciones_troncal_names[count_o];
                matriz_od[od_index][2] = est_d;
                matriz_od[od_index][3] = estaciones_troncal_names[count_d];
                matriz_od[od_index][4] = Integer.toString(contador);
                od_index++;
                count_d++;
            }
            count_o++;
        }
        // aqui escribir el archivo
        String outputFile_coord = "output/matriz_od_24h.csv";
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
