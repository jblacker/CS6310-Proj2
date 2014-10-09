/**
 * 
 */
package core;

/**
 * @author Kadeem Pardue
 * Utility functions
 */
public class Util {

	static boolean notNullOrEmpty(final String string) {
		return (string != null && !string.isEmpty());
	}
	
	/*
	 * Return a value limited by min and max.
	 */
	public static int limitValue(int value, int min, int max) {
		if (value > max) {
			value = max;
		}
		if (value < min) {
			value = min;
		}
		return value;
	}
}
