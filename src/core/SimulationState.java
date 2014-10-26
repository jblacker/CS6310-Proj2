package core;


public class SimulationState {

	private DataCell[][] cells;
	private double sunLongitude;
	private int runningTime;
	
	public SimulationState(DataCell[][] cells, double sunLong, int runningTime) {
		this.cells = cells;
		this.sunLongitude = sunLong;
		this.runningTime = runningTime;
        //printAverageTemp();
	}

    private void printAverageTemp() {
        double count = 0;
        double sum = 0;
        for (DataCell[] d_col : cells) {
            for (DataCell d : d_col) {
                count++;
                sum += d.getTemperature();
            }
        }
        System.out.printf("%f\n", sum / count);
    }

	public DataCell[][] getCells() {
		return cells;
	}

	public void setCells(DataCell[][] cells) {
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
