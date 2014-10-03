package core;

import core.Constants.BUFFERING_PARAMETERS;
import core.Constants.CONCURRENCY_PARAMETERS;
import core.Constants.INITIATIVE_PARAMETERS;

/**
 * @author Kadeem Pardue
 *	Configuration contains all the logic for setting up 
 *  when the application is invoked
 */
public class Config {
	/*
	 * Setup Configuration
	 */
	public Settings setupConfiguration(String[] args) {
		Settings settings = null;
		
		if (args != null && args.length > 0) {
			settings = new Settings();
			// Store those settings
			for (int i = 0; i < args.length; i++) {
				if (CONCURRENCY_PARAMETERS.__S.equals(args[i])) {
					settings.setValue(CONCURRENCY_PARAMETERS.__S, args[++i]);
				} else if (CONCURRENCY_PARAMETERS.__P.equals(args[i])) {
					settings.setValue(CONCURRENCY_PARAMETERS.__P, args[++i]);
				} else if (INITIATIVE_PARAMETERS.__T.equals(args[i])) {
					settings.setValue(INITIATIVE_PARAMETERS.__T, args[++i]);
				} else if (INITIATIVE_PARAMETERS.__R.equals(args[i])) {
					settings.setValue(INITIATIVE_PARAMETERS.__R, args[++i]);
				} else if (BUFFERING_PARAMETERS.__B.equals(args[i])) {
					settings.setValue(BUFFERING_PARAMETERS.__B, args[++i]);
				} else {
					System.err.println("Invalid or Unexpected value:" + args[i]);
				}
				// Validate those settings
				Settings.validate(settings);
			}
		} else {
			// Buffer argument required regardless
			throw new IllegalArgumentException("Missing required Buffer argument");
		}
		
		return settings;
	}

}
