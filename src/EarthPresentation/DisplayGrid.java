package EarthPresentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DisplayGrid extends JPanel implements Observer, ComponentListener {
	
	private static final long serialVersionUID = 1332713083204648860L;
	
	private final DisplayModel model;
	
	private JLabel mapPanel;
	
	public DisplayGrid(final DisplayModel model, Dimension initialSize) {
		super(new BorderLayout());
		
		mapPanel = new JLabel();		
		this.add(mapPanel, BorderLayout.CENTER);
		
		this.setOpaque(false);
		this.model = model;
		model.addObserver(this); //wire to model
		this.setPreferredSize(initialSize);
		this.addComponentListener(this);
	}
	
	/**
	 * Used to notify the model that the size of the panel has changed
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		Dimension updatedSize = this.mapPanel.getSize();
		model.setSize(updatedSize);		
	}

	/**
	 * Triggered when the model notifies the View
	 * This will cause the image to be updated & repainted.
	 */
	@Override
	public void update(Observable o, Object arg) {
		mapPanel.setIcon(new ImageIcon(model.getMapImage()));
		this.invalidate();
		this.repaint();	
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// Not Used
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// Not Used		
	}
	
	@Override
	public void componentShown(ComponentEvent e) {
		// Not Used
	}
}
