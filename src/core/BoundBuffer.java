package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class BoundBuffer<E> {

	private static BoundBuffer<?> boundBuffer;
	private ArrayBlockingQueue<?> buffer;
	private int capacity;
	
	private BoundBuffer() {
		buffer = new ArrayBlockingQueue<E>(1);
		capacity = 1;
	}
	
	private BoundBuffer(int capacity)
	{
		buffer = new ArrayBlockingQueue<E>(capacity, true);
		this.capacity = capacity;
	}
	
	public int getCapacity() {
		return this.capacity;
	}
	
	public void setCapcity(int capacity) {
		if(buffer.isEmpty()) {
			buffer = new ArrayBlockingQueue<E>(capacity);
			this.capacity = capacity;
		}
		else {
			int spaceNeeded = this.buffer.size();
			if(capacity < spaceNeeded)
				throw new IllegalArgumentException("Queue is populated with more items than requested capacity");
						
			Collection transferBuffer = new ArrayList<E>(spaceNeeded);
			this.buffer.drainTo(transferBuffer);
			
			buffer = new ArrayBlockingQueue<E>(capacity, true, transferBuffer);
			this.capacity = capacity;
		}
	}
}
