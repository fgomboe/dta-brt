package edu.univalle.mobsim.qsim.messagequeueengine;

import javax.inject.Inject;

import edu.univalle.mobsim.framework.events.MobsimBeforeSimStepEvent;
import edu.univalle.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import edu.univalle.mobsim.qsim.jdeqsimengine.SteppableScheduler;

class MessageQueueEngine implements MobsimBeforeSimStepListener {

	private final SteppableScheduler scheduler;

	@Inject
	MessageQueueEngine(final SteppableScheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
		scheduler.doSimStep(e.getSimulationTime());
	}

}
