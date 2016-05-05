package edu.univalle.mobsim.qsim;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.config.Config;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import edu.univalle.mobsim.qsim.interfaces.MobsimEngine;

public class TeleportationPlugin extends AbstractQSimPlugin
{

    public TeleportationPlugin(Config config) {
        super(config);
    }

    @Override
    public Collection<? extends Module> modules() {
        Collection<Module> result = new ArrayList<>();
        result.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TeleportationEngine.class).asEagerSingleton();
            }
        });
        return result;
    }

    @Override
    public Collection<Class<? extends MobsimEngine>> engines() {
        Collection<Class<? extends MobsimEngine>> result = new ArrayList<>();
        result.add(TeleportationEngine.class);
        return result;
    }
}
