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
	
	/**
	 * Handles the overall calculations of determining the latitude & longitude 
	 * of each corner of the cell
	 */
	public void processCalculations() {
		swCorner = new GeoCoordinate(this.swCornerLatitude, this.swCornerLongitiude);
		seCorner = calculateCoordinateForPoint(DirectionEnum.SOUTHEAST);
		nwCorner = calculateCoordinateForPoint(DirectionEnum.NORTHWEST);
		neCorner = calculateCoordinateForPoint(DirectionEnum.NORTHEAST);
		
	}
	
	/**
	 * Calculates the sides of the iso trapezoid on the earth 
	 * @return The distance in meters of the intersecting sides
	 */
	private double calculateCellSides(){
		double p = this.gridSpacing / 360f;
		return core.Constants.EARTH_CIRCUMFERENCE * p; 
	}
	
	/**
	 * Calculates the difference between an edge and the height line of the shorter base
	 * @return The difference in meters
	 */
	private double calculateBaseDifference(){
		if(this.widthBottom > this.widthTop){
			double diff = this.widthBottom - this.widthTop;
			return diff / 2f;
		}
		else {
			double diff = this.widthTop - this.widthBottom;
			return diff / 2f;
		}
	}
	
	/**
	 * Calculate the lat & long of a point on the cell other than the origin (SW)  
	 * Using the origin the other points can be calculated.
	 * @param direction GeoDirection from the Origin
	 * @return GeoCoordinate containing the latitude & longitude for that point
	 */
	public GeoCoordinate calculateCoordinateForPoint(DirectionEnum direction) {	
		double bearing;
		double distance;
		double lat1, lat2;
		double lon1, lon2;	
		
		switch(direction){
			case NORTHWEST:
				distance = calculateCellSides();
				double sin = height / distance;
				bearing = Math.asin(sin); //in radians!
				break;
			case SOUTHEAST:
				bearing = Math.toRadians(90);
				distance = this.widthBottom;
				break;
			case NORTHEAST:
				double modifiedBaseLength;
				if(this.widthBottom > this.widthTop)
					modifiedBaseLength = this.widthBottom - calculateBaseDifference();
				else
					modifiedBaseLength = this.widthTop - calculateBaseDifference();
				
				distance = Math.sqrt(Math.pow(modifiedBaseLength, 2) + Math.pow(height, 2));
				double tempSin = height / distance;
				bearing = Math.asin(tempSin); //in radians!
				break;
			default:
				throw new IllegalArgumentException("Invalid Direction Enum Value");
		}
		
		//convert to radians
		lat1 = Math.toRadians(swCorner.getLatitude());
		lon1 = Math.toRadians(swCorner.getLongitude());
		
		//calculate latitude of point
		lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / core.Constants.EARTH_RADIUS) +
				Math.cos(lat1) * Math.sin(distance / core.Constants.EARTH_RADIUS) * Math.cos(bearing));
		
		//calculate longitude of point
		lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distance / core.Constants.EARTH_RADIUS) * Math.cos(lat1), 
				Math.cos(distance / core.Constants.EARTH_RADIUS) - Math.sin(lat1) * Math.sin(lat2));
		
		return new GeoCoordinate(Math.toDegrees(lat2), Math.toDegrees(lon2));
	}

	/**
	 * Gets the cell's color publicly
	 * @return Color object
	 */
	public Color getColor() {
		return cellColor;
	}

	/**
	 * Gets the coordinates of SW Corner of the Cell
	 * @return the SW Corner Coordinates
	 */
	public GeoCoordinate getSwCorner() {
		return swCorner;
	}

	/**
	 * Gets the coordinates of SE Corner of the Cell
	 * @return the SE Corner Coordinates
	 */
	public GeoCoordinate getSeCorner() {
		return seCorner;
	}

	/**
	 * Gets the coordinates of NE Corner of the Cell
	 * @return the NE Corner Coordinates
	 */
	public GeoCoordinate getNeCorner() {
		return neCorner;
	}

	/**
	 * Gets the coordinates of NW Corner of the Cell
	 * @return the NW Corner Coordinates
	 */
	public GeoCoordinate getNwCorner() {
		return nwCorner;
	}

	/**
	 * Gets the temperature of the cell
	 * @return the temperature of the cell
	 */
	public double getTemperature() {
		return temperature;
	}
	
	/**
	 * Calculates and sets the color of the cell using the algorithm provided by the sample
	 * in the assignment page.
	 */
	private void calculateColor() {
		//convert to Celsius for more accurate color values
		double celsiusTemp = this.temperature - 273.15;
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
		
		cellColor = new Color(red, green, blue);
	}
	
	/**
	 * Used to check if a longitude is located within the cell
	 * @param lon Longitude to check
	 * @param cell A DisplayCell to test
	 * @return True if it's located within the cell, otherwise false
	 */
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
