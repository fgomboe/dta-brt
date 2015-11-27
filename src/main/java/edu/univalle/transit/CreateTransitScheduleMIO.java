package edu.univalle.transit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkReaderMatsimV1;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleWriterV1;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

import edu.univalle.utils.CsvReader;

public class CreateTransitScheduleMIO
{

	private TransitSchedule schedule;
	private Scenario scenario;
	private Network network;
	
	private String networkFile = "./input/specialNetwork.xml";
	private String nodeAttributesFile = "./input/specialNodeAttributes.xml";
	
	private String csvArcs = "./input/networkMIO/arcs.csv";

	private ObjectAttributes nodeAttributes = new ObjectAttributes();

	public static void main(String[] args)
	{

		CreateTransitScheduleMIO createTransit = new CreateTransitScheduleMIO();
		createTransit.init();
		createTransit.createSchedule();
		createTransit.write();
	}

	public void init()
	{
		this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new NetworkReaderMatsimV1(this.scenario).parse(networkFile);
		this.network = scenario.getNetwork();
		//this.scenario.getConfig().scenario().setUseTransit(true);
		//this.scenario.getConfig().scenario().setUseVehicles(true);
		schedule = this.scenario.getTransitSchedule();
	}

	public void write()
	{
		new TransitScheduleWriterV1(schedule).write("./output/transitSchedule.xml");
	}

	public void createSchedule()
	{

		new ObjectAttributesXmlReader(nodeAttributes).parse(nodeAttributesFile);

		Id<Link> startLink = null;
		Id<Link> endLink = null;
		List<Id<Link>> links = new ArrayList<Id<Link>>();

		List<TransitRouteStop> stops = new ArrayList<TransitRouteStop>();

		String currentLine = "", currentDirection = "";

		TransitStopFacility tStopFacil = null;
		TransitRouteStop tRouteStop = null;
		NetworkRoute nRoute = null;
		TransitRoute tRoute = null;
		TransitLine tLine = null;

		try {
			CsvReader arcos = new CsvReader(csvArcs);
			arcos.readHeaders();

			String line, direction;

			while (arcos.readRecord()) {
				line = arcos.get("LINEA");
				direction = arcos.get("SENTIDO");

				// The last condition is an ad hoc solution for the problem of the last route for the T31
				if (line.equals("T31") || line.equals("E31") || line.equals("E21") || currentLine.equals("T31")) {

					Node iniNode = network.getNodes().get(Id.createNodeId(arcos.get("PUNTO_INI")));
					Node endNode = network.getNodes().get(Id.createNodeId(arcos.get("PUNTO_FIN")));

					String iniStation = nodeAttributes.getAttribute(iniNode.getId().toString(), "ASSOCIATE_ID").toString();
					String endStation = nodeAttributes.getAttribute(endNode.getId().toString(), "ASSOCIATE_ID").toString();

					if (!direction.equals(currentDirection)) {
						if (currentDirection != "") {
							// because links contains also the endLink, otherwise it would be repeated
							links.remove(links.size() - 1);
							nRoute = new LinkNetworkRouteImpl(startLink, links, endLink);
							tRoute = schedule.getFactory().createTransitRoute(Id.create(currentDirection, TransitRoute.class), nRoute, stops, "bus");
							tLine.addRoute(tRoute);
							startLink = null;
							links.clear();
							endLink = null;
							stops.clear();
						}

						currentDirection = direction;

						startLink = Id.createLinkId(iniStation + "-" + iniNode.getId().toString());

						tStopFacil = createTransitStopFacility(iniNode, startLink);
						// ad hoc solution, mentioned above
						if (!schedule.getFacilities().containsKey(tStopFacil.getId()) && !line.equals("T40"))
							schedule.addStopFacility(tStopFacil);
						tRouteStop = schedule.getFactory().createTransitRouteStop(tStopFacil, 0, 0);
						tRouteStop.setAwaitDepartureTime(true);
						stops.add(tRouteStop);

						links.add(Id.createLinkId(iniNode.getId().toString() + "-" + iniStation));
					}
					if (!line.equals(currentLine)) {
						if (currentLine != "")
							schedule.addTransitLine(tLine);

						tLine = schedule.getFactory().createTransitLine(Id.create(line, TransitLine.class));

						currentLine = line;
					}

					links.add(Id.createLinkId(iniStation + "-" + endStation));
					links.add(Id.createLinkId(endStation + "-" + endNode.getId().toString()));

					tStopFacil = createTransitStopFacility(endNode, Id.createLinkId(endStation + "-" + endNode.getId().toString()));
					// ad hoc solution, mentioned above
					if (!schedule.getFacilities().containsKey(tStopFacil.getId()) && !line.equals("T40"))
						schedule.addStopFacility(tStopFacil);
					tRouteStop = schedule.getFactory().createTransitRouteStop(tStopFacil, 0, 0);
					tRouteStop.setAwaitDepartureTime(true);
					stops.add(tRouteStop);

					links.add(Id.createLinkId(endNode.getId().toString() + "-" + endStation));
					endLink = Id.createLinkId(endNode.getId().toString() + "-" + endStation);
				}

			}
			arcos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create departures
		try {

			String lineas[] = { "T31", "E31", "E21" };
			for (String linea : lineas) {
				CsvReader salidas = new CsvReader("./input/networkMIO/" + linea + ".csv");
				System.out.println("### extracting data from " + salidas.toString() + " ###");
				salidas.readHeaders();
				int id_dep = 1;

				while (salidas.readRecord())

				{
					Id<TransitLine> lineId = Id.create(linea, TransitLine.class);

					Departure ida = schedule.getFactory().createDeparture(Id.create(id_dep, Departure.class), Double.parseDouble(salidas.get("Hora_mat")));
					ida.setVehicleId(Id.create(salidas.get("Veh_ida"), org.matsim.vehicles.Vehicle.class));
					schedule.getTransitLines().get(lineId).getRoutes().get(Id.create("Ida", TransitRoute.class)).addDeparture(ida);

					Departure regreso = schedule.getFactory().createDeparture(Id.create(id_dep, Departure.class), Double.parseDouble(salidas.get("Hora_mat")));
					regreso.setVehicleId(Id.create(salidas.get("Veh_regreso"), org.matsim.vehicles.Vehicle.class));
					schedule.getTransitLines().get(lineId).getRoutes().get(Id.create("Vuelta", TransitRoute.class)).addDeparture(regreso);

					id_dep++;
				}
				salidas.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TransitStopFacility createTransitStopFacility(Node node, Id<Link> linkId)
	{
		TransitStopFacility tStopFacil = schedule.getFactory().createTransitStopFacility(Id.create(node.getId(), TransitStopFacility.class), node.getCoord(), true);
		tStopFacil.setLinkId(linkId);
		tStopFacil.setName((String) nodeAttributes.getAttribute(node.getId().toString(), "LONGNAME"));

		return tStopFacil;
	}
}