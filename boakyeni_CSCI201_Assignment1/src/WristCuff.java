import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


public class WristCuff {
	
	public static ArrayList<TimefallShelter> shelters;

	
    //set initial values for needed members
    public WristCuff() {
        
    }


    /**
     * List all available shelters within the min and max of supported Chiral frequencies
     */
    void listAllShelters(Set<Integer> chiralFrequencies) {
    	int min, max;
    	ArrayList<TimefallShelter> available = new ArrayList<TimefallShelter>();
    	min = Collections.min(chiralFrequencies);
    	max = Collections.max(chiralFrequencies);
    	int numResults;
    	
    	
    	
    	for(TimefallShelter shelter : shelters) {
    		int freq = shelter.getChiralFrequency();
    		/*
    		 * Checks user input frequencies to see in there is
    		 * an available shelter and if so adds to an array to
    		 * be printed
    		 */
    		if((freq <= max || freq >=min) 
    			&& shelter.getTimefall() == false) available.add(shelter);
    	}
    	numResults = available.size();
    	//prints number of results
    	System.out.println("\n" + numResults + " results \n");
    	for(TimefallShelter shelter: available) System.out.println(shelter.toString());
    }


    /**
     * Functions for:
     * Search for a shelter by Chiral frequency
     * Search for a shelter by name
     */
    public void chiralSearch() {
    	boolean found = false;
    	// keep trying until existing freq is given
    	while(found == false) {
	    	try {
	    		System.out.print("\nWhat Chiral frequency are you looking for? ");
	    		Scanner sc = new Scanner(System.in);
	    		int queryInt = Integer.parseInt(sc.nextLine());
	    		/*
	    		 * search shelter array for freq
	    		 */
	    		for(TimefallShelter shelter : shelters) {
	    			if(shelter.getChiralFrequency() == queryInt) 
	    			{ 
	    				System.out.println(shelter.toString());
	    				found = true;
	    			}
	    			else { continue; }
	    		}
	    		/*
	    		 * if freq is not found throw an exception and try again
	    		 */
	    		if(found == false) {
	    			throw new NoSuchElementException();
	    		}
	    	}
	    	catch(NoSuchElementException e) {
	    		System.out.println("\nThat Chiral frequency does not exist.");
	    	}
    	}
	    	
    }
    
    public void nameSearch() {
    	boolean found = false;
    	while(found == false) {
    		try {
    			System.out.print("\nWhat shelter's name are you looking for? ");
    			Scanner sc = new Scanner(System.in);
    			String queryName = sc.nextLine().trim().toLowerCase();
    			for(TimefallShelter shelter : shelters) {
    				String shelterName = shelter.getName().trim().toLowerCase();
    				if(queryName.equals(shelterName)) {
    					System.out.println("\nFound!\n");
    					System.out.println(shelter.toString());
	    				found = true;
    				}else {continue;}	
    			}
    			if(found == false) {
    				throw new NoSuchElementException();
    			}
    			
    		}
    		catch(NoSuchElementException e) {
    			System.out.println("\nNo such shelter...");
    		}
    	}
    	
    }
    

    /**
     * Find an available shelter with the lowest supported Chiral frequency
     */
    public TimefallShelter findShelter(ArrayList<Integer> chiralFrequencies) {
       TimefallShelter returnShelter = new TimefallShelter();
       ArrayList<TimefallShelter> removeShelter = new ArrayList<TimefallShelter>();
       returnShelter = null;
       System.out.println("\n=== Commencing timefall shelter search ===");
    	for(TimefallShelter shelter: shelters) {
    		if(chiralFrequencies.contains(shelter.getChiralFrequency()) 
    				&& shelter.getTimefall()==false) {
    			System.out.println("=== Matching timefall shelter found! ===");
    			System.out.println(shelter);
    			System.out.println("=== Commencing Chiral jump, see you in safety. ===");
    			//this should terminate loops once shelter is found
    			returnShelter = shelter;
    			break;
    			} else if (chiralFrequencies.contains(shelter.getChiralFrequency())
    					&& shelter.getTimefall()==true) {
    				System.out.println("=== Chiral frequency " + shelter.getChiralFrequency() + " unstable,"
    						+ " Chiral jump unavailable. ===");
    				//collect objects to be deleted
    				removeShelter.add(shelter);
    				System.out.println("=== Removing target shelter from the list of shelters and "
    						+ "saving updated list to disk. ===\n");
    			}
    			
    	}
    	//Prune unstable shelters
    	for(TimefallShelter rem: removeShelter) {
    		if(shelters.contains(rem)) {
    			shelters.remove(rem);
    		}
    	}
    	//write to disk
    	try {
			save();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
       	
    	return returnShelter;
       
    }



    /**
     * Sort shelters by Chiral frequency
     */
    public void sortShelters() throws FileNotFoundException {
        Collections.sort(shelters);
        System.out.println("\nShelters succesfully sorted by Chiral frequency.");
        save();
    }

    /**
     * Saves the updated list of shelters to disk
     */
    public void save() throws FileNotFoundException {
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
        	FileWriter writer = new FileWriter(Main.getSource());
			gson.toJson(shelters, writer);
			writer.flush();
			writer.close();
			
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
        
    }
}
