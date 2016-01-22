package edu.univalle.population;

import java.util.HashMap;
import java.util.Iterator;

public class Prueba
{

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        ProbDictionary dict = new ProbDictionary();
        dict.constructTripTable("C:/MATSim/workspace_mars/dta-brt/input/usos_xls/csv_closed/closed_trips_normal.csv");
        dict.constructProbTable();
        int bin = dict.calcBin(45000);
        HashMap<Integer, Double> probTable = dict.getProbability(bin, 39);
        Iterator<Integer> it = probTable.keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            System.out.println("Bin= " + bin + " - Destino= " + key + " - Prob: " + probTable.get(key));

        }
        // System.out.println("Dize= " + dize + " - Destination: " + destination);

    }

}
