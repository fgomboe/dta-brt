package edu.univalle;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;

import edu.univalle.statistics.ControllerLinkVolumesListener;

public class Controller
{

    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("input/config_dummy.xml");
        Controler controler = new Controler(config);
        controler.addControlerListener(new ControllerLinkVolumesListener(61200, 68399, 3600));
        controler.run();
    }

}