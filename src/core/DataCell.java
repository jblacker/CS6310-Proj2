package core;

public class DataCell {
	
	private double latitude;
	private double longitude;
	private double height;
	private double upperWidth;
	private double lowerWidth;
	private double temperature;
	
	public DataCell() {
		this.latitude = 0f;
		this.longitude = 0f;
		this.height = 0f;
		this.upperWidth = 0f;
		this.lowerWidth = 0f;
		this.temperature = 0f;
	}
	
	public DataCell(double lat, double lon, double height, double uWidth, double lWidth) {
		this.latitude = lat;
		this.longitude = lon;
		this.height = height;
		this.upperWidth = uWidth;
		this.lowerWidth = lWidth;
		this.temperature = 0f;
	}
	
	public DataCell(double lat, double lon, double height, double uWidth, double lWidth, double temp) {
		this.latitude = lat;
		this.longitude = lon;
		this.height = height;
		this.upperWidth = uWidth;
		this.lowerWidth = lWidth;
		this.temperature = temp;
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
	}

	public double getUpperWidth() {
		return upperWidth;
	}

	public void setUpperWidth(double width) {
		this.upperWidth = width;
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
	}

}
