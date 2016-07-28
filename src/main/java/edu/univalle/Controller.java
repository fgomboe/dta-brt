package edu.univalle;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import java.util.Map;

import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.qsim.ActivityEngine;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.TeleportationEngine;
import org.matsim.core.mobsim.qsim.agents.AgentFactory;
import org.matsim.core.mobsim.qsim.agents.DefaultAgentFactory;
import org.matsim.core.mobsim.qsim.agents.PopulationAgentSource;
import org.matsim.core.mobsim.qsim.agents.TransitAgentFactory;
import org.matsim.core.mobsim.qsim.changeeventsengine.NetworkChangeEventsEngine;
import org.matsim.core.mobsim.qsim.pt.ComplexTransitStopHandlerFactory;
import org.matsim.core.mobsim.qsim.pt.TransitQSimEngine;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine;
import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.univalle.mobsim.MIOLinkSpeedCalculator;
import edu.univalle.statistics.ControllerRouteVolumesListener;

public class Controller
{

    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("input/config_dummy.xml");
        Controler controler = new Controler(config);

        // Uncomment to install custom speed for buses (MIO)
        /*controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bindMobsim().toProvider(CustomSpeedOnLinks.class);
            }
        });*/

        ControllerRouteVolumesListener otroController = new ControllerRouteVolumesListener(61200, 68400);
        controler.addControlerListener(otroController);
        controler.run();
        System.out.println("El peor GEH es: " + otroController.getWorstGEH());
        Map<String, double[][]> gehValues = otroController.getGEHMatrix();
        for (double[][] geh : gehValues.values()) {
            for (double[] estacion : geh) {
                // System.out.println(estacion[0] + "\t" + estacion[1] + "\t" + estacion[2] + "\t" + estacion[3]);
            }
            // System.out.println("");
        }

        controler.run();
    }

    static class CustomSpeedOnLinks implements Provider<QSim>
    {

        @Inject
        Scenario scenario;
        @Inject
        EventsManager eventsManager;

        @Override
        public QSim get() {
            QSimConfigGroup conf = scenario.getConfig().qsim();
            if (conf == null) {
                throw new NullPointerException(
                        "There is no configuration set for the QSim. Please add the module 'qsim' to your config file.");
            }

            QSim qSim = new QSim(scenario, eventsManager);

            /*MessageQueue messageQueue = new MessageQueue();
            SteppableScheduler steppableScheduler = new SteppableScheduler(messageQueue);
            MessageQueueEngine messageQueueEngine = new MessageQueueEngine(steppableScheduler);
            qSim.addQueueSimulationListeners(messageQueueEngine);*/
            //
            ActivityEngine activityEngine = new ActivityEngine(eventsManager, qSim.getAgentCounter());
            qSim.addMobsimEngine(activityEngine);
            qSim.addActivityHandler(activityEngine);
            //
            QNetsimEngine netsimEngine = new QNetsimEngine(qSim);
            netsimEngine.setLinkSpeedCalculator(new MIOLinkSpeedCalculator());
            qSim.addMobsimEngine(netsimEngine);
            qSim.addDepartureHandler(netsimEngine.getDepartureHandler());
            //
            TeleportationEngine teleportationEngine = new TeleportationEngine(scenario, eventsManager);
            qSim.addMobsimEngine(teleportationEngine);

            AgentFactory agentFactory;
            if (scenario.getConfig().transit().isUseTransit()) {
                agentFactory = new TransitAgentFactory(qSim);
                TransitQSimEngine transitEngine = new TransitQSimEngine(qSim);
                transitEngine.setTransitStopHandlerFactory(new ComplexTransitStopHandlerFactory());
                qSim.addDepartureHandler(transitEngine);
                qSim.addAgentSource(transitEngine);
                qSim.addMobsimEngine(transitEngine);
            }
            else {
                agentFactory = new DefaultAgentFactory(qSim);
            }

            if (scenario.getConfig().network().isTimeVariantNetwork()) {
                qSim.addMobsimEngine(new NetworkChangeEventsEngine());
            }
            PopulationAgentSource agentSource = new PopulationAgentSource(scenario.getPopulation(), agentFactory, qSim);
            qSim.addAgentSource(agentSource);
            return qSim;
        }
    }
}