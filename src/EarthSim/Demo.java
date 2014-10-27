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
    	StringBuilder sb = new StringBuilder();
    	sb.append("Allowed arguments are as follows\n:");
    	sb.append("java EarthSim.Demo [-s] [-p] [-r|-t] [-b #]");
    	sb.append("-r & -t sent initiative.  They cannot BOTH be used");
    	sb.append("-s and -p are used to indicate threading.  We recommend that both of these are set");
    	sb.append("-b # is optional to set a non-default buffer size. (Default is 1)");
    	return sb.toString();
    }
   
}
