/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package edu.univalle.mioQSim.mioQNetsimengine;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.LinkSpeedCalculator;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

/**
 * A simple link speed calculator taking the vehicle's max speed and the link's
 * free speed into account;
 * 
 * @author mrieser / Senozon AG
 */
public class MIOLinkSpeedCalculator implements LinkSpeedCalculator
{

    @Override
    public double getMaximumVelocity(QVehicle vehicle, Link link, double time) {
        // return Math.min(vehicle.getMaximumVelocity(), link.getFreespeed(time));
        return 5.0;
    }

}
