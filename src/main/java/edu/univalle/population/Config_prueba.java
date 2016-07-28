package edu.univalle.population;

import java.io.File;
import java.util.Map;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

public class Config_prueba
{

    public static void generate_config() {
        Config config1 = ConfigUtils.createConfig();
        String input_dir = "./input";
        String output_dir = "./output";
        ConfigWriter prueba1 = new HeadXml(config1, input_dir, output_dir);

        // Set individual fixed parameters

        config1.setParam("global", "coordinateSystem", "EPSG:3115");

        config1.setParam("network", "inputNetworkFile", "./input/specialNetwork.xml");

        config1.setParam("plans", "inputPlansFile", "./input/plans_sitm_cali_01.04.2014_40porc_final_dummy.xml");

        config1.setParam("transit", "useTransit", "true");
        config1.setParam("transit", "transitScheduleFile", "./input/transitSchedule_time_MAX_VEHICLES.xml");
        config1.setParam("transit", "vehiclesFile", "./input/transitVehicles_40porc_MAX_VEHICLES.xml");

        config1.setParam("qsim", "startTime", "00:00:00");
        config1.setParam("qsim", "endTime", "28:00:00");

        config1.setParam("planCalcScore", "BrainExpBeta", "2.0");
        config1.setParam("planCalcScore", "lateArrival", "0.0");
        config1.setParam("planCalcScore", "earlyDeparture", "0.0");
        config1.setParam("planCalcScore", "performing", "6.0");
        config1.setParam("planCalcScore", "traveling", "0.0");
        config1.setParam("planCalcScore", "waiting", "0.0");
        config1.setParam("planCalcScore", "travelingPt", "0.0");
        config1.setParam("planCalcScore", "waitingPt", "-2.0");
        config1.setParam("planCalcScore", "traveling_walk", "-6.0");
        config1.setParam("planCalcScore", "utilityOfLineSwitch", "-1.0");
        config1.setParam("planCalcScore", "traveling_walk", "-6");
        config1.setParam("planCalcScore", "activityType_0", "dummy");
        config1.setParam("planCalcScore", "activityTypicalDuration_0", "12:00:00");
        config1.setParam("planCalcScore", "writeExperiencedPlans", "true");

        config1.setParam("strategy", "ModuleProbability_1", "0.8");
        config1.setParam("strategy", "Module_1", "ChangeExpBeta");
        config1.setParam("strategy", "ModuleProbability_2", "0.1");
        config1.setParam("strategy", "Module_2", "ReRoute");
        config1.setParam("strategy", "ModuleProbability_3", "0.1");
        config1.setParam("strategy", "Module_3", "TimeAllocationMutator");

        config1.setParam("linkStats", "averageLinkStatsOverIterations", "1");
        config1.setParam("linkStats", "writeLinkStatsInterval", "1");

        config1.setParam("controler", "outputDirectory", "./output");
        config1.setParam("controler", "firstIteration", "0");
        config1.setParam("controler", "lastIteration", "0");
        config1.setParam("controler", "routingAlgorithmType", "Dijkstra");
        config1.setParam("controler", "writePlansInterval", "1");
        config1.setParam("controler", "writeEventsInterval", "10");

        config1.setParam("TimeAllocationMutator", "mutationRange", "7200");

        config1.setParam("vspExperimental", "isGeneratingBoardingDeniedEvent", "true");
        config1.setParam("vspExperimental", "writingOutputEvents", "true");

        Map<Integer, String> param_name = new Cal_parameters().setCal_param_name();
        Map<Integer, Object> param_value = new Cal_parameters().setCal_param_value();
        Map<String, ModeRoutingParams> Routing_Params = config1.plansCalcRoute().getModeRoutingParams();

        // Set PSO parameters

        String value;
        switch ((Integer) param_value.get(1)) {

        case 1:
            value = "Dijkstra";
        break;
        case 2:
            value = "FastDijkstra";
        break;
        case 3:
            value = "AStarLandmarks";
        break;
        case 4:
            value = "FastAStarLandmarks";
        break;
        default:
            value = "Dijkstra";
        break;
        }

        config1.setParam("controler", param_name.get(1).toString(), value);
        config1.planCalcScore().getModes().get("car").setMarginalUtilityOfDistance((Integer) param_value.get(2));
        config1.planCalcScore().getModes().get("car").setMarginalUtilityOfTraveling((Integer) param_value.get(3));
        config1.planCalcScore().getModes().get("pt").setMarginalUtilityOfDistance((Integer) param_value.get(4));
        config1.planCalcScore().getModes().get("pt").setMarginalUtilityOfTraveling((Integer) param_value.get(5));
        config1.planCalcScore().getModes().get("walk").setMarginalUtilityOfDistance((Integer) param_value.get(6));
        config1.planCalcScore().getModes().get("walk").setMarginalUtilityOfTraveling((Integer) param_value.get(7));
        config1.planCalcScore().getModes().get("other").setMarginalUtilityOfDistance((Integer) param_value.get(8));
        config1.planCalcScore().getModes().get("other").setMarginalUtilityOfTraveling((Integer) param_value.get(9));
        // Routing_Params.get("car").setBeelineDistanceFactor((Double) param_value.get(10));
        Routing_Params.get("pt").setBeelineDistanceFactor((Double) param_value.get(11));
        Routing_Params.get("walk").setBeelineDistanceFactor((Double) param_value.get(12));
        Routing_Params.get("undefined").setBeelineDistanceFactor((Double) param_value.get(13));

        switch ((Integer) param_value.get(14)) {
        case 1:
            value = "FIFO";
        break;
        case 2:
            value = "PassingQ";
        break;
        default:
            value = "PassingQ";
        break;
        }
        config1.setParam("qsim", param_name.get(14), value);

        switch ((Integer) param_value.get(15)) {
        case 1:
            value = "true";
        break;
        case 2:
            value = "false";
        break;
        default:
            value = "false";
        break;
        }
        config1.setParam("qsim", param_name.get(15), value);
        config1.setParam("qsim", param_name.get(16), param_value.get(16).toString());

        switch ((Integer) param_value.get(17)) {
        case 1:
            value = "null";
        break;
        case 2:
            value = "queue";
        break;
        case 3:
            value = "withHoles";
        break;
        default:
            value = "queue";
        break;
        }
        config1.setParam("qsim", param_name.get(17), value);

        switch ((Integer) param_value.get(18)) {

        case 1:
            value = "null";
        break;
        case 2:
            value = "teleport";
        break;
        case 3:
            value = "wait";
        break;
        case 4:
            value = "exception";
        break;
        default:
            value = "teleport";
        break;
        }

        config1.setParam("qsim", param_name.get(18), value);
        config1.setParam("transitRouter", param_name.get(19), param_value.get(19).toString());
        config1.setParam("transitRouter", param_name.get(20), param_value.get(20).toString());
        config1.setParam("transitRouter", param_name.get(21), param_value.get(21).toString());
        config1.setParam("transitRouter", param_name.get(22), param_value.get(22).toString());
        switch ((Integer) param_value.get(23)) {
        case 1:
            value = "optimistic";
        break;
        case 2:
            value = "experimental_LastMile";
        break;
        default:
            value = "optimistic";
        break;
        }
        config1.setParam("travelTimeCalculator", param_name.get(23), value);
        config1.setParam("travelTimeCalculator", param_name.get(24), param_value.get(24).toString());
        switch ((Integer) param_value.get(25)) {
        case 1:
            value = "average";
        break;
        case 2:
            value = "linearinterpolation";
        break;
        default:
            value = "average";
        break;
        }
        config1.setParam("travelTimeCalculator", param_name.get(25), value);
        /**/
        prueba1.writeFileV2("config_param_v3.xml");
        // prueba1.writeFileV1("config_param_v1.xml");

        try {

            File source_file = new File("config_param_v3.xml");
            File target_file = new File("./input/" + source_file.getName());

            if (target_file.exists())
                target_file.delete();
            source_file.renameTo(target_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Config config2 = ConfigUtils.loadConfig("./input/config_param_v3.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config2);

        Controler controler = new Controler(scenario);
        controler.run();
        System.out.println("lo que sea");

    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        generate_config();

    }

}
