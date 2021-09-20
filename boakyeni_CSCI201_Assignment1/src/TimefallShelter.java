import java.io.FileNotFoundException;
import java.util.Collections;

import com.google.gson.annotations.Expose;

public class TimefallShelter implements Comparable<TimefallShelter> {
	@Expose private Integer chiralFrequency = 0;
	@Expose private Boolean timefall = true;
	@Expose private String guid = "";
	@Expose private String name = "";
	@Expose private String phone = "";
	@Expose private String address = "";

	/**
	 * Here: all the needed class members and their getters and setters
	 */

	public String getGuid() {return this.guid;}

	public String getName() {return this.name;}

	public String getPhone() {return this.phone;}

	public String getAddress() {return this.address;}
	
	public Integer getChiralFrequency() {return this.chiralFrequency;}

	public Boolean getTimefall() {return this.timefall;}
	
	public void setTimefall(boolean timefall) {this.timefall = timefall;}
	
	public void setChiralFrequency(int chiralFrequency) 
	{this.chiralFrequency = chiralFrequency;}

	public void setGuid(String guid) {this.guid = guid;}

	public void setName(String name) {this.name = name;}

	public void setPhone(String phone) {this.phone = phone;}

	public void setAddress(String address) {this.address = address;}

	
	/**
	 * overriding comparator for sorting
	 */

	@Override
	public int compareTo(TimefallShelter compShelter) {
		/* For Ascending order*/
		return getChiralFrequency() - compShelter.getChiralFrequency();
	}

	/**
	 * String representation of a shelter
	 */
	@Override
	public String toString() {
		String timefall;
		if(getTimefall() == true) { timefall = "Current";}
		else { timefall = "None";}
		String shelter = new String("Shelter information: \n"
				+ "- Chiral frequency: " + getChiralFrequency() + "\n"
				+ "- Timefall: " + timefall + "\n"
				+ "- GUID: " + getGuid() + "\n"
				+ "- Name: " + getName() + "\n"
				+ "- Phone: " + getPhone() + "\n"
				+ "- Address: " + getAddress() + "\n");
		return shelter;
		
		
		
	}
}
