package edu.univalle.mobsim.qsim.jdeqsimengine;

import edu.univalle.mobsim.jdeqsim.MessageQueue;
import edu.univalle.mobsim.qsim.QSim;

public class JDEQSimModule
{

    private JDEQSimModule() {
    }

    public static void configure(QSim qsim) {
        SteppableScheduler scheduler = new SteppableScheduler(new MessageQueue());
        JDEQSimEngine jdeqSimEngine = new JDEQSimEngine(qsim.getScenario(), qsim.getEventsManager(),
                qsim.getAgentCounter(), scheduler);
        qsim.addMobsimEngine(jdeqSimEngine);
        qsim.addActivityHandler(jdeqSimEngine);
    }

}
