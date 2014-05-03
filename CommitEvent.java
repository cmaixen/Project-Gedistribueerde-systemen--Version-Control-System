import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Timestamp;
public class CommitEvent extends Command {

	private static final long serialVersionUID = 1L;
	//Variables
	private String Comment;
	private String destinationDirectory;
	private ArrayList<String> files;
	private Timestamp timestamp;
	//Constructors
	public CommitEvent(String comment, String destination, ArrayList<String> commitfiles){
		Comment = comment;
		Command = "COMMIT";
		destinationDirectory = destination;
		files = commitfiles;
	     Date date = new Date();
	     timestamp = new Timestamp(date.getTime());
	}
	
	//Accesoren
	public String getComment(){
		return Comment;
	}

	public String getDestination(){
		return destinationDirectory;	
	}
	
	public ArrayList<String> getCommitFiles(){
		return files;
	}
	public Timestamp getTimestamp(){
		return timestamp;	
	}
	
	//Mutatoren
	public void Change_Destination(String Filename){
		destinationDirectory = Filename;
	}

}
