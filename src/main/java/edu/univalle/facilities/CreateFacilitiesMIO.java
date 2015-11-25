package edu.univalle.facilities;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;
import org.matsim.facilities.ActivityOptionImpl;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.facilities.OpeningTimeImpl;
import org.opengis.feature.simple.SimpleFeature;

public class CreateFacilitiesMIO {
	
	private final static Logger log = Logger.getLogger(CreateFacilitiesMIO.class);
	private Scenario scenario;
	private String estacionesShapeFile = "input/shapes/stations/mio_estaciones.shp";

	public static void main(String[] args) {
		CreateFacilitiesMIO facilitiesCreator = new CreateFacilitiesMIO();
		facilitiesCreator.init();
		facilitiesCreator.run();
		facilitiesCreator.write();
		log.info("Creation finished #################################");
	}
	
	private void init() {
		/*
		 * Create the scenario
		 */
		Config config = ConfigUtils.createConfig();
		this.scenario = ScenarioUtils.createScenario(config);	
	}
	
	private void run() {
		/*
		 * Read the shape file and create the facilities.
		 */
		this.readShapeFile();
		
	}
	
	public void write() {
		new FacilitiesWriter(((ScenarioImpl) this.scenario).getActivityFacilities()).write("./output/facilities.xml.gz");
	}
		
	private void readShapeFile() {
		int cnt = 0;
		ShapeFileReader shapeFileReader = new ShapeFileReader();
		shapeFileReader.readFileAndInitialize(estacionesShapeFile);
		Collection<SimpleFeature> features = shapeFileReader.getFeatureSet();
		
		Iterator<SimpleFeature> it = features.iterator();
		SimpleFeature ft = null;
		// Convert from GPS coordinate system to "Bogota 1975 / Colombia West Zone"
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:21896");
		log.info("Name of stations:");
		while (it.hasNext()) {
			ft = it.next();
			Object nombre = ft.getAttribute("ESTACION");
			System.out.println(nombre);
			Coord coord = CoordUtils.createCoord(Double.parseDouble(ft.getAttribute("GPS_X").toString()), Double.parseDouble(ft.getAttribute("GPS_Y").toString()));
			ActivityFacilityImpl facility = (ActivityFacilityImpl)this.scenario.getActivityFacilities().getFactory().createActivityFacility(Id.create(cnt, ActivityFacility.class), ct.transform(coord));
			facility.setDesc(nombre.toString());
			// make the activity in facility same as name
//			this.addActivityOption(facility, nombre.toString());
			this.addActivityOption(facility, "pt");
			this.scenario.getActivityFacilities().addActivityFacility(facility);

			cnt++;
		}
	}
	
	private void addActivityOption(ActivityFacility facility, String type) {
		((ActivityFacilityImpl) facility).createActivityOption(type);
		
		/*
		 * Specify the opening hours here for pt (public transport).
		 */
		ActivityOptionImpl actOption = (ActivityOptionImpl)facility.getActivityOptions().get(type);
		OpeningTimeImpl opentime = null;
		if (type.equals("pt")) {
			opentime = new OpeningTimeImpl(4.5 * 3600.0, 23.0 * 3600);
		}
		// make the activity in facility same as name
//		else {
//			opentime = new OpeningTimeImpl(4.5 * 3600.0, 23.0 * 3600);
//		}
		actOption.addOpeningTime(opentime);
	}

}
