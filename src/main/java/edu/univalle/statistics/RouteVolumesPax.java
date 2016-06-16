package edu.univalle.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.TransitDriverStartsEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.BoardingDeniedEvent;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.BoardingDeniedEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

public class RouteVolumesPax implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler,
        VehicleArrivesAtFacilityEventHandler, VehicleDepartsAtFacilityEventHandler, BoardingDeniedEventHandler,
        TransitDriverStartsEventHandler
{
    private final static Logger log = Logger.getLogger(LinkVolumesPax.class);

    private final int startTime;
    private final int endTime;

    private Map<Id<Vehicle>, LineAndRoute> transitVehicle2currentRoute = new HashMap<>();
    private Map<String, List<FacilityVolumes>> lineRouteFacilityVolumes = new HashMap<>();
    private static final String volumeVariables[] = { "entering", "leaving", "passthrough", "totalVolume" };

    RouteVolumesPax(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private class FacilityVolumes
    {
        Id<TransitStopFacility> facilityId;
        int entering;
        int leaving;
        int passthrough;
        int totalVolume;

        FacilityVolumes(Id<TransitStopFacility> id) {
            this.facilityId = id;
            this.entering = 0;
            this.leaving = 0;
            this.passthrough = 0;
            this.totalVolume = 0;
        }
    }

    private class LineAndRoute
    {

        final Id<TransitLine> transitLineId;
        final Id<TransitRoute> transitRouteId;
        final Id<Person> driverId;
        final Id<Departure> departureId;
        Id<TransitStopFacility> lastFacilityId;
        Id<TransitStopFacility> previousFacilityId;

        LineAndRoute(Id<TransitLine> transitLineId, Id<TransitRoute> transitRouteId, Id<Person> driverId,
                Id<Departure> departureId) {
            this.transitLineId = transitLineId;
            this.transitRouteId = transitRouteId;
            this.driverId = driverId;
            this.departureId = departureId;
        }

    }

    @Override
    public void reset(int iteration) {
        this.transitVehicle2currentRoute.clear();
        this.lineRouteFacilityVolumes.clear();

    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (event.getTime() >= startTime && event.getTime() <= endTime) {
            LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(event.getVehicleId());
            if (lineAndRoute != null) {
                if (!event.getPersonId().equals(lineAndRoute.driverId)) {
                    String lineRoute = lineAndRoute.transitLineId.toString() + "-"
                            + lineAndRoute.transitRouteId.toString();
                    lineRouteFacilityVolumes.get(lineRoute).get(lineAndRoute.lastFacilityId).entering++;
                    lineRouteFacilityVolumes.get(lineRoute).get(lineAndRoute.lastFacilityId).totalVolume++;
                }
            }
        }
    }

    @Override
    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
        if (event.getTime() >= startTime && event.getTime() <= endTime) {
            LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(event.getVehicleId());
            if (lineAndRoute != null) {
                lineAndRoute.previousFacilityId = lineAndRoute.lastFacilityId;
                lineAndRoute.lastFacilityId = event.getFacilityId();

                String lineRoute = lineAndRoute.transitLineId.toString() + "-" + lineAndRoute.transitRouteId.toString();
                if (!lineRouteFacilityVolumes.containsKey(lineRoute)) {
                    List<FacilityVolumes> facilityVolumes = new ArrayList<>();
                    // TODO Change map for list
                    facilityVolumes.add(new FacilityVolumes(lineAndRoute.lastFacilityId));
                    lineRouteFacilityVolumes.put(lineRoute, facilityVolumes);
                }
                else if (!lineRouteFacilityVolumes.get(lineRoute).containsKey(lineAndRoute.lastFacilityId)) {
                    lineRouteFacilityVolumes.get(lineRoute).put(lineAndRoute.lastFacilityId, new FacilityVolumes());
                }

                if (lineRouteFacilityVolumes.get(lineRoute).containsKey(lineAndRoute.previousFacilityId)) {
                    lineRouteFacilityVolumes.get(lineRoute)
                            .get(lineAndRoute.lastFacilityId).totalVolume = lineRouteFacilityVolumes.get(lineRoute)
                                    .get(lineAndRoute.previousFacilityId).totalVolume;

                    lineRouteFacilityVolumes.get(lineRoute)
                            .get(lineAndRoute.lastFacilityId).passthrough = lineRouteFacilityVolumes.get(lineRoute)
                                    .get(lineAndRoute.previousFacilityId).passthrough
                                    + lineRouteFacilityVolumes.get(lineRoute)
                                            .get(lineAndRoute.previousFacilityId).entering;
                }
            }
        }
    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (event.getTime() >= startTime && event.getTime() <= endTime) {
            LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(event.getVehicleId());
            if (lineAndRoute != null) {
                if (!event.getPersonId().equals(lineAndRoute.driverId)) {
                    String lineRoute = lineAndRoute.transitLineId.toString() + "-"
                            + lineAndRoute.transitRouteId.toString();
                    lineRouteFacilityVolumes.get(lineRoute).get(lineAndRoute.lastFacilityId).leaving++;
                    lineRouteFacilityVolumes.get(lineRoute).get(lineAndRoute.lastFacilityId).totalVolume--;
                    lineRouteFacilityVolumes.get(lineRoute).get(lineAndRoute.lastFacilityId).passthrough--;
                }
            }
        }
    }

    @Override
    public void handleEvent(BoardingDeniedEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleEvent(TransitDriverStartsEvent event) {
        if (event.getTime() >= startTime && event.getTime() <= endTime) {
            LineAndRoute lineAndRoute = new LineAndRoute(event.getTransitLineId(), event.getTransitRouteId(),
                    event.getDriverId(), event.getDepartureId());
            transitVehicle2currentRoute.put(event.getVehicleId(), lineAndRoute);
        }
    }

    public Map<Id<TransitStopFacility>, FacilityVolumes> getFacilityVolumesForRoute(String lineRoute) {
        return lineRouteFacilityVolumes.get(lineRoute);
    }

}
