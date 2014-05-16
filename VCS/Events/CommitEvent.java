package VCS.Events;
import java.util.UUID;

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
	private ArrayList<UUID> old_uuidlist;
	private Timestamp timestamp;
	private UUID commitUUID;
	private boolean Force;
	//Constructors
	public CommitEvent(String comment, String destination, ArrayList<String> commitfiles, UUID uuid_commit, ArrayList<UUID> old_uuidlist, boolean force){
		Comment = comment;
		Command = "COMMIT";
		destinationDirectory = destination;
		files = commitfiles;
		commitUUID = uuid_commit;
		Force = force;
		
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
	
	public ArrayList<UUID> GetOldUUIDList(){
		return old_uuidlist;
	}

	public UUID getCommitUUID(){
		return commitUUID;
	}
	
	public boolean getForce(){
		return Force;
	}
	
	//Mutatoren
	public void Change_Destination(String Filename){
		destinationDirectory = Filename;
	}
	

}
