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

import edu.univalle.utils.CsvReader;;

public class GeneratePopulation_final
{

    public static void main(String[] args) {
        Config config = ConfigUtils.createConfig();
        Scenario sc = ScenarioUtils.createScenario(config);
        Network network = sc.getNetwork();
        Population population = sc.getPopulation();
        PopulationFactory populationFactory = population.getFactory();
        CsvReader matriz_usos;
        try {
            String st_ID_PAS;
            String st_HORA;
            String st_O_COORD_X;
            String st_O_COORD_Y;
            String st_D_COORD_X;
            String st_D_COORD_Y;
            String st_START;
            String st_END;
            String producto;
            int counter_uv = 0;

            matriz_usos = new CsvReader("C:/MATSim/workspace_mars/dta-brt/input/usos_xls/csv_closed/normal_final.csv");
            System.out.println("### extracting data from " + matriz_usos.toString() + " ###");
            matriz_usos.readHeaders();

            Person person;
            Plan plan = null;
            while (matriz_usos.readRecord())

            {
                // ID_PAS ID_ESTACION N_ESTACION HORA COORD_X COORD_Y

                st_ID_PAS = matriz_usos.get("CRD_SNR");
                st_HORA = matriz_usos.get("HORA_MATSIM");
                st_O_COORD_X = matriz_usos.get("O_X_COORD");
                st_O_COORD_Y = matriz_usos.get("O_Y_COORD");
                st_D_COORD_X = matriz_usos.get("D_X_COORD");
                st_D_COORD_Y = matriz_usos.get("D_Y_COORD");
                st_START = matriz_usos.get("START");
                st_END = matriz_usos.get("END");
                producto = matriz_usos.get("PRODUCTO");

                if (producto.equals("UNIVIAJE-II")) {
                    counter_uv++;
                    st_ID_PAS = "UV-II_" + Integer.toString(counter_uv) + "-" + st_ID_PAS;

                }

                Coord homeCoordinates = CoordUtils.createCoord(Double.parseDouble(st_O_COORD_X),
                        Double.parseDouble(st_O_COORD_Y));

                // For single trip
                if (st_START.equals("YES") && (st_END.equals("YES")) || (producto.equals("UNIVIAJE-II"))) {

                    person = populationFactory.createPerson(Id.create(st_ID_PAS, Person.class));
                    population.addPerson(person);
                    plan = populationFactory.createPlan();
                    person.addPlan(plan);

                    Activity activity1 = populationFactory.createActivityFromCoord("h", homeCoordinates);
                    activity1.setEndTime(Double.parseDouble(st_HORA));
                    plan.addActivity(activity1);
                    plan.addLeg(populationFactory.createLeg("pt"));

                    Coord homeCoordinates2 = CoordUtils.createCoord(Double.parseDouble(st_D_COORD_X),
                            Double.parseDouble(st_D_COORD_Y));
                    Activity activity2 = populationFactory.createActivityFromCoord("h", homeCoordinates2);
                    plan.addActivity(activity2);

                }

                else {

                    if (st_START.equals("YES")) {

                        person = populationFactory.createPerson(Id.create(st_ID_PAS, Person.class));
                        population.addPerson(person);
                        plan = populationFactory.createPlan();
                        person.addPlan(plan);

                        Activity activity1 = populationFactory.createActivityFromCoord("h", homeCoordinates);
                        activity1.setEndTime(Double.parseDouble(st_HORA));
                        plan.addActivity(activity1);
                        plan.addLeg(populationFactory.createLeg("pt"));

                    }

                    if ((st_END.equals("YES"))) {

                        Activity activity1 = populationFactory.createActivityFromCoord("w", homeCoordinates);
                        activity1.setEndTime(Double.parseDouble(st_HORA));
                        plan.addActivity(activity1);
                        plan.addLeg(populationFactory.createLeg("pt"));

                        Coord homeCoordinates2 = CoordUtils.createCoord(Double.parseDouble(st_D_COORD_X),
                                Double.parseDouble(st_D_COORD_Y));
                        Activity activity2 = populationFactory.createActivityFromCoord("h", homeCoordinates2);
                        plan.addActivity(activity2);

                    }

                    else if (st_START.equals("0")) {

                        Activity activity1 = populationFactory.createActivityFromCoord("w", homeCoordinates);
                        activity1.setEndTime(Double.parseDouble(st_HORA));
                        plan.addActivity(activity1);
                        plan.addLeg(populationFactory.createLeg("pt"));
                    }
                }

            }

            MatsimWriter popWriter = new PopulationWriter(population, network);
            popWriter.write("./input/plans_sitm_cali_01.04.2014_40porc_final.xml");

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

}
