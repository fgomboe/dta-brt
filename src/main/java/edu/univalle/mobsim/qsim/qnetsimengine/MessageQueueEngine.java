package edu.univalle.mobsim.qsim.qnetsimengine;

import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.mobsim.qsim.jdeqsimengine.SteppableScheduler;

import javax.inject.Inject;

public class MessageQueueEngine implements MobsimBeforeSimStepListener
{

    private final SteppableScheduler scheduler;

    @Inject
    public MessageQueueEngine(final SteppableScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
        scheduler.doSimStep(e.getSimulationTime());
    }

}
