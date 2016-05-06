package edu.univalle.mobsim.qsim.qnetsimengine;

import javax.inject.Inject;
import javax.inject.Provider;

//import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine;
//import org.matsim.core.mobsim.qsim.qnetsimengine.VehicularDepartureHandler;

class QNetsimEngineDepartureHandlerProvider implements Provider<VehicularDepartureHandler>
{

    @Inject
    QNetsimEngine qNetsimEngine;

    @Override
    public VehicularDepartureHandler get() {
        return qNetsimEngine.getDepartureHandler();
    }
}
