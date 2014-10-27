package EarthSim;

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

                /* Add up the temperature of all neighbors scaled by the ratio
                 * of the length of the connecting side to the total perimeter and
                 * the ration of the cells surface area to the neighbor's surface
                 * area. */
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

				/* Calculate the heating from the sun which is:
				 * - the average temperature on earth
				 * - times the ratio of the surface area occupied
				 *   by the cell to the total surface area of the
				 *   earth
				 * - times 2 since only half the earth is heated
				 * - times 8, which is the total attenuation values
				 *   (integral of cos() from 0 to pi in both lat and
				 *   long directions).
				 * - times the attenuation multiplier (between 0 and 1).
				 */
				double Th = Constants.AVERAGE_EARTH_TEMPERATURE * beta * 2 * 8 * attenuation;

				/* Calculate cooling, which is the fraction of surface
				 * area of the earth occupied by the cell times the
				 * average temperature on earth. */
				double Tc = beta * Constants.AVERAGE_EARTH_TEMPERATURE;

                /* Apply the total temperature change. */
				cell.setTemperature(cell.getTemperature() + Th - Tc);
			}
		}
	}
	
	/* Calculate attenuation of sun's heat for a given grid cell. */
	private double calculateAttenuation(double lat, double lon, double sunLon) {
        double attn = 0;
        /* Get the difference in angle between the cell's longitude and the sun's. */
		double d = Math.abs(lon - sunLon);
        /* Handle wrap-around case. */
        if (Math.signum(lon) != Math.signum(sunLon)) {
            d = 360 - d;
        }
        /* Set value greater than zero only if on the day side of the planet. */
		if (d < 90)
            attn = Math.cos(Math.toRadians(d)) * Math.cos(Math.toRadians(lat));
		return attn;
	}
}
