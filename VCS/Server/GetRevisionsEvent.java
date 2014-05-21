package VCS.Server;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import VCS.Events.Command;

public class GetRevisionsEvent extends Command implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String File;
	private String Repo;
	private ArrayList<Timestamp> Revisionlist_time;

	private ArrayList<UUID> Revisionlist;
	
	public ArrayList<Timestamp> getRevisionlist_time() {
		return Revisionlist_time;
	}

	public ArrayList<UUID> getRevisionlist() {
		return Revisionlist;
	}
	public void setRevisionlist_time(ArrayList<Timestamp> revisionlist_time) {
		Revisionlist_time = revisionlist_time;
	}
	


	public void setRevisionlists(ArrayList<UUID> revisionlist2 ,ArrayList<Timestamp> revisionlist_time) {
		Revisionlist = revisionlist2;
		Revisionlist_time = revisionlist_time;
	}

	public GetRevisionsEvent (String repo, String file){
	Command = "GETREVISIONS";
	Repo = repo;
	File = file;
	}
	
	public String GetRepository(){
		return Repo;
	}
	
	public String GetFilename(){
		return File;
	}
	

	
	

}
