package EarthPresentation;

import java.awt.Color;

import core.DataCell;

public class DisplayCell {

	private final double temperature;
	private final double seCornerLatitude;
	private final double seCornerLongitiude;
	private final double height;
	private final double widthTop;
	private final double widthBottom;
	
	private GeoCoordinate swCorner;
	private GeoCoordinate seCorner;
	private GeoCoordinate neCorner;
	private GeoCoordinate nwCorner;
	private Color cellColor;
	
	public DisplayCell(double temp, double lat, double lon, double height, double wTop, double wBottom) {
		this.temperature = temp;
		this.seCornerLatitude = lat;
		this.seCornerLongitiude = lon;
		this.height = height;
		this.widthBottom = wBottom;
		this.widthTop = wTop;
		calculateColor();
		processCalculations();
	}
	
	public DisplayCell(DataCell data) {
		this.temperature = data.getTemperature();
		this.seCornerLatitude = data.getLatitude();
		this.seCornerLongitiude = data.getLongitude();
		this.height = data.getHeight();
		this.widthBottom = data.getLowerWidth();
		this.widthTop = data.getUpperWidth();
		calculateColor();
		processCalculations();
	}
	
	public void processCalculations() {
		seCorner = new GeoCoordinate(this.seCornerLatitude, this.seCornerLongitiude);
		swCorner = DisplayCell.convertHeightWidthToLatLong(seCorner, 0, widthBottom);
		nwCorner = DisplayCell.convertHeightWidthToLatLong(seCorner, height, widthTop);
		neCorner = DisplayCell.convertHeightWidthToLatLong(nwCorner, 0, -widthTop);
		
	}
	
	public static GeoCoordinate convertHeightWidthToLatLong(GeoCoordinate origin, double height, double width) {
		//convert to KM from M.  Height might be 0 (width should never be 0)	
		double bearing;
		double distance;
		double lat1, lat2;
		double lon1, lon2;
		
		width *= 1000;
		
		if(height != 0) {
			height *= 1000;
			distance = Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2));
			bearing = Math.asin(height / distance); 
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

	public Color getColor() {
		return cellColor;
	}

	public GeoCoordinate getSwCorner() {
		return swCorner;
	}

	public GeoCoordinate getSeCorner() {
		return seCorner;
	}

	public GeoCoordinate getNeCorner() {
		return neCorner;
	}

	public GeoCoordinate getNwCorner() {
		return nwCorner;
	}

	public double getTemperature() {
		return temperature;
	}
	
	private void calculateColor() {
		//convert to Celsius for more accurate color values
		double celsiusTemp = this.temperature - 273.15;
		int t = (int)Math.floor(celsiusTemp);
		int red, blue;
		int green = 126; //midway point
		
		red = (255  * t) / 100;
		blue = (255 * (100 - t)) / 100;
		
		cellColor = new Color(red, green, blue);
	}
	
	public static boolean isLongitudeInCell(double lon, DisplayCell cell) {
		boolean useNorth = cell.widthBottom < cell.widthTop;
				
		if(useNorth) {
			if(cell.neCorner.getLongitude() > 0 && cell.nwCorner.getLongitude() > 0) {
				//Eastern Hemispheres
				if(cell.neCorner.getLongitude() >= lon && cell.nwCorner.getLongitude() <= lon)
					return true;
				else
					return false;
			}
			else if (cell.neCorner.getLongitude() < 0 && cell.nwCorner.getLongitude() < 0){
				//Western Hemispheres
				if(cell.neCorner.getLongitude() <= lon && cell.nwCorner.getLongitude() >= lon)
					return true;
				else
					return false;
			}
			else {
				//Straddling Prime Meridian
				if(lon > 0 && cell.nwCorner.getLongitude() <= lon )
					return true;
				else if(lon < 0 && cell.neCorner.getLongitude() >= lon)
					return true;
				else
					return false;
			}
		}
		else {
			if(cell.seCorner.getLongitude() > 0 && cell.swCorner.getLongitude() > 0) {
				//Eastern Hemispheres
				if(cell.seCorner.getLongitude() >= lon && cell.swCorner.getLongitude() <= lon)
					return true;
				else
					return false;
			}
			else if (cell.seCorner.getLongitude() < 0 && cell.swCorner.getLongitude() < 0){
				//Western Hemispheres
				if(cell.seCorner.getLongitude() <= lon && cell.swCorner.getLongitude() >= lon)
					return true;
				else
					return false;
			}
			else {
				//Straddling Prime Meridian
				if(lon > 0 && cell.swCorner.getLongitude() <= lon )
					return true;
				else if(lon < 0 && cell.seCorner.getLongitude() >= lon)
					return true;
				else
					return false;
			}
		}
	}
}
