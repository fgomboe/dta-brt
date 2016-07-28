package edu.univalle.population;

import java.util.HashMap;
import java.util.Map;

public class Cal_parameters
{

    public Map<Integer, String> setCal_param_name() {
        Map<Integer, String> name_param = new HashMap<Integer, String>();
        name_param.put(1, "routingAlgorithmType");
        name_param.put(2, "marginalUtilityOfDistance_util_m");
        name_param.put(3, "marginalUtilityOfTraveling_util_hr");
        name_param.put(4, "marginalUtilityOfDistance_util_m");
        name_param.put(5, "marginalUtilityOfTraveling_util_hr");
        name_param.put(6, "marginalUtilityOfDistance_util_m");
        name_param.put(7, "marginalUtilityOfTraveling_util_hr");
        name_param.put(8, "marginalUtilityOfDistance_util_m");
        name_param.put(9, "marginalUtilityOfTraveling_util_hr");
        name_param.put(10, "beelineDistanceFactor");
        name_param.put(11, "beelineDistanceFactor");
        name_param.put(12, "beelineDistanceFactor");
        name_param.put(13, "beelineDistanceFactor");
        name_param.put(14, "linkDynamics");
        name_param.put(15, "removeStuckVehicles");
        name_param.put(16, "stuckTime");
        name_param.put(17, "trafficDynamics");
        name_param.put(18, "vehicleBehavior");
        name_param.put(19, "additionalTransferTime");
        name_param.put(20, "maxBeelineWalkConnectionDistance");
        name_param.put(21, "searchRadius");
        name_param.put(22, "extensionRadius");
        name_param.put(23, "travelTimeAggregator");
        name_param.put(24, "travelTimeBinSize");
        name_param.put(25, "travelTimeGetter");
        return name_param;

    }

    public Map<Integer, Object> setCal_param_value() {
        Map<Integer, Object> val_param = new HashMap<Integer, Object>();
        val_param.put(1, 1);
        val_param.put(2, 0);
        val_param.put(3, 0);
        val_param.put(4, 0);
        val_param.put(5, 0);
        val_param.put(6, 0);
        val_param.put(7, -6);
        val_param.put(8, 0);
        val_param.put(9, -6);
        val_param.put(10, 1.3);
        val_param.put(11, 1.3);
        val_param.put(12, 1.3);
        val_param.put(13, 1.3);
        val_param.put(14, 2);
        val_param.put(15, 2);
        val_param.put(16, 10);
        val_param.put(17, 2);
        val_param.put(18, 2);
        val_param.put(19, 0);
        val_param.put(20, 100);
        val_param.put(21, 200);
        val_param.put(22, 100);
        val_param.put(23, 1);
        val_param.put(24, 900);
        val_param.put(25, 1);
        return val_param;

    }

}
