package edu.univalle.population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

public class POnePersonPopulationGenerator {

	public static void main(String[] args) {
		Config config = ConfigUtils.createConfig();
		Scenario sc = ScenarioUtils.createScenario(config);
		
		Network network = sc.getNetwork();
		Population population = sc.getPopulation();
		PopulationFactory populationFactory = population.getFactory();
		
		Person person = populationFactory.createPerson(Id.createPersonId(0));
		population.addPerson(person);
		
		Plan plan = populationFactory.createPlan();
		person.addPlan(plan);
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(
				TransformationFactory.WGS84, "EPSG:21896");
		
		Coord homeCoordinates = CoordUtils.createCoord(-76.517787, 3.370083);
		Activity activity1 = 
				populationFactory.createActivityFromCoord(
					"home", ct.transform(homeCoordinates)
				);
		activity1.setEndTime(25200);
		plan.addActivity(activity1);
		plan.addLeg(populationFactory.createLeg("car"));
		
		Activity activity2 = 
				populationFactory.createActivityFromCoord(
					"work", ct.transform(CoordUtils.createCoord(-76.53295,3.37419))
				);
		activity2.setEndTime(59200);
		plan.addActivity(activity2);
		plan.addLeg(populationFactory.createLeg("car"));
		
		Activity activity3 =
				populationFactory.createActivityFromCoord(
					"home", ct.transform(homeCoordinates));
		plan.addActivity(activity3);
		
		MatsimWriter popWriter = new PopulationWriter(population, network);
		popWriter.write("./output/plans.HandMade.xml.gz");

	}

}
