import org.matsim.core.config.ConfigWriter;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
javaaddpath C:\MATSim\workspace_mars\dta-brt\target\classes\edu\univalle\
import edu.univalle.population.*;
javaaddpath C:\Users\UV\git\matsim\matsim\target\classes\

o=Config_prueba;
javaMethod('hacer', o);
%%
config1 = ConfigUtils.createConfig();
prueba1 = ConfigWriter(config1);
config1.setParam('TimeAllocationMutator', 'mutationRange', '15');
config1.setParam('network', 'inputNetworkFile', 'C:\MATSim\workspace_mars\dta-brt\input\specialNetwork.xml');
config1.setParam('plans', 'inputPlansFile', 'C:\MATSim\workspace_mars\dta-brt\input\plans_sitm_cali_01.04.2014_troncal.xml');
config1.setParam('transit', 'transitScheduleFile', 'C:\MATSim\workspace_mars\dta-brt\input\transitSchedule.xml');
config1.setParam('transit', 'vehiclesFile', 'C:\MATSim\workspace_mars\dta-brt\input\transitVehicles.xml');
config1.setParam('transit', 'transitModes', 'pt');
config1.setParam('global', 'coordinateSystem', 'EPSG:3115');
config1.setParam('global', 'randomSeed', '5399998864266990000');

prueba1.write('prueba16.xml');
%ConfigUtils.modifyFilePaths(config1, 'input/');

scenario = ScenarioUtils.loadScenario(config1);

controler = Controler(scenario);
controler.run();


%%
