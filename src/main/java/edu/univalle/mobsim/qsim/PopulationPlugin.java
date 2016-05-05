package edu.univalle.mobsim.qsim;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.config.Config;

import com.google.inject.Module;

import edu.univalle.mobsim.framework.AgentSource;
import edu.univalle.mobsim.qsim.agents.AgentFactory;
import edu.univalle.mobsim.qsim.agents.DefaultAgentFactory;
import edu.univalle.mobsim.qsim.agents.PopulationAgentSource;
import edu.univalle.mobsim.qsim.agents.TransitAgentFactory;

public class PopulationPlugin extends AbstractQSimPlugin
{

    public PopulationPlugin(Config config) {
        super(config);
    }

    @Override
    public Collection<? extends Module> modules() {
        Collection<Module> result = new ArrayList<>();
        result.add(new com.google.inject.AbstractModule() {
            @Override
            protected void configure() {
                bind(PopulationAgentSource.class).asEagerSingleton();
                if (getConfig().transit().isUseTransit()) {
                    bind(AgentFactory.class).to(TransitAgentFactory.class).asEagerSingleton();
                }
                else {
                    bind(AgentFactory.class).to(DefaultAgentFactory.class).asEagerSingleton();
                }
            }
        });
        return result;
    }

    @Override
    public Collection<Class<? extends AgentSource>> agentSources() {
        Collection<Class<? extends AgentSource>> result = new ArrayList<>();
        result.add(PopulationAgentSource.class);
        return result;
    }
}
