
package EarthSim;

import core.Constants;
import core.Util;

public class ColumnCell {
    public final double mTopLength;
    public final double mBottomLength;
    public final double mSideLength;
    public final double mSurfaceArea;
    public final Cell[] mCellRow;
    
    public ColumnCell(Simulation simulation, int yIndex) {
	double topLatitude = simulation.cellIndexToLatitude(yIndex);
	double bottomLatitude = topLatitude + simulation.mSpacing;

	mTopLength = calculateWidthFromLatitiude(topLatitude, simulation.mSpacing);
	mBottomLength = calculateWidthFromLatitiude(bottomLatitude, simulation.mSpacing);
	mSurfaceArea = Util.calculateTrapezoidArea(
		mTopLength, mBottomLength, simulation.mCellHeight);
	mSideLength = calculateSideLength(mTopLength, mBottomLength, simulation.mCellHeight);
	
	mCellRow = new Cell[simulation.mGridWidth];
	for (int x = 0; x < simulation.mGridWidth; x++) {
	    mCellRow[x] = new Cell(simulation.getCellNormal(x, yIndex));
	}
    }
    
    private double calculateWidthFromLatitiude(double latitude, double spacing) {
	double radius = Math.cos(Math.abs(latitude)) * Constants.EARTH_RADIUS;
	return 2 * radius * Math.sin(spacing);
    }
    
    private double calculateSideLength(double topWidth, double bottomWidth, double height) {
	double base = Math.abs(topWidth - bottomWidth) / 2;
	return Math.sqrt(Math.pow(base, 2) + Math.pow(height, 2));
    }
}
