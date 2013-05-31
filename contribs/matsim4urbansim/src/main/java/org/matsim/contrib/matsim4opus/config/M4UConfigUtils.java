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

/**
 * 
 */
package org.matsim.contrib.matsim4opus.config;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.matsim.contrib.matsim4opus.config.modules.AccessibilityConfigModule;
import org.matsim.contrib.matsim4opus.config.modules.ImprovedPseudoPtConfigModule;
import org.matsim.contrib.matsim4opus.config.modules.M4UControlerConfigModuleV3;
import org.matsim.contrib.matsim4opus.config.modules.UrbanSimParameterConfigModuleV3;
import org.matsim.contrib.matsim4opus.constants.InternalConstants;
import org.matsim.contrib.matsim4opus.matsim4urbansim.jaxbconfig2.ConfigType;
import org.matsim.contrib.matsim4opus.matsim4urbansim.jaxbconfig2.Matsim4UrbansimType;
import org.matsim.contrib.matsim4opus.matsim4urbansim.jaxbconfig2.MatsimConfigType;
import org.matsim.contrib.matsim4opus.utils.ids.IdFactory;
import org.matsim.contrib.matsim4opus.utils.io.Paths;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.MatsimConfigReader;
import org.matsim.core.config.Module;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.VspExperimentalConfigGroup.VspExperimentalConfigKey;
import org.matsim.core.utils.io.UncheckedIOException;

/**
 * @author nagel
 *
 */
public class M4UConfigUtils {

	private static final Logger log = Logger.getLogger(M4UConfigUtils.class);
	// module and param names for matsim4urbansim settings stored in an external MATSim config file
	public static final String MATSIM4URBANSIM_MODULE_EXTERNAL_CONFIG = "matsim4urbansimParameter";// module
	public static final String URBANSIM_ZONE_SHAPEFILE_LOCATION_DISTRIBUTION = "urbanSimZoneShapefileLocationDistribution";
//	public static final String PT_STOPS = "ptStops";
//	public static final String PT_STOPS_SWITCH = "usePtStops";
//	public static final String PT_TRAVEL_TIMES = "ptTravelTimes";
//	public static final String PT_TRAVEL_DISTANCES = "ptTravelDistances";
//	public static final String PT_TRAVEL_TIMES_AND_DISTANCES_SWITCH = "useTravelTimesAndDistances";

	/**
	 * Setting 
	 * 
	 * @param matsim4UrbanSimParameter
	 * @param matsim4urbansimModule TODO
	 * @param config TODO
	 */
	static void initMATSim4UrbanSimControler(Matsim4UrbansimType matsim4UrbanSimParameter, 
			Module matsim4urbansimModule, Config config){

		boolean computeCellBasedAccessibility	= matsim4UrbanSimParameter.getMatsim4UrbansimContoler().isCellBasedAccessibility();
		boolean computeCellBasedAccessibilityNetwork   = false;
		boolean computeCellbasedAccessibilityShapeFile = false;

		String shapeFile						= matsim4UrbanSimParameter.getMatsim4UrbansimContoler().getShapeFileCellBasedAccessibility().getInputFile();

		// if cell-based accessibility is enabled, check whether a shapefile is given 
		if(computeCellBasedAccessibility){ 
			if(!Paths.pathExsits(shapeFile)){ // since no shape file found, accessibility computation is applied on the area covering the network
				computeCellBasedAccessibilityNetwork   = true;
				log.warn("No shape-file given or shape-file not found:" + shapeFile);
				log.warn("Instead the boundary of the road network is used to determine the area for which the accessibility computation is applied.");
				log.warn("This may be ok of that was your intention.") ;
				// yyyyyy the above is automagic; should be replaced by a flag.  kai, apr'13
			} else {
				computeCellbasedAccessibilityShapeFile = true;
			}
		}

		// ===

		// set parameter in module 
		M4UControlerConfigModuleV3 module = getMATSim4UrbaSimControlerConfigAndPossiblyConvert(config);
		module.setAgentPerformance(matsim4UrbanSimParameter.getMatsim4UrbansimContoler().isAgentPerformance());
		module.setZone2ZoneImpedance(matsim4UrbanSimParameter.getMatsim4UrbansimContoler().isZone2ZoneImpedance());
		module.setZoneBasedAccessibility(matsim4UrbanSimParameter.getMatsim4UrbansimContoler().isZoneBasedAccessibility());
		module.setCellBasedAccessibility(computeCellBasedAccessibility);
		
		ImprovedPseudoPtConfigModule ippcm = M4UImprovedPseudoPtConfigUtils.getConfigModuleAndPossiblyConvert(config) ;
//		ippcm.setPtStopsInputFile(ptStops);
//		ippcm.setPtTravelTimesInputFile(ptTravelTimes);
//		ippcm.setPtTravelDistancesInputFile(ptTravelDistances);
		
		AccessibilityConfigModule acm = M4UAccessibilityConfigUtils.getConfigModuleAndPossiblyConvert(config) ;
//		acm.setTimeOfDay(timeOfDay) ;
		acm.setUsingCustomBoundingBox(matsim4UrbanSimParameter.getMatsim4UrbansimContoler().isUseCustomBoundingBox());
		acm.setBoundingBoxLeft(matsim4UrbanSimParameter.getMatsim4UrbansimContoler().getBoundingBoxLeft());
		acm.setBoundingBoxBottom(matsim4UrbanSimParameter.getMatsim4UrbansimContoler().getBoundingBoxBottom());
		acm.setBoundingBoxRight(matsim4UrbanSimParameter.getMatsim4UrbansimContoler().getBoundingBoxRight());
		acm.setBoundingBoxTop(matsim4UrbanSimParameter.getMatsim4UrbansimContoler().getBoundingBoxTop());
		acm.setCellSizeCellBasedAccessibility(matsim4UrbanSimParameter.getMatsim4UrbansimContoler().getCellSizeCellBasedAccessibility().intValue());
		acm.setCellBasedAccessibilityShapeFile(computeCellbasedAccessibilityShapeFile);
		acm.setCellBasedAccessibilityNetwork(computeCellBasedAccessibilityNetwork);
		acm.setShapeFileCellBasedAccessibility(shapeFile);
	}

