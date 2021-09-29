import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Schedule {

	public static BlockingQueue<Task> schedule;
	public static Integer index = 0;
	private static Lock lock = new ReentrantLock(true);
	
    /** 
     * Stock Trades Schedule 
     * Keep the track of tasks
    */
    public Schedule() {
        schedule = new LinkedBlockingQueue<Task>();
    }
    
    public synchronized static Task get() {
    	
    	Task ret = new Task(0,null,0);
    	
			try {
				ret = schedule.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
    	
    	return ret;
    	
    }
    
    public static Task peek() {
    	synchronized(schedule) {
    		return schedule.peek();
    	}
    }
    public void increment() {
    	lock.lock();
    	try {
    		index++;
    		
    	} catch(Exception e) {
    		
    	} finally {
    		lock.unlock();
    	}
    	
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
