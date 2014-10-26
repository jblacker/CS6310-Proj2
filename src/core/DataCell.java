package core;

public class DataCell implements Cloneable {
	
	private double latitude;
	private double longitude;
	private double height;
	private double sideLength;
	private double upperWidth;
	private double lowerWidth;
	private double perimeter;
	private double area;
	private double temperature;
	
	public DataCell(DataCell other) {
		this.latitude = other.latitude;
		this.longitude = other.longitude;
		this.height = other.height;
		this.sideLength = other.sideLength;
		this.upperWidth = other.upperWidth;
		this.lowerWidth = other.lowerWidth;
		this.perimeter = other.perimeter;
		this.area = other.area;
		this.temperature = other.temperature;
	}

	public DataCell(double lat, double lon, int spacing, double temp) {
		this.latitude = lat;
		this.longitude = lon;
		this.sideLength = calculateSideLength(spacing);
		this.upperWidth = calculateUpperWidth(spacing);
		this.lowerWidth = calculateLowerWidth();
		this.height = calculateHeight();
		this.perimeter = calculatePerimeter();
		this.area = calculateArea();
		this.temperature = temp;
	}

	private double calculateSideLength(int spacing) {
		return (spacing / 360f) * Constants.EARTH_CIRCUMFERENCE;
	}

	private double calculateUpperWidth(int spacing) {
		return Math.cos(2f * Math.PI * (this.latitude + spacing) / 360) * this.sideLength;
	}
	
	private double calculateLowerWidth() {
		return Math.cos(2f * Math.PI * this.latitude / 360) * this.sideLength;
	}

	private double calculateHeight() {
		return Math.sqrt(Math.pow(this.sideLength, 2)
				- Math.pow(this.lowerWidth - this.upperWidth, 2) / 4f);
	}

	private double calculatePerimeter() {
		return 2f * this.sideLength + this.upperWidth + this.lowerWidth;
	}
	
	private double calculateArea() {
		return (this.upperWidth + this.lowerWidth) / 2f * this.height;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
		this.perimeter = calculatePerimeter();
		this.area = calculateArea();
	}

	public double getUpperWidth() {
		return upperWidth;
	}

	public void setUpperWidth(double width) {
		this.upperWidth = width;
		this.perimeter = calculatePerimeter();
		this.area = calculateArea();
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public double getLowerWidth() {
		return lowerWidth;
	}

	public void setLowerWidth(double lowerWidth) {
		this.lowerWidth = lowerWidth;
		this.perimeter = calculatePerimeter();
		this.area = calculateArea();
	}

	public double getPerimeter() {
		return this.perimeter;
	}
	
	public double getArea() {
		return this.area;
	}
}
