package core;


public class SimulationState {

	private DataCell[][] cells;
	private double sunLongitude;
	
	public SimulationState(DataCell[][] cells, double sunLong) {
		this.cells = cells;
		this.sunLongitude = sunLong;
	}

	public DataCell[][] getCells() {
		return cells;
	}

	public double getSunLongitude() {
		return sunLongitude;
	}
}
