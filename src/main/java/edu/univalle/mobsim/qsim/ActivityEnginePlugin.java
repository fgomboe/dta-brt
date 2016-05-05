package edu.univalle.mobsim.qsim;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.config.Config;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import edu.univalle.mobsim.qsim.interfaces.ActivityHandler;
import edu.univalle.mobsim.qsim.interfaces.MobsimEngine;

public class ActivityEnginePlugin extends AbstractQSimPlugin
{

    public ActivityEnginePlugin(Config config) {
        super(config);
    }

    @Override
    public Collection<? extends Module> modules() {
        Collection<Module> result = new ArrayList<>();
        result.add(new AbstractModule() {
            @Override
            public void configure() {
                bind(ActivityEngine.class).asEagerSingleton();
            }
        });
        return result;
    }

    @Override
    public Collection<Class<? extends ActivityHandler>> activityHandlers() {
        Collection<Class<? extends ActivityHandler>> result = new ArrayList<>();
        result.add(ActivityEngine.class);
        return result;
    }

    @Override
    public Collection<Class<? extends MobsimEngine>> engines() {
        Collection<Class<? extends MobsimEngine>> result = new ArrayList<>();
        result.add(ActivityEngine.class);
        return result;
    }
}
