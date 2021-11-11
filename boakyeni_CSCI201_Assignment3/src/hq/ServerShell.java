package hq;

import java.net.*;
import java.util.*;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import models.DeliveryInformation;
import models.Event;
import models.Location;
import java.io.*;

public class ServerShell {
	public static void main(String[] args) {
		readSchedule();
		readLatitude();
		readLongitude();
		createDelInfo();
		readDrivers();
		threading();
	}
	static Scanner in = new Scanner(System.in);
	static Location hqLoc;
	static AtomicInteger drivers;
	static ConcurrentMap<Integer,List<Event>> uniqueSchedule;
	static BlockingQueue<DeliveryInformation> delivery;
	
	
	public static void readSchedule() {
		String schedule = new String();
		uniqueSchedule = new ConcurrentHashMap<>();
		List<Event> temp = new ArrayList<>();
		try {
			System.out.println("What is the name of the schedule file?");
			schedule = in.nextLine().trim().toLowerCase();
			File scheduleFile = new File(schedule);
			Scanner scan = new Scanner(scheduleFile);
			while(scan.hasNextLine()) {
				temp.add(getEventFromLine(scan.nextLine()));
			}
			//organize into map of time to list of events
			for(Event e: temp) {
				uniqueSchedule.putIfAbsent(e.getTime(), new ArrayList<Event>());
				uniqueSchedule.get(e.getTime()).add(e);
			}
			
			//Send Delivery Info to Drivers
			/*
			//Each entry value is the list of orders the driver should complete
			//from that construct delivery info
			delivery = new LinkedBlockingQueue<>();
			for(Map.Entry<Integer, List<Event>> entry: uniqueSchedule.entrySet()) {
				List<String> restaurants = new ArrayList<>();
				List<String> items = new ArrayList<>();
				for(Event e: entry.getValue()) {
					items.add(e.getItemName());
					restaurants.add(e.getStartLocation());
				}
				delivery.put(new DeliveryInformation(restaurants, items, hqLoc));
				//System.out.println(delivery.size());
			}
			*/
			
		} catch(FileNotFoundException fnf) {
				System.out.println("The file " + schedule + " does not exist. ");
				readSchedule();
		} 
	}
	public static Event getEventFromLine(String line) throws IllegalArgumentException{
		Integer start = null;
		String restaurant = null;
		String item = null;
		List<String> eventData = new ArrayList<>();
		//reads in line separated by comma places in array
		try {
			Scanner rs = new Scanner(line);
			rs.useDelimiter(",");
			while(rs.hasNext()) {
				eventData.add(rs.next().trim().toLowerCase());
			}
			//Check contents of individual event
			start = Integer.parseInt(eventData.get(0));
			restaurant = eventData.get(1);
			item = eventData.get(2);
			if(eventData.get(1).length() == 0 || eventData.get(2).length() == 0) {
				throw new IllegalArgumentException();
			}
			
		} catch(NumberFormatException nfe) {
			System.out.println("The file has not accepted fields.\n");
			readSchedule();
		} catch(IllegalArgumentException ila) {
			System.out.println("The file has not accepted fields.\n");
			readSchedule();
		}
		return new Event(start, restaurant, item);
	}

		/*
		 * Prompting for coordinates of the driver
		 */
	static double latitude;
	public static void readLatitude() {
		try { 
			System.out.println("\nWhat is your latitude?");
			latitude = Double.parseDouble(in.nextLine());
		} catch (NumberFormatException nfe) {
			System.out.println("Please enter a number for this");
			readLatitude();
		}
	}
	static double longitude;
	public static void readLongitude() {
		try { 
			System.out.println("\nWhat is your longitude?");
			longitude = Double.parseDouble(in.nextLine());
		} catch (NumberFormatException nfe) {
			System.out.println("Please enter a number for this");
			readLongitude();
		}
		
		hqLoc = new Location(latitude, longitude);
		
	}
	public static void readDrivers() {
		/*
		 * Prompting for the number of drivers to be dispatched
		 */
		try { 
			System.out.println("\nHow many drivers will be in service today?");
			drivers = new AtomicInteger(Integer.parseInt(in.nextLine()));
		} catch (NumberFormatException nfe) {
			System.out.println("Please enter an integer for this");
			readDrivers();
		}
	}
	public static void createDelInfo() {
		//Each entry value is the list of orders the driver should complete
		//from that construct delivery info
		delivery = new LinkedBlockingQueue<>();
		for(Map.Entry<Integer, List<Event>> entry: uniqueSchedule.entrySet()) {
			List<String> restaurants = new ArrayList<>();
			List<String> items = new ArrayList<>();
			for(Event e: entry.getValue()) {
				items.add(e.getItemName());
				restaurants.add(e.getStartLocation());
			}
			try {
				delivery.put(new DeliveryInformation(restaurants, items, hqLoc));
			} catch (InterruptedException e1) {
				
			}
			//System.out.println(delivery.size());
		}
	}
	
