package edu.univalle;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;

public class Controller
{

    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("input/config_dummy.xml");
        Controler controler = new Controler(config);

        /*controler.setModules(new AbstractModule() {
        	@Override
        	public void install() {
        		// Include some things from ControlerDefaultsModule.java,
        		// but leave out TravelTimeCalculator.
        		// You can just comment out these lines if you don't want them,
        		// these modules are optional.
        		install(new DefaultMobsimModule());
        		install(new CharyparNagelScoringFunctionModule());
        		install(new TripRouterModule());
        		install(new StrategyManagerModule());
        		install(new LinkStatsModule());
        		install(new VolumesAnalyzerModule());
        		install(new LegHistogramModule());
        		install(new TravelDisutilityModule());
        
        		// Because TravelTimeCalculatorModule is left out,
        		// we have to provide a TravelTime.
        		// This line says: Use this thing here as the TravelTime implementation.
        		// Try removing this line: You will get an error because there is no
        		// TravelTime and someone needs it.
        		bind(TravelTime.class).toInstance(new FelisTravelTime());
        	}
        });*/

        // this uses "addOVERRIDINGModule". It thus uses the Controler defaults,
        // and overrides them or adds to them.
        /*
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                this.bind(TravelTime.class).toInstance(new FelisTravelTime());
            }
        });
        */

        // controler.setOverwriteFiles(true) ;
        controler.run();
    }

}