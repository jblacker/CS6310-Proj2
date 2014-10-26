package EarthPresentation;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import EarthSim.Simulation;

import core.Config;
import core.DataCell;
import core.SimulationState;
import core.ThreadedEnum;

public class DisplayModel extends Observable implements Runnable, ActionListener{

	//true if has initiative, false if simulation has initiative, null if MasterGui has it
	private final Boolean hasInitative;
	
	//Flags for handling stop, start, pause, resume, and size changed during simulation
	private volatile boolean isCancelled = false; 
	private volatile boolean sizeChanged = false;
	private volatile boolean pauseRequested = false;

	private Timer refreshTimer;
	
	//Images for View
	private BufferedImage temperatureMapImage;
	private BufferedImage solarOverlay;
	private BufferedImage compositeMap;
	private BufferedImage mapImage;
	
	//current size of canvases
	private int mapCanvasHeight;
	private int mapCanvasWidth;
	
	//used for when a change is sensed
	private int newCanvasHeight;
	private int newCanvasWidth;
	
	//Grid Size Parameters
	private int rows;
	private int cols;
	
	//timing flags
	private boolean running;
	private boolean imageReady;
	private boolean rescaleMap;
	private long startTime;
	private long stopTime;
	private int timeStep;
	private int refreshRate;
	
	//used for DisplayCell calculations
	private int gridSpacing;
	
	public DisplayModel(int height, int width, Boolean initiative) {
		this.mapCanvasHeight = height;
		this.mapCanvasWidth = width;
		this.refreshRate = 1000;
		this.refreshTimer = new Timer(refreshRate, this); //default?
		this.timeStep = 1; //default?
		this.hasInitative = initiative;
	}
	
	public DisplayModel(int height, int width, int timeStep, Boolean initiative) {
		this.mapCanvasHeight = height;
		this.mapCanvasWidth = width;
		this.timeStep = timeStep;
		this.refreshRate = 1000; 
		this.refreshTimer = new Timer(refreshRate, this);
		this.hasInitative = initiative;
	}
	
	public DisplayModel(int height, int width, int timeStep, int refreshRate, Boolean initiative) {
		this.mapCanvasHeight = height;
		this.mapCanvasWidth = width;
		this.timeStep = timeStep;
		this.refreshRate = refreshRate;
		this.refreshTimer = new Timer(refreshRate, this);
		this.hasInitative = initiative;
	}
	
	/**
	 * This function is used to drain the buffer and notify the view when
	 * running unthreaded.
	 */
	public void consume() {
		Config config = Config.getInstance();
		while(!config.getBuffer().isEmpty()) {
			if(sizeChanged) {
				updateSize();
			}
			
			generateNextImageSet();
		}
	}
	
