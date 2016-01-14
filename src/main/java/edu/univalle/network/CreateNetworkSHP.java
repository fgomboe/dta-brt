package edu.univalle.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;

import edu.univalle.utils.Utils;

public class CreateNetworkSHP
{

    public static void main(String[] args) {

        String configFile = "input/config.xml";
        Config config = ConfigUtils.loadConfig(configFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();

        ObjectAttributes linkAttributes = new ObjectAttributes();
        ObjectAttributes nodeAttributes = new ObjectAttributes();
        new ObjectAttributesXmlReader(linkAttributes).parse("./input/specialLinkAttributes.xml");
        new ObjectAttributesXmlReader(nodeAttributes).parse("./input/specialNodeAttributes.xml");

        CoordinateReferenceSystem crs = MGC.getCRS("EPSG:3115");		// EPSG Code for MAGNA-SIRGAS / Colombia West Zone coordinate system

        Collection<SimpleFeature> features = new ArrayList<SimpleFeature>();
        PointFeatureFactory nodeFactory = new PointFeatureFactory.Builder().setCrs(crs).setName("nodes")
                .addAttribute("id", Integer.class).addAttribute("longName", String.class)
                .addAttribute("gpsX", Double.class).addAttribute("gpsY", Double.class)
                .addAttribute("lines", String.class).addAttribute("inLines", String.class)
                .addAttribute("outLines", String.class).create();

        for (Node node : network.getNodes().values()) {
            if (node.getId().toString().length() <= 2) {
                int id = Integer.parseInt(node.getId().toString());
                String longName = (String) nodeAttributes.getAttribute(node.getId().toString(), "ESTACION");
                Double gpsX = (Double) nodeAttributes.getAttribute(node.getId().toString(), "GPS_X");
                Double gpsY = (Double) nodeAttributes.getAttribute(node.getId().toString(), "GPS_Y");
                String lines = (String) nodeAttributes.getAttribute(node.getId().toString(), "LINES");
                String inLines = (String) nodeAttributes.getAttribute(node.getId().toString(), "IN_LINES");
                String outLines = (String) nodeAttributes.getAttribute(node.getId().toString(), "OUT_LINES");
                SimpleFeature ft = nodeFactory.createPoint(node.getCoord(),
                        new Object[] { id, longName, gpsX, gpsY,
                                lines != null ? lines.substring(1, lines.length() - 1) : lines,
                                inLines != null ? inLines.substring(1, inLines.length() - 1) : inLines,
                                outLines != null ? outLines.substring(1, outLines.length() - 1) : outLines },
                        null);
                // Comment the following 3 lines to export entire network
                List<String> linesArray = new ArrayList<String>();
                linesArray = Utils.parseToList(lines);
                if (linesArray.contains("E21") || linesArray.contains("E31") || linesArray.contains("T31"))
                    features.add(ft);
            }
        }
        ShapeFileWriter.writeGeometries(features,
                "input/shapes/Cali_special_generated_trunk_network/network_nodes.shp");

        features = new ArrayList<SimpleFeature>();
        PolylineFeatureFactory linkFactory = new PolylineFeatureFactory.Builder().setCrs(crs).setName("link")
                .addAttribute("id", Integer.class).addAttribute("name", String.class).
                // addAttribute("fromNode", String.class).
                addAttribute("fromNode", Integer.class).
                // addAttribute("toNode", String.class).
                addAttribute("toNode", Integer.class).addAttribute("length", Double.class)
                .addAttribute("freespeed", Double.class).addAttribute("capacity", Double.class)
                .addAttribute("noOfLanes", Integer.class).addAttribute("lines", String.class)
                .addAttribute("line_route", String.class).create();

        int id = 1;
        for (Link link : network.getLinks().values()) {
            String fromNode = link.getFromNode().getId().toString();
            String toNode = link.getToNode().getId().toString();
            if (fromNode.length() <= 2 && toNode.length() <= 2) {
                Coordinate fromNodeCoordinate = new Coordinate(link.getFromNode().getCoord().getX(),
                        link.getFromNode().getCoord().getY());
                Coordinate toNodeCoordinate = new Coordinate(link.getToNode().getCoord().getX(),
                        link.getToNode().getCoord().getY());
                Coordinate linkCoordinate = new Coordinate(link.getCoord().getX(), link.getCoord().getY());
                String linesAtt = (String) linkAttributes.getAttribute(link.getId().toString(), "LINES");
                String linesRoutesAtt = (String) linkAttributes.getAttribute(link.getId().toString(), "LINE_ROUTE");
                SimpleFeature ft = linkFactory.createPolyline(
                        new Coordinate[] { fromNodeCoordinate, linkCoordinate, toNodeCoordinate },
                        // new Object [] {id, link.getId().toString(), link.getFromNode().getId().toString(), link.getToNode().getId().toString(), link.getLength(), link.getFreespeed(), link.getCapacity(), link.getNumberOfLanes(), linesAtt.substring(1, linesAtt.length() - 1), linesRoutesAtt.substring(1, linesRoutesAtt.length() - 1)}, null);
                        new Object[] { id, link.getId().toString(), fromNode, toNode, link.getLength(),
                                link.getFreespeed(), link.getCapacity(), link.getNumberOfLanes(),
                                linesAtt.substring(1, linesAtt.length() - 1),
                                linesRoutesAtt.substring(1, linesRoutesAtt.length() - 1) },
                        null);
                // Comment the following 3 lines to export entire network
                List<String> linesArray = new ArrayList<String>();
                linesArray = Utils.parseToList(linesAtt);
                if (linesArray.contains("E21") || linesArray.contains("E31") || linesArray.contains("T31"))
                    features.add(ft);
                id++;
            }
        }
        ShapeFileWriter.writeGeometries(features,
                "input/shapes/Cali_special_generated_trunk_network/network_links.shp");
    }
}