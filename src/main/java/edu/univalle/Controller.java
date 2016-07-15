package edu.univalle;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;
import org.matsim.core.mobsim.qsim.ActivityEnginePlugin;
import org.matsim.core.mobsim.qsim.PopulationPlugin;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.TeleportationPlugin;
import org.matsim.core.mobsim.qsim.changeeventsengine.NetworkChangeEventsPlugin;
import org.matsim.core.mobsim.qsim.interfaces.ActivityHandler;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.messagequeueengine.MessageQueuePlugin;
import org.matsim.core.mobsim.qsim.pt.TransitEnginePlugin;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEnginePlugin;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import org.matsim.core.controler.AbstractModule;

public class Controller
{

    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("input/config_dummy.xml");
        Controler controler = new Controler(config);

        EventsManager eventsManager = EventsUtils.createEventsManager(controler.getConfig());
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bindMobsim().toProvider(new Provider<QSim>() {
                    private Injector injector;

                    @Override
                    public QSim get() {
                        QSimConfigGroup conf = config.qsim();
                        if (conf == null) {
                            throw new NullPointerException(
                                    "There is no configuration set for the QSim. Please add the module 'qsim' to your config file.");
                        }

                        Collection<AbstractQSimPlugin> plugins = new ArrayList<>();
                        plugins.add(new MessageQueuePlugin(config));
                        plugins.add(new ActivityEnginePlugin(config));
                        plugins.add(new QNetsimEnginePlugin(config));
                        if (controler.getConfig().network().isTimeVariantNetwork()) {
                            plugins.add(new NetworkChangeEventsPlugin(config));
                        }
                        if (controler.getConfig().transit().isUseTransit()) {
                            plugins.add(new TransitEnginePlugin(config));
                        }
                        plugins.add(new TeleportationPlugin(config));
                        plugins.add(new PopulationPlugin(config));

                        com.google.inject.AbstractModule module = new com.google.inject.AbstractModule() {
                            @Override
                            protected void configure() {
                                for (AbstractQSimPlugin plugin : plugins) {
                                    for (Module module : plugin.modules()) {
                                        install(module);
                                    }
                                }
                                bind(QSim.class).asEagerSingleton();
                                bind(Netsim.class).to(QSim.class);
                            }
                        };
                        Injector qSimLocalInjector = injector.createChildInjector(module);
                        QSim qSim = qSimLocalInjector.getInstance(QSim.class);
                        for (AbstractQSimPlugin plugin : plugins) {
                            for (Class<? extends MobsimEngine> mobsimEngine : plugin.engines()) {
                                qSim.addMobsimEngine(qSimLocalInjector.getInstance(mobsimEngine));
                            }
                            for (Class<? extends ActivityHandler> activityHandler : plugin.activityHandlers()) {
                                qSim.addActivityHandler(qSimLocalInjector.getInstance(activityHandler));
                            }
                            for (Class<? extends DepartureHandler> mobsimEngine : plugin.departureHandlers()) {
                                qSim.addDepartureHandler(qSimLocalInjector.getInstance(mobsimEngine));
                            }
                            for (Class<? extends MobsimListener> mobsimListener : plugin.listeners()) {
                                qSim.addQueueSimulationListeners(qSimLocalInjector.getInstance(mobsimListener));
                            }
                            for (Class<? extends AgentSource> agentSource : plugin.agentSources()) {
                                qSim.addAgentSource(qSimLocalInjector.getInstance(agentSource));
                            }
                        }
                        return qSim;
                    }
                });
            }
        });

        controler.run();
    }

}