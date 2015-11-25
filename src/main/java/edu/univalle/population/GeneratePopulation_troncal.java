package edu.univalle.population;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import edu.univalle.brt.utils.CsvReader;;

public class GeneratePopulation_troncal {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Config config = ConfigUtils.createConfig();
		Scenario sc = ScenarioUtils.createScenario(config);
		Network network = sc.getNetwork();
		Population population = sc.getPopulation();   
		PopulationFactory populationFactory = population.getFactory();
		CsvReader matriz_usos;
		CsvReader estaciones;
		try {
			String first_COORD_X= null;
			String first_COORD_Y = null;
			String st_ID_PAS;
			String st_ID_ESTACION;
			String st_N_ESTACION;
			String st_HORA;
			String st_COORD_X;
			String st_COORD_Y;
			String st_START;
			String st_END;
			String troncal;
			String estacion_matriz;

			matriz_usos = new CsvReader("data_filtered.csv");
			System.out.println("### extracting data from "+matriz_usos.toString()+" ###");
			matriz_usos.readHeaders();

			Person person;
			Plan plan = null; 
			while (matriz_usos.readRecord())

			{
				//	ID_PAS	ID_ESTACION	N_ESTACION	HORA	COORD_X	COORD_Y
				st_ID_PAS=matriz_usos.get("ID_PAS");
				st_ID_ESTACION=matriz_usos.get("ID_ESTACION");
				st_N_ESTACION=matriz_usos.get("N_ESTACION");
				st_HORA=matriz_usos.get("HORA");
				st_COORD_X=matriz_usos.get("COORD_X");
				st_COORD_Y=matriz_usos.get("COORD_Y");
				st_START=matriz_usos.get("START");
				st_END=matriz_usos.get("END");

				Coord homeCoordinates = CoordUtils.createCoord(Double.parseDouble(st_COORD_X), Double.parseDouble(st_COORD_Y));


				try {
					estaciones = new CsvReader("input/estaciones_matrizUsos2.csv");
					System.out.println("### extracting data from "+estaciones.toString()+" ###");

					estaciones.readHeaders();

					while (estaciones.readRecord())

					{
						troncal=estaciones.get("TRONCAL");
						System.out.println("troncal="+troncal);
						estacion_matriz=estaciones.get("MATRIZ USOS");
						
						System.out.println("estacion="+st_N_ESTACION+" usos="+estacion_matriz);
						
						if(st_N_ESTACION==estacion_matriz && troncal=="YES"){
							if(st_START.equals("YES")){

								person = populationFactory.createPerson(Id.create(st_ID_PAS, Person.class));
								population.addPerson(person);
								plan = populationFactory.createPlan();
								person.addPlan(plan);

								first_COORD_X=st_COORD_X;
								first_COORD_Y=st_COORD_Y;

								Activity activity1 = populationFactory.createActivityFromCoord("h",homeCoordinates);
								activity1.setEndTime(Double.parseDouble(st_HORA));
								plan.addActivity(activity1);
								plan.addLeg(populationFactory.createLeg("pt"));

							}
							else{

								if((st_END.equals("YES"))){

									Activity activity1 = populationFactory.createActivityFromCoord("w",homeCoordinates);
									activity1.setEndTime(Double.parseDouble(st_HORA));
									plan.addActivity(activity1);
									plan.addLeg(populationFactory.createLeg("pt"));

									Coord homeCoordinates2 = CoordUtils.createCoord(Double.parseDouble(first_COORD_X), Double.parseDouble(first_COORD_Y));
									Activity activity2 = populationFactory.createActivityFromCoord("h",homeCoordinates2);
									plan.addActivity(activity2);

								}

								else{

									Activity activity1 = populationFactory.createActivityFromCoord("w",homeCoordinates);
									activity1.setEndTime(Double.parseDouble(st_HORA));
									plan.addActivity(activity1);
									plan.addLeg(populationFactory.createLeg("pt"));
								}
							}
							
							break;

						}
					}
					
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}




			}

			MatsimWriter popWriter = new PopulationWriter(population, network);
			popWriter.write("./input/plans_sitm_cali_01.04.2014_troncal.xml");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

}
