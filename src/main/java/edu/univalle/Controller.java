package edu.univalle;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.NetworkConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.pt.config.TransitConfigGroup;

import com.google.inject.Provides;

import edu.univalle.mobsim.MIOMobsimModule;
import edu.univalle.mobsim.framework.Mobsim;
import edu.univalle.mobsim.qsim.AbstractQSimPlugin;
import edu.univalle.mobsim.qsim.ActivityEnginePlugin;
import edu.univalle.mobsim.qsim.PopulationPlugin;
import edu.univalle.mobsim.qsim.QSimProvider;
import edu.univalle.mobsim.qsim.TeleportationPlugin;
import edu.univalle.mobsim.qsim.changeeventsengine.NetworkChangeEventsPlugin;
import edu.univalle.mobsim.qsim.messagequeueengine.MessageQueuePlugin;
import edu.univalle.mobsim.qsim.pt.TransitEnginePlugin;
import edu.univalle.mobsim.qsim.qnetsimengine.QNetsimEnginePlugin;

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
                this.install(new com.google.inject.AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Mobsim.class).toProvider(QSimProvider.class);
                    }

                    @Provides
                    Collection<AbstractQSimPlugin> provideQSimPlugins(TransitConfigGroup transitConfigGroup,
                            NetworkConfigGroup networkConfigGroup, Config config) {
                        final Collection<AbstractQSimPlugin> plugins = new ArrayList<>();
                        plugins.add(new MessageQueuePlugin(config));
                        plugins.add(new ActivityEnginePlugin(config));
                        plugins.add(new QNetsimEnginePlugin(config));
                        if (networkConfigGroup.isTimeVariantNetwork()) {
                            plugins.add(new NetworkChangeEventsPlugin(config));
                        }
                        if (transitConfigGroup.isUseTransit()) {
                            plugins.add(new TransitEnginePlugin(config));
                        }
                        plugins.add(new TeleportationPlugin(config));
                        plugins.add(new PopulationPlugin(config));
                        return plugins;
                    }
                });
                // this.bind(AbstractModule.class).toInstance(new DefaultMobsimModule());

                // this.install(new MIOMob simModule());

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