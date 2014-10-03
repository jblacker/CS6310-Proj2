package EarthPresentation;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/*
 * DisplayEarth.java is the GUI Presentation Layer to display
 * the Earth in a Swing Panel
 */
import javax.swing.*;        
 
public class DisplayMapProjection {
    /**
     * DisplayEarth constructor
     */
	public DisplayMapProjection() {
		init();
	}
    /**
     * Initializes the GUI.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
   private static void init() {
	  javax.swing.SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
	          try {
				createGUI();
			} catch (IOException e) {
				e.printStackTrace();
			}
	      }
	  });   
   }
    /**
     * Create the GUI and components
     * @throws IOException 
     */
    private static void createGUI() throws IOException {
        //Create and set up the window.
        JFrame frame = new JFrame("Heated Earth Results");
        frame.setPreferredSize(new Dimension(1050,630));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add the Earth map
        BufferedImage myPicture = ImageIO.read(new File("resources/earth_map.jpg"));
        JLabel picLabel = new JLabel(new ImageIcon(myPicture));
        frame.getContentPane().add(picLabel);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}