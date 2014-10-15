package EarthPresentation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class DisplayGrid extends JPanel implements Observer {
	
	private static final long serialVersionUID = 1332713083204648860L;
	private final DisplayModel model;
	
	public DisplayGrid(final DisplayModel model, Dimension initialSize) {
		this.setOpaque(false);
		this.model = model;
		this.setPreferredSize(initialSize);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				model.setSize(e.getComponent().getSize());
			}
		});
	}
	
	@Override
	public void paintComponents(Graphics g) {		
		Graphics2D g2d = (Graphics2D)g;
		g2d.drawRenderedImage(model.getImage(), new AffineTransform());
		super.paintComponents(g);
	}

	@Override
	public void update(Observable o, Object arg) {
		this.repaint();	
	}
}
