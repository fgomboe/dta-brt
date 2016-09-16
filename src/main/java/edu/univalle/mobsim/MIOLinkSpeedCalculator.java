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

package edu.univalle.mobsim;

import java.util.Random;

import org.matsim.api.core.v01.network.Link;

import org.matsim.core.mobsim.qsim.qnetsimengine.LinkSpeedCalculator;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.utils.objectattributes.ObjectAttributes;

import edu.univalle.statistics.CalcLinkSpeedStats;

/**
 * A simple link speed calculator taking the vehicle's max speed and the link's
 * free speed into account;
 * 
 * @author mrieser / Senozon AG
 */
public class MIOLinkSpeedCalculator implements LinkSpeedCalculator
{
    CalcLinkSpeedStats calculator;
    Random r;

    public MIOLinkSpeedCalculator() {
        calculator = new CalcLinkSpeedStats();
        calculator.readInput("input/specialNodeAttributes.xml", "input/networkMIO/stops.csv",
                "input/linkSpeeds/semana_abril.csv");
        r = new Random();

    }

    @Override
    public double getMaximumVelocity(QVehicle vehicle, Link link, double time) {

        // This accounts for the links, 19 meters long, between trunk stations and their bays
        String[] linkName = link.getId().toString().split("-");
        Object assoc_link0 = calculator.nodeAttributes.getAttribute(linkName[0], "ASSOCIATE_ID");
        Object assoc_link1 = calculator.nodeAttributes.getAttribute(linkName[1], "ASSOCIATE_ID");
        if (assoc_link0 != null && assoc_link0.toString().equals(linkName[1]))
            return Math.min(vehicle.getMaximumVelocity(), link.getFreespeed(time));
        else if (assoc_link1 != null && assoc_link1.toString().equals(linkName[0]))
            return Math.min(vehicle.getMaximumVelocity(), link.getFreespeed(time));

        double average = calculator.getAverage(link.getId().toString(), time);
        double stdDev = calculator.getStdDev(link.getId().toString(), time);
        double randomSpeed;
        do {
            randomSpeed = r.nextGaussian() * stdDev + average;
        } while (randomSpeed <= 0);

        return Math.min(randomSpeed, Math.min(vehicle.getMaximumVelocity(), link.getFreespeed(time)));
    }

}
