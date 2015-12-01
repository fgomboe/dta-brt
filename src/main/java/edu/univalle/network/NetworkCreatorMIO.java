package edu.univalle.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;
import org.opengis.feature.simple.SimpleFeature;

import edu.univalle.utils.CsvReader;
import edu.univalle.utils.CsvWriter;
import edu.univalle.utils.Utils;

public class NetworkCreatorMIO
{

	private final static Logger log = Logger.getLogger(NetworkCreatorMIO.class);
	private Network net;
	ObjectAttributes nodeAttributes = new ObjectAttributes();
	ObjectAttributes linkAttributes = new ObjectAttributes();

	// working with csv files
	private String arcsCsvFile = "./input/networkMIO/arcs.csv";
	private String stopsCsvFile = "./input/networkMIO/stops.csv";
	
	// working with shape files
	// private String stopsShapeFile = "input/shapes/stops/mio_paradas_140714.shp";
	private String stationsShapeFile = "input/shapes/stations/mio_estaciones.shp";
	
	private String stationsCodingFile = "output/stationsCoding.csv";

	// // working with shape files
	// private static final String stopsAttributes[] = {"X", "Y", "RUTA", "DIRECCION", "PLAN", "ZONA", "ESTRUCTURA", "ORDEN_RUTA", "OBSERVACIO"};
	private static final String stationsAttLabels[] = { "GPS_X", "GPS_Y", "ID_ESTACIO", "ESTACION", "DIRECCION", "VAGONES", "TIPO_ESTAC", "CORREDOR_T" };

	public void init(Network network, ObjectAttributes nodeAtt, ObjectAttributes linkAtt)
	{
		this.net = network;
		this.nodeAttributes = nodeAtt;
		this.linkAttributes = linkAtt;
	}

	public void run(String type)
	{
		createNodes(this.net);
		if (type.equalsIgnoreCase("simplified")) {
			createLinks(this.net);
		} else if (type.equalsIgnoreCase("special")) {
			createSpecialLinks(this.net);
		}
		log.info("Creation of " + type + "Network, finished #################################");
	}

	public void write(String type)
	{
		new NetworkWriter(this.net).write("./output/" + type + "Network.xml");
		new ObjectAttributesXmlWriter(this.nodeAttributes).writeFile("./output/" + type + "NodeAttributes.xml");
		new ObjectAttributesXmlWriter(this.linkAttributes).writeFile("./output/" + type + "LinkAttributes.xml");
		log.info("Writing of " + type + "Network, finished #################################");
	}

