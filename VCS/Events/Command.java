package VCS.Events;
import java.io.Serializable;

/**
 * A global class to know which sort of object we receive from the client
 * @author yannickmerckx
 *
 */
//sort of tag
public class Command  implements Serializable {
	
	private static final long serialVersionUID = 1L;
	protected String Command;

	
	public String getCommand(){
		return Command;
	}
}
