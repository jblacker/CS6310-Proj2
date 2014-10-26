package EarthPresentation;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JSlider;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import EarthSim.Simulation;

import core.Config;
import core.InitiativeEnum;
import core.ThreadedEnum;

public class MasterGui {

	private JFrame frame;
	private int spacingValue;
	private int simulationTime;
	private int refreshRate;
	private JSlider spacingSlider;
	private JSlider timeSlider;
	private JSlider refreshSlider;
	private JButton simulateBtn;
	private JButton pauseBtn;
	private volatile boolean isPaused;
	private volatile boolean isRunning;
	
	private DisplayGrid view;
	private DisplayModel model;
	
	private Simulation simulation;

	/**
	 * Launch the application.
	 */
	public static void start() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MasterGui window = new MasterGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MasterGui() {
		initialize();
		this.spacingValue = 15;
		this.isRunning = false;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 650, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel controlPanel = new JPanel();
		frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
		GridBagLayout gbl_controlPanel = new GridBagLayout();
		gbl_controlPanel.columnWidths = new int[] {10, 65, 45};
		gbl_controlPanel.rowHeights = new int[]{20, 20, 20};
		gbl_controlPanel.columnWeights = new double[]{1, 4, 1};
		gbl_controlPanel.rowWeights = new double[]{Double.MIN_VALUE};
		controlPanel.setLayout(gbl_controlPanel);
		
		JLabel spacingLbl = new JLabel("Grid Spacing");
		JLabel timingLbl = new JLabel("Timing");
		JLabel refreshLbl = new JLabel("Refresh Rate");
		GridBagConstraints gbc_spacingLbl = new GridBagConstraints();
		gbc_spacingLbl.insets = new Insets(0, 0, 5, 5);
		gbc_spacingLbl.gridx = 0;
		gbc_spacingLbl.gridy = 0;
		controlPanel.add(spacingLbl, gbc_spacingLbl);
		
		GridBagConstraints gbc_timingLbl = new GridBagConstraints();
		gbc_timingLbl.insets = new Insets(0, 0, 5, 5);
		gbc_timingLbl.gridx = 0;
		gbc_timingLbl.gridy = 1;
		controlPanel.add(timingLbl, gbc_timingLbl);
		
		GridBagConstraints gbc_refreshLbl = new GridBagConstraints();
		gbc_refreshLbl.insets = new Insets(0, 0, 5, 5);
		gbc_refreshLbl.gridx = 0;
		gbc_refreshLbl.gridy = 2;
		controlPanel.add(refreshLbl, gbc_refreshLbl);
		
		
		spacingSlider = new JSlider();
		Hashtable<Integer, JLabel> spacingTicks = new Hashtable<Integer, JLabel>();
		spacingTicks.put(1, new JLabel("1"));
		spacingTicks.put(2, new JLabel("2"));
		spacingTicks.put(3, new JLabel("3"));
		spacingTicks.put(4, new JLabel("4"));
		spacingTicks.put(5, new JLabel("5"));
		spacingTicks.put(6, new JLabel("6"));
		spacingTicks.put(7, new JLabel("9"));
		spacingTicks.put(8, new JLabel("10"));
		spacingTicks.put(9, new JLabel("12"));
		spacingTicks.put(10, new JLabel("15"));
		spacingTicks.put(11, new JLabel("18"));
		spacingTicks.put(12, new JLabel("20"));
		spacingTicks.put(13, new JLabel("30"));
		spacingTicks.put(14, new JLabel("36"));
		spacingTicks.put(15, new JLabel("45"));
		spacingTicks.put(16, new JLabel("60"));
		spacingTicks.put(17, new JLabel("90"));
		spacingTicks.put(18, new JLabel("180"));
		spacingSlider.setLabelTable(spacingTicks);
		spacingSlider.setMinimum(1);
		spacingSlider.setMaximum(18);
		spacingSlider.setSnapToTicks(true);
		spacingSlider.setPaintLabels(true);
		spacingSlider.setValue(10);
		spacingSlider.addChangeListener(new ChangeListener(){
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				switch(spacingSlider.getValue()){
					case 1:
						spacingValue = 1;
						break;
					case 2:
						spacingValue = 2;
						break;
					case 3:
						spacingValue = 3;
						break;
					case 4:
						spacingValue = 4;
						break;
					case 5:
						spacingValue = 5;
						break;
					case 6:
						spacingValue = 6;
						break;
					case 7:
						spacingValue = 9;
						break;
					case 8:
						spacingValue = 10;
						break;
					case 9:
						spacingValue = 12;
						break;
					case 10:
						spacingValue = 15;
						break;
					case 11:
						spacingValue = 18;
						break;
					case 12:
						spacingValue = 20;
						break;
					case 13:
						spacingValue = 30;
						break;
					case 14:
						spacingValue = 36;
						break;
					case 15:
						spacingValue = 45;
						break;
					case 16:
						spacingValue = 60;
						break;
					case 17:
						spacingValue = 90;
						break;
					case 18:
						spacingValue = 180;
						break;
				}
			}	
		});
		GridBagConstraints gbc_spacingSlider = new GridBagConstraints();
		gbc_spacingSlider.insets = new Insets(0, 0, 5, 5);
		gbc_spacingSlider.gridx = 1;
		gbc_spacingSlider.gridy = 0;
		gbc_spacingSlider.fill = GridBagConstraints.HORIZONTAL;
		controlPanel.add(spacingSlider, gbc_spacingSlider);
		
