package edu.univalle.network;

import java.util.Random;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

/**
 * How to tell the Controler *not* to use the Events-collecting
 * TravelTimeCalculator to provide the TravelTime for the next iteration,
 * but to use the custom Feli's travel time instead.
 *
 * @author michaz
 *
 */
public class MIOTravelTime implements TravelTime
{
	int seed = 12475;
	Random rand = new Random(seed);

	public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle)
	{
		Double prob = rand.nextDouble();
		if (prob < 0.5)
			return link.getLength() / 22.222222222222222;
		else
			return link.getLength() / 1.388888888888889;
	}

}
