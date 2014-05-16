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
	


	public void setRevisionlist(ArrayList<UUID> revisionlist2) {
		Revisionlist = revisionlist2;
	}

	public GetRevisionsEvent (String repo, String file){
	Command = "GETREVISION";
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
