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
    private CellColumn[][] mGrid;
    /* Current temperatures */
    private double[][] mTemperatures;


    Simulation(int spacing, int timestep) {
	mSpacing = spacing % 180;
	if (timestep < 1) {
	    timestep = 1;
	}
	else if (timestep >= 140) {
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
	int[] dims = new int[2];

	dims[0] = (int)((180 + Util.limitValue(longitude, -179, 180)) /
		((double) mSpacing));
	dims[1] = (int)((90 - Util.limitValue(latitude, -89, 90)) /
		((double) mSpacing));
	
	return dims;
    }
    
    /*
     * Convert y-index to the latitude of the center of the cell.
     */
    public double cellIndexToLatitude(int yIndex) {
	return 90 - Util.limitValue(yIndex, 0, mGridHeight-1) * mSpacing;
    }
    
    /*
     * Convert x-index to the longitude of the center of the cell.
     */
    public double cellIndexToLongitude(int xIndex) {
	return Util.limitValue(xIndex, 0, mGridWidth-1) * mSpacing - 180;
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
    
    public double calculateRadiantTempertaure(double prevTemperatre, int[] index) {
	
	/* Get cell */
	CellColumn cell = getCellFromIndex(index);
	
	/* Calculate the heating from the sun */
	double Th = Util.zeroedDotProduct(cell.mSurfaceNormal, mSunVector);
	
	/* @TODO: Calculate cooling - some constant based on blahhh... */
	double Tc = 0;
	
	return prevTemperatre + Th - Tc;
    }
    
    public CellColumn getCellFromIndex(int[] index) {
	return mGrid[index[0]][index[1]];
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
