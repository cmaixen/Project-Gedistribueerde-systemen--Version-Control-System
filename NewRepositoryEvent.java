import java.io.Serializable;

/** 
 * Object that will be sent when the clients wants to create a new repository
   */
public class NewRepositoryEvent extends Command implements Serializable{
	
	//Contructor
	public NewRepositoryEvent(String command, String name){
		Command = command;
		Name = name;
	};
	
	private static final long serialVersionUID = -7965362049008228296L;
	
	String Name;

}