	static List<Runnable> threadArray;
	public static void threading() {
		//int driversLeft = drivers.intValue();
		threadArray = new ArrayList<>();
		ServerSocket serverSocket = null;
		ExecutorService pool = Executors.newFixedThreadPool(drivers.intValue());
			try {
				//Create a server socket
				serverSocket = new ServerSocket(3456);
				
				System.out.println("Listening on port 3456. \nWaiting for drivers...");
				while(true) {
					//Listen for a new connection request
					Socket socket = serverSocket.accept();
					//drivers.decrementAndGet();
					/* drivers only have input so only creating output
					 * unique stream for each driver
					 */
					//ObjectOutputStream outputToDriver = new ObjectOutputStream(socket.getOutputStream());
					//create thread but don't start them
					threadArray.add(new DriverHandler(socket));
					InetAddress inetAddress = socket.getInetAddress();
					System.out.println("Connection from " + inetAddress.getHostAddress());
					//ObjectOutputStream outputToDriver = new ObjectOutputStream(socket.getOutputStream());
					 //Waiting for more threads
					if(drivers.intValue() != 0) {
						System.out.println("Waiting for " 
								+ drivers.intValue() + " more driver(s) ...");
						//outputToDriver.writeObject(drivers.intValue());
						
					}//starting threads
					else {
						
						System.out.println("Starting service.");
						//tell the drivers
						//outputToDriver.writeInt(driversLeft);
						/*
						 * At this point the first while loop of the Driver Shell
						 * has broken and we have entered the second loop
						 */
						for(Runnable t: threadArray) {
							pool.execute(t);
						}	
					}
					
					
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			}
			
			pool.shutdown();
			if(pool.isTerminated()) {
				System.out.println("All orders completed!");
			}	
	}
	
	public static class DriverHandler implements Runnable {
		private Socket socket; // A connected socket
		ObjectOutputStream outputToDriver = null;
		Integer driverNo;
		//Constructing a thread
		public DriverHandler(Socket socket) throws IOException {
			this.socket = socket;
			try {
				Thread.sleep(100);
				//replies from server to client 
				outputToDriver = new ObjectOutputStream(socket.getOutputStream());
				Thread.sleep(100);
				//when constructed decrease
				//tell the drivers
				/*
				 * 
				 */
				driverNo = drivers.decrementAndGet();
				outputToDriver.writeObject(driverNo);
				outputToDriver.reset();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
			}
			
			
			
		}
		
		public void run() {
			try {
				// Create data in and out streams
				//grabs system.in/out of client
				//ObjectInputStream inputFromDriver = new ObjectInputStream(socket.getInputStream());
				
				
				
				//Continuously serve the client
				//send over ints
				/*
				while(true) {
					if(drivers.intValue() == 0) {
						Thread.sleep(500);
						outputToDriver.writeObject(drivers.intValue());
						outputToDriver.reset();
						break;
					}
				}*/
				
				//send over delivery info
				/*
				 * last driver to arrive already got go ahead
				 */
				if(drivers.intValue() >= 0 && driverNo > 0 ) {
					outputToDriver.writeObject(drivers.intValue());
					outputToDriver.reset();
				}
				
				while(true) {
					
					DeliveryInformation del = delivery.poll(1, TimeUnit.SECONDS);
					outputToDriver.writeObject(del);
					outputToDriver.reset();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (InterruptedException ex) {
				
			}
		}
	}
	
	}

