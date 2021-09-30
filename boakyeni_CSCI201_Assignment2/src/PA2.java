import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;


public class PA2 {

	private static String companyFile;
	private static String scheduleFile;
	private static ArrayList<Stock> stocks = new ArrayList<Stock>();
	private static ArrayList<ArrayList<Schedule.Task>> uniqueTasks;
	private static HashMap<Schedule.Task,Semaphore> semaphoreList;
	private static HashMap<Schedule.Task,Integer> brokerList;
	private static ArrayList<Trade> traders;
	private static Schedule schedule;
	private static ArrayList<Schedule> schedList;
     /**
      * Read Stock Json File inputed by user using GSON
      */
    private static void readStockFile() {
    	System.out.println("What is the name of the file containing the company information?");
		Scanner sc = new Scanner(System.in);
		companyFile = sc.nextLine().trim(); 
		Gson gs = new GsonBuilder()
				.create();
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(companyFile));
			reader.setLenient(true);
			stocks = gs.fromJson(reader,
					new TypeToken<List<Stock>>() {}.getType());
		}catch (FileNotFoundException e) {
			System.out.println("The file " + companyFile + " could not be found.\n");
			readStockFile();
		} catch (JsonParseException ex) {
			objectReader();
		} catch(NullPointerException ex) {
			System.out.println("The file " + companyFile + " has empty fields.\n");
			readStockFile();
		} catch(IllegalArgumentException ex) {
			System.out.println("The file " + companyFile + " has not accepted fields.\n");
			readStockFile();
		} 
			
			System.out.println(stocks.size());
    }
    /*
     * Checks if json is json object with array instead of array
     */
    private static void objectReader() {
    	try {
    		Gson gson = new Gson();
    		JsonReader reader = new JsonReader(new FileReader(companyFile));
    		StockList stocklist = gson.fromJson(reader, StockList.class);
    		stocks = stocklist.getStockList();
    	} catch (FileNotFoundException e) {
			System.out.println("The file " + companyFile + " could not be found.\n");
			readStockFile();
    	} catch (JsonParseException ex) {
			System.out.println("The file " + companyFile + " could not be parsed.\n");
			readStockFile();
		} catch(NullPointerException ex) {
			System.out.println("The file " + companyFile + " has empty fields.\n");
			readStockFile();
		} catch(IllegalArgumentException ex) {
			System.out.println("The file " + companyFile + " has not accepted fields.\n");
			readStockFile();
		}
    }
   
    /**
     * Read Stock Trades CSV File inputed by user
     */
    private static void readScheduleFile() {
    	/*
    	 * Add a check to make sure that inputed trades are part of companies
    	 * in json
    	 */
    	System.out.println("What is the name of the file containing the schedule information?");
		Scanner sc = new Scanner(System.in);
		scheduleFile = sc.nextLine().trim(); 
		
		List<List<String>> tradeList = new ArrayList<>();
		try {
			Scanner scanner;
			scanner = new Scanner(new File(scheduleFile));
			while(scanner.hasNextLine()) {
				//Checks input as well
				tradeList.add(getRecordFromLine(scanner.nextLine()));
			}
		}catch (FileNotFoundException e) {
			System.out.println("The file " + scheduleFile + " could not be found.\n");
			readScheduleFile();
		} catch (JsonParseException ex) {
			System.out.println("The file " + scheduleFile + " could not be parsed.\n");
			readScheduleFile();
		} catch(NullPointerException ex) {
			System.out.println("The file " + scheduleFile + " has empty fields.\n");
			readScheduleFile();
		}catch(IllegalArgumentException ex) {
			System.out.println("The file " + scheduleFile + " has not accepted fields1.\n");
			readScheduleFile();
		}
		schedule = new Schedule();
		//called in constructor
		//schedule.schedule = new LinkedBlockingQueue<Schedule.Task>();
		for(List<String> trade: tradeList) {
			//already checked for exception for parseInt
			try {
				schedule.schedule.put(
						new Schedule.Task(Integer.parseInt(trade.get(0).replaceAll("[^0-9]", "")),
						trade.get(1),
						Integer.parseInt(trade.get(2)))
						);
				
			} catch (NumberFormatException e) {
				
				
			} catch (InterruptedException e) {
				
				
			}
		}
		
		/*
		 * Organize tasks into arrays per company
		 */
		uniqueTasks = new ArrayList<>();
		ArrayList<Schedule.Task> indSched;
		for(Stock stock: stocks) {
			indSched = new ArrayList<>();
			for(Schedule.Task task: schedule.schedule) {
				if(stock.getTicker().equalsIgnoreCase(task.getTicker())) {
					indSched.add(task);
				}
			}
			uniqueTasks.add(indSched);
		}
		schedList = new ArrayList<>();
		for(ArrayList<Schedule.Task> ind: uniqueTasks) {
			schedList.add(new Schedule(ind));
		}
		
		sc.close();
    }

    /**
     *Set up Semaphore for Stock Brokers
     */

    private static void initializeSemaphor() {
    	semaphoreList = new HashMap<Schedule.Task, Semaphore>();
    	brokerList = new HashMap<Schedule.Task, Integer>();
    	/*
    	 * Put into map so that each task has its traders/threads in
    	 * its semaphore and execution can be easier
    	 */
    	for(Schedule.Task task: schedule.schedule) {
    		int brokers = (find(task.getTicker()).getStockBrokers());
    		brokerList.put(task, brokers);
    		Semaphore sem = new Semaphore(brokers);
    		semaphoreList.put(task, sem);
    	}
    }
    
    /*
     * starts the threads, work is contained within thread
     */
    private static void executeTrades() throws InterruptedException {
    	traders = new ArrayList<Trade>();
    	ScheduledExecutorService ex = Executors.newScheduledThreadPool(10);
    	
    	
    	/*for(Schedule.Task entry: Schedule.schedule) {
    		
    		Semaphore currSemaphore = semaphoreList.get(entry);
    		//brokerList holds number of threads/traders to make
    		Integer threadCount = brokerList.get(entry);
    		ex = Executors.newScheduledThreadPool(threadCount);
    		//for(int i =0; i < Schedule.schedule.size(); i++) {
    		ex.schedule(new Trade(entry, currSemaphore), 0, TimeUnit.SECONDS);
    		    		
    		//}
    		
    		/*for(int i = 0; i < threadCount; i++) {
    			//traders for a specific company will be indistinguishable
    			traders.add(new Trade(currTask, currSemaphore));
    		}
    		
    		
    	}
    	*/
    	List<Callable<Object>> callableTasks = new ArrayList<>();
    	for(Map.Entry<Schedule.Task, Semaphore> entry: semaphoreList.entrySet()) {
    		for(int i = 0; i < stocks.size(); i++ ) {
    			if(uniqueTasks.get(i).size() > 0) {
    			if(uniqueTasks.get(i).get(0).getTicker().equals(entry.getKey().getTicker())) {
    				//give trader their unique schedule for company
    				//order of schedList and uniqueTask should correspond
    				//this way all traders for the same company operate on same list
    				callableTasks.add(Executors.callable(new Trade(entry.getKey(), entry.getValue(), schedList.get(i))));
    			}else {
    				continue;
    			}
    			}
    		}
    	}
    	try {
    		ex.invokeAll(callableTasks);
    		
    	} catch(Exception e) {
    		
    	}
    	/*int val = entry.getKey().getStartTime();
		ex.schedule(new Trade(entry.getKey(), entry.getValue()),val-1,TimeUnit.SECONDS);*/
    	 ex.shutdown();
    	
    	 if(ex.isShutdown()) {
 			System.out.println("All trades completed!");
 		}
    	
    	
    	
    	
    	
		
    	
    }
    
    private static Stock find(String ticker) {
    	Stock returnStock = new Stock();
    	for(Stock stock: stocks) {
    		if(stock.getTicker().equalsIgnoreCase(ticker)) {
    			returnStock = stock;
    		}
    	}
    	return returnStock;
    }
    
    private static List<String> getRecordFromLine(String line) throws IllegalArgumentException{
    	List<String> trades = new ArrayList<String>();
    	try {
    		Scanner rs = new Scanner(line);
    		rs.useDelimiter(",");
    		while(rs.hasNext()) {
    			trades.add(rs.next());
    			//System.out.println(trades);
    		}
    		/*
    		if(trades.size() < 3) {
    			throw new IllegalArgumentException();
    		}*/
    		
    		//make sure time and number of stocks are integers
    		String timestart = trades.get(0).replaceAll("[^0-9]", "");
    		String numstock = trades.get(2).trim();
    		Integer.parseInt(timestart);
    		Integer.parseInt(numstock);
    		//make sure there is a ticker
    		if(trades.get(1).trim().length()==0) {
    			throw new IllegalArgumentException();
    		}
    	}catch(NumberFormatException ex){
    		System.out.println("The file " + scheduleFile + " has not accepted fields2.\n");
    		ex.printStackTrace();
    		readScheduleFile();
    	}catch(IllegalArgumentException e){
    		System.out.println("The file " + scheduleFile + " has not accepted fields3.\n");
    		readScheduleFile();
    	}finally {
    		
    	}
    		
    		
    	
    	return trades;
    }

    public static void main(String[] args) throws InterruptedException {
    	readStockFile();
    	readScheduleFile();
    	initializeSemaphor();
    	executeTrades();
    	
    }
}
