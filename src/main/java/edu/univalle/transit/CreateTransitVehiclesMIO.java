package edu.univalle.transit;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkReaderMatsimV1;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.*;
import org.matsim.vehicles.VehicleType.DoorOperationMode;

import edu.univalle.utils.CsvReader;


public class CreateTransitVehiclesMIO {
	
	private String flotaFile = "./input/networkMIO/flota.csv";

	private Vehicles vehiculos;
	private Scenario scenario;
	
	private String networkFile = "./input/specialNetwork.xml";
	
	public static void main(String[] args) {

		CreateTransitVehiclesMIO creatVehicles = new CreateTransitVehiclesMIO();
		creatVehicles.init();
		creatVehicles.createVechiclesTypes();
		creatVehicles.write();

	}

	public void init() {
		this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new NetworkReaderMatsimV1(this.scenario).parse(networkFile);
		scenario.getNetwork();
		this.scenario.getConfig().scenario().setUseTransit(true);
		this.scenario.getConfig().scenario().setUseVehicles(true);
		vehiculos = this.scenario.getVehicles();

	}

	public void write() {
		new VehicleWriterV1(vehiculos).writeFile("./output/transitVehicles.xml");
	}

	public void createVechiclesTypes() {

		VehicleCapacity cap_articulados = new VehicleCapacityImpl();
		cap_articulados.setSeats(48);
		cap_articulados.setStandingRoom(112);

		VehicleType articulado = vehiculos.getFactory().createVehicleType(Id.create("art", VehicleType.class));
		articulado.setLength(18);
		articulado.setWidth(2.60);
		articulado.setDescription("Autobús articulado de dos vagones");
		articulado.setDoorOperationMode(DoorOperationMode.parallel);
		articulado.setCapacity(cap_articulados);
		vehiculos.addVehicleType(articulado);


		try{

			CsvReader flota = new CsvReader(flotaFile);
			System.out.println("### extracting data from "+flota.toString()+" ###");
			flota.readHeaders();

			while (flota.readRecord())

			{
				String ruta = flota.get("Ruta");
				String flota_a = flota.get("Flota");
				int flota_disp = Integer.parseInt(flota_a);
				for(int i = 1; i <= flota_disp; ++i){
					org.matsim.vehicles.Vehicle bus= vehiculos.getFactory().createVehicle(Id.create(ruta+"_"+i, org.matsim.vehicles.Vehicle.class), articulado);
					vehiculos.addVehicle(bus);
				}
			}
			flota.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}


