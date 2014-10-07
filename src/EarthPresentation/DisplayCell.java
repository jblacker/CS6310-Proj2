package EarthPresentation;

import java.awt.Color;

import javax.swing.JPanel;

public class DisplayCell extends JPanel {

	private static final long serialVersionUID = 5433812543678339960L;
	private double temperature;
	
	/**
	 * Create the panel.
	 */
	public DisplayCell(int width, int height) {
		this.temperature = 288d;
		this.setOpaque(true);
		this.setColor(this.temperature);
		this.setSize(width, height);
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
		setColor(this.temperature);		
	}
	
	private void setColor(double temp) {
		//convert to Celsius for more accurate color values
		double celsiusTemp = temp - 273.15;
		int t = (int)Math.floor(celsiusTemp);
		int red, blue;
		int green = 126; //midway point
		Color cellColor;
		
		red = (255  * t) / 100;
		blue = (255 * (100 - t)) / 100;
		
		cellColor = new Color(red, green, blue);
		this.setBackground(cellColor);
	}
	
	

}