	/**
	 * store UrbanSimParameter
	 * 
	 * @param matsim4UrbanSimParameter
	 * @param matsim4urbansimModule TODO
	 * @param config TODO
	 */
	static void initUrbanSimParameter(Matsim4UrbansimType matsim4UrbanSimParameter, Module matsim4urbansimModule, Config config){

		// get every single matsim4urbansim/urbansimParameter
		String projectName 		= ""; // not needed anymore dec'12
		double populationSamplingRate = matsim4UrbanSimParameter.getUrbansimParameter().getPopulationSamplingRate();
		int year 				= matsim4UrbanSimParameter.getUrbansimParameter().getYear().intValue();

		boolean useShapefileLocationDistribution = false;
		String urbanSimZoneShapefileLocationDistribution = null;
		double randomLocationDistributionRadiusForUrbanSimZone = matsim4UrbanSimParameter.getUrbansimParameter().getRandomLocationDistributionRadiusForUrbanSimZone();

		if(matsim4urbansimModule != null)
			urbanSimZoneShapefileLocationDistribution = matsim4urbansimModule.getValue(URBANSIM_ZONE_SHAPEFILE_LOCATION_DISTRIBUTION);
		log.info("This message affects UrbanSim ZONE applications only:");
		if(urbanSimZoneShapefileLocationDistribution != null && Paths.pathExsits(urbanSimZoneShapefileLocationDistribution)){
			useShapefileLocationDistribution = true;
			log.info("Found a zone shape file: " + urbanSimZoneShapefileLocationDistribution);
			log.info("This activates the distribution of persons within a zone using the zone boundaries of this shape file."); 
		}
		else{
			log.info("Persons are distributed within a zone using the zone centroid and a radius of " + randomLocationDistributionRadiusForUrbanSimZone + " meter.");
			log.info("In order to use exact zone boundaries for your sceanrio provide a zone shape file and enter the file location in the external MATSim config file as follows:");
			log.info("<module name=\"matsim4urbansimParameter\" >");
			log.info("<param name=\"urbanSimZoneShapefileLocationDistribution\" value=\"/path/to/shapeFile\" />");
			log.info("</module>");
		}

		String opusHome 		= Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getOpusHome() );
		String opusDataPath 	= Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getOpusDataPath() );
		String matsim4Opus 		= Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4Opus() );
		String matsim4OpusConfig= Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4OpusConfig() );
		String matsim4OpusOutput= Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4OpusOutput() );
		String matsim4OpusTemp 	= Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4OpusTemp() );
		String matsim4OpusBackup= Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4Opus() ) + Paths.checkPathEnding( "backup" );
		boolean isTestRun 		= matsim4UrbanSimParameter.getUrbansimParameter().isIsTestRun();
		boolean backupRunData 	= matsim4UrbanSimParameter.getUrbansimParameter().isBackupRunData();
		String testParameter 	= matsim4UrbanSimParameter.getUrbansimParameter().getTestParameter();

		// // set parameter in module 
		UrbanSimParameterConfigModuleV3 module = getUrbanSimParameterConfigAndPossiblyConvert(config);
		module.setProjectName(projectName);
		// module.setSpatialUnitFlag(spatialUnit); // tnicolai not needed anymore dec'12
		module.setPopulationSampleRate(populationSamplingRate);
		module.setYear(year);
		module.setOpusHome(opusHome);
		module.setOpusDataPath(opusDataPath);
		module.setMATSim4Opus(matsim4Opus);
		module.setMATSim4OpusConfig(matsim4OpusConfig);
		module.setMATSim4OpusOutput(matsim4OpusOutput);
		module.setMATSim4OpusTemp(matsim4OpusTemp);
		module.setMATSim4OpusBackup(matsim4OpusBackup);
		module.setTestParameter(testParameter);
		module.setUsingShapefileLocationDistribution(useShapefileLocationDistribution);
		module.setUrbanSimZoneShapefileLocationDistribution(urbanSimZoneShapefileLocationDistribution);
		module.setUrbanSimZoneRadiusLocationDistribution(randomLocationDistributionRadiusForUrbanSimZone);
		module.setBackup(backupRunData);
		module.setTestRun(isTestRun);	

		// setting paths into constants structure
		InternalConstants.setOPUS_HOME(module.getOpusHome());
		InternalConstants.OPUS_DATA_PATH = module.getOpusDataPath();
		InternalConstants.MATSIM_4_OPUS = module.getMATSim4Opus();
		InternalConstants.MATSIM_4_OPUS_CONFIG = module.getMATSim4OpusConfig();
		InternalConstants.MATSIM_4_OPUS_OUTPUT = module.getMATSim4OpusOutput();
		InternalConstants.MATSIM_4_OPUS_TEMP = module.getMATSim4OpusTemp();
		InternalConstants.MATSIM_4_OPUS_BACKUP = module.getMATSim4OpusBackup();
	}

	/**
	 * @param matsim4urbansimConfigPart1
	 * @throws UncheckedIOException
	 */
	 static Module getM4UModuleFromExternalConfig(String externalMATSimConfigFilename) throws UncheckedIOException {

		if(externalMATSimConfigFilename != null && Paths.pathExsits(externalMATSimConfigFilename)){
			Config tempConfig = ConfigUtils.loadConfig( externalMATSimConfigFilename.trim() );

			// loading additional matsim4urbansim parameter settings from external config file
			Module module = tempConfig.getModule(MATSIM4URBANSIM_MODULE_EXTERNAL_CONFIG);
			if(module == null)
				log.info("No \""+ MATSIM4URBANSIM_MODULE_EXTERNAL_CONFIG + "\" settings found in " + externalMATSimConfigFilename);
			else
				log.info("Found \""+ MATSIM4URBANSIM_MODULE_EXTERNAL_CONFIG + "\" settings in " + externalMATSimConfigFilename);
			return module ;
		}
		return null ;
	}

	/**
	 * setting MATSim network
	 * 
	 * NOTE: If the MATSim4UrbanSim network section contains a road network 
	 * this overwrites a previous network, e.g. from an external MATSim configuration
	 * <p/>
	 * (The above statement is correct.  But irrelevant, since at the end everything is overwritten 
	 * again from an external MATSim configuration. kai, apr'13)
	 * 
	 * @param matsimParameter
	 * @param config TODO
	 */
	static void insertNetworkParams(ConfigType matsimParameter, Config config){
		log.info("Setting NetworkConfigGroup to config...");
		String networkFile = matsimParameter.getNetwork().getInputFile();
		if( !networkFile.isEmpty() )  // the MATSim4UrbanSim config contains a network file
			config.network().setInputFile( networkFile );
//		else
//			throw new RuntimeException("Missing MATSim network! The network must be specified either directly in the " +
//					"MATSim4UrbanSim configuration or in an external MATSim configuration.");
//
//		// yyyyyy ???  Aber es gibt die exception doch auch, wenn es in der external matsim config gesetzt ist? kai, apr'13
		// moved to consistency checker. kai, may'13

		log.info("...done!");
	}

	/**
	 * setting input plans file (for warm/hot start)
	 * 
	 * @param matsimParameter
	 * @param config TODO
	 */
	static void insertPlansParamsAndConfigureWarmOrHotStart(ConfigType matsimParameter, Config config){
		log.info("Checking for warm or hot start...");
		// get plans file for hot start
		String hotStartFileName = matsimParameter.getHotStartPlansFile().getInputFile();
		// get plans file for warm start 
		String warmStartFileName = matsimParameter.getInputPlansFile().getInputFile();

		M4UControlerConfigModuleV3 module = getMATSim4UrbaSimControlerConfigAndPossiblyConvert(config);

		// setting plans file as input
		if( !hotStartFileName.equals("")  && (new File(hotStartFileName)).exists() ) {
			log.info("Hot Start detcted!");
			setPlansFile( hotStartFileName, config );
			module.setHotStart(true);
		}
		else if( !warmStartFileName.equals("") ){
			log.info("Warm Start detcted!");
			setPlansFile( warmStartFileName, config );
			module.setWarmStart(true);
		}
		else{
			log.info("Cold Start (no plans file) detected!");
			module.setColdStart(true);
		}

		// setting target location for hot start plans file
		if(!hotStartFileName.equals("")){
			log.info("The resulting plans file after this MATSim run is stored at a specified place to enable hot start for the following MATSim run.");
			log.info("The specified place is : " + hotStartFileName);
			module.setHotStartTargetLocation(hotStartFileName);
		}
		else
			module.setHotStartTargetLocation("");
	}

	/**
	 * sets (either a "warm" or "hot" start) a plans file, see above.
	 * @param config TODO
	 */
	static void setPlansFile(String plansFile, Config config) {

		log.info("Setting PlansConfigGroup to config...");
		PlansConfigGroup plansCG = (PlansConfigGroup) config.getModule(PlansConfigGroup.GROUP_NAME);
		// set input plans file
		plansCG.setInputFile( plansFile );

		log.info("...done!");
	}

	/**
	 * setting controler parameter
	 * 
	 * @param matsimParameter
	 * @param config TODO
	 */
	static void initControler(ConfigType matsimParameter, Config config){
		log.info("Setting ControlerConfigGroup to config...");
		int firstIteration = matsimParameter.getControler().getFirstIteration().intValue();
		int lastIteration = matsimParameter.getControler().getLastIteration().intValue();
		ControlerConfigGroup controlerCG = config.controler() ;
		// set values
		controlerCG.setFirstIteration( firstIteration );
		controlerCG.setLastIteration( lastIteration);
		controlerCG.setOutputDirectory( InternalConstants.MATSIM_4_OPUS_OUTPUT );
		// yyyy don't use static variables (this is a variable albeit it claims to be a constant).  kai, may'13

//		controlerCG.setSnapshotFormat(Arrays.asList("otfvis")); // I don't think that this is necessary.  kai, may'13
		controlerCG.setWriteSnapshotsInterval( 0 ); // disabling snapshots

		// set Qsim
		controlerCG.setMobsim(QSimConfigGroup.GROUP_NAME);
		// yyyy if we do this, do we not get a warning that we should also put in a corresponding config group?  Maybe done later ...

		log.info("...done!");
	}

	/**
	 * setting planCalcScore parameter
	 * 
	 * @param matsimParameter
	 * @param config TODO
	 */
	static void insertPlanCalcScoreParams(ConfigType matsimParameter, Config config){
		log.info("Setting PlanCalcScore to config...");
		String activityType_0 = matsimParameter.getPlanCalcScore().getActivityType0();
		String activityType_1 = matsimParameter.getPlanCalcScore().getActivityType1();

		ActivityParams homeActivity = new ActivityParams(activityType_0);
		homeActivity.setTypicalDuration( matsimParameter.getPlanCalcScore().getHomeActivityTypicalDuration().intValue() ); 	// should be something like 12*60*60

		ActivityParams workActivity = new ActivityParams(activityType_1);
		workActivity.setTypicalDuration( matsimParameter.getPlanCalcScore().getWorkActivityTypicalDuration().intValue() );	// should be something like 8*60*60
		workActivity.setOpeningTime( matsimParameter.getPlanCalcScore().getWorkActivityOpeningTime().intValue() );			// should be something like 7*60*60
		workActivity.setLatestStartTime( matsimParameter.getPlanCalcScore().getWorkActivityLatestStartTime().intValue() );	// should be something like 9*60*60
		config.planCalcScore().addActivityParams( homeActivity );
		config.planCalcScore().addActivityParams( workActivity );

		log.info("...done!");
	}

	/**
	 * @param config TODO
	 * 
	 */
	static StrategyConfigGroup.StrategySettings getChangeLegModeStrategySettings(Config config) {
		Iterator<StrategyConfigGroup.StrategySettings> iter = config.strategy().getStrategySettings().iterator();
		StrategyConfigGroup.StrategySettings setting = null;
		while(iter.hasNext()){
			setting = iter.next();
			if(setting.getModuleName().equalsIgnoreCase("ChangeLegMode") || setting.getModuleName().equalsIgnoreCase("ChangeSingleLegMode"))
				break;
			setting = null;
		}
		return setting;
	}

	/**
	 * setting qsim
	 * @param matsim4urbansimConfig TODO
	 * @param config TODO
	 */
	static void initQSim(MatsimConfigType matsim4urbansimConfig, Config config){
		log.info("Setting QSimConfigGroup to config...");

		QSimConfigGroup qsimCG = config.getQSimConfigGroup();
		if( qsimCG == null){		
			qsimCG = new QSimConfigGroup();
			config.addQSimConfigGroup( qsimCG );
		}

		// setting number of threads
		//		qsimCG.setNumberOfThreads(Runtime.getRuntime().availableProcessors());
		// log.error("setting qsim number of threads automagically; this is almost certainly not good; fix") ;
		// just changed this, setting it to one:  kai, apr'13
		qsimCG.setNumberOfThreads(1);

		double popSampling = matsim4urbansimConfig.getMatsim4Urbansim().getUrbansimParameter().getPopulationSamplingRate();
		log.info("FlowCapFactor and StorageCapFactor are adapted to the population sampling rate (sampling rate = " + popSampling + ").");
		// setting FlowCapFactor == population sampling rate (no correction factor needed here)
		qsimCG.setFlowCapFactor( popSampling );	

		// Adapting the storageCapFactor has the following reason:
		// Too low SorageCapacities especially with small sampling 
		// rates can (eg 1%) lead to strong backlogs on the traffic network. 
		// This leads to an unstable behavior of the simulation (by breakdowns 
		// during the learning progress).
		// The correction fetch factor introduced here raises the 
		// storage capacity at low sampling rates and becomes flatten 
		// with increasing sampling rates (at a 100% sample, the 
		// storage capacity == 1).			tnicolai nov'11
		if(popSampling <= 0.){
			// yyyyyy how can this happen???? kai, apr'13
			// yyyyyy if this has happens, it is plausible that the flow cap factor is NOT corrected??? kai, apr'13
			double popSamplingBefore = popSampling ;
			popSampling = 0.01;
			log.warn("Raised popSampling rate from " + popSamplingBefore + 
					" to " + popSampling + " to to avoid errors while calulating the correction factor ...");
		}
		// tnicolai dec'11
		double storageCapCorrectionFactor = Math.pow(popSampling, -0.25);	// same as: / Math.sqrt(Math.sqrt(sample))
		// setting StorageCapFactor
		qsimCG.setStorageCapFactor( popSampling * storageCapCorrectionFactor );	

		qsimCG.setRemoveStuckVehicles( false );
		qsimCG.setStuckTime(10.);
		qsimCG.setEndTime(30.*3600.); // 30h

		log.info("...done!");
	}

