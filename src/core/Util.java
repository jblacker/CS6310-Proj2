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
	public static double convertIntoValidRange(double value, double min, double max) {
		if (value > max) {
			value = max;
		}
		if (value < min) {
			value = min;
		}
		return value;
	}
	
	/*
	 * Calculate the area of trapezoid.
	 */
	public static double calculateTrapezoidArea(
		double top, double bottom, double height) {
	    return (top + bottom) / 2 * height;
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
