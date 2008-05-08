/* *********************************************************************** *
 * project: org.matsim.*
 * EnterpriseCensusParser.java
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

package org.matsim.enterprisecensus;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.matsim.gbl.Gbl;

public class EnterpriseCensusParser {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private static Logger log = Logger.getLogger(EnterpriseCensusParser.class);

	//////////////////////////////////////////////////////////////////////
	// constructor
	//////////////////////////////////////////////////////////////////////

	public EnterpriseCensusParser(EnterpriseCensus enterpriseCensus) {
	}

	public void parse(EnterpriseCensus enterpriseCensus) {
		this.readPresenceCodes(enterpriseCensus);
		this.readHectareAggregations(enterpriseCensus);
	}

	private final void readPresenceCodes(EnterpriseCensus enterpriseCensus) {

		log.info("Reading the presence code file...");

		int lineCounter = 0;
		int skip = 1;

		String filename = Gbl.getConfig().getParam(EnterpriseCensus.EC_MODULE, EnterpriseCensus.EC_PRESENCECODEFILE);
		String separator = Gbl.getConfig().getParam(EnterpriseCensus.EC_MODULE, EnterpriseCensus.EC_PRESENCECODESEPARATOR);

		File file = new File(filename);

		LineIterator it = null;
		String line = null;
		String[] tokens = null;
		String reli = null;
		TreeMap<Integer, String> presenceCodesNOGATypes = new TreeMap<Integer, String>();

		try {
			it = FileUtils.lineIterator(file, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while (it.hasNext()) {
				line = it.nextLine();
				tokens = line.split(separator);

				if (lineCounter == 0) {
					log.info("Processing header line...");
					for (int pos = 3; pos < tokens.length; pos++) {
						presenceCodesNOGATypes.put(new Integer(pos), EnterpriseCensus.trim(tokens[pos], '"'));
					}
					log.info("Processing header line...done.");
				} else {

					reli = tokens[0];
					for (int pos = 3; pos < tokens.length; pos++) {
						if (Pattern.matches("1", tokens[pos])) {
							enterpriseCensus.addPresenceCode(reli, presenceCodesNOGATypes.get(new Integer(pos)));
						}
					}					
				}

				lineCounter++;
				if (lineCounter % skip == 0) {
					log.info("Processed hectares: " + Integer.toString(lineCounter));
					skip *= 2;
				}
			}
		} finally {
			LineIterator.closeQuietly(it);
		}

		log.info("Processed hectares: " + Integer.toString(lineCounter));

		log.info("Reading the presence code file...done.");

	}

	private final void readHectareAggregations(EnterpriseCensus enterpriseCensus) {

		log.info("Reading the hectare aggregation file...");

		String filename = Gbl.getConfig().getParam(EnterpriseCensus.EC_MODULE, EnterpriseCensus.EC_INPUTHECTAREAGGREGATIONFILE);
		String separator = Gbl.getConfig().getParam(EnterpriseCensus.EC_MODULE, EnterpriseCensus.EC_INPUTHECTAREAGGREGATIONSEPARATOR);
		File file = new File(filename);

		LineIterator it = null;
		String line = null;
		String[] tokens = null;
		String reli = null;
		int lineCounter = 0, skip = 1;
		TreeMap<Integer, String> hectareAggregationsNOGATypes = new TreeMap<Integer, String>();

		try {
			it = FileUtils.lineIterator(file, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while (it.hasNext()) {
				line = it.nextLine();
				tokens = line.split(separator);

				if (lineCounter == 0) {
					log.info("Processing header line...");
					for (int pos = 3; pos < tokens.length; pos++) {
						hectareAggregationsNOGATypes.put(new Integer(pos), EnterpriseCensus.trim(tokens[pos], '"'));
					}
					log.info("Processing header line...done.");
				} else {

					reli = tokens[0];
					for (int pos = 3; pos < tokens.length; pos++) {
						if (!Pattern.matches("0", tokens[pos])) {
							enterpriseCensus.addHectareAggregationInformation(
									reli, 
									hectareAggregationsNOGATypes.get(new Integer(pos)),
									Double.parseDouble(tokens[pos]));
						}
					}					
				}

				lineCounter++;
				if (lineCounter % skip == 0) {
					log.info("Processed hectares: " + Integer.toString(lineCounter));
					skip *= 2;
				}
			}
		} finally {
			LineIterator.closeQuietly(it);
		}

		log.info("Processed hectares: " + Integer.toString(lineCounter));

		log.info("Reading the hectare aggregation file...done.");

	}
}
