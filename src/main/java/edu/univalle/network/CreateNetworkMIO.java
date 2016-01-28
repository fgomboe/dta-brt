package edu.univalle.network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;

public class CreateNetworkMIO
{

    public static void main(String[] args) {
        Config config = ConfigUtils.createConfig();
        Scenario sc = ScenarioUtils.createScenario(config);
        Network net = sc.getNetwork();
        ObjectAttributes nodeAttributes = new ObjectAttributes();
        ObjectAttributes linkAttributes = new ObjectAttributes();

        // NetworkCreatorMIO simplifiedNetworkCreator = new NetworkCreatorMIO();
        // simplifiedNetworkCreator.init(net, nodeAttributes, linkAttributes);
        // simplifiedNetworkCreator.run("simplified");
        // // new NetworkCleanerMIO().run(net);
        // simplifiedNetworkCreator.write("simplified");

        NetworkCreatorMIO specialNetworkCreator = new NetworkCreatorMIO();
        specialNetworkCreator.init(net, nodeAttributes, linkAttributes);
        specialNetworkCreator.run("special");
        new NetworkCleanerMIO().run(net, nodeAttributes, linkAttributes);
        specialNetworkCreator.write("special");

    }

}