//	/**
//	 * setting walk speed in plancalcroute
//	 * @param config TODO
//	 */
//	static void initPlanCalcRoute(Config config){
//		log.info("Setting PlanCalcRouteGroup to config...");
//
//		double defaultWalkSpeed = 1.38888889; 	// 1.38888889m/s corresponds to 5km/h -- alternatively: use 0.833333333333333m/s corresponds to 3km/h
//		double defaultBicycleSpeed = 4.16666666;// 4.16666666m/s corresponds to 15 km/h
//		double defaultPtSpeed 	= 6.94444444;	// 6.94444444m/s corresponds to 25 km/h
//
//		//  log.error( "ignoring any external default speeds for walk/bicycle/pt and using internal values.  fix!!" ) ;
//		//  this is not a problem since the complete config is overwritten by the external config at the very end.
//
//		/*
//		 * To me this seems to be a problem. PlansCalcRoute is intialized with some defaults. Using the direct setters will NOT clear
//		 * the defaults, but only add the new values. E.g. the for pt the default is FreespeedFactor. Daniel, May '13
//		 * yyyyyy This silently does something to the matsim default values.  "silently" is not good.  Do we need this at all
//		 * (except maybe for backwards compatability)?  Kai, may'13
//		 */
//		// setting teleportation speeds in router
//		config.plansCalcRoute().setWalkSpeed( defaultWalkSpeed ); 
//		config.plansCalcRoute().setBikeSpeed( defaultBicycleSpeed );
//		config.plansCalcRoute().setPtSpeed( defaultPtSpeed );
//
//		log.info("...done!");
//	}

	/**
	 * setting strategy
	 * @param config TODO
	 */
	static void insertStrategyParams(ConfigType matsim4urbansimConfig, Config config){
		log.info("Setting StrategyConfigGroup to config...");

		// some modules are disables after 80% of overall iterations, 
		// last iteration for them determined here tnicolai feb'12
		int disableStrategyAfterIteration = (int) Math.ceil(config.controler().getLastIteration() * 0.8);

		// configure strategies for re-planning (should be something like 5)
		config.strategy().setMaxAgentPlanMemorySize( matsim4urbansimConfig.getStrategy().getMaxAgentPlanMemorySize().intValue() );

		StrategyConfigGroup.StrategySettings timeAlocationMutator = new StrategyConfigGroup.StrategySettings(IdFactory.get(1));
		timeAlocationMutator.setModuleName("TimeAllocationMutator"); 	// module name given in org.matsim.core.replanning.StrategyManagerConfigLoader
		timeAlocationMutator.setProbability( matsim4urbansimConfig.getStrategy().getTimeAllocationMutatorProbability() ); // should be something like 0.1
		timeAlocationMutator.setDisableAfter(disableStrategyAfterIteration);
		config.strategy().addStrategySettings(timeAlocationMutator);
		// change mutation range to 2h. tnicolai feb'12
		config.setParam("TimeAllocationMutator", "mutationRange", "7200"); 

		StrategyConfigGroup.StrategySettings changeExpBeta = new StrategyConfigGroup.StrategySettings(IdFactory.get(2));
		changeExpBeta.setModuleName("ChangeExpBeta");					// module name given in org.matsim.core.replanning.StrategyManagerConfigLoader
		changeExpBeta.setProbability( matsim4urbansimConfig.getStrategy().getChangeExpBetaProbability() ); // should be something like 0.9
		config.strategy().addStrategySettings(changeExpBeta);

		StrategyConfigGroup.StrategySettings reroute = new StrategyConfigGroup.StrategySettings(IdFactory.get(3));
		reroute.setModuleName("ReRoute");  // old name "ReRoute_Dijkstra"						// module name given in org.matsim.core.replanning.StrategyManagerConfigLoader
		reroute.setProbability( matsim4urbansimConfig.getStrategy().getReRouteDijkstraProbability() ); 	// should be something like 0.1
		reroute.setDisableAfter(disableStrategyAfterIteration);
		config.strategy().addStrategySettings(reroute);

		// check if a 4th module is given in the external MATSim config
		// the external config is not loaded at this point. Thus, a possible 4th module is only load with the settings from the external config...
		StrategyConfigGroup.StrategySettings changeLegMode = getChangeLegModeStrategySettings(config);
		boolean set4thStrategyModule = ( changeLegMode != null && 
				( changeLegMode.getModuleName().equalsIgnoreCase("ChangeLegMode") || changeLegMode.getModuleName().equalsIgnoreCase("ChangeSingleLegMode")) && 
				changeLegMode.getProbability() > 0.);
		if(set4thStrategyModule){
			// to be consistent, setting the same iteration number as in the strategies above 
			changeLegMode.setDisableAfter(disableStrategyAfterIteration);
			log.warn("setting disableStrategyAfterIteration for ChangeLegMode to " + disableStrategyAfterIteration + "; possibly overriding config settings!");
			// check if other modes are set
			Module changelegMode = config.getModule("changeLegMode");
			if(changelegMode != null && changelegMode.getValue("modes") != null)
				log.info("Following modes are found: " + changelegMode.getValue("modes"));
		}
		log.info("...done!");
	}

	/**
	 * loads the external config into a temporary structure
	 * this is done to initialize MATSim4UrbanSim settings that are defined
	 * in the external MATSim config
	 * 
	 * @param matsimParameter
	 * @param config TODO
	 * @throws UncheckedIOException
	 */
	static void loadExternalConfigAndOverwriteMATSim4UrbanSimSettings(ConfigType matsimParameter, Config config) throws UncheckedIOException {
		// check if external MATsim config is given
		String externalMATSimConfig = matsimParameter.getMatsimConfig().getInputFile();
		if(externalMATSimConfig != null && Paths.pathExsits(externalMATSimConfig)){

			log.info("Loading settings from external MATSim config: " + externalMATSimConfig);
			log.warn("NOTE: MATSim4UrbanSim settings will be overwritten by settings in the external config! Make sure that this is what you intended!");
			new MatsimConfigReader(config).parse(externalMATSimConfig);
			log.info("... loading settings done!");
		}
	}

	/**
	 * creates an empty MATSim config to be filled by MATSim4UrbanSim + external MATSim config settings
	 */
	static Config createEmptyConfigWithSomeDefaults() {
		log.info("Creating an empty MATSim scenario.");
		Config config = ConfigUtils.createConfig();

		//"materialize" the local config groups:
		config.addModule(UrbanSimParameterConfigModuleV3.GROUP_NAME, 
				new UrbanSimParameterConfigModuleV3(UrbanSimParameterConfigModuleV3.GROUP_NAME) ) ;
		config.addModule(M4UControlerConfigModuleV3.GROUP_NAME,
				new M4UControlerConfigModuleV3());
		config.addModule(AccessibilityConfigModule.GROUP_NAME,
				new AccessibilityConfigModule()) ;

		// set some defaults:
		VspExperimentalConfigGroup vsp = config.vspExperimental();
		vsp.addParam(VspExperimentalConfigKey.vspDefaultsCheckingLevel, VspExperimentalConfigGroup.ABORT ) ;
		vsp.setActivityDurationInterpretation(VspExperimentalConfigGroup.ActivityDurationInterpretation.tryEndTimeThenDuration) ;
		vsp.setRemovingUnneccessaryPlanAttributes(true) ;

		return config ;
	}

	/**
	 * loading, validating and initializing MATSim config.
	 */
	static MatsimConfigType unmarschal(String matsim4urbansimConfigFilename){

		// JAXBUnmaschal reads the UrbanSim generated MATSim config, validates it against
		// the current xsd (checks e.g. the presents and data type of parameter) and generates
		// an Java object representing the config file.
		JAXBUnmaschalV2 unmarschal = new JAXBUnmaschalV2( matsim4urbansimConfigFilename );

		MatsimConfigType matsim4urbansimConfig = null;

		// binding the parameter from the MATSim Config into the JAXB data structure
		if( (matsim4urbansimConfig = unmarschal.unmaschalMATSimConfig()) == null){
			log.error("Unmarschalling failed. SHUTDOWN MATSim!");
			System.exit(InternalConstants.UNMARSCHALLING_FAILED);
		}
		return matsim4urbansimConfig;
	}

	public static UrbanSimParameterConfigModuleV3 getUrbanSimParameterConfigAndPossiblyConvert(Config config) {
		Module m = config.getModule(UrbanSimParameterConfigModuleV3.GROUP_NAME);
		if (m instanceof UrbanSimParameterConfigModuleV3) {
			return (UrbanSimParameterConfigModuleV3) m;
		}
		UrbanSimParameterConfigModuleV3 upcm = new UrbanSimParameterConfigModuleV3(UrbanSimParameterConfigModuleV3.GROUP_NAME);
		//		config.getModules().put(UrbanSimParameterConfigModuleV3.GROUP_NAME, upcm);
		// yyyyyy the above code does NOT convert but throws the config entries away.
		// In contrast, config.addModule(...) would convert.  kai, may'13 
		// I just changed that:
		config.addModule( UrbanSimParameterConfigModuleV3.GROUP_NAME, upcm ) ;
		return upcm;
	}

	public static M4UControlerConfigModuleV3 getMATSim4UrbaSimControlerConfigAndPossiblyConvert(Config config) {
		Module m = config.getModule(M4UControlerConfigModuleV3.GROUP_NAME);
		if (m instanceof M4UControlerConfigModuleV3) {
			return (M4UControlerConfigModuleV3) m;
		}
		M4UControlerConfigModuleV3 mccm = new M4UControlerConfigModuleV3();
		//		config.getModules().put(MATSim4UrbanSimControlerConfigModuleV3.GROUP_NAME, mccm);
		// yyyyyy the above code does NOT convert but throws the config entries away.
		// In contrast, config.addModule(...) would convert.  kai, may'13
		// I just changed that:
		config.addModule(M4UControlerConfigModuleV3.GROUP_NAME, mccm ) ;
		return mccm;
	}

	/**
	 * returns a matsim4urbansim parameter as double or zero in case of conversion errors.
	 * 
	 * @param paramName
	 * @return matsim4urbansim parameter as double
	 */
	private static double getValueAsDouble(Module module, String paramName){
		if(module != null){
			try{
				double tmp = Double.parseDouble(module.getValue(paramName));
				return tmp;
			} catch(Exception e){}
			return 0.;
		}
		return 0.;
	}

	/**
	 * printing UrbanSimParameterSettings
	 */
	static void printUrbanSimParameterSettings( UrbanSimParameterConfigModuleV3 module) {

		//		UrbanSimParameterConfigModuleV3 module = this.getUrbanSimParameterConfig();

		log.info("UrbanSimParameter settings:");
		log.info("ProjectName: " + module.getProjectName() );
		log.info("PopulationSamplingRate: " + module.getPopulationSampleRate() );
		log.info("Year: " + module.getYear() ); 
		log.info("OPUS_HOME: " + InternalConstants.getOPUS_HOME() );
		log.info("OPUS_DATA_PATH: " + InternalConstants.OPUS_DATA_PATH );
		log.info("MATSIM_4_OPUS: " + InternalConstants.MATSIM_4_OPUS );
		log.info("MATSIM_4_OPUS_CONIG: " + InternalConstants.MATSIM_4_OPUS_CONFIG );
		log.info("MATSIM_4_OPUS_OUTPUT: " + InternalConstants.MATSIM_4_OPUS_OUTPUT );
		log.info("MATSIM_4_OPUS_TEMP: " + InternalConstants.MATSIM_4_OPUS_TEMP ); 
		log.info("MATSIM_4_OPUS_BACKUP: " + InternalConstants.MATSIM_4_OPUS_BACKUP );
		log.info("(Custom) Test Parameter: " + module.getTestParameter() );
		log.info("UsingShapefileLocationDistribution:" + module.isUsingShapefileLocationDistribution());
		log.info("UrbanSimZoneShapefileLocationDistribution:" + module.getUrbanSimZoneShapefileLocationDistribution());
		log.info("RandomLocationDistributionRadiusForUrbanSimZone:" + module.getUrbanSimZoneRadiusLocationDistribution());
		log.info("Backing Up Run Data: " + module.isBackup() );
		log.info("Is Test Run: " + module.isTestRun() );
	}

	/**
	 * printing MATSim4UrbanSimControlerSettings
	 */
	static void printMATSim4UrbanSimControlerSettings( M4UControlerConfigModuleV3 module ) {

		//		MATSim4UrbanSimControlerConfigModuleV3 module = getMATSim4UrbaSimControlerConfig();

		// view results
		log.info("MATSim4UrbanSimControler settings:");
		log.info("Compute Agent-performance: " + module.isAgentPerformance() );
		log.info("Compute Zone2Zone Impedance Matrix: " + module.isZone2ZoneImpedance() ); 
		log.info("Compute Zone-Based Accessibilities: " + module.isZoneBasedAccessibility() );
//		log.info("Compute Parcel/Cell-Based Accessibilities (using ShapeFile): " + module.isCellBasedAccessibilityShapeFile() ); 
//		log.info("Compute Parcel/Cell-Based Accessibilities (using Network Boundaries): " + module.isCellBasedAccessibilityNetwork() );
//		log.info("Cell Size: " + module.getCellSizeCellBasedAccessibility() );
//		log.info("Using (Custom) Network Boundaries: " + module.usingCustomBoundingBox() );
//		log.info("Network Boundary (Top): " + module.getBoundingBoxTop() ); 
//		log.info("Network Boundary (Left): " + module.getBoundingBoxLeft() ); 
//		log.info("Network Boundary (Right): " + module.getBoundingBoxRight() ); 
//		log.info("Network Boundary (Bottom): " + module.getBoundingBoxBottom() ); 
//		log.info("Shape File: " + module.getShapeFileCellBasedAccessibility() );
//		log.info("Time of day: " + module.getTimeOfDay() );
//		log.info("Pt Stops Input File: " + module.getPtStopsInputFile());
//		log.info("Pt Travel Times Input File: " + module.getPtTravelTimesInputFile());
//		log.info("Pt travel Distances Input File: " + module.getPtTravelDistancesInputFile());
	}

	static final void checkConfigConsistencyAndWriteToLog(Config config, final String message) {
		String newline = System.getProperty("line.separator");// use native line endings for logfile
		log.info(newline + newline + message + ":");
		StringWriter writer = new StringWriter();
		new ConfigWriter(config).writeStream(new PrintWriter(writer), newline);
		log.info(newline + "Complete config dump:" + newline + writer.getBuffer().toString());
		log.info("Complete config dump done.");
		log.info("Checking consistency of config...");
		config.checkConsistency();
		log.info("Checking consistency of config done.");
		log.info("("+message+")" + newline + newline ) ;
	}

}
