/* *********************************************************************** *
 * project: org.matsim.*
 * VolumesAnalyzer.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package edu.univalle.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.BoardingDeniedEvent;
import org.matsim.core.api.experimental.events.handler.BoardingDeniedEventHandler;
import org.matsim.vehicles.Vehicle;

/**
 * Counts the number of vehicles leaving a link, aggregated into time bins of a specified size.
 *
 * @author mrieser
 */
public class LinkVolumesPax implements LinkLeaveEventHandler, PersonEntersVehicleEventHandler,
        PersonLeavesVehicleEventHandler, BoardingDeniedEventHandler
{

    private final static Logger log = Logger.getLogger(LinkVolumesPax.class);
    private final int timeBinSize;
    private final int startTime;
    private final int endTime;
    private final int maxSlotIndex;
    private final Map<Id<Link>, int[]> links;
    private Map<Id<Vehicle>, Integer> vehicleLoads;
    private Map<Id<Person>, Integer> nonBoardingPassengers;

    // @Inject
    // LinkVolumesPax(Network network, EventsManager eventsManager) {
    // this(0, 24 * 3600, 3600, network);
    // eventsManager.addHandler(this);
    // }

    // public LinkVolumesPax(final int startTime, final int endTime, final int timeBinSize, final Network network) {
    // this(startTime, endTime, timeBinSize, network, true);
    // }

    public LinkVolumesPax(final int startTime, final int endTime, final int timeBinSize, final Network network,
            final int numOfVehicles) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeBinSize = timeBinSize;
        this.maxSlotIndex = (this.endTime - this.startTime) / this.timeBinSize; // because slot 0 contains from startTime to timeBinSize-1 (e.g. from 0 to 3599)
        this.links = new HashMap<>((int) (network.getLinks().size() * 1.1), 0.95f);
        this.vehicleLoads = new HashMap<Id<Vehicle>, Integer>((int) (numOfVehicles * 1.1), 0.95f);
        this.nonBoardingPassengers = new HashMap<Id<Person>, Integer>();
    }

    @Override
    public void handleEvent(final LinkLeaveEvent event) {
        if (event.getTime() >= startTime && event.getTime() <= endTime) {
            int[] volumes = this.links.get(event.getLinkId());
            if (volumes == null) {
                volumes = new int[this.maxSlotIndex + 1]; // initialized to 0 by default, according to JVM specs
                this.links.put(event.getLinkId(), volumes);
            }
            int timeslot = getTimeSlotIndex(event.getTime());
            if (vehicleLoads.containsKey(event.getVehicleId()))
                volumes[timeslot] += vehicleLoads.get(event.getVehicleId()) - 1; // minus 1 because of the driver
        }

    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (event.getTime() >= startTime && event.getTime() <= endTime) {
            if (vehicleLoads.containsKey(event.getVehicleId())) {
                int vehicleLoad = vehicleLoads.get(event.getVehicleId());
                vehicleLoad--;
                vehicleLoads.put(event.getVehicleId(), vehicleLoad);
            }
        }

    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (event.getTime() >= startTime && event.getTime() <= endTime) {
            if (vehicleLoads.containsKey(event.getVehicleId())) {
                int vehicleLoad = vehicleLoads.get(event.getVehicleId());
                vehicleLoad++;
                vehicleLoads.put(event.getVehicleId(), vehicleLoad);
                if (vehicleLoad > 64) {
                    log.warn("Passenger " + event.getPersonId() + " exceeds vehicle capacity for vehicle: "
                            + event.getVehicleId() + " at time: " + event.getTime());
                    nonBoardingPassengers.put(event.getPersonId(), null);
                }
                else if (nonBoardingPassengers.containsKey(event.getPersonId())) {
                    log.info("Passenger: " + event.getPersonId() + " boards vehicle: " + event.getVehicleId()
                            + " at time: " + event.getTime());
                    nonBoardingPassengers.remove(event.getPersonId());
                }
            }
            else {
                vehicleLoads.put(event.getVehicleId(), 1);
            }
        }

    }

    @Override
    public void handleEvent(BoardingDeniedEvent event) {
        // log.warn("Boarding denied to passenger: " + event.getPersonId() + " in vehicle: " + event.getVehicleId()
        // + ", persons in vehicle: " + vehicleLoads.get(event.getVehicleId()));

    }

    private int getTimeSlotIndex(final double time) {
        if (time > this.endTime) {
            return this.maxSlotIndex;
        }
        return (((int) time - this.startTime) / this.timeBinSize);
    }

    /**
     * @param linkId
     * @return Array containing the number of vehicles leaving the link <code>linkId</code> per time bin,
     *         starting with time bin 0 from 0 seconds to (timeBinSize-1)seconds.
     */
    public int[] getVolumesForLink(final Id<Link> linkId) {
        return this.links.get(linkId);
    }

    /**
     *
     * @return The size of the arrays returned by calls to the {@link #getVolumesForLink(Id)} and the {@link #getVolumesForLink(Id, String)}
     *         methods.
     */
    public int getVolumesArraySize() {
        return this.maxSlotIndex + 1;
    }

    /*
     * This procedure is only working if (hour % timeBinSize == 0)
     * 
     * Example: 15 minutes bins
     *  ___________________
     * |  0 | 1  | 2  | 3  |
     * |____|____|____|____|
     * 0   900 1800  2700 3600
        ___________________
     * |      hour 0       |
     * |___________________|
     * 0                  3600
     * 
     * hour 0 = bins 0,1,2,3
     * hour 1 = bins 4,5,6,7
     * ...
     * 
     * getTimeSlotIndex = (int)time / this.timeBinSize => jumps at 3600.0!
     * Thus, starting time = (hour = 0) * 3600.0
     */
    public double[] getVolumesPerHourForLink(final Id<Link> linkId) {
        if (3600.0 % this.timeBinSize != 0)
            log.error("Volumes per hour and per link probably not correct!");

        double[] volumes = new double[24];
        for (int hour = 0; hour < 24; hour++) {
            volumes[hour] = 0.0;
        }

        int[] volumesForLink = this.getVolumesForLink(linkId);
        if (volumesForLink == null)
            return volumes;

        int slotsPerHour = (int) (3600.0 / this.timeBinSize);
        for (int hour = 0; hour < 24; hour++) {
            double time = hour * 3600.0;
            for (int i = 0; i < slotsPerHour; i++) {
                volumes[hour] += volumesForLink[this.getTimeSlotIndex(time)];
                time += this.timeBinSize;
            }
        }
        return volumes;
    }

    /**
     * @return Set of Strings containing all link ids for which counting-values are available.
     */
    public Set<Id<Link>> getLinkIds() {
        return this.links.keySet();
    }

    public Map<Id<Person>, Integer> getNonBoardingPassengers() {
        return this.nonBoardingPassengers;
    }

    @Override
    public void reset(final int iteration) {
        this.links.clear();
        this.vehicleLoads.clear();
        this.nonBoardingPassengers.clear();
    }
}
