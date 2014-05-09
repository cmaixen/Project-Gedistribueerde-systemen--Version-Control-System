package VCS.Events;

import java.io.Serializable;

public class GetEvent extends Command{
	private static final long serialVersionUID = 1L;
	
	private String Whattoget;
	
	public GetEvent(String command , String whattoget){
		Command = command;
		Whattoget = whattoget;
	}
	
	public String Whattoget(){
		return Whattoget;
	}
	
}

