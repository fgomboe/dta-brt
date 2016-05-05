package edu.univalle.mobsim.qsim.pt;

import com.google.inject.Module;

import edu.univalle.mobsim.framework.AgentSource;
import edu.univalle.mobsim.qsim.AbstractQSimPlugin;
import edu.univalle.mobsim.qsim.interfaces.DepartureHandler;
import edu.univalle.mobsim.qsim.interfaces.MobsimEngine;

import org.matsim.core.config.Config;
import edu.univalle.mobsim.qsim.pt.ComplexTransitStopHandlerFactory;
import edu.univalle.mobsim.qsim.pt.TransitQSimEngine;
import edu.univalle.mobsim.qsim.pt.TransitStopHandlerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class TransitEnginePlugin extends AbstractQSimPlugin
{

    public TransitEnginePlugin(Config config) {
        super(config);
    }

    @Override
    public Collection<? extends Module> modules() {
        Collection<Module> result = new ArrayList<>();
        result.add(new com.google.inject.AbstractModule() {
            @Override
            protected void configure() {
                bind(TransitQSimEngine.class).asEagerSingleton();
                bind(TransitStopHandlerFactory.class).to(ComplexTransitStopHandlerFactory.class).asEagerSingleton();
            }
        });
        return result;
    }

    @Override
    public Collection<Class<? extends DepartureHandler>> departureHandlers() {
        Collection<Class<? extends DepartureHandler>> result = new ArrayList<>();
        result.add(TransitQSimEngine.class);
        return result;
    }

    @Override
    public Collection<Class<? extends AgentSource>> agentSources() {
        Collection<Class<? extends AgentSource>> result = new ArrayList<>();
        result.add(TransitQSimEngine.class);
        return result;
    }

    @Override
    public Collection<Class<? extends MobsimEngine>> engines() {
        Collection<Class<? extends MobsimEngine>> result = new ArrayList<>();
        result.add(TransitQSimEngine.class);
        return result;
    }

}
