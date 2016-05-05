package edu.univalle.mobsim.qsim.changeeventsengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.matsim.core.config.Config;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import edu.univalle.mobsim.qsim.AbstractQSimPlugin;
import edu.univalle.mobsim.qsim.interfaces.MobsimEngine;

public class NetworkChangeEventsPlugin extends AbstractQSimPlugin
{

    public NetworkChangeEventsPlugin(Config config) {
        super(config);
    }

    @Override
    public Collection<? extends Module> modules() {
        return Collections.singletonList(new AbstractModule() {
            @Override
            protected void configure() {
                bind(NewNetworkChangeEventsEngine.class).asEagerSingleton();
            }
        });
    }

    @Override
    public Collection<Class<? extends MobsimEngine>> engines() {
        Collection<Class<? extends MobsimEngine>> result = new ArrayList<>();
        result.add(NewNetworkChangeEventsEngine.class);
        return result;
    }
}
