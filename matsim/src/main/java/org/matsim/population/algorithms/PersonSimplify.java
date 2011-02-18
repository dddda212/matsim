/* *********************************************************************** *
 * project: org.matsim.*
 * PersonSimplify.java
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

package org.matsim.population.algorithms;

import java.util.List;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PlanImpl;

public class PersonSimplify extends AbstractPersonAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

//	private final int max_short_dur = 4*3600;

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public PersonSimplify() {
		super();
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(Person person) {
		List plans = person.getPlans();
		for (int i=0; i<plans.size(); i++) {
			PlanImpl plan = (PlanImpl)plans.get(i);

			// set the first and last activty to home
			List acts_legs = plan.getPlanElements();
			((Activity)acts_legs.get(0)).setType("h");
			((Activity)acts_legs.get(acts_legs.size()-1)).setType("h");

			// set all "m" activities (unspecified ones) to "l" (what else should i do....)
			for (int j=0; j<acts_legs.size(); j+=2) {
				Activity act = (ActivityImpl)acts_legs.get(j);
				if (act.getType().equals("m")) {
					act.setType("l");
				}
			}

			// if start time is after midnight subtract 24 hours
			double end_time = ((Activity)acts_legs.get(0)).getEndTime();
			if (end_time >= 24*3600) { ((Activity)acts_legs.get(0)).setEndTime(end_time-24*3600); }

			// group the durations to predefined ones. (manually choosen according to the
			// duration distributions of the given microcensus2000)
			// in between homes:
			// 0-3 => 1.5 ; 3-13 => 5
			// work:
			// 0-3 => 2 ; 3-7 => 4 ; 7-19 => 8
			// educ:
			// 0-3 => 2 ; 3-5 => 4 ; 5-20 => 8
			// shop:
			// 0-2 => 2 ; 2-13 => 4
			// leis:
			// 0-3 => 2 ; 3-19 => 5
			for (int j=2; j<acts_legs.size()-2; j+=2) {
				ActivityImpl act = (ActivityImpl)acts_legs.get(j);
				if (act.getType().equals("h")) {
					if (act.getMaximumDuration() < 3*3600) { act.setMaximumDuration((int)(1.5*3600)); }
					else { act.setMaximumDuration(5*3600); }
				}
				else if (act.getType().equals("w")) {
					if (act.getMaximumDuration() < 3*3600) { act.setMaximumDuration(2*3600); }
					else if (act.getMaximumDuration() < 7*3600) { act.setMaximumDuration(4*3600); }
					else { act.setMaximumDuration(8*3600); }
				}
				else if (act.getType().equals("e")) {
					if (act.getMaximumDuration() < 3*3600) { act.setMaximumDuration(2*3600); }
					else if (act.getMaximumDuration() < 5*3600) { act.setMaximumDuration(4*3600); }
					else { act.setMaximumDuration(8*3600); }
				}
				else if (act.getType().equals("s")) {
					if (act.getMaximumDuration() < 2*3600) { act.setMaximumDuration(1*3600); }
					else { act.setMaximumDuration(4*3600); }
				}
				else if (act.getType().equals("l")) {
					if (act.getMaximumDuration() < 3*3600) { act.setMaximumDuration(2*3600); }
					else { act.setMaximumDuration(5*3600); }
				}
				else {
					Gbl.errorMsg("[something is wrong]");
				}
			}

//			// merge activities to one which are next to each other and have the same type
//			for (int j=acts_legs.size()-1; j>0; j-=2) {
//				Act prev_act = (Act)acts_legs.get(j-2);
//				Act this_act = (Act)acts_legs.get(j);
//				if (prev_act.getType().equals(this_act.getType())) {
//					prev_act.setDur(prev_act.getDur()+this_act.getDur());
//					plan.removeAct(j);
//				}
//			}
		}

//		// remove plans which have only one activity
//		for (int i=plans.size()-1; i>=0; i--) {
//			if (((Plan)plans.get(i)).getActsLegs().size() < 2) {
//				plans.remove(i);
//			}
//		}
	}
}
