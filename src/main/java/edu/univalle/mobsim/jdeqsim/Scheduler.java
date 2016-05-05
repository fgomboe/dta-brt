/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package edu.univalle.mobsim.jdeqsim;

import org.apache.log4j.Logger;
import org.matsim.core.gbl.Gbl;
import edu.univalle.mobsim.jdeqsim.Message;
import edu.univalle.mobsim.jdeqsim.MessageQueue;
import edu.univalle.mobsim.jdeqsim.Scheduler;
import edu.univalle.mobsim.jdeqsim.SimulationParameters;

/**
 * The scheduler of the micro-simulation.
 *
 * @author rashid_waraich
 */
public class Scheduler
{

    private static final Logger log = Logger.getLogger(Scheduler.class);
    private double simTime = 0;
    protected MessageQueue queue = null;
    private double simulationStartTime = System.currentTimeMillis();
    private double hourlyLogTime = 3600;

    public Scheduler(MessageQueue queue) {
        super();
        this.queue = queue;
    }

    public void schedule(Message m) {
        queue.putMessage(m);
    }

    public void unschedule(Message m) {
        queue.removeMessage(m);
    }

    public void startSimulation() {
        Message m;
        while (!queue.isEmpty() && simTime < SimulationParameters.getSimulationEndTime()) {
            m = queue.getNextMessage();
            if (m != null) {
                simTime = m.getMessageArrivalTime();
                m.processEvent();
                m.handleMessage();
            }
            printLog();
        }
    }

    public double getSimTime() {
        return simTime;
    }

    private void printLog() {

        // print output each hour
        if (simTime / hourlyLogTime > 1) {
            hourlyLogTime = simTime + 3600;
            log.info("Simulation at " + simTime / 3600 + "[h]; s/r:"
                    + simTime / (System.currentTimeMillis() - simulationStartTime) * 1000);
            Gbl.printMemoryUsage();
        }
    }

}
