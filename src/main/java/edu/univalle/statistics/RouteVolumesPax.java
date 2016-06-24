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
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

public class RouteVolumesPax implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler,
        VehicleArrivesAtFacilityEventHandler, TransitDriverStartsEventHandler
{
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getLogger(LinkVolumesPax.class);

    private final int startTime;
    private final int endTime;

    private Map<Id<Vehicle>, LineAndRoute> transitVehicle2currentRoute = new HashMap<>();
    private Map<String, List<FacilityVolumes>> lineRouteFacilityVolumes = new HashMap<>();

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
        Id<TransitStopFacility> lastFacilityId;

        LineAndRoute(Id<TransitLine> transitLineId, Id<TransitRoute> transitRouteId, Id<Person> driverId) {
            this.transitLineId = transitLineId;
            this.transitRouteId = transitRouteId;
            this.driverId = driverId;
            lastFacilityId = null;
        }

    }

    @Override
    public void reset(int iteration) {
        this.transitVehicle2currentRoute.clear();
        this.lineRouteFacilityVolumes.clear();

    }

    @Override
    public void handleEvent(TransitDriverStartsEvent event) {
        // Only this event limits the buses counted (have time into account)
        if (event.getTime() >= startTime && event.getTime() <= endTime) {
            LineAndRoute lineAndRoute = new LineAndRoute(event.getTransitLineId(), event.getTransitRouteId(),
                    event.getDriverId());
            transitVehicle2currentRoute.put(event.getVehicleId(), lineAndRoute);
        }
    }

    @Override
    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
        LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(event.getVehicleId());
        if (lineAndRoute != null) {
            lineAndRoute.lastFacilityId = event.getFacilityId();

            String lineRoute = lineAndRoute.transitLineId.toString() + "_" + lineAndRoute.transitRouteId.toString();
            if (!lineRouteFacilityVolumes.containsKey(lineRoute)) {
                List<FacilityVolumes> facilityVolumes = new ArrayList<>();
                facilityVolumes.add(new FacilityVolumes(lineAndRoute.lastFacilityId));
                lineRouteFacilityVolumes.put(lineRoute, facilityVolumes);
            }
            else if (checkIdInFacilityVolumeList(lineAndRoute.lastFacilityId,
                    lineRouteFacilityVolumes.get(lineRoute)) == -1) {
                lineRouteFacilityVolumes.get(lineRoute).add(new FacilityVolumes(lineAndRoute.lastFacilityId));
            }
        }
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(event.getVehicleId());
        if (lineAndRoute != null) {
            if (!event.getPersonId().equals(lineAndRoute.driverId)) {
                getFacilityVolumesForRouteAndFacility(lineAndRoute, lineAndRoute.lastFacilityId).entering++;
            }
        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        LineAndRoute lineAndRoute = transitVehicle2currentRoute.get(event.getVehicleId());
        if (lineAndRoute != null) {
            if (!event.getPersonId().equals(lineAndRoute.driverId)) {
                getFacilityVolumesForRouteAndFacility(lineAndRoute, lineAndRoute.lastFacilityId).leaving++;
            }
        }

        // This if checks if the event is ahead of the endTime to make aggregation of volumes
        if (event.getTime() > endTime) {
            for (Map.Entry<String, List<FacilityVolumes>> entry : lineRouteFacilityVolumes.entrySet()) {
                entry.getValue().get(0).totalVolume = entry.getValue().get(0).entering;
                entry.getValue().get(0).passthrough = 0;

                for (int iii = 1; iii < entry.getValue().size(); ++iii) {
                    entry.getValue().get(iii).totalVolume = entry.getValue().get(iii - 1).totalVolume
                            + entry.getValue().get(iii).entering - entry.getValue().get(iii).leaving;

                    entry.getValue().get(iii).passthrough = entry.getValue().get(iii - 1).passthrough
                            + entry.getValue().get(iii - 1).entering - entry.getValue().get(iii).leaving;
                }
            }
        }
    }

    private FacilityVolumes getFacilityVolumesForRouteAndFacility(LineAndRoute lineAndRoute,
            Id<TransitStopFacility> id) {
        String lineRoute = lineAndRoute.transitLineId.toString() + "_" + lineAndRoute.transitRouteId.toString();
        int posOfFacility = checkIdInFacilityVolumeList(id, lineRouteFacilityVolumes.get(lineRoute));
        if (posOfFacility != -1) {
            return lineRouteFacilityVolumes.get(lineRoute).get(posOfFacility);
        }
        else {
            return null;
        }
    }

    private int checkIdInFacilityVolumeList(Id<TransitStopFacility> id, List<FacilityVolumes> list) {
        if (id == null)
            return -1;

        int pos = 0;
        for (FacilityVolumes facVolumes : list) {
            if (facVolumes.facilityId.toString().equals(id.toString()))
                return pos;
            pos++;
        }
        return -1;
    }

    public int[] getVolumesForRouteAndFacility(String lineRoute, Id<TransitStopFacility> id) {
        int[] volumes = new int[4];
        int posOfFacility = checkIdInFacilityVolumeList(id, lineRouteFacilityVolumes.get(lineRoute));
        if (posOfFacility != -1) {
            volumes[0] = lineRouteFacilityVolumes.get(lineRoute).get(posOfFacility).entering;
            volumes[1] = lineRouteFacilityVolumes.get(lineRoute).get(posOfFacility).leaving;
            volumes[2] = lineRouteFacilityVolumes.get(lineRoute).get(posOfFacility).passthrough;
            volumes[3] = lineRouteFacilityVolumes.get(lineRoute).get(posOfFacility).totalVolume;
        }
        else {
            volumes = null;
        }

        return volumes;
    }

    public String[] getLineRoutes() {
        return lineRouteFacilityVolumes.keySet().toArray(new String[lineRouteFacilityVolumes.keySet().size()]);
    }

    public List<Id<TransitStopFacility>> getFacilities(String lineRoute) {
        List<Id<TransitStopFacility>> facilities = new ArrayList<>();
        for (FacilityVolumes volumes : lineRouteFacilityVolumes.get(lineRoute)) {
            facilities.add(volumes.facilityId);
        }
        return facilities;
    }

}
