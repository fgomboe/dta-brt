package edu.univalle;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;

import edu.univalle.mobsim.MIOMobsimModule;

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
                bind(TravelTime.class).toInstance(new MIOTravelTime());
            }
        });*/

        // this uses "addOVERRIDINGModule". It thus uses the Controler defaults,
        // and overrides them or adds to them.

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                // this.bind(AbstractModule.class).toInstance(new DefaultMobsimModule());

                this.install(new MIOMobsimModule());

                /*this.bindMobsim().toProvider(new Provider<Mobsim>() {
                    @Override
                    public Mobsim get() {
                        QSimConfigGroup conf = config.qsim();
                        if (conf == null) {
                            throw new NullPointerException(
                                    "There is no configuration set for the QSim. Please add the module 'qsim' to your config file.");
                        }
                
                        QSim qSim = new QSim(controler.getScenario(), controler.getEvents());
                        ActivityEngine activityEngine = new ActivityEngine(controler.getEvents(),
                                qSim.getAgentCounter());
                        qSim.addMobsimEngine(activityEngine);
                        qSim.addActivityHandler(activityEngine);
                        //
                        QNetsimEngine netsimEngine = new QNetsimEngine(qSim);
                        netsimEngine.setLinkSpeedCalculator(new MIOLinkSpeedCalculator());
                        qSim.addMobsimEngine(qSimLocalInjector.getInstance(netsimEngine));
                        qSim.addDepartureHandler(netsimEngine.getDepartureHandler());
                        //
                        TeleportationEngine teleportationEngine = new TeleportationEngine(controler.getScenario(),
                                controler.getEvents());
                        qSim.addMobsimEngine(teleportationEngine);
                        AgentFactory agentFactory;
                        if (config.transit().isUseTransit()) {
                            agentFactory = new TransitAgentFactory(qSim);
                            TransitQSimEngine transitEngine = new TransitQSimEngine(qSim);
                            transitEngine.setTransitStopHandlerFactory(new ComplexTransitStopHandlerFactory());
                            qSim.addDepartureHandler(transitEngine);
                            qSim.addAgentSource(transitEngine);
                            qSim.addMobsimEngine(transitEngine);
                        }
                        else {
                            agentFactory = new DefaultAgentFactory(qSim);
                        }
                        if (config.network().isTimeVariantNetwork()) {
                            qSim.addMobsimEngine(new NetworkChangeEventsEngine());
                        }
                        PopulationAgentSource agentSource = new PopulationAgentSource(
                                controler.getScenario().getPopulation(), agentFactory, qSim);
                        qSim.addAgentSource(agentSource);
                        return qSim;
                    }
                });*/
            }
        });

        controler.run();
    }

}