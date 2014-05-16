package VCS.Events;

import java.io.Serializable;

public class ConflictEvent extends Command implements Serializable{
	
private	Command Event;
private	String Message;
	
	public ConflictEvent(Command event, String message){
		Command = "CONFLICT";
		Event = event;
		Message = message;
	}
	
	public Command GetEvent(){
		return Event;
	}
	
	public String GetMessage(){
		return Message;
	}
	
	
	public String GetTypeEvent(){
		return Event.getCommand();
		
	}
}
