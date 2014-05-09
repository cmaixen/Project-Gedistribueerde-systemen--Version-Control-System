package VCS.Events;

import java.io.Serializable;


public class CheckoutEvent extends Command implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//Variables
	private String Name;
	private String destinationDirectory;
	
	//Constructors
	public CheckoutEvent(String name,String destination){
		Name = name;
		Command = "CHECKOUT";
		destinationDirectory = destination;
	}
	
	//Accesoren
	public String getName(){
		return Name;
	}
	
	public String getDestination(){
		return destinationDirectory;	
	}

}
