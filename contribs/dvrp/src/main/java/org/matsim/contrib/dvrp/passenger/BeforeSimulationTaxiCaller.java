/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package org.matsim.contrib.dvrp.passenger;

import java.util.Collection;

import org.matsim.api.core.v01.population.*;
import org.matsim.core.mobsim.framework.*;
import org.matsim.core.mobsim.framework.events.MobsimInitializedEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimInitializedListener;
import org.matsim.core.mobsim.qsim.QSim;


public class BeforeSimulationTaxiCaller
    implements MobsimInitializedListener
{
    private final PassengerEngine passengerEngine;


    public BeforeSimulationTaxiCaller(PassengerEngine passengerEngine)
    {
        this.passengerEngine = passengerEngine;
    }


    @Override
    public void notifyMobsimInitialized(@SuppressWarnings("rawtypes") MobsimInitializedEvent e)
    {
        Collection<MobsimAgent> agents = ((QSim)e.getQueueSimulation()).getAgents();
        String mode = passengerEngine.getMode();

        for (MobsimAgent mobsimAgent : agents) {
            if (mobsimAgent instanceof PlanAgent) {
                Plan plan = ((PlanAgent)mobsimAgent).getCurrentPlan();

                for (PlanElement elem : plan.getPlanElements()) {
                    if (elem instanceof Leg) {
                        Leg leg = (Leg)elem;

                        if (leg.getMode().equals(mode)) {
                            passengerEngine.callAhead(0, (MobsimPassengerAgent)mobsimAgent, leg);
                        }
                    }
                }
            }
        }
    }
}
