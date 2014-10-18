package core;

import java.util.List;

public class SimulationState {

	private List<DataCell> cells;
	private double sunLongitude;
	private int runningTime;
	
	public SimulationState(List<DataCell> cells, double sunLong, int runningTime) {
		this.cells = cells;
		this.sunLongitude = sunLong;
		this.runningTime = runningTime;
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

	public int getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(int runningTime) {
		this.runningTime = runningTime;
	}

}
