package EarthSim;

import core.SimulationState;


public class Simulation {
	/* Interval in minutes of simulation time in which
	 * temperature values are calculated. */
	public final int mTimestep;
	/* Time in minutes since the beginning of the simulation. */
	private int mRunningTime = 0;
	/* Current longitude of the sun. */
	private double mSunLongitude = 0;
	/* Grid of simulation cells. */
	private SimulationGrid mGrid;

	Simulation(int spacing, int timestep) {

		if (timestep < 1)
			mTimestep = 1;
		else if (timestep >= 1440)
			mTimestep = 1440;
		else
			mTimestep = timestep;
		
		mGrid = new SimulationGrid(spacing);
	}

	/* Process on step in the simulation. */
	public void processStep() {

		/* Process radiant temperature changes. */
		mGrid.calculateRadiantTempertaures(mSunLongitude);
		
		/* Create a new simulation grid that is a copy of the previous one. */
		SimulationGrid newGrid = new SimulationGrid(mGrid);

		/* Process convection by calculating values from the
		 * old grid to the new one. */
		newGrid.processConvection(mGrid);
		
		/* Overwrite the old grid with the new one. */
		mGrid = newGrid;

		/* Create a simulation state. */
		SimulationState state = new SimulationState(
				mGrid.getCellList(),
				mSunLongitude,
				mRunningTime);

		// @TODO: Enqueue simulation state.
		
		/* Advance running time. */
		mRunningTime += mTimestep;
		
		/* Advance the sun for the next simulation state. */
		mSunLongitude = (mRunningTime % 1440) * 360 / 1440;
	}
}
