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
	 * Concurrency Constants for Heated Earth Simulation
	 * 
	 * All four subsets of {-s, -p} are allowed. For example,
	 * in the absence of both -s and -p, all three components
	 * (Simulation, Presentation and GUI) should run in the 
	 * same thread. Note that the threads used for the -s and
	 * -p options should be full partners. By this is meant
	 * that they are not subordinate to the GUI thread or to
	 * each other. In particular, you should not make use of
	 * the SwingWorker mechanism provided by the Java libraries.
	 */
	public static final class CONCURRENCY_PARAMETERS {
		/* -s Indicates that the Simulation should run in its own thread */
		public static final String __S = "-s";
		/* -p: Indicates that the Presentation should run in its own thread */
		public static final String __P = "-p";
	}
	/*
	 * Initiative Constants for Heated Earth Simulation
	 * 
	 * If neither -t nor -r are present, then a third party
	 * (presumably in the GUI thread) should be responsible
	 * for invoking both the Presentation and the Simulation
	 * and coordinating their interaction in a correct and
	 * efficient fashion.
	 */
	public static final class INITIATIVE_PARAMETERS {
		/* -t: Indicates that the Simulation, after producing an updated grid, should instruct the Presentation to consume it */
		public static final String __T = "-t";
		/* -r: Indicates that the Presentation, after completing the display of a grid, should instruct the Simulation to produce another */
		public static final String __R = "-r";
	}
	/*
	 * Buffering Constants for Heated Earth Simulation
	 * 
	 * This # controls buffering, where # is a positive integer
	 * indicating the length of the buffer. In all combinations
	 * of parameters, data should be passed between the
	 * Simulation and the Presentation using a shared variable.
	 * If no explicit -b # parameter is present, the shared
	 * variable can be thought of as a buffer of length one.
	 * That is, the absence of the -b parameter is treated as
	 * if -b 1 appeared.
	 */
	public static final class BUFFERING_PARAMETERS {
		/* -b: Indicates that the Simulation, after producing an updated grid, should instruct the Presentation to consume it */
		public static final String __B = "-b";
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
