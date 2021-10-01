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
	private static Map<String, List<Stock>> stocksData;
	private static ArrayList<ArrayList<Schedule.Task>> uniqueTasks;
	private static HashMap<Schedule.Task,Semaphore> semaphoreList;
	private static HashMap<Schedule.Task,Integer> brokerList;
	private static Schedule schedule;
	private static ArrayList<Schedule> schedList;
	private static Integer threadPool = 0;
     /**
      * Read Stock Json File inputed by user using GSON
      */
    private static void readStockFile() {
    	System.out.println("What is the name of the file containing the company information?");
		Scanner sc = new Scanner(System.in);
		companyFile = sc.nextLine().trim().toLowerCase(); 
		Gson gs = new GsonBuilder()
				.create();
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(companyFile));
			reader.setLenient(true);
			stocks = gs.fromJson(reader,
					new TypeToken<List<Stock>>() {}.getType());
			//checks for empty
			for(Stock st: stocks) {
				if(st.getTicker().length() == 0 || st.getStockBrokers() == 0
						|| st.getName().length() == 0) {
					throw new NullPointerException();
				}
			}
		}catch (FileNotFoundException e) {
			System.out.println("The file " + companyFile + " could not be found.\n");
			readStockFile();
		} catch (JsonParseException ex) {
			objectReader(gs);
		} catch(NullPointerException ex) {
			System.out.println("The file " + companyFile + " has empty fields.\n");
			readStockFile();
		} catch(IllegalArgumentException ex) {
			System.out.println("The file " + companyFile + " has not accepted fields.\n");
			readStockFile();
		} 
		/*
		 * Thread Pool size is number of total brokers	
		 */
		for(Stock stock: stocks) {
			threadPool+=stock.getStockBrokers();
		}
    }
    /*
     * Checks if json is json object with array instead of array
     */
    private static void objectReader(Gson gs) {
    	try {
    		JsonReader reader = new JsonReader(new FileReader(companyFile));
    		stocksData = gs.fromJson(reader, new TypeToken<Map<String, List<Stock>>>() {}.getType());
    		for(List<Stock> stocklist: stocksData.values()) {
    			stocks.addAll(stocklist);
    		}
    		
    	} catch (FileNotFoundException e) {
			System.out.println("The file " + companyFile + " could not be found.\n");
			readStockFile();
    	} catch (JsonParseException ex) {
			System.out.println("The file " + companyFile + " could not be parsed2.\n");
			readStockFile();
		} catch(NullPointerException ex) {
			System.out.println("The file " + companyFile + " has empty fields2.\n");
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
		scheduleFile = sc.nextLine().trim().toLowerCase(); 
		
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
						trade.get(1).trim(),
						Integer.parseInt(trade.get(2)))
						);
				
			} catch (NumberFormatException e) {
				System.out.println("The file " + scheduleFile + " has not accepted fields.\n");
	    		readScheduleFile();
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
				if(stock.getTicker().trim().equalsIgnoreCase(task.getTicker().trim())) {
					indSched.add(task);
				}	
			}
			
			uniqueTasks.add(indSched);
		}
		/*
		 * check to see if tasks are in companylist
		 */
		
		for(Schedule.Task task: schedule.schedule) {
			Boolean inList = false;
			for(Stock stock: stocks) {
				if(task.getTicker().trim().equalsIgnoreCase(stock.getTicker().trim())) {
					inList = true;
				}
			}
			try {
			if(inList == false) {
				throw new IllegalArgumentException();
			}
			} catch(IllegalArgumentException e) {
				System.out.println("Company from schedule not in json");
			}
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
    	
    	ScheduledExecutorService ex = Executors.newScheduledThreadPool(threadPool);
    	
    	List<Callable<Object>> callableTasks = new ArrayList<>();
    	for(Map.Entry<Schedule.Task, Semaphore> entry: semaphoreList.entrySet()) {
    		for(int i = 0; i < stocks.size(); i++ ) {
    			if(uniqueTasks.get(i).size() > 0) {
    				if(uniqueTasks.get(i).get(0).getTicker().equalsIgnoreCase(entry.getKey().getTicker())) {
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
    	 ex.shutdown();
    	 if(ex.isShutdown()) {
 			System.out.println("All trades completed!");
 		}

    }
    /*
     * Finds stock in data by ticker
     */
    private static Stock find(String ticker) {
    	Stock returnStock = new Stock();
    	for(Stock stock: stocks) {
    		if(stock.getTicker().trim().equalsIgnoreCase(ticker.trim())){
    			returnStock = stock;
    		}
    	}
    	return returnStock;
    }
    /*
     * Parses individual lines in csv file
     */
    private static List<String> getRecordFromLine(String line) throws IllegalArgumentException{
    	List<String> trades = new ArrayList<String>();
    	try {
    		Scanner rs = new Scanner(line);
    		rs.useDelimiter(",");
    		while(rs.hasNext()) {
    			trades.add(rs.next().trim());
    			//System.out.println(trades);
    		}
    		
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
    		System.out.println("The file " + scheduleFile + " has not accepted fields.\n");
    		readScheduleFile();
    	}catch(IllegalArgumentException e){
    		System.out.println("The file " + scheduleFile + " has not accepted fields.\n");
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
