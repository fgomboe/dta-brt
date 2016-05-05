package edu.univalle.mobsim.qsim.messagequeueengine;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.config.Config;

import com.google.inject.Module;

import edu.univalle.mobsim.framework.listeners.MobsimListener;
import edu.univalle.mobsim.jdeqsim.MessageQueue;
import edu.univalle.mobsim.qsim.AbstractQSimPlugin;
import edu.univalle.mobsim.qsim.jdeqsimengine.SteppableScheduler;

public class MessageQueuePlugin extends AbstractQSimPlugin
{

    public MessageQueuePlugin(Config config) {
        super(config);
    }

    @Override
    public Collection<? extends Module> modules() {
        Collection<Module> result = new ArrayList<>();
        result.add(new com.google.inject.AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageQueue.class).asEagerSingleton();
                bind(SteppableScheduler.class).asEagerSingleton();
                bind(MessageQueueEngine.class).asEagerSingleton();
            }
        });
        return result;
    }

    @Override
    public Collection<Class<? extends MobsimListener>> listeners() {
        Collection<Class<? extends MobsimListener>> result = new ArrayList<>();
        result.add(MessageQueueEngine.class);
        return result;
    }
}
