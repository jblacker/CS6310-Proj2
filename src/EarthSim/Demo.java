package EarthSim;

import EarthPresentation.MasterGui;
import core.Config;

public class Demo {
	/* 
	 * Starts Heated Earth application
	 */
    public static void main(String[] args) {
    	try{
    		Config.buildGlobalConfig(args);
    		MasterGui.start();
    	}
    	catch(IllegalArgumentException ex) {
    		System.out.print("Could not start application: ");
    		System.out.println(ex.getMessage());
    		System.out.println();
    		System.out.println(getSyntaxMessage());
    	}
    }
    
    public static String getSyntaxMessage() {
    	//TODO: Create message on proper usage of arguments for error display;
    	return null;
    }
   
}
