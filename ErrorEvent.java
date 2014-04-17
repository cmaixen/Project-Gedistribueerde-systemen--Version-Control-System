import java.io.Serializable;


public class ErrorEvent extends Command implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String Notification;
	
	public ErrorEvent(String notification){
		Command = "ERROR";
	    Notification = "ERROR: " + notification;
	}
	
    public String getNotification(){
    	return Notification;
    }
}
