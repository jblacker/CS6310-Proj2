package core;

import java.util.EnumSet;
import java.util.concurrent.ArrayBlockingQueue;

import core.Constants.BUFFERING_PARAMETERS;
import core.Constants.CONCURRENCY_PARAMETERS;
import core.Constants.INITIATIVE_PARAMETERS;

public class Config {
	
	private static Config instance = null;
	
	public static void buildGlobalConfig(String[] args){
		if(instance != null)
			throw new IllegalStateException("This method cannot be called more than once globally");
		
		int bufferSize = 0;
		InitiativeEnum initiative = null;
		EnumSet<ThreadedEnum> threading = EnumSet.noneOf(ThreadedEnum.class);
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("-s")) {
				if(threading.add(ThreadedEnum.SIMULATION))
					continue;
				else
					throw new IllegalArgumentException("-s flag cannot be used twice");
			}
			else if(args[i].equals("-p")) {
				if(threading.add(ThreadedEnum.PRESENTATION))
					continue;
				else
					throw new IllegalArgumentException("-p flag cannot be used twice");
			}
			else if(args[i].equals("-r")) {
				if(initiative == null)
					initiative = InitiativeEnum.PRESENTATION;
				else
					throw new IllegalArgumentException("-r & -s flags are mutually exclusive");
			}
			else if(args[i].equals("-s")) {
				if(initiative == null)
					initiative = InitiativeEnum.SIMULATION;
				else
					throw new IllegalArgumentException("-r & -s flags are mutually exclusive");
			}
			else if(args[i].equals("-b")) {
				if(bufferSize > 0)
					throw new IllegalArgumentException("-b flag cannot be used twice");
				else {
					try {
						bufferSize = Integer.parseInt(args[++i]);
						if(bufferSize < 1)
							throw new IllegalArgumentException("Buffer size must be greater than 0");
					}
					catch(NumberFormatException ex) {
						throw new IllegalArgumentException("A number greater than 0 must follow the -b flag", ex);
					}
				}
			}
		}
		
		instance = new Config(bufferSize, threading, initiative);
	}

	public static synchronized Config getInstance() {
		if(instance == null)
			throw new IllegalStateException("buildGlobalConfiguration must be called before this method can be used");
		
		return instance;
	}

	private final ArrayBlockingQueue<SimulationState> buffer;
	private final EnumSet<ThreadedEnum> threadingFlags;
	private final InitiativeEnum initiative;
	
	private boolean initativeFlag;
	
	private Config(int bufferSize, EnumSet<ThreadedEnum> threading, InitiativeEnum initiative) {
		if(bufferSize == 0)
			bufferSize = 1;
		
		this.buffer = new ArrayBlockingQueue<SimulationState>(bufferSize);
		this.threadingFlags = threading;
		if(initiative == null)
			this.initiative = InitiativeEnum.MASTER_CONTROL;
		else
			this.initiative = initiative;
	}

	public synchronized ArrayBlockingQueue<SimulationState> getBuffer() {
		return buffer;
	}

	public synchronized EnumSet<ThreadedEnum> getThreadingFlags() {
		return threadingFlags;
	}

	public synchronized InitiativeEnum getInitiative() {
		return initiative;
	}
	
	public synchronized boolean requested() {
		return initativeFlag;
	}
	
	public synchronized void completed() {
		if(initativeFlag)
			initativeFlag = false;
	}
	
	public synchronized void request() {
		if(!initativeFlag)
			initativeFlag = true;
	}

}
