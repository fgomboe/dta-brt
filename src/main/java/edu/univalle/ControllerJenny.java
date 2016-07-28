package edu.univalle;

import java.util.Map;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;

import edu.univalle.statistics.ControllerRouteVolumesListener;

public class ControllerJenny
{

    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("input/config_dummy.xml");
        Controler controler = new Controler(config);
        ControllerRouteVolumesListener otroController = new ControllerRouteVolumesListener(21600, 28800);
        controler.addControlerListener(otroController);
        controler.run();
        System.out.println("El peor GEH es: " + otroController.getWorstGEH());
        Map<String, double[][]> gehValues = otroController.getGEHMatrix();
        int count_total = 0;
        int count_pass = 0;
        for (Map.Entry<String, double[][]> geh : gehValues.entrySet()) {
            System.out.println(geh.getKey());
            for (double[] estacion : geh.getValue()) {
                if (estacion[0] <= 5) {
                    count_pass++;
                }
                if (estacion[1] <= 5) {
                    count_pass++;
                }
                if (estacion[2] <= 5) {
                    count_pass++;
                }
                if (estacion[3] <= 5) {
                    count_pass++;
                }
                count_total = count_total + 4;
                System.out.println(estacion[0] + "\t" + estacion[1] + "\t" + estacion[2] + "\t" + estacion[3]);
            }

            System.out.println("");
        }
        double porc_validated = (count_pass / count_total) * 100;
        System.out.println(porc_validated);
        System.out.println(count_pass);
        System.out.println(count_total);
    }
}