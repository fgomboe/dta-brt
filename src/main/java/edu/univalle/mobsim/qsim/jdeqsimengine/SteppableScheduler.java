package edu.univalle.mobsim.qsim.jdeqsimengine;

import javax.inject.Inject;

import edu.univalle.mobsim.framework.Steppable;
import edu.univalle.mobsim.jdeqsim.Message;
import edu.univalle.mobsim.jdeqsim.MessageQueue;
import edu.univalle.mobsim.jdeqsim.Scheduler;

public class SteppableScheduler extends Scheduler implements Steppable {

    private Message lookahead;
    private boolean finished = false;

    @Inject
    public SteppableScheduler(MessageQueue queue) {
        super(queue);
    }

    @Override
    public void doSimStep(double time) {
        finished = false; // I don't think we can restart once the queue has run dry, but just in case.
        if (lookahead != null && time < lookahead.getMessageArrivalTime()) {
            return;
        }
        if (lookahead != null) {
            lookahead.processEvent();
            lookahead.handleMessage();
            lookahead = null;
        }
        while (!queue.isEmpty()) {
            Message m = queue.getNextMessage();
            if (m != null && m.getMessageArrivalTime() <= time) {
                m.processEvent();
                m.handleMessage();
            } else {
                lookahead = m;
                return;
            }
        }
        finished = true; // queue has run dry.
    }

    public boolean isFinished() {
        return finished;
    }

}
