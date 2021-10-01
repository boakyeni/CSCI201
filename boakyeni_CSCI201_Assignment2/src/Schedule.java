import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Schedule {

	public BlockingQueue<Task> schedule;
	public static Integer index = 0;
	
    /** 
     * Stock Trades Schedule 
     * Keep the track of tasks
    */
    public Schedule() {
        schedule = new LinkedBlockingQueue<Task>();
    }
    public Schedule(ArrayList<Task> sched) {
    	schedule = new LinkedBlockingQueue<Task>();
    	for(Task task: sched) {
    		try {
				schedule.put(task);
			} catch (InterruptedException e) {
				
			}
    	}
    }
    
    public synchronized Task get() {
    	
    	Task ret = new Task(0,null,0);
			try {
				ret = schedule.take();
			} catch (InterruptedException e) {
				
			}
    	return ret;	
    }
    
    public Task peek() {
    	return schedule.peek();
    }
    


    /**
     * Inner class to store task object
     */

    public static class Task {
    	private Integer startTime;
    	private String ticker = "";
    	private Integer numOfStock;
    	
    	public Integer getNumOfStock() {return this.numOfStock;}
    	public String getTicker() {return this.ticker;    	}
    	public Integer getStartTime() {return this.startTime; }
    	
        public Task(Integer startTime, String ticker, Integer numOfStock ) {
        	this.startTime = startTime;
        	this.ticker = ticker;
        	this.numOfStock = numOfStock;
        }
    }
    
}
