package VCS.Events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class GetCommitsEvent extends Command implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<UUID> CommitTable = null;
	private ArrayList<CommitEvent> CommitEventTable = null;
	
	public ArrayList<CommitEvent> getCommitEventTable() {
		return CommitEventTable;
	}

	private String Repo;
	public GetCommitsEvent(String repo){
	Repo = repo;	
	Command ="LOGS";
}
	
	public void SetCommitTable(ArrayList<UUID> committable ,ArrayList<CommitEvent> commiteventtable){
		CommitTable = committable;
		CommitEventTable = commiteventtable;
	}
	
	
	public ArrayList<UUID> GetCommitTable(){
		return CommitTable;
	}
	public ArrayList<CommitEvent> GetCommitEventTable(){
		return CommitEventTable;
	}
	
	public String GetRepository(){
		return Repo;
	}
	
}
