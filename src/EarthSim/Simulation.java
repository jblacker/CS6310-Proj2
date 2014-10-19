package EarthSim;

import java.util.concurrent.BlockingQueue;

import core.SimulationState;


public class Simulation implements Runnable {
	/* Interval in minutes of simulation time in which
	 * temperature values are calculated. */
	public final int mTimestep;
	/* Time in minutes since the beginning of the simulation. */
	private int mRunningTime = 0;
	/* Current longitude of the sun. */
	private double mSunLongitude = 0;
	/* Grid of simulation cells. */
	private SimulationGrid mGrid;
	/* The next simulation state that will be added to the queue. */
	private SimulationState mNextSimulationState;
	/* Queue used to send data to the presentation. */
	private BlockingQueue<SimulationState> mQueue;
	private boolean mInitiative;
	private boolean mStopped = false;

	Simulation(int spacing,
			int timestep,
			BlockingQueue<SimulationState> queue,
			boolean initiative) {

		if (timestep < 1)
			mTimestep = 1;
		else if (timestep >= 1440)
			mTimestep = 1440;
		else
			mTimestep = timestep;
		
		mGrid = new SimulationGrid(spacing);
		
		mQueue = queue;
		mInitiative = initiative;
	}

	/* Free run the simulation until these queue is full. */
	public void run() {
		
		/* If we have initiative, run forever.
		 * Otherwise stop when the queue is full.
		 * Note that the producer and consumer cannot
		 * both initiative at the same time.
		 * And always exit if stop() was called. */
		do {
			/* If we have initiative, then call on
			 * the consumer to start consuming. */
			if (mInitiative) {
				// TODO: Call on presentation to consume. 
			}
			processStep();
		}
		while ((enqueueNextSimulationState() || mInitiative) && !mStopped);
	}
	
	/* Force stop the simulation on its next iteration. */
	public void stop() {
		mStopped = true;
	}
	
	/* Process on step in the simulation. */
	private void processStep() {

		/* Process radiant temperature changes. */
		mGrid.calculateRadiantTempertaures(mSunLongitude);
		
		/* Create a new simulation grid that is a copy of the previous one. */
		SimulationGrid newGrid = new SimulationGrid(mGrid);

		/* Process convection by calculating values from the
		 * old grid to the new one. */
		newGrid.processConvection(mGrid);
		
		/* Overwrite the old grid with the new one. */
		mGrid = newGrid;

		/* Create the next simulation state. */
		mNextSimulationState = new SimulationState(
				mGrid.getCellList(),
				mSunLongitude,
				mRunningTime);
		
		/* Advance running time. */
		mRunningTime += mTimestep;
		
		/* Advance the sun for the next simulation state. */
		mSunLongitude = (mRunningTime % 1440) * 360 / 1440;
	}
	
	boolean enqueueNextSimulationState() {
		return mQueue.add(mNextSimulationState);
	}
}
