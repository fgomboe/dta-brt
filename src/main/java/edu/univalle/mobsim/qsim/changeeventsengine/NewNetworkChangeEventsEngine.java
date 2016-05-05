package edu.univalle.mobsim.qsim.changeeventsengine;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkImpl;

import edu.univalle.mobsim.jdeqsim.Message;
import edu.univalle.mobsim.jdeqsim.MessageQueue;
import edu.univalle.mobsim.qsim.InternalInterface;
import edu.univalle.mobsim.qsim.interfaces.MobsimEngine;

import javax.inject.Inject;
import java.util.Collection;

class NewNetworkChangeEventsEngine implements MobsimEngine {

	private final MessageQueue messageQueue;
	private final Network network;
	private InternalInterface internalInterface;

	@Inject
	NewNetworkChangeEventsEngine(Network network, MessageQueue messageQueue) {
		this.network = network;
		this.messageQueue = messageQueue;
	}

	@Override
	public void onPrepareSim() {
		Collection<NetworkChangeEvent> changeEvents = ((NetworkImpl) network).getNetworkChangeEvents();
		for (final NetworkChangeEvent changeEvent : changeEvents) {
			Message m = new Message() {
				@Override
				public void processEvent() {

				}

				@Override
				public void handleMessage() {
					for (Link link : changeEvent.getLinks()) {
						internalInterface.getMobsim().getNetsimNetwork().getNetsimLink(link.getId())
								.recalcTimeVariantAttributes(internalInterface.getMobsim().getSimTimer().getTimeOfDay());
					}
				}
			};
			m.setMessageArrivalTime(changeEvent.getStartTime());
			messageQueue.putMessage(m);
		}
	}

	@Override
	public void afterSim() {

	}

	@Override
	public void setInternalInterface(InternalInterface internalInterface) {
		this.internalInterface = internalInterface;
	}

	@Override
	public void doSimStep(double time) {

	}
}
