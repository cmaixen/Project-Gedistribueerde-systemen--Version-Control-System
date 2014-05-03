import java.io.Serializable;

	
	public class LocalEvent extends Command implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private String Notification;
		
		public LocalEvent(String notification){
			Command = "LOCAL";
		    Notification = notification;
		}
	
		
		
	    public String getNotification(){
	    	return Notification;
	    }
	}


