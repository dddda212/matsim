/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package org.matsim.contrib.taxi.run;

import org.matsim.contrib.dvrp.data.VrpData;
import org.matsim.contrib.dynagent.run.DynRoutingModule;
import org.matsim.contrib.taxi.optimizer.*;
import org.matsim.contrib.taxi.util.TaxiSimulationConsistencyChecker;
import org.matsim.contrib.taxi.util.stats.*;
import org.matsim.core.controler.AbstractModule;
import org.matsim.vehicles.*;

import com.google.inject.name.Names;


public class TaxiModule
    extends AbstractModule
{
    public static final String TAXI_MODE = "taxi";

    private final VrpData taxiData;
    private final VehicleType vehicleType;


    public TaxiModule(VrpData taxiData)
    {
        this(taxiData, VehicleUtils.getDefaultVehicleType());
    }


    public TaxiModule(VrpData taxiData, VehicleType vehicleType)
    {
        this.taxiData = taxiData;
        this.vehicleType = vehicleType;
    }


    @Override
    public void install()
    {
        addRoutingModuleBinding(TAXI_MODE).toInstance(new DynRoutingModule(TAXI_MODE));
        bind(VrpData.class).toInstance(taxiData);
        bind(VehicleType.class).annotatedWith(Names.named(TAXI_MODE)).toInstance(vehicleType);

        bind(TaxiOptimizerFactory.class).to(DefaultTaxiOptimizerFactory.class);
        // yyyy: Why is this a factory?  See comment in TaxiOptimizerFactory.  kai, jan'2017

        addControlerListenerBinding().to(TaxiSimulationConsistencyChecker.class);
        addControlerListenerBinding().to(TaxiStatsDumper.class);

        if (TaxiConfigGroup.get(getConfig()).getTimeProfiles()) {
            addMobsimListenerBinding().toProvider(TaxiStatusTimeProfileCollectorProvider.class);
            //add more time profiles if necessary
        }
    }
}
