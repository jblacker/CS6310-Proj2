package EarthPresentation;

import java.awt.AlphaComposite;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import javax.swing.Timer;

import core.Config;
import core.DataCell;
import core.InitiativeEnum;
import core.SimulationState;
import core.ThreadedEnum;

public class DisplayModel extends Observable implements Runnable, ActionListener{

	private final Boolean hasInitative;
	
	private volatile boolean isCancelled = false; 
	private volatile boolean sizeChanged = false;
	private volatile boolean pauseRequested = false;

	private Timer refreshTimer;
	private BufferedImage temperatureMapImage;
	private BufferedImage solarImage;
	private BufferedImage solarOverlay;
	private BufferedImage compositeMap;
	
	private int mapCanvasHeight;
	private int mapCanvasWidth;
	private int solarCanvasHeight;
	
	private int newCanvasHeight;
	private int newCanvasWidth;
	
	private boolean running;
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
	
	public void consume() {
		Config config = Config.getInstance();
		while(!config.getBuffer().isEmpty()) {
			if(sizeChanged) {
				updateSize();
			}
			
			generateNextImageSet();
		}
	}
	
	public void run() {
		Config config = Config.getInstance();
		running = true;
		refreshTimer.start();
		startTime = System.currentTimeMillis();
		if(config.getInitiative() == InitiativeEnum.MASTER_CONTROL){
			while(!isCancelled){
				if(sizeChanged) {
					updateSize();
				}
				generateNextImageSet();
			}
		}
		else if(config.getInitiative() == InitiativeEnum.SIMULATION 
				&& config.getThreadingFlags().contains(ThreadedEnum.SIMULATION)) {
			while(!isCancelled){
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
		else if(config.getInitiative() == InitiativeEnum.SIMULATION) {
			while(!isCancelled){
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
		else if(config.getInitiative() == InitiativeEnum.PRESENTATION
				&& config.getThreadingFlags().contains(ThreadedEnum.SIMULATION)){
			config.request();
			while(!isCancelled){
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
		else if(config.getInitiative() == InitiativeEnum.PRESENTATION){
			//construct producer object
			while(!isCancelled){
				//ask producer to produce
				while(!config.getBuffer().isEmpty()) {
					generateNextImageSet();
				}
			}
		}	
			
		stopTime = System.currentTimeMillis();
		refreshTimer.stop();
		running = false;
	}
	
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
		generateSolarOverlayImage(simState.getSunLongitude(), cells);
		generateCompositeMapImage();
		generateNextSolarImage(simState.getSunLongitude());
	}
	
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
		Graphics2D graphics = nextImage.createGraphics();		
		
		//set transparent to use as overlay
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 2.0f));
		
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
	
	private void generateSolarOverlayImage(double solarLongitude, List<DisplayCell> cellData) {
		//filter out cells under sun
		List<DisplayCell> sunOverheadCells = new LinkedList<DisplayCell>();
		for(DisplayCell cell : cellData) {
			if(DisplayCell.isLongitudeInCell(solarLongitude, cell))
				sunOverheadCells.add(cell);
		}
		
		BufferedImage nextImage = new BufferedImage(mapCanvasWidth, mapCanvasHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = nextImage.createGraphics();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		for(DisplayCell cell : sunOverheadCells) {
			Polygon cellPolygon = new Polygon();
			Point ne = LatLongToMercatorPoint(cell.getNeCorner());
			Point nw = LatLongToMercatorPoint(cell.getNwCorner());
			Point se = LatLongToMercatorPoint(cell.getSeCorner());
			Point sw = LatLongToMercatorPoint(cell.getSwCorner());
			cellPolygon.addPoint(ne.x, ne.y);
			cellPolygon.addPoint(nw.x, nw.y);
			cellPolygon.addPoint(se.x, se.y);
			cellPolygon.addPoint(sw.x, sw.y);
			
			graphics.setColor(Color.YELLOW);
			graphics.fillPolygon(cellPolygon);
		}
		
		this.solarOverlay = nextImage;
	}
	
	private void generateCompositeMapImage() {
		BufferedImage nextImage = new BufferedImage(mapCanvasWidth, mapCanvasHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = nextImage.createGraphics();		
		
		//set transparent to use as overlay
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 2.0f));

		graphics.drawImage(this.temperatureMapImage, 0, 0, null);
		graphics.drawImage(this.solarOverlay, 0, 0, null);
		
		this.compositeMap = nextImage;
	}
	
	private void generateNextSolarImage(double solarLongitude) {
		BufferedImage nextImage = new BufferedImage(mapCanvasWidth, solarCanvasHeight, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = nextImage.createGraphics();
		
		double x = CalculateSolarMercatorPoint(solarLongitude).getX();
		double radius = this.solarCanvasHeight / 2;
		Shape sunCircle = new Ellipse2D.Double(this.solarCanvasHeight, this.solarCanvasHeight, x - radius, 0);
		
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(Color.YELLOW);
		graphics.fill(sunCircle);
		
		this.solarImage = nextImage;
	}
	
	public Point LatLongToMercatorPoint(GeoCoordinate coords) {
		double x = (coords.getLongitude() + 180 ) * (this.mapCanvasWidth / 360);
		x = Math.floor(x);
		double latRadians = (coords.getLatitude() * Math.PI) / 180;
		double mercScaled = Math.log(Math.tan(Math.PI / 4) + (latRadians/2));
		double y = (this.mapCanvasHeight / 2) - ((this.mapCanvasWidth * mercScaled) / (Math.PI * 2));
		y = Math.floor(y);
		
		return new Point((int)x, (int)y);
	}
	
	public Point CalculateSolarMercatorPoint(double longitude) {
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
			throw new IllegalStateException("Refresh rate cannot be set while running");
		
		refreshTimer = new Timer(ms, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == refreshTimer) {
			this.setChanged();
			this.notifyObservers(getElapsedTime());
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