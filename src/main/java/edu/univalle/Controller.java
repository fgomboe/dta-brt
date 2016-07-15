package edu.univalle;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.NetworkConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;
import org.matsim.core.mobsim.qsim.ActivityEnginePlugin;
import org.matsim.core.mobsim.qsim.PopulationPlugin;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimProvider;
import org.matsim.core.mobsim.qsim.TeleportationPlugin;
import org.matsim.core.mobsim.qsim.changeeventsengine.NetworkChangeEventsPlugin;
import org.matsim.core.mobsim.qsim.interfaces.ActivityHandler;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.messagequeueengine.MessageQueuePlugin;
import org.matsim.core.mobsim.qsim.pt.TransitEnginePlugin;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEnginePlugin;
import org.matsim.pt.config.TransitConfigGroup;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;

import edu.univalle.qnetsimengine.BRTProvider;

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
                install(new com.google.inject.AbstractModule() {

                    @Override
                    protected void configure() {
                        bind(Mobsim.class).toProvider(BRTProvider.class);
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
            }
        });

        controler.run();
    }

}