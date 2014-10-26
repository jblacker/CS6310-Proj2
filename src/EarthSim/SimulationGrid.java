package EarthSim;

import java.util.ArrayList;
import java.util.List;

import core.Constants;
import core.DataCell;

public class SimulationGrid {
	/*
	 * Number of degrees spanned by each grid cell in both latitude and
	 * longitude directions.
	 */
	private final int mSpacing;
	/* The width of the 2d array representing temperature values. */
	public final int mWidth;
	/* The height of the 2d array representing temperature values. */
	public final int mHeight;
	/* 2d-array of simulation cells. */
	private DataCell[][] mCells;
	
	
	public DataCell[][] getCells() {
		return mCells;
	}

	public SimulationGrid(int spacing) {
		mSpacing = ((spacing-1) % 180) + 1;
		mWidth = 360 / mSpacing;
		mHeight = 180 / mSpacing;
		mCells = new DataCell[mWidth][mHeight];
		
		for (int x = 0; x < mWidth; x++) {
			double longitude = cellIndexToLongitude(x);
			for (int y = 0; y < mHeight; y++) {
				mCells[x][y] = new DataCell(
						cellIndexToLatitude(y),
						longitude,
						mSpacing,
						Constants.AVERAGE_EARTH_TEMPERATURE);
			}
		}
	}
	
	public SimulationGrid(SimulationGrid other) {
		mSpacing = other.mSpacing;
		mWidth = other.mWidth;
		mHeight = other.mHeight;
		
		/* Perform deep copy of 2D data cell array. */
		mCells = new DataCell[mWidth][mHeight];
		for (int x = 0; x < mWidth; x++) {
			for (int y = 0; y < mHeight; y++) {
				mCells[x][y] = new DataCell(other.getCellFromIndex(x, y));
			}
		}
	}
	
	/*
	 * Convert y-index to the latitude of the center of the cell.
	 */
	public double cellIndexToLatitude(int yIndex) {
		return (yIndex - (mHeight / 2f)) * mSpacing;
	}

	/*
	 * Convert x-index to the longitude of the center of the cell.
	 */
	public double cellIndexToLongitude(int xIndex) {
		if ((xIndex < ((mWidth) / 2)))
			return -1f * (xIndex + 1) * mSpacing;
		else
			return 360f - (xIndex + 1) * mSpacing;	
	}
	
	/* Get DataCell object from grid. */
	public DataCell getCellFromIndex(int[] index) {
		return mCells[index[0]][index[1]];
	}
	public DataCell getCellFromIndex(int x, int y) {
		return mCells[x][y];
	}
	
	/* Calculate West neighbor. */
	public int[] getWestNeighbor(int[] index) {
		return new int[] { (index[0] - 1 + mWidth) % mWidth, index[1] };
	}

	/* Calculate West neighbor. */
	public int[] getEastNeighbor(int[] index) {
		return new int[] { (index[0] + 1) % mWidth, index[1]};
	}

	/* Calculate North neighbor. */
	public int[] getNorthNeighbor(int[] index) {
		if (index[1] < mHeight - 1)
			return new int[] {index[0], index[1] + 1};
		else
			return new int[] {(index[0] + mWidth / 2) % mWidth, index[1]};
	}

	/* Calculate South neighbor. */
	public int[] getSouthNeighbor(int[] index) {
		if (index[1] > 0)
			return new int[] {index[0], index[1] - 1};
		else
			return new int[] {(index[0] + mWidth / 2) % mWidth, index[1]};
	}
	
	/* Update all cells' temperatures form the neighboring
	 * cells in another grid. */
	public void processConvection(SimulationGrid other) {
		for (int x = 0; x < mWidth; x++) {
			for (int y = 0; y < mHeight; y++) {
				int[] index = new int[]{x, y};
				DataCell cell = getCellFromIndex(index);
				DataCell north = getCellFromIndex(other.getNorthNeighbor(index));
				DataCell south = getCellFromIndex(other.getSouthNeighbor(index));
				DataCell east = getCellFromIndex(other.getEastNeighbor(index));
				DataCell west = getCellFromIndex(other.getWestNeighbor(index));

				double newTemperature =
                        (cell.getUpperWidth() * north.getTemperature() / north.getArea()
						+ cell.getLowerWidth() * south.getTemperature() / south.getArea()
						+ cell.getHeight() * east.getTemperature() / east.getArea()
                        + cell.getHeight() * west.getTemperature() / west.getArea())
						/ cell.getPerimeter() * cell.getArea();

				cell.setTemperature(newTemperature);
			}
		}
	}
	
	/* Get a cell list object from the grid. */
	public List<DataCell> getCellList() {
		List<DataCell> list = new ArrayList<DataCell>(mWidth * mHeight);
		
		int index = 0;
		for (int x = 0; x < mWidth; x++) {
			for (int y = 0; y < mHeight; y++) {
				list.add(index++, getCellFromIndex(x, y));
			}
		}
		
		return list;
	}
	
	/* Calculate radiant temperature. */
	public void calculateRadiantTemperatures(double sunLongitude) {

		for (int x = 0; x < mWidth; x++) {
			for (int y = 0; y < mHeight; y++) {
				DataCell cell = getCellFromIndex(x, y);

                /* Calculate fraction of surface area of the earth occupied
                 * by the cell. */
                double beta = cell.getArea() / Constants.EARTH_SURFACE_AREA;

				/* Calculate attenuation. */
				double attenuation = calculateAttenuation(
						cell.getLatitude(),
						cell.getLongitude(),
						sunLongitude);

				/* Calculate the heating from the sun */
				double Th = Constants.AVERAGE_EARTH_TEMPERATURE * beta * 2 * 4 * attenuation * 2;

				/* Calculate cooling. */
				double Tc = beta * Constants.AVERAGE_EARTH_TEMPERATURE;

				cell.setTemperature(cell.getTemperature() + Th - Tc);
			}
		}
	}
	
	/* Calculate attenuation. */
	private double calculateAttenuation(double lat, double lon, double sunLon) {
        double attn = 0;
		double d = Math.abs(lon - sunLon);
        if (Math.signum(lon) != Math.signum(sunLon)) {
            d = 360 - d;
        }
		if (d < 90)
            attn = Math.cos(Math.toRadians(d)) * Math.cos(Math.toRadians(lat));
		return attn;
	}
}
