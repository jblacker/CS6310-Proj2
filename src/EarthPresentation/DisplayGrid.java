package EarthPresentation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class DisplayGrid extends JPanel implements Observer {
	
	private static final long serialVersionUID = 1332713083204648860L;
	
	private ArrayList<DisplayCell> cells;
	
	public ArrayList<DisplayCell> getCells() {
		return cells;
	}

	public void setCells(ArrayList<DisplayCell> cells) {
		this.cells = cells;
		this.invalidate();
	}
	
	@Override
	public void paintComponents(Graphics g) {		
		// TODO Populate image from model
		super.paintComponents(g);
		
	}

	@Override
	public void update(Observable o, Object arg) {
		this.repaint();
		
	}
}
