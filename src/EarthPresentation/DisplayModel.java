package EarthPresentation;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.swing.Timer;

import core.DataCell;

public class DisplayModel extends Observable implements Runnable, ActionListener{

	private final Boolean hasInitative;
	
	private volatile boolean isCancelled = false; 

	private Timer refreshTimer;
	private BufferedImage image;
	private int canvasHeight;
	private int canvasWidth;
	private boolean running;
	private long startTime;
	private long stopTime;
	private int timeStep;
	private int refreshRate;
	
	public DisplayModel(int height, int width, Boolean initiative) {
		this.canvasHeight = height;
		this.canvasWidth = width;
		this.refreshRate = 1000;
		this.refreshTimer = new Timer(refreshRate, this); //default?
		this.timeStep = 1; //default?
		this.hasInitative = initiative;
	}
	public DisplayModel(int height, int width, int timeStep, Boolean initiative) {
		this.canvasHeight = height;
		this.canvasWidth = width;
		this.timeStep = timeStep;
		this.refreshRate = 1000; 
		this.refreshTimer = new Timer(refreshRate, this);
		this.hasInitative = initiative;
	}
	
	public DisplayModel(int height, int width, int timeStep, int refreshRate, Boolean initiative) {
		this.canvasHeight = height;
		this.canvasWidth = width;
		this.timeStep = timeStep;
		this.refreshRate = refreshRate;
		this.refreshTimer = new Timer(refreshRate, this);
		this.hasInitative = initiative;
	}
	
	public void run() {
		running = true;
		refreshTimer.start();
		startTime = System.currentTimeMillis();
		while(!isCancelled) {
			generateNextImage();
			if(hasInitative == null || !hasInitative)
				Thread.yield();
			else {
				try {
					Thread.sleep(refreshRate);
				}
				catch(InterruptedException ex) {
					break;
				}
			}
		}
		stopTime = System.currentTimeMillis();
		refreshTimer.stop();
		running = false;
	}
	
	public void generateNextImage() {
		List<DataCell> simData = null; //TODO: THIS IS A STUB! INITIALIZE IN if.else BELOW
		List<DisplayCell> displayCells = new ArrayList<DisplayCell>();
		BufferedImage nextImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = nextImage.createGraphics();
		
		if(hasInitative == null){
			//block wait for next simulation dataset
		}
		else if(hasInitative){
			//asks for next simulation dataset
		}
		else {
			// not sure yet (simulation has initative)
		}
		
		//calculate latitude & longitude for ALL cell points
		for(DataCell cell : simData) {
			displayCells.add(new DisplayCell(cell));
		}
		
		//create polygon primitives
		for(DisplayCell cell : displayCells) {
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
			
			this.image = nextImage;
		}
	}
	
	private Point LatLongToMercatorPoint(GeoCoordinate coords) {
		double x = (coords.getLongitude() + 180 ) * (this.canvasWidth / 360);
		x = Math.floor(x);
		double latRadians = (coords.getLatitude() * Math.PI) / 180;
		double mercScaled = Math.log(Math.tan(Math.PI / 4) + (latRadians/2));
		double y = (this.canvasHeight / 2) - ((this.canvasWidth * mercScaled) / (Math.PI * 2));
		y = Math.floor(y);
		
		return new Point((int)x, (int)y);
	}
	
	public synchronized BufferedImage getImage() {
		return this.image;
	}
	
	public synchronized void stop() {
		isCancelled = true;		
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
}
