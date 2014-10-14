package EarthPresentation;

import java.awt.Point;

import javax.swing.JPanel;

public class DisplayGrid extends JPanel {
	
	private static final long serialVersionUID = 1332713083204648860L;
	
	private DisplayCell[][] grid;
	private int cellSpacing;
	
	public DisplayGrid(int spacing) {
		this.cellSpacing = spacing;
	}
	
	public int getCellSpacing() {
		return cellSpacing;
	}

	public void setCellSpacing(int cellSpacing) {
		if(cellSpacing < 0 || cellSpacing > 180)
			throw new IllegalArgumentException("Spacing must be between 0 & 180");
		
		this.cellSpacing = cellSpacing;
	}

	public void Initialize() {
		//TODO: Implement based on mercator projection
		
		//build grid
		int size = 180 / this.cellSpacing;
		grid = new DisplayCell[size][size];
		
		//TODO:  initialize all cells.  Awaiting research on Mercator projections
	}
	
	public Point LatLongToMercatorPoint(double lat, double lon) {
		double x = (lon + 180 ) * (this.getWidth() / 360);
		x = Math.floor(x);
		double latRadians = (lat * Math.PI) / 180;
		double mercScaled = Math.log(Math.tan(Math.PI / 4) + (latRadians/2));
		double y = (this.getHeight() / 2) - ((this.getWidth() * mercScaled) / (Math.PI * 2));
		y = Math.floor(y);
		
		return new Point((int)x, (int)y);
	}
	
	public GeoCoordinate convertHeightWidthToLatLong(GeoCoordinate origin, double height, double width) {
		//convert to KM from M.  Height might be 0 (width should never be 0)	
		double bearing;
		double distance;
		double lat1, lat2;
		double lon1, lon2;
		
		width *= 1000;
		
		if(height > 0) {
			height *= 1000;
			distance = Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2));
			bearing = Math.asin(height/distance); 
		}
		else {
			distance = width;
			bearing = Math.toRadians(90);
		}
		
		lat1 = Math.toRadians(origin.getLatitude());
		lon1 = Math.toRadians(origin.getLongitude());
		
		lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / core.Constants.EARTH_RADIUS) +
				Math.cos(lat1) * Math.sin(distance / core.Constants.EARTH_RADIUS) * Math.cos(bearing));
		
		lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distance / core.Constants.EARTH_RADIUS) * Math.cos(lat1), 
				Math.cos(distance / core.Constants.EARTH_RADIUS) - Math.sin(lat1) * Math.sin(lat2));
		
		return new GeoCoordinate(Math.toDegrees(lat2), Math.toDegrees(lon2));
	}
}
