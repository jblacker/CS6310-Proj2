package core;

import java.util.List;

public class SimulationState {

	private List<DataCell> cells;
	private double sunLongitude;
	
	public SimulationState(List<DataCell> cells, double sunLong) {
		this.cells = cells;
		this.sunLongitude = sunLong;
	}

	public List<DataCell> getCells() {
		return cells;
	}

	public void setCells(List<DataCell> cells) {
		this.cells = cells;
	}

	public double getSunLongitude() {
		return sunLongitude;
	}

	public void setSunLongitude(double sunLongitude) {
		this.sunLongitude = sunLongitude;
	}

}
