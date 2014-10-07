package EarthPresentation;

import javax.swing.JPanel;

public class DisplayGrid extends JPanel {
	
	private static final long serialVersionUID = 1332713083204648860L;
	
	private DisplayCell[][] grid;
	private int cellSpacing;
	
	public DisplayGrid(int spacing) {
		this.cellSpacing = spacing;
	}
	
	public int getCellSpacing() {
		return cellSpacing;
	}

	public void setCellSpacing(int cellSpacing) {
		if(cellSpacing < 0 || cellSpacing > 180)
			throw new IllegalArgumentException("Spacing must be between 0 & 180");
		
		this.cellSpacing = cellSpacing;
	}

	public void Initialize() {
		//TODO: Implement based on mercator projection
		
		//build grid
		int size = 180 / this.cellSpacing;
		grid = new DisplayCell[size][size];
		
		//TODO:  initialize all cells.  Awaiting research on Mercator projections
	}
	
	public DisplayCell getCell(int x, int y) {
		//TODO: Implement based on conversions
		return null;
	}

}
