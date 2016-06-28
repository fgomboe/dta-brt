package edu.univalle;

import java.util.Map;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;

import edu.univalle.statistics.ControllerRouteVolumesListener;

public class Controller
{

    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("input/config_dummy.xml");
        Controler controler = new Controler(config);
        ControllerRouteVolumesListener otroController = new ControllerRouteVolumesListener(61200, 68400);
        controler.addControlerListener(otroController);
        controler.run();
        System.out.println("El peor GEH es: " + otroController.getWorstGEH());
        Map<String, double[][]> gehValues = otroController.getGEHMatrix();
        for (double[][] geh : gehValues.values()) {
            for (double[] estacion : geh) {
                // System.out.println(estacion[0] + "\t" + estacion[1] + "\t" + estacion[2] + "\t" + estacion[3]);
            }
            // System.out.println("");
        }
    }

}