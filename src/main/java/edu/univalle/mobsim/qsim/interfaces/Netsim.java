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

package edu.univalle.mobsim.qsim.interfaces;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;

import edu.univalle.mobsim.framework.MobsimTimer;
import edu.univalle.mobsim.framework.ObservableMobsim;
import edu.univalle.mobsim.qsim.qnetsimengine.NetsimNetwork;

public interface Netsim extends ObservableMobsim
{

    NetsimNetwork getNetsimNetwork();

    EventsManager getEventsManager();

    AgentCounter getAgentCounter();

    Scenario getScenario();

    MobsimTimer getSimTimer();
}