/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package playground.jbischoff.taxibus.scheduler;
import java.util.Set;


import playground.jbischoff.taxibus.passenger.TaxibusRequest;



public interface TaxibusTaskWithRequests
    extends TaxibusTask
{
    Set<TaxibusRequest> getRequests();


    //called (when removing a task) in order to update the request2task assignment 
    void removeFromRequest(TaxibusRequest request);
    void removeFromAllRequests();
}