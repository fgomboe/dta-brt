/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * DefaultMobsimModule.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2015 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package edu.univalle.mobsim;

import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.jdeqsim.JDEQSimulation;

import edu.univalle.mobsim.mioQSim.QSimModule;

public class MIOMobsimModule extends AbstractModule
{
    @Override
    public void install() {
        if (getConfig().controler().getMobsim().equals(ControlerConfigGroup.MobsimType.qsim.toString())) {
            install(new QSimModule());
        }
        else if (getConfig().controler().getMobsim().equals(ControlerConfigGroup.MobsimType.JDEQSim.toString())) {
            bindMobsim().to(JDEQSimulation.class);
        }
    }
}
