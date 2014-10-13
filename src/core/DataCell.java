package core;

public class DataCell {
	
	private double latitude;
	private double longitude;
	private double height;
	private double width;
	private double temperature;
	
	public DataCell() {
		this.latitude = 0f;
		this.longitude = 0f;
		this.height = 0f;
		this.width = 0f;
		this.temperature = 0f;
	}
	
	public DataCell(double lat, double lon, double height, double width) {
		this.latitude = lat;
		this.longitude = lon;
		this.height = height;
		this.width = width;
		this.temperature = 0f;
	}
	
	public DataCell(double lat, double lon, double height, double width, double temp) {
		this.latitude = lat;
		this.longitude = lon;
		this.height = height;
		this.width = width;
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

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

}
