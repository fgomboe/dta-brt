package edu.univalle.mobsim.qsim;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.config.Config;
import org.matsim.core.config.groups.NetworkConfigGroup;
import org.matsim.pt.config.TransitConfigGroup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import edu.univalle.mobsim.framework.Mobsim;
import edu.univalle.mobsim.qsim.changeeventsengine.NetworkChangeEventsPlugin;
import edu.univalle.mobsim.qsim.messagequeueengine.MessageQueuePlugin;
import edu.univalle.mobsim.qsim.pt.TransitEnginePlugin;
import edu.univalle.mobsim.qsim.qnetsimengine.QNetsimEnginePlugin;

public class QSimModule extends AbstractModule
{
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
}
