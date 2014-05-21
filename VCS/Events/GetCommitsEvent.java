package VCS.Events;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class GetCommitsEvent extends Command implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HashMap<UUID, CommitEvent> CommitTable = null;
	
	private String Repo;
	private int id;
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public GetCommitsEvent(String repo){
	Repo = repo;	
	Command ="LOGS";
	id = 5;
}
	
	public void SetCommitTable(HashMap<UUID, CommitEvent> committable){
		CommitTable = committable;
	}
	
	
	public HashMap<UUID,CommitEvent> GetCommitTable(){
		return CommitTable;
	}
	
	public String GetRepository(){
		return Repo;
	}
	
}
