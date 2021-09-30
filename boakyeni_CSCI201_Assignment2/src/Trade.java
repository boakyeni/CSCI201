
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.*;

public class Trade extends Thread {
	
	private Schedule.Task company;
	private Semaphore sem;
	private Schedule sched;
	
	public Trade() {
		
	}

	public Trade(Schedule.Task company, Semaphore sem, Schedule sched) {
		this.company = company;
		this.sem = sem;
		this.sched = sched;
    }

	/**
	 * Trading function using locks
	 */
	public void run() {
		while(!sched.schedule.isEmpty()) {
		try {
			
				/*
				 * time between start of program and current time to know when to execute
				 * trade
				 */
				
				Integer numStocks = sched.peek().getNumOfStock();
				// purchase of selling stock
				String purSell;
				if(numStocks < 0) {
					purSell = "sale";
				} else {
					purSell = "purchase";
				}
				
				//can cast int to long without loss of info
				Long timeCast = (long)company.getStartTime();
				/*
				 * Pop company from schedule when done so that next thread can just check the
				 * top index 
				 */
				String ticker;
				
				ticker = sched.peek().getTicker();
				
				Duration currTime = Duration.between(Utility.getStartInstant(), Instant.now());
				if(company.getTicker().equals(ticker) 
						&& timeCast == currTime.toSeconds() ) {
					sem.acquire();
					System.out.println(Utility.getZeroTimestamp() + " Starting " + purSell + " of "
						+ Math.abs(numStocks) + " of " + company.getTicker());
					sched.get();
					Thread.sleep(1000);
					System.out.println(Utility.getZeroTimestamp() + " Finished " + purSell + " of "
							+ Math.abs(numStocks) + " of " + company.getTicker());
				} else {
					//if its not this threads turn let another thread see if its theirs
					//sem.release();
				}
			
		} catch(InterruptedException e) {
			
		}finally {
			sem.release();
		}
		}
		
	}
}
