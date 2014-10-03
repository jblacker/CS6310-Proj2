package core;

import java.util.HashMap;
import java.util.Map;

import core.Constants.BUFFERING_PARAMETERS;

/**
 * @author Kadeem Pardue
 * Settings for the application
 */
public class Settings {
	// Map user input arguments
	private final Map<String, String> simulationArguments = new HashMap<String, String>();

	/*
	 * Validate settings just to be sure
	 * static (class) method
	 */
	public static void validate(Settings settings) {
		// Buffer Validation
		if (!Util.notNullOrEmpty(settings.getValue(BUFFERING_PARAMETERS.__B))) {
			throw new IllegalArgumentException("Buffer > 0 is required");
		}else{
			try{
				if(Integer.parseInt(settings.getValue(BUFFERING_PARAMETERS.__B)) <= 0)
				{
					throw new IllegalArgumentException("Buffer > 0 is required");
				}
			}catch(NumberFormatException ex)
			{
				throw new RuntimeException("Buffer is not a number");
			}
		}
	}
	/*
	 * Set the value of arguments to the hash map.
	 */
	public void setValue(String key, String value) {
		simulationArguments.put(key, value);
	}

	/*
	 * Get the value from arguments of the hash map
	 */
	public String getValue(String key) {
		return simulationArguments.get(key);
	}
	
}
