package EarthPresentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
	private JLabel solarPanel;
	
	public DisplayGrid(final DisplayModel model, Dimension initialSize) {
		super();
		this.setLayout(new BorderLayout());
		mapPanel = new JLabel();
		
		this.add(mapPanel, BorderLayout.CENTER);
		
		this.setOpaque(false);
		this.model = model;
		model.addObserver(this);
		this.setPreferredSize(initialSize);
		this.addComponentListener(this);
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		Dimension updatedSize = this.mapPanel.getSize();
		model.setSize(updatedSize);		
	}
	/*
	@Override
	public void paintComponents(Graphics g) {		
		Graphics2D g2d = (Graphics2D)g;
		g2d.drawRenderedImage(model.getMapImage(), new AffineTransform());
		System.out.println("In View Paint Method");
		super.paintComponents(g);
	}*/

	@Override
	public void update(Observable o, Object arg) {
		mapPanel.setIcon(new ImageIcon(model.getMapImage()));
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
