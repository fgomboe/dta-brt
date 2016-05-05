/* *********************************************************************** *
 * project: org.matsim.*
 * Vehicle.java
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

package edu.univalle.mobsim.qsim.pt;

import edu.univalle.mobsim.qsim.pt.TransitStopHandler;
import org.matsim.vehicles.Vehicle;

import edu.univalle.mobsim.framework.MobsimDriverAgent;
import edu.univalle.mobsim.qsim.interfaces.MobsimVehicle;

public interface TransitVehicle extends MobsimVehicle
{

    /**
     * @return the <code>BasicVehicle</code> that this simulation vehicle represents
     */
    @Override
    public Vehicle getVehicle();

    @Override
    public org.matsim.core.mobsim.framework.MobsimDriverAgent getDriver();

    public TransitStopHandler getStopHandler();

}
