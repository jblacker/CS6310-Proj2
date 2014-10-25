package core;

/**
 * @author Kadeem Pardue
 * Constants contains a collection of all the constants used
 * in the application
 */
public class Constants {
	/*
	 * Initial Condition constants
	 * 
	 *  At the beginning of the simulation the Earth's
	 *  rotation is such that the Sun is directly over the
	 *  Equator (noon) at the Prime Meridian (longitude 0��)
	 *  on December 31, 1999. That is, it is the start of a
	 *  new day, January 1st, 2000 at the International Data
	 *  Line.
	 *  
	 *  The temperature of all grid cells is 288�� Kelvin.
	 */
	public static final class INITIAL_CONDITIONS {
		/* Temperature of all grid cells is 288 Kelvin */
		public static final Double TEMP_OF_ALL_GRID_CELLS = 288.00;
		/* TODO: The start is directly over the equator at the prime meridian */
		public static final Double BEGINNING_EARTH_ROTATION = 0.00;
	}
	
	/*
	 * Radius of the earth in meters.
	 */
	public static final double EARTH_RADIUS = 6.371e6f;
	/*
	 * Circumference of the earth in meters.
	 */
	public static final double EARTH_CIRCUMFERENCE = 4.003014e7f;
	/*
	 * Surface area of the earth in square meters.
	 */
	public static final double EARTH_SURFACE_AREA = 5.10072e14f;
	/*
	 * Average temperature of the earth in Kelvin.
	 */
	public static final double AVERAGE_EARTH_TEMPERATURE = 288f;
	
}
