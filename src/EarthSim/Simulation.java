package EarthSim;

import java.util.concurrent.BlockingQueue;

import core.Config;
import core.ControlledEndpoint;
import core.InitiativeEnum;
import core.SimulationState;
import core.ThreadedEnum;


public class Simulation implements ControlledEndpoint {
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
	/* If producer is set to active, then create a thread to
	 * process the simulation. */
	private SimulationThread mThread;
	
	/* Flag indicating whether the simulation has the initiative. */
	private boolean mInitiative = false;
	/* Flag indicating whether the simulation is run on its own
	 * thread. */
	private boolean mActive = false;
	
	/* State flag indicating that the simulation is to be canceled. */
	private boolean mCanceled = false;
	/* State flag indicating that the simulation has been paused. */
	private boolean mPaused = false;
	/* State flag indicating that a call to run() is to be handled
	 * asynchronously. */
	private boolean mToRun = false;
	
	/* Pointer to consumer's ControlledEnpoint interface.
	 * This shall be used to control the consumer if the
	 * Simulation was configured to have the initiative. */
	private ControlledEndpoint mOtherEndpoint;


	Simulation(int spacing,
			int timestep,
			BlockingQueue<SimulationState> queue,
			ControlledEndpoint otherEndpoint) {

		if (timestep < 1)
			mTimestep = 1;
		else if (timestep >= 1440)
			mTimestep = 1440;
		else
			mTimestep = timestep;
		
		mGrid = new SimulationGrid(spacing);
		
		mQueue = queue;
		mInitiative = (Config.getInstance().getInitiative() ==
				InitiativeEnum.SIMULATION);
		mActive = Config.getInstance().getThreadingFlags()
				.contains(ThreadedEnum.SIMULATION);

		mOtherEndpoint = otherEndpoint;
		
		if (mActive) {
			mThread = new SimulationThread();
			mThread.start();
		}
	}

	/* Run the simulation until the buffer s full.
	 * Depending on whether the simulation thread has
	 * been enabled, runSimulation is either processed
	 * in mThread or in the context of the caller. */
	public void run() {
		if (mActive) {
			mToRun = true;
		}
		else {
			runSimulation();
		}
			
	}
	
	/* Pause the simulation on its next iteration. */
	public void pause() {
		mPaused = true;
	}
	
	/* Resume a paused simulation. */
	public void resume() {
		mPaused = false;
	}
	
	/* Force stop the simulation on its next iteration. */
	public void cancel() {
		mCanceled = true;
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
	
	private void runSimulation() {
		/* If we have initiative, run forever.
		 * Otherwise stop when the queue is full.
		 * Note that the producer and consumer cannot
		 * both have the initiative at the same time.
		 * And always exit if stop() or pause() was
		 * called. */
		do {
			/* If we have initiative, then call on
			 * the consumer to start consuming. */
			if (mInitiative) {
				mOtherEndpoint.run(); 
			}
			
			processStep();
		}
		while ((enqueueNextSimulationState() || mInitiative) &&
				!mCanceled && !mPaused);
	}
	
	/* Thread to run simulation on if simulation thread was enabled. */
	private class SimulationThread extends Thread {
		
		/* Free run the simulation until these queue is full. */
		public void run() {
			
			while (waitForRun() && waitPaused()) {
				runSimulation();
			}
		}
		
		/* Wait for call to Simulation.run(), which will set
		 * the mToRun flag. Return false if simulation was
		 * canceled. */
		private boolean waitForRun() {
			while (!mToRun && !mCanceled) {
				try {
					sleep(100);
				}
				catch (InterruptedException e) { }
			}
			mToRun = false;
			return !mCanceled;
		}
		
		/* If simulation was paused, wait for call to
		 * Simulation.resume(), which will set the mPaused
		 * flag to false. Return false if simulation was
		 * canceled while waiting. */
		private boolean waitPaused() {
			while (mPaused && !mCanceled) {
				try {
					sleep(100);
				}
				catch (InterruptedException e) { }
			}
			return !mCanceled;
		}
	}
}