	private void createNodes(Network network)
	{
		// Convert from GPS coordinate system to "MAGNA-SIRGAS / Colombia West Zone"
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:3115");

		// ------------------------------------------------------------------------------------------
		// read nodes from the stations shape file
		// ------------------------------------------------------------------------------------------
		ShapeFileReader shapeFileReader = new ShapeFileReader();
		Collection<SimpleFeature> features = null;
		SimpleFeature ft = null;
		Iterator<SimpleFeature> it = null;

		int id = 1;
		shapeFileReader.readFileAndInitialize(stationsShapeFile);
		features = shapeFileReader.getFeatureSet();
		
		try {
            CsvWriter writer = new CsvWriter(stationsCodingFile);
            writer.writeRecord(new String[]{"NAME","UV_CODES","METROCALI_CODES"});
    
    		it = features.iterator();
    		log.info("stations processed:");
    		while (it.hasNext()) {
    			ft = it.next();
    			Coord coord = CoordUtils.createCoord(Double.parseDouble(ft.getAttribute(stationsAttLabels[0]).toString()), Double.parseDouble(ft.getAttribute(stationsAttLabels[1]).toString()));
    			Node node = network.getFactory().createNode(Id.createNodeId(id), ct.transform(coord));
    
    			for (String att : stationsAttLabels)
    				nodeAttributes.putAttribute(Integer.toString(id), att, ft.getAttribute(att));
    
    			network.addNode(node);
    
    			System.out.println("Id: " + id + " - " + ft.getAttribute(stationsAttLabels[3]));
    			writer.writeRecord(new String[]{(String) ft.getAttribute("ESTACION"), Integer.toString(id), (String) ft.getAttribute("ID_ESTACIO")});
                
    			id++;
    		}
    		writer.close();
		} 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
		catch (IOException e) {
            e.printStackTrace();
        }
		features.clear();
		// ------------------------------------------------------------------------------------------

		
		// ------------------------------------------------------------------------------------------
		// read nodes from the stops .csv file
		// ------------------------------------------------------------------------------------------
		try {
			CsvReader nodes = new CsvReader(stopsCsvFile);
			nodes.readHeaders();

			log.info("stops processed:");

			while (nodes.readRecord()) {

				//Coord coord = new CoordImpl(Double.parseDouble(nodes.get("LONGITUDE")), Double.parseDouble(nodes.get("LATITUDE")));
				Coord coord = CoordUtils.createCoord(Double.parseDouble(nodes.get("LONGITUDE")), Double.parseDouble(nodes.get("LATITUDE")));

				// same Id as short name
				Node node = network.getFactory().createNode(Id.createNodeId(nodes.get("SHORTNAME")), ct.transform(coord));

				// iii begins with 1 to skip MetroCali's Id
				for (int iii = 1; iii < nodes.getColumnCount(); iii++)
					nodeAttributes.putAttribute(nodes.get("SHORTNAME"), nodes.getHeader(iii), ((iii > 2) ? Double.parseDouble(nodes.get(iii)) : nodes.get(iii)));

				// Looks for an associated station, i.e. the station general name this stop is associated with
				String longName = nodeAttributes.getAttribute(node.getId().toString(), "LONGNAME").toString();
				for (int iii = 1; iii < id; iii++) {
					String station = Utils.stripAccents(nodeAttributes.getAttribute(Integer.toString(iii), stationsAttLabels[3]).toString());
					if (longName.startsWith(station)) {
						nodeAttributes.putAttribute(node.getId().toString(), "ASSOCIATE_NAME", station);
						nodeAttributes.putAttribute(node.getId().toString(), "ASSOCIATE_ID", iii);
					}
				}

				if (network.getNodes().get(node.getId()) == null)
					network.addNode(node);

				System.out.println("Id: " + nodes.get(1) + " - " + nodes.get(2));
			}
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		// ------------------------------------------------------------------------------------------

	}

	private void createLinks(Network network)
	{

		// it seems like you only can specify pt
		Set<String> modes = new HashSet<String>();
		modes.add("pt");
		// modes.add("com");
		// modes.add("pad");
		// modes.add("art");

		try {
			CsvReader links = new CsvReader(arcsCsvFile);
			links.readHeaders();

			log.info("arcs processed:");

			while (links.readRecord()) {

				// don't process duplicated links
				if (network.getLinks().get(Id.createLinkId(links.get("ID"))) == null) {
					Link link = network.getFactory().createLink(Id.createLinkId(links.get("ID")), network.getNodes().get(Id.createNodeId(links.get("PUNTO_INI"))),
							network.getNodes().get(Id.createNodeId(links.get("PUNTO_FIN"))));
					// Speed = speed limit inside the city
					// capacity = secondary highway in OsmNetworkReader
					// numOfLanes = most of the main lines in the SITM-MIO are 1-lane
					// modes = We aren't certain whether this is a main or a secondary link in the system, so all links have "com, pad, art"
					setLinkAttributes(link, Double.parseDouble(links.get("LENGTH")), 60, 1000, 1, modes);
					network.addLink(link);

					// Setting attributes. iii begins with 1 to skip Id
					for (int iii = 1; iii < links.getColumnCount(); iii++) {
						if (iii == 1 || iii == 2 || iii == 9)
							linkAttributes.putAttribute(links.get("ID"), links.getHeader(iii), links.get(iii));
					}
				}

				// Setting additional attributes to fromNode and toNode
				// -----------------------------------------------------------------------------------------------------
				// put LINES and OUT_LINES to the fromNode
				setLinesAttribute(network.getNodes().get(Id.createNodeId(links.get("PUNTO_INI"))), links.get("LINEA"), links.get("SENTIDO"), "OUT");
				// put LINES and IN_LINES to the toNode
				setLinesAttribute(network.getNodes().get(Id.createNodeId(links.get("PUNTO_FIN"))), links.get("LINEA"), links.get("SENTIDO"), "IN");

				System.out.println("Id: " + links.get("ID") + " - " + links.get("CONCAT_SHT"));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createSpecialLinks(Network network)
	{

		Set<String> modes = new HashSet<String>();

		Node assocNode;
		Node fromNode;
		Boolean fromNodeStation;
		Node toNode;
		Boolean toNodeStation;
		Link link;
		Id<Link> linkId;

		try {
			CsvReader links = new CsvReader(arcsCsvFile);
			links.readHeaders();

			log.info("arcs processed:");

			while (links.readRecord()) {

				// it seems like you only can specify pt
				modes.clear();
				modes.add("pt");
				// modes.add("com");
				// modes.add("pad");

				assocNode = null;
				fromNode = network.getNodes().get(Id.createNodeId(links.get("PUNTO_INI")));
				fromNodeStation = false;
				toNode = network.getNodes().get(Id.createNodeId(links.get("PUNTO_FIN")));
				toNodeStation = false;
				link = null;
				linkId = null;

				// fromNode processing
				if (nodeAttributes.getAttribute(links.get("PUNTO_INI"), "ASSOCIATE_ID") != null) {
					assocNode = network.getNodes().get(Id.createNodeId(nodeAttributes.getAttribute(links.get("PUNTO_INI"), "ASSOCIATE_ID").toString()));

					linkId = Id.createLinkId(assocNode.getId().toString() + "-" + fromNode.getId().toString());
					link = network.getFactory().createLink(linkId, assocNode, fromNode);
					// Setting additional attributes to link
					// LINE_ROUTE
					// LINES
					setLinesAttribute(link, links.get("LINEA"), links.get("SENTIDO"));
					if (network.getLinks().get(linkId) == null) {
						// lenght = maximum length ('articulado' + 1m)
						// speed = speed limit inside the city
						// capacity = secondary highway in OsmNetworkReader
						// numOfLanes = most of the main lines in the SITM-MIO are 1-lane
						// In the main lines always articulated and padron vehicle types allowed
						// modes = Using Arrays to initialize the HashSet in the same line
						// setLinkAttributes(link, 19, 60, 1000, 1, new HashSet<String>(Arrays.asList("pad", "art")));
						setLinkAttributes(link, 19, 60, 1000, 1, new HashSet<String>(Arrays.asList("pt")));
						network.addLink(link);
					}

					linkId = Id.createLinkId(fromNode.getId().toString() + "-" + assocNode.getId().toString());
					link = network.getFactory().createLink(linkId, fromNode, assocNode);
					// Setting additional attributes to link
					// LINE_ROUTE
					// LINES
					setLinesAttribute(link, links.get("LINEA"), links.get("SENTIDO"));
					if (network.getLinks().get(linkId) == null) {
						// setLinkAttributes(link, 19, 60, 1000, 1, new HashSet<String>(Arrays.asList("pad", "art")));
						setLinkAttributes(link, 19, 60, 1000, 1, new HashSet<String>(Arrays.asList("pt")));
						network.addLink(link);
					}

					// Setting additional attributes to fromNode
					// --------------------------------------------------------
					// put LINES and OUT_LINES to the fromNode
					setLinesAttribute(fromNode, links.get("LINEA"), links.get("SENTIDO"), "OUT");

					fromNode = assocNode;
					fromNodeStation = true;
				}

				// toNode processing
				if (nodeAttributes.getAttribute(links.get("PUNTO_FIN"), "ASSOCIATE_ID") != null) {
					assocNode = network.getNodes().get(Id.createNodeId(nodeAttributes.getAttribute(links.get("PUNTO_FIN"), "ASSOCIATE_ID").toString()));

					linkId = Id.createLinkId(assocNode.getId().toString() + "-" + toNode.getId().toString());
					link = network.getFactory().createLink(linkId, assocNode, toNode);
					// Setting additional attributes to link
					// LINE_ROUTE
					// LINES
					setLinesAttribute(link, links.get("LINEA"), links.get("SENTIDO"));
					if (network.getLinks().get(linkId) == null) {
						// setLinkAttributes(link, 19, 60, 1000, 1, new HashSet<String>(Arrays.asList("pad", "art")));
						setLinkAttributes(link, 19, 60, 1000, 1, new HashSet<String>(Arrays.asList("pt")));
						network.addLink(link);
					}

					linkId = Id.createLinkId(toNode.getId().toString() + "-" + assocNode.getId().toString());
					link = network.getFactory().createLink(linkId, toNode, assocNode);
					// Setting additional attributes to link
					// LINE_ROUTE
					// LINES
					setLinesAttribute(link, links.get("LINEA"), links.get("SENTIDO"));
					if (network.getLinks().get(linkId) == null) {
						// setLinkAttributes(link, 19, 60, 1000, 1, new HashSet<String>(Arrays.asList("pad", "art")));
						setLinkAttributes(link, 19, 60, 1000, 1, new HashSet<String>(Arrays.asList("pt")));
						network.addLink(link);
					}

					// Setting additional attributes to fromNode
					// -----------------------------------------------------
					// put LINES and IN_LINES to the toNode
					setLinesAttribute(toNode, links.get("LINEA"), links.get("SENTIDO"), "IN");

					toNode = assocNode;
					toNodeStation = true;
				}

				// if both nodes are 'stations', then this must be a trunk link, i.e. padron & articulado
				if (fromNodeStation && toNodeStation) {
					// it seems like you only can specify pt
					modes.clear();
					modes.add("pt");
					// modes.add("pad");
					// modes.add("art");
				}

				linkId = Id.createLinkId(fromNode.getId().toString() + "-" + toNode.getId().toString());
				link = network.getFactory().createLink(linkId, fromNode, toNode);
				// Setting additional attributes to link
				// LINE_ROUTE
				// LINES
				setLinesAttribute(link, links.get("LINEA"), links.get("SENTIDO"));
				if (network.getLinks().get(linkId) == null) {
					setLinkAttributes(link, Double.parseDouble(links.get("LENGTH")), 60, 1000, 1, modes);
					network.addLink(link);
				}

				// Setting additional attributes to fromNode and toNode
				// --------------------------------------------------------
				// put LINES and OUT_LINES to the fromNode
				setLinesAttribute(fromNode, links.get("LINEA"), links.get("SENTIDO"), "OUT");
				// put LINES and IN_LINES to the toNode
				setLinesAttribute(toNode, links.get("LINEA"), links.get("SENTIDO"), "IN");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Sets the normal attributes to a link
	 * 
	 * @param link
	 * @param lenght
	 *            - Double
	 * @param speed
	 *            - Double
	 * @param capacity
	 *            - Double
	 * @param numOfLanes
	 *            - Integer
	 * @param modes
	 *            - String chain containing all the modes allowed in this link, separated by commas ( , )
	 */
	public void setLinkAttributes(Link link, double lenght, double speed, double capacity, int numOfLanes, Set<String> modes)
	{
		// link.setAllowedModes( new HashSet<String>(Arrays.asList("pad", "art")) );
		link.setAllowedModes(new HashSet<String>(Arrays.asList("pt")));
		link.setLength(lenght);
		// Because of the speed limit inside the city
		link.setFreespeed(speed / 3.6);
		// The same as a secondary highway in OsmNetworkReader
		link.setCapacity(capacity);
		// Most of the main lines in the SITM-MIO are one laned
		link.setNumberOfLanes(numOfLanes);
		// In the main lines always articulated and padrón vehicle types allowed
		link.setAllowedModes(modes);
	}

	/**
	 * Adds the attributes "LINE_ROUTE" and "LINES" to the specified link inside the linkAttributes object
	 * 
	 * @param link
	 *            - Object of type Link
	 * @param line
	 *            - String
	 * @param route
	 *            - String
	 */
	public void setLinesAttribute(Link link, String line, String route)
	{
		List<String> lines = new ArrayList<String>();
		lines = Utils.parseToList((String) linkAttributes.getAttribute(link.getId().toString(), "LINE_ROUTE"));
		if (!lines.contains(line + "-" + route))
			lines.add(line + "-" + route);
		linkAttributes.putAttribute(link.getId().toString(), "LINE_ROUTE", lines.toString());
		lines = Utils.parseToList((String) linkAttributes.getAttribute(link.getId().toString(), "LINES"));
		if (!lines.contains(line))
			lines.add(line);
		linkAttributes.putAttribute(link.getId().toString(), "LINES", lines.toString());
	}

	/**
	 * Adds the attributes "LINES" and "IN/OUT_LINES" to the specified node inside the nodeAttributes object
	 * 
	 * @param node
	 *            - Object of type Node
	 * @param line
	 *            - String
	 * @param route
	 *            - String
	 * @param in_out
	 *            - String containing either "IN" other "OUT"
	 */
	public void setLinesAttribute(Node node, String line, String route, String in_out)
	{
		List<String> lines = new ArrayList<String>();
		lines = Utils.parseToList((String) nodeAttributes.getAttribute(node.getId().toString(), "LINES"));
		if (!lines.contains(line))
			lines.add(line);
		nodeAttributes.putAttribute(node.getId().toString(), "LINES", lines.toString());
		lines = Utils.parseToList((String) nodeAttributes.getAttribute(node.getId().toString(), in_out + "_LINES"));
		if (!lines.contains(line + "-" + route))
			lines.add(line + "-" + route);
		nodeAttributes.putAttribute(node.getId().toString(), in_out + "_LINES", lines.toString());
	}

}