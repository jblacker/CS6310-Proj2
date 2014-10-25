package EarthPresentation;

import java.awt.Color;

import core.DataCell;

public class DisplayCell {

	private final double temperature;
	private final double swCornerLatitude;
	private final double swCornerLongitiude;
	private final double height;
	private final double widthTop;
	private final double widthBottom;
	private final int gridSpacing;
	
	private GeoCoordinate swCorner;
	private GeoCoordinate seCorner;
	private GeoCoordinate neCorner;
	private GeoCoordinate nwCorner;
	private Color cellColor;
	
	
	public DisplayCell(DataCell data, int spacing) {
		this.temperature = data.getTemperature();
		this.swCornerLatitude = data.getLatitude();
		this.swCornerLongitiude = data.getLongitude();
		this.height = data.getHeight();
		this.widthBottom = data.getLowerWidth();
		this.widthTop = data.getUpperWidth();
		this.gridSpacing = spacing;
		calculateColor();
		processCalculations();
	}
	
	public void processCalculations() {
		swCorner = new GeoCoordinate(this.swCornerLatitude, this.swCornerLongitiude);
		seCorner = convertHeightWidthToLatLong(swCorner, DirectionEnum.SOUTHEAST);
		nwCorner = convertHeightWidthToLatLong(swCorner, DirectionEnum.NORTHWEST);
		neCorner = convertHeightWidthToLatLong(swCorner, DirectionEnum.NORTHEAST);
		
	}
	
	private double calculateCellSides(){
		double p = this.gridSpacing / 360;
		return core.Constants.EARTH_CIRCUMFERENCE * p; 
	}
	
	private double calculateBaseDifference(){
		if(this.widthBottom > this.widthTop){
			double diff = (this.widthBottom * 1000) - (this.widthTop * 1000);
			return diff / 2f;
		}
		else {
			double diff = (this.widthTop * 1000) - (this.widthBottom * 1000);
			return diff / 2f;
		}
	}
	
	public GeoCoordinate convertHeightWidthToLatLong(GeoCoordinate swOrigin, DirectionEnum direction) {	
		double bearing;
		double distance;
		double lat1, lat2;
		double lon1, lon2;
		double heightM;
		
		//convert to meters from KM
		heightM = this.height * 1000;		
		
		switch(direction){
			case NORTHWEST:
				distance = calculateCellSides();
				double sin = heightM / distance;
				bearing = Math.asin(sin); //in radians!
				break;
			case SOUTHEAST:
				bearing = Math.toRadians(90);
				distance = this.widthBottom * 1000; //convert width to meters
				break;
			case NORTHEAST:
				double modifiedBaseLength;
				if(this.widthBottom > this.widthTop)
					modifiedBaseLength = (this.widthBottom * 1000) - calculateBaseDifference();
				else
					modifiedBaseLength = (this.widthTop * 1000) - calculateBaseDifference();
				
				distance = Math.sqrt(Math.pow(modifiedBaseLength, 2) + Math.pow(heightM, 2));
				double tempSin = heightM / distance;
				bearing = Math.asin(tempSin); //in radians!
				break;
			default:
				throw new IllegalArgumentException("Invalid Direction Enum Value");
		}
		
		lat1 = Math.toRadians(swOrigin.getLatitude());
		lon1 = Math.toRadians(swOrigin.getLongitude());
		
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
		int temp = (int)Math.floor(celsiusTemp);
		int red, blue, green;

		if (temp <= -100) {
			blue = 170;
			green = 100;
			red = 170;
		}
		else if (temp <= -46) {
			temp = -1 * temp;
			blue = 255;
			green = 145 - (temp * 10) % 115;
			red = 255;
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
			blue = 145;
			green = 255 - (temp * 5) % 115;
			red = 255;
		}
		else {
			blue = 145 - (temp * 10) % 115;
			green = 145 - (temp * 10) % 115;
			red = 255;
		}
		
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
