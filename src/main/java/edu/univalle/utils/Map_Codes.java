package edu.univalle.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class Map_Codes
{
    public static HashMap<Integer, String> map_codes(String filename) {

        CsvReader estaciones;
        String troncal;
        HashMap<Integer, String> stations_code = new HashMap<>();

        try {
            // Para sacar la lista de las estaciones de la troncal
            estaciones = new CsvReader(filename);
            estaciones.readHeaders();
            while (estaciones.readRecord()) {
                troncal = estaciones.get("TRONCAL");

                if (troncal.equals("YES")) {
                    stations_code.put(Integer.decode((estaciones.get("UV_CODES"))), estaciones.get("STA_NAME"));
                }
            }

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return stations_code;

    }
}
