package EarthSim;

import EarthPresentation.DisplayMapProjection;
import core.Config;
import core.Settings;

/**
 * @author Kadeem Pardue
 * SimulatorEngine is what is invoked to create the simulation program.
 */
public class SimulatorEngine {
	/*
	 * Run the simulation engine
	 */
	public final void runSimulation(String[] args){
		 
		// Configure settings
		Config config = new Config();
		Settings settings = config.setupConfiguration(args);
		
		// TODO with those settings
		
		// TODO pseudocode - swapping plates from Heated Plate
		
		// Memory, performance metrics etc.
		Runtime runtime = Runtime.getRuntime();
		
		//Plate oldPlate = Plate.createPlate(type);
		//Plate newPlate = Plate.createPlate(type);

		//oldPlate.initialize(settings);
		//newPlate.initialize(settings);
		
    	// Create Earth Presentation
    	new DisplayMapProjection(); 
	}
}
