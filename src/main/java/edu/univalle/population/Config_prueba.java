package edu.univalle.population;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class Config_prueba
{
    public static void hacer() {
        Config config1 = ConfigUtils.createConfig();
        ConfigWriter prueba1 = new ConfigWriter(config1);
        config1.setParam("TimeAllocationMutator", "mutationRange", "15");
        prueba1.write("prueba13.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config1);
        // Controler controler = new Controler(scenario);
        // controler.run();

    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        hacer();

    }

}
