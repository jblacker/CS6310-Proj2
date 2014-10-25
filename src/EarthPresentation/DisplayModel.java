package EarthPresentation;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import EarthSim.Simulation;

import core.Config;
import core.DataCell;
import core.InitiativeEnum;
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
	private BufferedImage solarImage;
	private BufferedImage solarOverlay;
	private BufferedImage compositeMap;
	
	//current size of canvases
	private int mapCanvasHeight;
	private int mapCanvasWidth;
	private int solarCanvasHeight;
	
	//used for when a change is sensed
	private int newCanvasHeight;
	private int newCanvasWidth;
	
	//timing flags
	private boolean running;
	private boolean imageReady;
	private long startTime;
	private long stopTime;
	private int timeStep;
	private int refreshRate;
	
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
	
	//Manual Buffer Drain & Display (not threaded)
	public void consume() {
		Config config = Config.getInstance();
		while(!config.getBuffer().isEmpty()) {
			if(sizeChanged) {
				updateSize();
			}
			
			generateNextImageSet();
		}
	}
	
	//Runnable context for threads
	public void run() {
		Config config = Config.getInstance();
		running = true;
		refreshTimer.start();
		startTime = System.currentTimeMillis();
		switch(config.getInitiative()){
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
		case SIMULATION: 
				if(config.getThreadingFlags().contains(ThreadedEnum.SIMULATION)) {
					while(!isCancelled){
						checkPause();
						if(sizeChanged) {
							updateSize();
						}
						
						if(config.requested() && config.getBuffer().isEmpty())
							config.completed();
						else if(config.requested() && !config.getBuffer().isEmpty())
							generateNextImageSet();
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
		case PRESENTATION:
				if(config.getThreadingFlags().contains(ThreadedEnum.SIMULATION)){
					config.request();
					while(!isCancelled){
						checkPause();
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
	
	//Handle a pause and spinwait if paused
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
	
	//generates the entire set of images per buffer pull
	public void generateNextImageSet() {
		SimulationState simState;
		if(hasInitative == null || !hasInitative){
			try{
				simState = Config.getInstance().getBuffer().take();
			}
			catch(InterruptedException ex) {
				return;
			}
		}
		else {
			simState = Config.getInstance().getBuffer().poll();
			if(simState == null)
				return;
		}
		
		List<DisplayCell> cells = generateDisplayCells(simState.getCells());
		generateNextMapImage(cells);
		generateSolarOverlayImage(simState.getSunLongitude());
		generateCompositeMapImage();
		imageReady = true;
	}
	
	//
	private List<DisplayCell> generateDisplayCells(List<DataCell> simData) {
		List<DisplayCell> displayCells = new ArrayList<DisplayCell>();
		
		//calculate latitude & longitude for ALL cell points
		for(DataCell cell : simData) {
			displayCells.add(new DisplayCell(cell));
		}
		
		return displayCells;
	}
	
	private void generateNextMapImage(List<DisplayCell> cellData) {
		
		BufferedImage nextImage = new BufferedImage(mapCanvasWidth, mapCanvasHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = null;
		try{
			graphics = nextImage.createGraphics();		
			
			//set transparent to use as overlay
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			
			//create polygon primitives
			for(DisplayCell cell : cellData) {
				Polygon cellPolygon = new Polygon();
				Point ne = LatLongToMercatorPoint(cell.getNeCorner());
				Point nw = LatLongToMercatorPoint(cell.getNwCorner());
				Point se = LatLongToMercatorPoint(cell.getSeCorner());
				Point sw = LatLongToMercatorPoint(cell.getSwCorner());
				cellPolygon.addPoint(ne.x, ne.y);
				cellPolygon.addPoint(nw.x, nw.y);
				cellPolygon.addPoint(se.x, se.y);
				cellPolygon.addPoint(sw.x, sw.y);
				
				graphics.setColor(cell.getColor());
				graphics.fillPolygon(cellPolygon);
			}
			
			this.temperatureMapImage = nextImage;
		}
		finally{
			if(graphics != null)
				graphics.dispose();
		}
	}
	
	private void generateSolarOverlayImage(double solarLongitude) {
		Point p = CalculateSolarMercatorPoint(solarLongitude);
		
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
	
	private void generateCompositeMapImage() {
		BufferedImage nextImage = new BufferedImage(mapCanvasWidth, mapCanvasHeight, BufferedImage.TYPE_4BYTE_ABGR);
		BufferedImage scaledMap = new BufferedImage(mapCanvasWidth, mapCanvasHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = null;
		Graphics2D scaledImageGraphics = null;
		try{
			ClassLoader cl = getClass().getClassLoader();
			File file = new File(cl.getResource("Mercator.gif").getFile());
			BufferedImage mercatorMap = ImageIO.read(file);
			scaledImageGraphics = scaledMap.createGraphics();
			
			scaledImageGraphics.drawImage(mercatorMap, 0, 0, mapCanvasWidth, mapCanvasHeight, null);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			if(scaledImageGraphics != null)
				scaledImageGraphics.dispose();
		}
		try{
			
			graphics = nextImage.createGraphics();			
			
			//set transparent to use as overlay
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			
			graphics.drawImage(scaledMap, 0, 0, null);			
			graphics.drawImage(this.temperatureMapImage, 0, 0, null);
			graphics.drawImage(this.solarOverlay, 0, 0, null);
			
			this.compositeMap = nextImage;
		}
		finally{
			if(graphics != null)
				graphics.dispose();
		}
	}

	public Point LatLongToMercatorPoint(GeoCoordinate coords) {
		//convert to radians
		double lat = Math.toRadians(coords.getLatitude());
		//calc x coordinate
		double x = (coords.getLongitude() + 180 ) * (this.mapCanvasWidth / 360);
		
		//calc y coodinate
		double mercScaled = Math.toDegrees(Math.log(Math.tan(Math.PI / 4) + (lat/2)));
		double y = (this.mapCanvasHeight / 2) - ((this.mapCanvasWidth * mercScaled) / (Math.PI * 2));
		
		return new Point((int)x, (int)y);
	}
	
	public Point CalculateSolarMercatorPoint(double longitude) {
        System.out.printf("display: %s\n", longitude);
		//longitude = Math.toRadians(longitude);
		double x = (longitude + 180) * (this.mapCanvasWidth / 360);
		x = Math.floor(x);
		
		return new Point((int)x, 0);
	}
	
	private void updateSize() {
		if(this.newCanvasHeight != this.mapCanvasHeight) {
			this.mapCanvasHeight = this.newCanvasHeight;
			this.solarCanvasHeight = this.newCanvasHeight / 5;
		}
		if(this.newCanvasWidth != this.mapCanvasWidth) {
			this.mapCanvasWidth = this.newCanvasWidth;
		}
		
		this.sizeChanged = false;
	}
	public synchronized BufferedImage getMapImage() {
		return this.compositeMap;
	}
	
	public synchronized BufferedImage getSolarImage() {
		return this.solarImage;
	}
	
	public synchronized void stop() {
		isCancelled = true;
	}
	
	public synchronized void pause() {
		pauseRequested = true;
	}
	
	public synchronized void resume() {
		pauseRequested = false;
	}
	
	public synchronized void setRefreshRate(int ms) {
		if(running)
			return;
		
		refreshTimer = new Timer(ms, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == refreshTimer && imageReady) {
			this.setChanged();
			this.notifyObservers(getElapsedTime());
			imageReady = false;
		}
	}
	
	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized long getElapsedTime() {
		//currently in milliseconds
		if(running)
			return System.currentTimeMillis() - startTime; 
		else
			return stopTime - startTime;
	}
	
	public synchronized void setSize(Dimension d) {
		if(this.running) {
			this.newCanvasHeight = d.height;
			this.newCanvasWidth = d.width;
			this.sizeChanged = true;
		}
		else {
			this.mapCanvasHeight = d.height;
			this.mapCanvasWidth = d.width;
			this.solarCanvasHeight = d.height / 5;
		}
	}
}