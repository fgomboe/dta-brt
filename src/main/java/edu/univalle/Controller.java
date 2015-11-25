package edu.univalle;

import org.matsim.analysis.LegHistogramModule;
import org.matsim.analysis.LinkStatsModule;
import org.matsim.analysis.VolumesAnalyzerModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.DefaultMobsimModule;
import org.matsim.core.replanning.StrategyManagerModule;
import org.matsim.core.router.TripRouterModule;
import org.matsim.core.router.costcalculators.TravelDisutilityModule;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionModule;
import edu.univalle.network.FelisTravelTime;

@SuppressWarnings("unused")
public class Controller {
	
	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig("input/config.xml");
		Controler controler = new Controler(config) ;
		
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
		
		// this uses "addOVERRIDINGModule".  It thus uses the Controler defaults, and overrides them or adds to them.
		controler.addOverridingModule( new AbstractModule(){
			@Override public void install() {
				this.bind( TravelTime.class ).toInstance( new FelisTravelTime() );
			}
		});
		
		//controler.setOverwriteFiles(true) ;
		controler.run();
	}
	
}