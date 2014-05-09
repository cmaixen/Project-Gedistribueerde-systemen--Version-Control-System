package VCS.Events;

import java.io.Serializable;


public class ExitEvent extends Command implements Serializable {
	
	/**
	 * Gives the client the Notion to exit the system
	 */
	private static final long serialVersionUID = 1L;

	private String Notification =  "Client: Close connection with server...";
	
	public ExitEvent(){
		Command = "EXIT";
	}
	
	public String getNotification(){
		return Notification;
	}

}
