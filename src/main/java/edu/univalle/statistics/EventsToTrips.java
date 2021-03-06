/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.TransitDriverStartsEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.api.experimental.events.AgentWaitingForPtEvent;
import org.matsim.core.api.experimental.events.TeleportationArrivalEvent;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.AgentWaitingForPtEventHandler;
import org.matsim.core.api.experimental.events.handler.TeleportationArrivalEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.routes.GenericRouteImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.pt.routes.ExperimentalTransitRoute;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

/**
 * 
 * Converts a stream of Events into a stream of Legs. Passes Legs to a single LegHandler which must be registered with this class.
 * Mainly intended for scoring, but can be used for any kind of Leg related statistics. Essentially, it allows you to read
 * Legs from the simulation like you would read Legs from Plans, except that the Plan does not even need to exist.
 * 
 * Note that the instances of Leg passed to the LegHandler will never be identical to those in the Scenario! Even
 * in a "no-op" simulation which only reproduces the Plan, new instances will be created. So if you attach your own data
 * to the Legs in the Scenario, that's your own lookout.
 * 
 * @author michaz
 *
 */
public final class EventsToTrips implements PersonDepartureEventHandler, PersonArrivalEventHandler,
        LinkLeaveEventHandler, LinkEnterEventHandler, TeleportationArrivalEventHandler, TransitDriverStartsEventHandler,
        PersonEntersVehicleEventHandler, VehicleArrivesAtFacilityEventHandler, PersonLeavesVehicleEventHandler,
        ActivityStartEventHandler, AgentWaitingForPtEventHandler
{

    class LegPlusWait
    {
        final LegImpl leg;
        double waitingTime;

        public LegPlusWait(LegImpl leg, double waitingTime) {
            this.leg = leg;
            this.waitingTime = waitingTime;
        }
    }

    private class PendingTransitTravel
    {

        final Id<Vehicle> vehicleId;
        final Id<TransitStopFacility> accessStop;
        final double waitingTime;

        public PendingTransitTravel(Id<Vehicle> vehicleId, Id<TransitStopFacility> accessStop, double waitingTime) {
            this.vehicleId = vehicleId;
            this.accessStop = accessStop;
            this.waitingTime = waitingTime;
        }

    }

    private class LineAndRoute
    {

        final Id<TransitLine> transitLineId;
        final Id<TransitRoute> transitRouteId;
        final Id<Person> driverId;
        final Id<Departure> departureId;
        Id<TransitStopFacility> lastFacilityId;

        LineAndRoute(Id<TransitLine> transitLineId, Id<TransitRoute> transitRouteId, Id<Person> driverId,
                Id<Departure> departureId) {
            this.transitLineId = transitLineId;
            this.transitRouteId = transitRouteId;
            this.driverId = driverId;
            this.departureId = departureId;
        }

    }

    public interface TripHandler
    {
        void handleTrip(Id<Person> agentId, List<LegPlusWait> trip);
    }

    private Scenario scenario;
    private Map<Id<Person>, List<LegPlusWait>> trip = new HashMap<>();
    private Map<Id<Person>, List<Id<Link>>> experiencedRoutes = new HashMap<>();
    private Map<Id<Person>, TeleportationArrivalEvent> routelessTravels = new HashMap<>();
    private Map<Id<Person>, Double> transitWaits = new HashMap<>();
    private Map<Id<Person>, PendingTransitTravel> transitTravels = new HashMap<>();
    private Map<Id<Vehicle>, LineAndRoute> transitVehicle2currentRoute = new HashMap<>();
    private TripHandler tripHandler;

    public EventsToTrips(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        LegImpl leg = new LegImpl(event.getLegMode());
        leg.setDepartureTime(event.getTime());

        if (!trip.containsKey(event.getPersonId())) {
            List<LegPlusWait> legs = new ArrayList<>();
            legs.add(new LegPlusWait(leg, 0));
            trip.put(event.getPersonId(), legs);
        }
        else {
            trip.get(event.getPersonId()).add(new LegPlusWait(leg, 0));
        }

        List<Id<Link>> route = new ArrayList<>();
        route.add(event.getLinkId());
        experiencedRoutes.put(event.getPersonId(), route);
    }

    @Override
    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
        LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(event.getVehicleId());
        if (lineAndRoute != null) {
            lineAndRoute.lastFacilityId = event.getFacilityId();
        }
    }

    @Override
    public void handleEvent(AgentWaitingForPtEvent event) {
        transitWaits.put(event.getPersonId(), event.getTime());

    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(event.getVehicleId());
        // transit drivers are not considered to travel by transit
        // passenger must be waiting for a waitingTime to be set
        if (transitWaits.containsKey(event.getPersonId()) && lineAndRoute != null) {
            if (!event.getPersonId().equals(lineAndRoute.driverId)) {
                transitTravels.put(event.getPersonId(), new PendingTransitTravel(event.getVehicleId(),
                        lineAndRoute.lastFacilityId, event.getTime() - transitWaits.get(event.getPersonId())));
            }
        }
        else if (!transitWaits.containsKey(event.getPersonId()) && lineAndRoute != null) {
            if (!event.getPersonId().equals(lineAndRoute.driverId)) {
                transitTravels.put(event.getPersonId(),
                        new PendingTransitTravel(event.getVehicleId(), lineAndRoute.lastFacilityId, 0));
            }
        }
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {

    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        List<Id<Link>> route = experiencedRoutes.get(event.getDriverId());
        route.add(event.getLinkId());
    }

    @Override
    public void handleEvent(TeleportationArrivalEvent travelEvent) {
        routelessTravels.put(travelEvent.getPersonId(), travelEvent);
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        List<LegPlusWait> legs = trip.get(event.getPersonId());
        LegPlusWait legPlusWait = legs.get(legs.size() - 1);
        legPlusWait.leg.setArrivalTime(event.getTime());
        double travelTime = legPlusWait.leg.getArrivalTime() - legPlusWait.leg.getDepartureTime();
        legPlusWait.leg.setTravelTime(travelTime);
        List<Id<Link>> experiencedRoute = experiencedRoutes.remove(event.getPersonId());
        assert experiencedRoute.size() >= 1;
        PendingTransitTravel pendingTransitTravel;
        if (experiencedRoute.size() > 1) {
            NetworkRoute networkRoute = RouteUtils.createNetworkRoute(experiencedRoute, null);
            networkRoute.setTravelTime(travelTime);

            networkRoute.setDistance(RouteUtils.calcDistance(networkRoute, scenario.getNetwork()));
            // TODO MATSIM-227: replace the above by taking distance from List<Id<Link>> experiencedRoute (minus first/last link)
            // and add manually distance on first/last link. Newly based on VehicleEnters/LeavesTrafficEvents, which should (newly)
            // contain this information. kai/mz, sep'15

            legPlusWait.leg.setRoute(networkRoute);
        }
        else if ((pendingTransitTravel = transitTravels.remove(event.getPersonId())) != null) {
            LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(pendingTransitTravel.vehicleId);
            TransitLine line = scenario.getTransitSchedule().getTransitLines().get(lineAndRoute.transitLineId);
            ExperimentalTransitRoute experimentalTransitRoute = new ExperimentalTransitRoute(
                    scenario.getTransitSchedule().getFacilities().get(pendingTransitTravel.accessStop), line,
                    line.getRoutes().get(lineAndRoute.transitRouteId),
                    scenario.getTransitSchedule().getFacilities().get(lineAndRoute.lastFacilityId));
            experimentalTransitRoute.setTravelTime(travelTime);
            experimentalTransitRoute.setDistance(RouteUtils.calcDistance(experimentalTransitRoute,
                    scenario.getTransitSchedule(), scenario.getNetwork()));
            legPlusWait.leg.setRoute(experimentalTransitRoute);
            legPlusWait.waitingTime = pendingTransitTravel.waitingTime;
        }
        else {
            TeleportationArrivalEvent travelEvent = routelessTravels.remove(event.getPersonId());
            Route genericRoute = new GenericRouteImpl(experiencedRoute.get(0), event.getLinkId());
            genericRoute.setTravelTime(travelTime);
            if (travelEvent != null) {
                genericRoute.setDistance(travelEvent.getDistance());
            }
            else {
                genericRoute.setDistance(0.0);
            }
            legPlusWait.leg.setRoute(genericRoute);
        }

        // TODO Verify if I need this
        // legHandler.handleLeg(event.getPersonId(), leg);
    }

    @Override
    public void handleEvent(TransitDriverStartsEvent event) {
        LineAndRoute lineAndRoute = new LineAndRoute(event.getTransitLineId(), event.getTransitRouteId(),
                event.getDriverId(), event.getDepartureId());
        transitVehicle2currentRoute.put(event.getVehicleId(), lineAndRoute);
    }

    @Override
    public void reset(int iteration) {
        trip.clear();
        experiencedRoutes.clear();
    }

    public void setTripHandler(TripHandler tripHandler) {
        this.tripHandler = tripHandler;
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(event.getVehicleId());
        if (lineAndRoute != null && event.getPersonId().equals(lineAndRoute.driverId)) {
            transitVehicle2currentRoute.remove(event.getVehicleId());
        }

    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (event.getActType().equals("dummy")) {
            if (trip.containsKey(event.getPersonId()))
                tripHandler.handleTrip(event.getPersonId(), trip.remove(event.getPersonId())); // method remove returns the value previous to removal
        }
    }

}
