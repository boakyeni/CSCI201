import java.io.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;

public class Main {
	private static String source;
	public static String getSource() {
		return source;
	}
	/**
	 * Uses GSON to read the file inputed by the user
	 */
	private static WristCuff temp = new WristCuff();
	private void readFile() {
		
		System.out.print("Please provide timefall shelter data souce: ");
		Scanner sc = new Scanner(System.in);
		source = sc.nextLine().trim(); 
		Gson gs = new GsonBuilder()
				.excludeFieldsWithoutExposeAnnotation()
				.create();
		JsonReader reader;
		// from baeldung.com regex for internatonal number with prefix
		String phoneNumber = "^\s*(\\+\\d{1,3}( )?)?\s*((\\(\\d{3}\\)))[- .]*\\d{3}[-][- .]*\\d{4}\s*$";
		String guidNumber = "^\s*[0-9a-fA-F]+[-][0-9a-fA-F]+[-][0-9a-fA-F]+[-][0-9a-fA-F]+[-][0-9a-fA-F]+\s*$";
		try {
			reader = new JsonReader(new FileReader(source));
			reader.setLenient(true);
			WristCuff.shelters = gs.fromJson(reader, 
					new TypeToken<List<TimefallShelter>>() {}.getType());
			//Validating user input
			for(TimefallShelter shelter: WristCuff.shelters) {
				if(shelter.getGuid().length() == 0 || shelter.getPhone().length() == 0
						|| shelter.getName().length() == 0) {
					throw new NullPointerException();
				}
				if(!shelter.getPhone().matches(phoneNumber)) {
					throw new IllegalArgumentException();
				}
				if(!shelter.getGuid().matches(guidNumber)) {
					throw new IllegalArgumentException();
				}
				
			}
			System.out.println("=== Data accepted ===\n");
		}catch (FileNotFoundException e) {
			System.out.println("The file " + source + " could not be found.\n");
			this.readFile();
		} catch (JsonParseException ex) {
			System.out.println("The file " + source + " could not be parsed.\n");
			this.readFile();
		} catch(NullPointerException ex) {
			System.out.println("The file " + source + " has empty fields.\n");
			this.readFile();
		} catch(IllegalArgumentException ex) {
			System.out.println("The file " + source + " has not accepted fields.\n");
			this.readFile();
		}
		
		
	}


	/**
	 * Gets the supported chiral frequencies from the user
	 */
	Set<Integer> frequencies = new HashSet<Integer>();
	ArrayList<Integer> freqArray;
	private void setSupportedFrequencies() {
		System.out.print("Please provide supported Chiral frequencies: ");
		Scanner sc = new Scanner(System.in);
		String data = sc.nextLine();
		//convert data to array of strings holding freqs
		String[] tempData = data.split(",");
		
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		/*
		 * No frequencies is still valid, so leave numbers empty if 
		 * no frequencies entered
		 */
		if(!data.trim().isEmpty()) {
			//change each element into int and place in set
			try {
				for(int i = 0; i < tempData.length; ++i) {
					numbers.add(Integer.parseInt(tempData[i]));
				}
			} catch (NumberFormatException e) {
				System.out.println("Please only integers separated by comma for Chiral frequencies.");
				setSupportedFrequencies();
			}
		}
		
		frequencies.addAll(numbers);
		//just cause findShelter takes arrayList, but listshelter takes set
		freqArray = new ArrayList<Integer>(frequencies);
	}
	
	
	


	/**
	 * Prints the option menu
	 */
	private void displayOptions() {
		System.out.println(
				"\n\t1) List all available shelters within the min and max of supported Chiral frequencies\n"
				+ "\t2) Search for a shelter by Chiral frequency\n"
				+ "\t3) Search for a shelter by name\n"
				+ "\t4) Sort shelters by Chiral frequency\n"
				+ "\t5) Jump to a shelter with the lowest supported Chiral frequency"
		);
	}
	/*
	 * Function keeps asking user to choose option until 5 is chosen
	 */
	private void chooseOption() {
		System.out.print("Choose an option: ");
		Scanner sc = new Scanner(System.in);
		String option = sc.nextLine();
		switch(Integer.parseInt(option)) {
			case 1: temp.listAllShelters(frequencies);
					this.displayOptions();
					this.chooseOption();
					break;
			case 2: temp.chiralSearch();
					this.displayOptions();
					this.chooseOption();
					break;
			case 3: temp.nameSearch();
					this.displayOptions();
					this.chooseOption();
					break;
			case 4: try {
				temp.sortShelters();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
					this.displayOptions();
					this.chooseOption();
					break;
			case 5: TimefallShelter result = temp.findShelter(freqArray);
					if(result == null) {
						System.out.println("=== No shelter available. You are DOOMED. ===");
					}
					break;
			
				
			
		}
	}


	public static void main(String[] args) {
		Main solution = new Main();
		System.out.println("Welcome to Bridges Link.\n");
		solution.readFile();
		solution.setSupportedFrequencies();
		
		solution.displayOptions();
		solution.chooseOption();
		
	}
}
