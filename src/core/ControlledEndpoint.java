package core;

/**
 * Common interface for both producer and consumer endpoints. 
 *
 */
public interface ControlledEndpoint {
	
	/**
	 * Method that is used to run the endpoint
	 * until the buffer is full or empty. This
	 * method may be executed synchronously or
	 * asynchronously depending on whether the
	 * endpoint was configured to be running 
	 * its own thread.
	 */
	public void run();
	
	/**
	 *  Pause the endpoint.
	 */
	public void pause();
	
	/**
	 * Resume from a pause.
	 */
	public void resume();
	
	/**
	 * Permanently stop the endpoint.
	 */
	public void cancel();
}