		timeSlider = new JSlider();
		timeSlider.setMinimum(1);
		timeSlider.setMaximum(1440);
		timeSlider.setMajorTickSpacing(100);
		timeSlider.setMinorTickSpacing(10);
		timeSlider.setPaintTicks(true);
		timeSlider.setSnapToTicks(true);
		timeSlider.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent arg0) {
				simulationTime = timeSlider.getValue();		
			}
		});
		GridBagConstraints gbc_timeSlider = new GridBagConstraints();
		gbc_timeSlider.insets = new Insets(0, 0, 5, 5);
		gbc_timeSlider.gridx = 1;
		gbc_timeSlider.gridy = 1;
		gbc_timeSlider.fill = GridBagConstraints.HORIZONTAL;
		controlPanel.add(timeSlider, gbc_timeSlider);
		
		refreshSlider = new JSlider();
		refreshSlider.setMinimum(1);
		refreshSlider.setMaximum(100); //TODO: CONFIRM AN ACTUAL VALUE FOR THIS
		refreshSlider.setSnapToTicks(true);
		refreshSlider.setPaintTicks(true);
		refreshSlider.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				refreshRate = refreshSlider.getValue();				
			}
		});
		GridBagConstraints gbc_refreshSlider = new GridBagConstraints();
		gbc_refreshSlider.insets = new Insets(0, 0, 0, 5);
		gbc_refreshSlider.gridx = 1;
		gbc_refreshSlider.gridy = 2;
		gbc_refreshSlider.fill = GridBagConstraints.HORIZONTAL;
		controlPanel.add(refreshSlider, gbc_refreshSlider);
		
		simulateBtn = new JButton("Start");
		simulateBtn.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(isRunning) {
					isRunning = false;
					simulateBtn.setText("Start");
					spacingSlider.setEnabled(true);
					timeSlider.setEnabled(true);
					refreshSlider.setEnabled(true);
					pauseBtn.setEnabled(false);
					if(pauseBtn.getText().equals("Resume"))
						pauseBtn.setText("Pause");
					endSimulation();
					
				}
				else {
					isRunning = true;
					simulateBtn.setText("Stop");
					spacingSlider.setEnabled(false);
					timeSlider.setEnabled(false);
					refreshSlider.setEnabled(false);
					pauseBtn.setEnabled(true);
					
					simulation = new Simulation(spacingValue, simulationTime);
					model.setRefreshRate(refreshRate);
					model.setGridSpacing(spacingValue);
					simulate();
				}
			}
		});
		GridBagConstraints gbc_simulateBtn = new GridBagConstraints();
		gbc_simulateBtn.insets = new Insets(0, 0, 5, 0);
		gbc_simulateBtn.gridx = 2;
		gbc_simulateBtn.gridy = 1;
		controlPanel.add(simulateBtn, gbc_simulateBtn);
		
		pauseBtn = new JButton("Pause");
		pauseBtn.setEnabled(false);
		pauseBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(isPaused){
					pauseHandle(true);
					pauseBtn.setText("Pause");
				}
				else {
					pauseHandle(false);
					pauseBtn.setText("Resume");
				}				
			}
		});
		
		GridBagConstraints gbc_pauseBtn = new GridBagConstraints();
		gbc_pauseBtn.insets = new Insets(2,2,5,5);
		gbc_pauseBtn.gridx = 2;
		gbc_pauseBtn.gridy = 2;
		controlPanel.add(pauseBtn, gbc_pauseBtn);
		
		Boolean initiative = null;
		if(Config.getInstance().getInitiative().equals(InitiativeEnum.PRESENTATION))
			initiative = true;
		else if(Config.getInstance().getInitiative().equals(InitiativeEnum.SIMULATION))
			initiative = false;
		
		model = new DisplayModel(400, 1000, initiative);
		view = new DisplayGrid(model, new Dimension(1000,400));
		view.setVisible(true);
		frame.getContentPane().add(view, BorderLayout.CENTER);
		
		frame.pack();
		
	}
	
	private void endSimulation() {
		Config config = Config.getInstance();
		if(config.getThreadingFlags().contains(ThreadedEnum.PRESENTATION))
			model.stop();
		if(config.getThreadingFlags().contains(ThreadedEnum.SIMULATION))
			simulation.cancel();
		
		config.getBuffer().clear();
	}
	
	private void pauseHandle(boolean resume) {
		Config config = Config.getInstance();
		if(resume){
			if(config.getThreadingFlags().contains(ThreadedEnum.PRESENTATION))
				model.resume();
			if(config.getThreadingFlags().contains(ThreadedEnum.SIMULATION))
				simulation.resume();
			
			if(!config.getThreadingFlags().equals(EnumSet.allOf(ThreadedEnum.class)))
				simulate();
			else
				isPaused = false;
		}
		else{
			isPaused = true;
			if(config.getThreadingFlags().contains(ThreadedEnum.PRESENTATION))
				model.pause();
			if(config.getThreadingFlags().contains(ThreadedEnum.SIMULATION))
				simulation.pause();
		}	
	}
	
	private void simulate(){
		Config config = Config.getInstance();
		config.getBuffer().clear();
		boolean resuming = false;
		
		//set non initiative objects
		if(!isPaused){
			if(config.getInitiative().equals(InitiativeEnum.PRESENTATION))
				config.setNonInitativeObject(simulation);
			else if(config.getInitiative().equals(InitiativeEnum.SIMULATION))
				config.setNonInitativeObject(model);
		}
		else {
			isPaused = false;
			resuming = true;			
		}
		
		if(config.getThreadingFlags().equals(EnumSet.allOf(ThreadedEnum.class))){
			Thread sim = new Thread(simulation);
			Thread presentation = new Thread(model);
			sim.start();
			presentation.start();
		}
		else if(config.getThreadingFlags().equals(EnumSet.noneOf(ThreadedEnum.class))){					
			while(isRunning) {
				if(isPaused)
					break;
				switch(config.getInitiative()) {
					case SIMULATION:
						simulation.produce();
						break;
					case PRESENTATION:
						model.consume();
						break;
					case MASTER_CONTROL:
						simulation.produce();
						model.consume();
						break;
				}
			}
		}
		else if(config.getThreadingFlags().equals(EnumSet.of(ThreadedEnum.PRESENTATION))) {
			Thread presentation = new Thread(model);
			if(!resuming)
				presentation.start();
			if(!config.getInitiative().equals(InitiativeEnum.PRESENTATION)){
				while(isRunning){
					if(isPaused)
						break;
					simulation.produce();
					Thread.yield();
				}
			}
		}
		else if(config.getThreadingFlags().equals(EnumSet.of(ThreadedEnum.SIMULATION))) {
			Thread sim = new Thread(simulation);
			if(!resuming)
				sim.start();
			if(!config.getInitiative().equals(InitiativeEnum.SIMULATION)) {
				while(isRunning){
					if(isPaused)
						break;
					model.consume();
					Thread.yield();
				}
			}
		}
	}
}
