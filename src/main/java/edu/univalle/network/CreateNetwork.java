package edu.univalle.network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

public class CreateNetwork
{

	public static void main(String[] args)
	{
		String osm = "input/cali.osm";
		Config config = ConfigUtils.createConfig();
		Scenario sc = ScenarioUtils.createScenario(config);
		Network net = sc.getNetwork();
		// Coordinate system: MAGNA-SIRGAS / Colombia West zone
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:3115");
		OsmNetworkReader onr = new OsmNetworkReader(net, ct);
		onr.setHighwayDefaults(3, "service", 1, 16.7 / 3.6, 1.0, 1500, true);
		onr.setKeepPaths(true);
		onr.parse(osm);
		new NetworkCleaner().run(net);
		new NetworkWriter(net).write("output/network.xml");
	}
}