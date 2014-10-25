package EarthSim;

import java.util.concurrent.BlockingQueue;

import EarthPresentation.DisplayModel;

import core.Config;
import core.InitiativeEnum;
import core.SimulationState;
import core.ThreadedEnum;


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
	
	/* Flag indicating whether the simulation has the initiative. */
	private boolean mInitiative = false;
	/* Flag indicating whether the simulation is run on its own
	 * thread. */
	private boolean mActive = false;
	
	/* State flag indicating that the simulation is to be canceled. */
	private volatile boolean mCanceled = false;
	/* State flag indicating that the simulation has been paused. */
	private volatile boolean mPaused = false;
	/* State flag indicating that a call to run() is to be handled
	 * asynchronously. */
	private boolean mToRun = false;


	public Simulation(int spacing,
			int timestep) {

		if (timestep < 1)
			mTimestep = 1;
		else if (timestep >= 1440)
			mTimestep = 1440;
		else
			mTimestep = timestep;
		
		mGrid = new SimulationGrid(spacing);
		
		mQueue = Config.getInstance().getBuffer();
		mInitiative = (Config.getInstance().getInitiative() ==
				InitiativeEnum.SIMULATION);
		mActive = Config.getInstance().getThreadingFlags()
				.contains(ThreadedEnum.SIMULATION);
		}

	/* Run the simulation until the buffer s full.
	 * Depending on whether the simulation thread has
	 * been enabled, runSimulation is either processed
	 * in mThread or in the context of the caller. */
	public void run() {
		Config config = Config.getInstance();
		switch(config.getInitiative()){
		case MASTER_CONTROL:
			while(!mCanceled){
				checkPaused();
				processStep();
				boolean retry;
				do{
					retry = mQueue.offer(mNextSimulationState);
					if(retry){
						try{
							Thread.sleep(50);
						}
						catch(InterruptedException ex){
							break;
						}
					}
				}
				while(retry);	
			}
			break;
		case SIMULATION: 
				if(config.getThreadingFlags().contains(ThreadedEnum.PRESENTATION)) {
					while(!mCanceled){
						checkPaused();
						do{
							processStep();
						}
						while(mQueue.offer(mNextSimulationState));
				
						if(!config.requested())
							config.request();
						
						while(config.requested()){
							try{
								Thread.sleep(50);
							}
							catch(InterruptedException ex){
								break;
							}
						}
					}
				}
				else {
					while(!mCanceled){
						checkPaused();
						do{
							processStep();
						}
						while(mQueue.offer(mNextSimulationState));
						
						DisplayModel presentation = (DisplayModel) config.getNonInitativeObject();
						presentation.consume();
					}
				}	
		case PRESENTATION:
			while(!mCanceled){
				if(config.requested()){
					checkPaused();
					do{
						processStep();
					}
					while(mQueue.offer(mNextSimulationState));
					config.completed();
				}
				else {
					try{
						Thread.sleep(50);
					}
					catch(InterruptedException ex){
						break;
					}
				}
			}
		}			
	}
	
	private void checkPaused() {
		try{
			Thread.sleep(50);
		}
		catch(InterruptedException ex){
			return;
		}
	}
	
	public void produce(){
		do{
			processStep();
		}
		while(mQueue.offer(mNextSimulationState));
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
		mGrid.calculateRadiantTemperatures(mSunLongitude);
		
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
		mSunLongitude = (mRunningTime % 1440) * 360 / 1440 - 180;
	}
	
//	boolean enqueueNextSimulationState() {
//		return mQueue.add(mNextSimulationState);
//	}
	
}
