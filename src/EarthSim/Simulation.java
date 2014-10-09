package EarthSim;

import core.Util;


public class Simulation {
    /* Number of degrees spanned by each grid cell in both
     * latitude and longitude directions. */
    public final int mSpacing;
    /* Interval of simulation time in which temperature values
     * are calculated. */
    public final int mTimestep;
    /* The length of the array containing temperature values. */
    public final int mValueArraySize;

    private final int mGridWidth;

    Simulation() {
	mSpacing = 15;
	mTimestep = 1;
	mGridWidth = 360 / mSpacing;
	mValueArraySize = (180 / mSpacing) * mGridWidth;
    }

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
	mValueArraySize = (180 / mSpacing) * mGridWidth;
    }

    /*
     * Convert Longitude and Latitude into an index into the
     * temperature value array.
     */
    public int getCellIndex(int latitude, int longitude) {
	int bin_y = (90 - Util.limitValue(latitude, -89, 90)) / mSpacing;
	int bin_x = (180 - Util.limitValue(longitude, -179, 180)) / mSpacing;

	return mGridWidth * bin_y + bin_x;
    }
}
