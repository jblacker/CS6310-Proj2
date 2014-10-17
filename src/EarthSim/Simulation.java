package EarthSim;

import core.Constants;
import core.Util;


public class Simulation {
    /* Number of degrees spanned by each grid cell in both
     * latitude and longitude directions. */
    public final int mSpacing;
    /* Interval of simulation time in which temperature values
     * are calculated. */
    public final int mTimestep;
    /* The width of the 2d array representing temperature values. */
    public final int mGridWidth;
    /* The height of the 2d array representing temperature values. */
    public final int mGridHeight;
    /* Height of each cell. */
    public final double mCellHeight;
    /* 2d vector representing the direction the sun is shining on
     * the earth. */
    private double[] mSunVector;
    /* Grid of simulation cells. */
    private ColumnCell[] mGrid;
    /* Current temperatures */
    private double[][] mTemperatures;


    Simulation(int spacing, int timestep) {
	mSpacing = spacing % 180;
	if (timestep < 1) {
	    timestep = 1;
	}
	else if (timestep >= 1440) {
	    timestep = 1440;
	}
	mTimestep = timestep;
	mGridWidth = 360 / mSpacing;
	mGridHeight = 180 / mSpacing;
	
	mCellHeight = Util.calculateCellHeight(Constants.EARTH_RADIUS, mSpacing);
	
	mSunVector = new double[2];
	mSunVector[0] = 0;
	mSunVector[1] = 0;
	
	mTemperatures = new double[mGridWidth][mGridHeight];
    }

    /*
     * Convert Longitude and Latitude into an index into the
     * temperature value array.
     */
    public int[] getCellIndex(double latitude, double longitude) {
	int[] dims = {
		(int) Math.floor(longitude / (double) mSpacing) + mGridWidth / 2,
		(int) Math.floor(latitude / (double) mSpacing) + mGridWidth / 2
	};
	return dims;
    }
    
    /*
     * Convert y-index to the latitude of the center of the cell.
     */
    public double cellIndexToLatitude(int yIndex) {
	return 90 - Util.convertIntoValidRange(yIndex, 0, mGridHeight-1) * mSpacing;
    }
    
    /*
     * Convert x-index to the longitude of the center of the cell.
     */
    public double cellIndexToLongitude(int xIndex) {
	return Util.convertIntoValidRange(xIndex, 0, mGridWidth-1) * mSpacing - 180;
    }
    
    /* Calculate West neighbor. */
    public void getWestNeighbor(int[] index) {
	index[0] = (index[0] + 1) % mGridWidth;
    }

    /* Calculate West neighbor. */
    public void getEastNeighbor(int[] index) {
	index[0] = (index[0] - 1 + mGridWidth) % mGridWidth;
    }

    /* Calculate North neighbor. */
    public void getNorthNeighbor(int[] index) {
	if (index[1] < mGridHeight - 1) {
	    index[1]++;
	}
	else {
	    index[0] = (index[0] + mGridWidth / 2) % mGridWidth;
	}
    }

    /* Calculate South neighbor. */
    public void getSouthNeighbor(int[] index) {
	if (index[1] > 0) {
	    index[1]--;
	}
	else {
	    index[0] = (index[0] + mGridWidth / 2) % mGridWidth;
	}
    }

    /*
     * Calculate the normal vector for a given cell.
     */
    public double[] getCellNormal(int xIndex, int yIndex) {
	double[] normal = {
		cellIndexToLongitude(xIndex) + ((double) mSpacing) / 2,
		cellIndexToLatitude(yIndex) + ((double) mSpacing) / 2 };
	return normal;
    }
    
    public double calculateRadiantTempertaure(
	    double prevTemperatre, int xIndex, int yIndex) {
	
	/* Get column cell */
	ColumnCell column = getCellFromIndex(yIndex);
	
	/* Calculate the heating from the sun */
	double Th = Util.zeroedDotProduct(column.mCellRow[xIndex].mSurfaceNormal, mSunVector);
	
	/* @TODO: Calculate cooling - some constant based on blahhh... */
	double Tc = 0;
	
	return prevTemperatre + Th - Tc;
    }
    
    public ColumnCell getCellFromIndex(int index) {
	return mGrid[index];
    }
    
    public void processStep() {
	
	/* Process radiant temperature changes. */
	for (int x = 0; x < mGridWidth; x++) {
	    for (int y = 0; y < mGridHeight; y++) {
		mTemperatures[x][y] = calculateRadiantTempertaure(
			mTemperatures[x][y], new int[]{x, y});
	    }
	}
	
	/* Process convection. */
	for (int x = 0; x < mGridWidth; x++) {
	    for (int y = 0; y < mGridHeight; y++) {
		// @TODO: how to prorate??
	    }
	}
    }
}
