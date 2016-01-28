package edu.univalle.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class Map_coord
{

    // TODO Auto-generated method stub
    public static HashMap<Integer, String> map_x_coord(String filename) {

        CsvReader estaciones;
        String troncal;
        HashMap<Integer, String> stations_x_coord = new HashMap<>();

        try {
            // Para sacar la lista de las estaciones de la troncal
            estaciones = new CsvReader(filename);
            estaciones.readHeaders();
            while (estaciones.readRecord()) {
                troncal = estaciones.get("TRONCAL");

                if (troncal.equals("YES")) {
                    stations_x_coord.put(Integer.decode((estaciones.get("UV_CODES"))), estaciones.get("COORD_X"));
                }
            }

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return stations_x_coord;

    }

    public static HashMap<Integer, String> map_y_coord(String filename) {

        CsvReader estaciones;
        String troncal;
        HashMap<Integer, String> stations_y_coord = new HashMap<>();

        try {
            // Para sacar la lista de las estaciones de la troncal
            estaciones = new CsvReader(filename);
            estaciones.readHeaders();
            while (estaciones.readRecord()) {
                troncal = estaciones.get("TRONCAL");

                if (troncal.equals("YES")) {
                    stations_y_coord.put(Integer.decode((estaciones.get("UV_CODES"))), estaciones.get("COORD_Y"));
                }
            }

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return stations_y_coord;

    }

}
