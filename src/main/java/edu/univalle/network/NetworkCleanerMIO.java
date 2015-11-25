package edu.univalle.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.utils.objectattributes.ObjectAttributes;

public class NetworkCleanerMIO extends NetworkCleaner
{

	private static final Logger log = Logger.getLogger(NetworkCleanerMIO.class);
	
	@Override
	public void run(final Network network, final ObjectAttributes nodeAttributes, final ObjectAttributes linkAttributes)
	{

		FileAppender appender;
		try {
			appender = new FileAppender(new PatternLayout("[%d{MMM dd HH:mm:ss}] %-5p (%F:%L) - %m%n"), "./output/cleaner.log", false);
			BasicConfigurator.configure(appender);
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("running " + this.getClass().getName() + " algorithm...");
		log.info("  initial network contains " + network.getNodes().size() + " nodes and " + network.getLinks().size() + " links.");
		
		this.removeIsolatedNodes(network, nodeAttributes);
		this.removeZeroLengthLinks(network, linkAttributes);
		
		log.info("  resulting network contains " + network.getNodes().size() + " nodes and " + network.getLinks().size() + " links.");
		log.info("done.");
	}
	
	public void removeIsolatedNodes(Network network, ObjectAttributes nodeAttributes)
	{
		log.info("  checking " + network.getNodes().size() + " nodes for isolated nodes...");
		
		List<Node> allNodes = new ArrayList<Node>(network.getNodes().values());
		final Map<Id<Node>, Node> visitedNodes = new TreeMap<Id<Node>, Node>();
		
		for (Node startNode : allNodes) {
			if (!visitedNodes.containsKey(startNode.getId())) {
				Map<Id<Node>, Node> cluster = this.findCluster(startNode, network);
				visitedNodes.putAll(cluster);
				if (cluster.size() < 2) {
					network.removeNode(startNode.getId());
					nodeAttributes.removeAllAttributes(startNode.getId().toString());
					log.info("    node " + startNode.getId().toString() + " removed from network...");
				}
			}
		}

		log.info("    " + ( allNodes.size() - network.getNodes().size() ) + " nodes removed from the network.");
		log.info("  done.");
		
	}

}
