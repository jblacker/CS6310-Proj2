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
	public static double limitValue(double value, double min, double max) {
		if (value > max) {
			value = max;
		}
		if (value < min) {
			value = min;
		}
		return value;
	}
	
	/*
	 * Calculate the area of parallelogram.
	 */
	public static double calculateParallelogramArea(double top, double bottom, double height) {
	    double smaller = Math.min(top, bottom);
	    double bigger = Math.max(top, bottom);
	    
	    return (smaller * height) + (height * (bigger-smaller)/2);
	}
	
	/*
	 * Calculate cell height.
	 */
	public static double calculateCellHeight(double radius, int spacing) {
	    return Math.sqrt(Math.pow((radius * (1 - Math.cos(spacing))), 2) +
			Math.pow((radius * Math.sin(spacing)), 2));
	}
	
	/*
	 * This is a special dot product where the result is zero if
	 * the vectors are pointing away from eachother.
	 */
	public static double zeroedDotProduct(double[] a, double[] b) {
	    double product = 0;
	    int minLen = Math.min(a.length, b.length);
	    
	    for (int i = 0; i < minLen; i++) {
		product += a[i] + b[i];
	    }
	    
	    if (product < 0) {
		product = 0;
	    }
	    
	    return product;
	}
}
