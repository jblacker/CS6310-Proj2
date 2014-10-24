package EarthPresentation;

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
		
		int height = initialSize.height / 5;
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] { initialSize.width };
		layout.rowHeights = new int[] { height, height, height, height, height, height };
		layout.columnWeights = new double[] { Double.MIN_VALUE };
		layout.rowWeights = new double[] { Double.MIN_VALUE };
		this.setLayout(layout);		
		
		mapPanel = new JLabel();
		
		GridBagConstraints mapConstraint = new GridBagConstraints();
		mapConstraint.insets = new Insets(5, 5, 5, 5);
		mapConstraint.gridx = 0;
		mapConstraint.gridy = 0;
		mapConstraint.gridheight = 5;
		mapConstraint.gridwidth = 1;
		mapConstraint.fill = GridBagConstraints.BOTH;
		this.add(mapPanel, mapConstraint);
		
		solarPanel = new JLabel();
		GridBagConstraints solarConstraint = new GridBagConstraints();
		solarConstraint.insets = new Insets(5, 5, 5, 5);
		solarConstraint.gridx = 0;
		solarConstraint.gridy = 5;
		solarConstraint.gridheight = 1;
		solarConstraint.gridwidth = 1;
		solarConstraint.fill = GridBagConstraints.BOTH;
		this.add(solarPanel, solarConstraint);
		
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
		solarPanel.setIcon(new ImageIcon(model.getSolarImage()));
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
