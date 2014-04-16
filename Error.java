import java.io.Serializable;


public class Error extends Command implements Serializable {

	String Notification;
	
	public Error(String notification){
		Command = "ERROR";
	    Notification = notification;
	}
}