	/**
	 * This is the runnable context for when running threaded.  It automatically determines
	 * how it should behave based on the flags set in the config singleton.
	 */
	public void run() {
		Config config = Config.getInstance();
		running = true;
		refreshTimer.start();
		startTime = System.currentTimeMillis();
		switch(config.getInitiative()){
		//Master control has initiative, run forever & consume when possible
		case MASTER_CONTROL:
			while(!isCancelled){
				checkPause();
				if(sizeChanged) {
					updateSize();
				}
				if(config.getBuffer().isEmpty()){
					try{
						Thread.sleep(50);
					}
					catch(InterruptedException ex){
						break;
					}
				}
				else
					generateNextImageSet();
				
				Thread.yield();
			}
			break;
		//simulation has initiative.  Poll forever and run when flag on config object changes
		case SIMULATION: 
				//simulation is threaded so handle accordingly
				if(config.getThreadingFlags().contains(ThreadedEnum.SIMULATION)) {
					while(!isCancelled){
						checkPause();
						if(sizeChanged) {
							updateSize();
						}
						//buffer is drained.  Notify completion
						if(config.requested() && config.getBuffer().isEmpty())
							config.completed();
						//buffer contains data.  Consume & display
						else if(config.requested() && !config.getBuffer().isEmpty())
							generateNextImageSet();
						//Not requested to run.  Spinwait.
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
				//simulation is NOT threaded.  No need for buffer checking, only run when requested
				else {
					while(!isCancelled){
						checkPause();
						if(sizeChanged) {
							updateSize();
						}
						if(config.requested()){
							generateNextImageSet();
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
				break;
			//we have initiative!
		case PRESENTATION:
				//simulation is threaded.  Use notify flags
				if(config.getThreadingFlags().contains(ThreadedEnum.SIMULATION)){
					config.request(); //request data and pull from buffer asap
					while(!isCancelled){
						checkPause();
						//no data in buffer, request & spinwait
						if(config.getBuffer().isEmpty()){
							config.request();
							try{
								Thread.sleep(50);
							}
							catch(InterruptedException ex){
								break;
							}
						}
						else {
							generateNextImageSet();
						}
					}
				}
				//simulation is NOT threaded.  Take ownership and run directly
				else{
					Simulation producer = (Simulation) config.getNonInitativeObject();
					while(!isCancelled){
						producer.produce();
						while(!config.getBuffer().isEmpty()) {
							checkPause();
							generateNextImageSet();
						}
						Thread.yield();
					}
				}
				break;
		}
			
		stopTime = System.currentTimeMillis();
		refreshTimer.stop();
		running = false;
	}
	
	/**
	 * Check if paused and spinwait until paused is false
	 */
	public void checkPause() {
 		while(pauseRequested) {
			try{
				Thread.sleep(500);
				Thread.yield();
			}
			catch(InterruptedException ex) {
				break;
			}
		}
	}
	
	/**
	 * This is the master image generating function.
	 * This function handles the order of when images need to be processed
	 */
	public void generateNextImageSet() {
		SimulationState simState;
		if(hasInitative == null || !hasInitative){
			try{
				//if we don't have initiative block dequeue from buffer
				simState = Config.getInstance().getBuffer().take();
			}
			catch(InterruptedException ex) {
				return;
			}
		}
		else {
			//We have initiative.  DO NOT BLOCK!
			simState = Config.getInstance().getBuffer().poll();
			if(simState == null)
				return;
		}
		
		DataCell[][] cells = simState.getCells();
		generateNextMapImage(cells);
		generateSolarOverlayImage(simState.getSunLongitude());
		generateCompositeMapImage();
		imageReady = true; //notify view outside of refresh rate
	}
	
	/**
	 * Converts DataCells to DisplayCells.  In doing so additional calculations are performed
	 * to properly determine latitude & longitude of all sides of the cell for conversion to X & Y coords.
	 * @param simData Data from Simulation
	 * @return A list of converted DisplayCells
	 */
//	private List<DisplayCell> generateDisplayCells(List<DataCell> simData) {
//		List<DisplayCell> displayCells = new ArrayList<DisplayCell>();
//		
//		//calculate latitude & longitude for ALL cell points
//		for(DataCell cell : simData) {
//			displayCells.add(new DisplayCell(cell, this.gridSpacing));
//		}
//		
//		return displayCells;
//	}
	
	private Dimension calculateCellDimension() {
		double latDegPerPixel = getPixelsPerDegree(LatLonEnum.LATITUDE);
		
		int width = (int) Math.floor(latDegPerPixel * this.gridSpacing);
		
		double lonDegPerPixel = getPixelsPerDegree(LatLonEnum.LONGITUDE);
		int height = (int) Math.floor(lonDegPerPixel * this.gridSpacing);
		
		return new Dimension(width, height);
	}
	
	private double getPixelsPerDegree(LatLonEnum latlon){
		switch(latlon){
			case LATITUDE:
				return (double) this.mapCanvasWidth / 360f;
			case LONGITUDE:
				return (double) this.mapCanvasHeight / 180f;
			default:
				throw new IllegalArgumentException("Unknown Enum Value");
		}
	}
	
	private Color[][] calculateColors(DataCell[][] cells) {
		Color[][] cellColors = new Color[this.cols][this.rows];
		for(int i = 0; i < cols; i++){
			for(int j = 0; j < rows; j++){
				cellColors[i][j] = calculateColor(cells[i][j].getTemperature());
			}
		}
		
		return cellColors;
	}
	
	/**
	 * Calculates and sets the color of the cell using the algorithm provided by the sample
	 * in the assignment page.
	 */
	private static Color calculateColor(double temperature) {
        System.out.printf("%f\n", temperature);
		//convert to Celsius for more accurate color values
		double celsiusTemp = temperature - 273.15;
		int temp = (int)Math.floor(celsiusTemp);
		int red, blue, green;

		if (temp <= -100) {
			blue = 255;
			green = 0;
			red = 0;
		}
		else if (temp <= -46) {
			temp = -1 * temp;
			blue = 255;
			green = 145 - (temp * 10) % 115;
			red = 0;
		}
		else if (temp <= -23 && temp > -46) {
			temp = -1 * temp;
			blue = 255;
			green = 145;
			red = 145 + (temp * 5) % 115;
		}
		else if (temp < 0 && temp > -23) {
			temp = -1 * temp;
			blue = 255;
			green = 145;
			red = 145 - (temp * 5);
		}
		else if (temp == 0) {
			blue = 225;
			green = 145;
			red = 145;
		}
		else if (temp > 0 && temp < 23) {
			blue = 255;
			green = 145 + (temp * 5);
			red = 145;
		}
		else if (temp >= 23 && temp < 46) {
			blue = 255 - (temp * 5) % 115;
			green = 255;
			red = 145;
		}
		else if (temp >= 46 && temp < 69) {
			blue = 145;
			green = 255;
			red = 145 + (temp * 5) % 115;
		}
		else if (temp >= 69 && temp < 92) {
			blue = 100;
			green = 255 - (temp * 5) % 115;
			red = 255;
		}
		else {
			blue = 0;
			green = 145 - (temp * 10) % 115;
			red = 255;
		}
		
		return new Color(red, green, blue);
	}
	
	/**
	 * Create the colored overlay of cells to represent the heating of the planet
	 * @param cells The converted simulation data
	 */
	private void generateNextMapImage(DataCell[][] cells) {
		
		BufferedImage nextImage = new BufferedImage(mapCanvasWidth, mapCanvasHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = null;
		Dimension cellDimensions = calculateCellDimension();
		Color[][] colors = calculateColors(cells);
		try{
			graphics = nextImage.createGraphics();		
			
			//set transparent to use as overlay
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			
			//create rectangle primitives for each cell
			int currentXPixel = 0;
			int currentYPixel;
			for(int i = 0; i < this.cols; i++){
				currentYPixel = 0;
				for(int j = 0; j < this.rows; j++){
					graphics.setColor(colors[i][j]);
					graphics.fillRect(currentXPixel, currentYPixel, cellDimensions.width, cellDimensions.height);
					currentYPixel += cellDimensions.height;
				}
				currentXPixel += cellDimensions.width;
			}
			
			this.temperatureMapImage = nextImage;
		}
		finally{
			if(graphics != null)
				graphics.dispose();
		}
	}
	
	/**
	 * Creates a transparent overlay image containing a single yellow line representing the current longitude of the sun
	 * @param solarLongitude The current longitude of the sun
	 */
	private void generateSolarOverlayImage(double solarLongitude) {
		Point p = calculateSolarPoint(solarLongitude);
		
		BufferedImage nextImage = new BufferedImage(mapCanvasWidth, mapCanvasHeight, BufferedImage.TYPE_4BYTE_ABGR);
		
		Graphics2D graphics = null;
		try{
				graphics = nextImage.createGraphics();
				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				graphics.setColor(Color.YELLOW);
				graphics.setStroke(new BasicStroke(10));
				graphics.drawLine(p.x, 0, p.x, mapCanvasHeight);
			
				this.solarOverlay = nextImage;
		}
		finally{
			if(graphics != null)
				graphics.dispose();
		}
	}
	
	/**
	 * Combines the overlay images into a single image, which is placed on top of the mapImage
	 * This creates a brand new image that is set to compositeMap variable for the View to pull
	 * If for some reason the underlying map cannot be located it will attempt to display without it
	 */
	private void generateCompositeMapImage() {
		BufferedImage nextImage = new BufferedImage(mapCanvasWidth, mapCanvasHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = null;
		boolean useMap = true;
		
		try{
			if(mapImage == null || this.rescaleMap )
				mapImage = loadAndScaleMapImage();
		}
		catch(IOException ex){
			ex.printStackTrace();
			useMap = false;
		}
		
		try{
			
			graphics = nextImage.createGraphics();			
			
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			if(useMap)
				graphics.drawImage(mapImage, 0, 0, null);
			
			graphics.drawImage(this.temperatureMapImage, 0, 0, null);
			graphics.drawImage(this.solarOverlay, 0, 0, null);
			
			this.compositeMap = nextImage;
		}
		finally{
			if(graphics != null)
				graphics.dispose();
		}
	}

	/** Loads the underlying map from disk from and scales it appropriately
	 * for the current size of the display 
	 * @return BufferedImage of the Map scaled to the current display
	 * @throws IOException When fails to locate the map image.
	 */
	private BufferedImage loadAndScaleMapImage() throws IOException {
		BufferedImage scaledMap = new BufferedImage(mapCanvasWidth, mapCanvasHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D scaledImageGraphics = null;
		try{
			ClassLoader cl = getClass().getClassLoader();
			File file = new File(cl.getResource("world-map.jpg").getFile());
			BufferedImage map = ImageIO.read(file);
			scaledImageGraphics = scaledMap.createGraphics();
			
			scaledImageGraphics.drawImage(map, 0, 0, mapCanvasWidth, mapCanvasHeight, null);
		}
		finally{
			if(scaledImageGraphics != null)
				scaledImageGraphics.dispose();
		}
		
		this.rescaleMap = false;
		return scaledMap;
	}

	/**
	 * Converts Latitude & Longitude into an (X,Y) point on the canvas using the
	 * Mercator projection algorithm.
	 * @param coords Latitude & Longititude coordinates
	 * @return Point on the canvas
	 */
//	public Point latLongToPoint(GeoCoordinate coords) {
//		double x, y;
//		
//		x = (coords.getLongitude() + 180) / 360;
//        y = (coords.getLatitude() / (Math.cos(Math.toRadians(coords.getLatitude())) + 0.000001) + 90) / 360;
//		//double siny = Math.sin(Math.toRadians(coords.getLatitude()));
//		//y = 0.5 * Math.log((1 + siny) / (1 - siny)) / -(2 * Math.PI) + 0.5;
//		return new Point((int)(x * (double)this.mapCanvasWidth),(int)(y * (double)this.mapCanvasHeight));
//	}
//	
//	/**
//	 * Calculates the X coordinate on the canvas for the Solar Line using the Mercator projection algorithm
//	 * @param longitude
//	 * @return Topmost point of the canvas where the solar line's origin is located.
//	 */
	public Point calculateSolarPoint(double longitude) {
        //System.out.printf("display: %s\n", longitude);
		double x = (longitude + 180) * (this.mapCanvasWidth / 360);
		x = Math.floor(x);
		
		return new Point((int)x, 0);
	}
	
	/**
	 * When new dimensions are provided mid-simulation this function commits 
	 * them to the object to be used in subsequent calculations.
	 */
	private void updateSize() {
		if(this.newCanvasHeight != this.mapCanvasHeight) {
			this.mapCanvasHeight = this.newCanvasHeight;
		}
		if(this.newCanvasWidth != this.mapCanvasWidth) {
			this.mapCanvasWidth = this.newCanvasWidth;
		}
		
		this.sizeChanged = false;
	}
	
	/**
	 * Used by the view to get the next image for display
	 * @return BufferedImage representing the work of the Presentation
	 */
	public synchronized BufferedImage getMapImage() {
		return this.compositeMap;
	}
	
	/**
	 * Sets the stop flag for when running threaded.
	 */
	public synchronized void stop() {
		isCancelled = true;
	}
	
	/**
	 * Sets the pause flag when running threaded
	 */
	public synchronized void pause() {
		pauseRequested = true;
	}
	
	/**
	 * If already paused, releases the pause flag
	 */
	public synchronized void resume() {
		pauseRequested = false;
	}
	
	/**
	 * Updates the rate that the View is notified to pull from the Model.
	 * If the model is currently running this is ignored completely.
	 * @param ms Time in Milliseconds to notify the View to refresh
	 */
	public synchronized void setRefreshRate(int ms) {
		if(running)
			return;
		
		refreshTimer = new Timer(ms, this);
	}

	/**
	 * Each tick of the refresh timer all observers (aka the View) are notified to update.
	 * However, if a new image is not yet ready the notification is skipped until 
	 * it is ready for display
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == refreshTimer && imageReady) {
			this.setChanged();
			this.notifyObservers(getElapsedTime());
			imageReady = false;
		}
	}
	
	/**
	 * Allows an outside object to check if the Model is currently running
	 * @return True if running and threaded, otherwise false.
	 */
	public synchronized boolean isRunning() {
		return running;
	}

	/**
	 * INCOMPLETE!  This gets the current running time of the model.  This may want to be fixed
	 * to calculate the time elapsed in the simulaton
	 * @return Time in Milliseconds
	 */
	public synchronized long getElapsedTime() {
		//currently in milliseconds
		if(running)
			return System.currentTimeMillis() - startTime; 
		else
			return stopTime - startTime;
	}
	
	/**
	 * If running sets a flag that the new dimension needs to be used on next run,
	 * otherwise immediately updates the current size of the generated images.
	 * @param d Updated dimensions for next image.
	 */
	public synchronized void setSize(Dimension d) {
		if(this.running) {
			this.newCanvasHeight = d.height;
			this.newCanvasWidth = d.width;
			this.sizeChanged = true;
		}
		else {
			this.mapCanvasHeight = d.height;
			this.mapCanvasWidth = d.width;
		}
		
		this.rescaleMap = true;
	}

	/**
	 * Gets the current grid spacing
	 * @return Spacing in degrees
	 */
	public int getGridSpacing() {
		return gridSpacing;
	}

	/**
	 * Sets the grid spacing
	 * @param gridSpacing
	 */
	public void setGridSpacing(int gridSpacing) {
		this.gridSpacing = gridSpacing;
		this.rows = 180 / this.gridSpacing;
		this.cols = 360 / this.gridSpacing;
	}
}