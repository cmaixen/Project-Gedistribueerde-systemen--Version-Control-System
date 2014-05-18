package VCS.Server;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;	

import VCS.Events.CommitEvent;

// houd de metadata bij van iedere repo
public class MetaDataServer implements Serializable {

	//al de commits zijn opgeslagen in deze table
	//er is steed een uniek uuid verbonden met een commitevent dit commitevent bevat:
	//					*	de comment
	//					* 	timestamp	
	//					*	welke files er zijn gecommit

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;


	private HashMap<UUID,CommitEvent> CommitTable = new HashMap<UUID,CommitEvent>();


	//deze table bevat de recentste files van de table;
	private HashMap<String , ArrayList<UUID>> Fileindex = new HashMap<String,ArrayList<UUID>>();
		
	public HashMap<String, ArrayList<UUID>> getFileindex() {
		return Fileindex;
	}

	//toevoegen of veranderen van key/value pair aan Committable table	
	public void AddCommit(UUID uuid, CommitEvent given_event){
		CommitTable.put(uuid, given_event);
	}

	//toevoegen of veranderen van key/value pair aan fileUUID table
	public void Addfile(String name, UUID uuid){
		if (Fileindex.get(name) == null){
			//cree‘r nieuwe lijst voor de file
			ArrayList<UUID> newfile = new ArrayList<UUID>();
			//nieuwe versionnumber toevoegen aan lijst
			System.out.println("Before:" + newfile);
			newfile.add(uuid);
			System.out.println("After:" + newfile);
			Fileindex.put(name,newfile);
			
		}else{
		//als de file al bestaat voegen we revisie gewoon toe
		ArrayList<UUID> fileindex = Fileindex.get(name);
		System.out.println(fileindex);
		//file toevoegen aan list in table
		System.out.println("Before:" + Fileindex);
		fileindex.add(uuid);
		System.out.println("After:" + Fileindex);
	}
}


	public UUID GetUUID(String filename){
		ArrayList<UUID> fileindex = Fileindex.get(filename);
		//geef het achterste UUID terug, dat is de recentste
		System.out.println(fileindex);
		int size = fileindex.size();
		return fileindex.get(size - 1);
	}
	
	//geeft null terug als er geen previousUUID is
	//en anders gewoon het vorige uuid;
	public UUID GetPreviousUUID(String filename){
		ArrayList<UUID> fileindex = Fileindex.get(filename);
		//geef het achterste UUID terug, dat is de recentste
		System.out.println(fileindex);
		int size = fileindex.size();
		if (size == 1){
			return null;
		}
		else{
		return fileindex.get(size - 2);
		}
	}
	
	public Timestamp GetTimestamp(UUID uuid){
		CommitEvent commitevent = CommitTable.get(uuid);
		return commitevent.getTimestamp();		
	}

	public void RemoveVersion(String filename, UUID uuid){
		Fileindex.remove(uuid);
	}


//geeft verschillende revisies terug
public ArrayList<UUID> GetRevisions(String File){
	
	return Fileindex.get(File);
	
}	

//check of gegeven uuid bestaat
public boolean CheckUUID(String filename, String uuid){
	boolean succes = false;
	
	ArrayList<UUID> uuids = Fileindex.get(filename);
	
	for(UUID given_uuid : uuids){
		if((given_uuid.toString()).equals(uuid)){
			succes = true;
		}
		System.out.println(given_uuid.toString());
		System.out.println(uuid);
		
	}
		return succes;	
}
public HashMap<UUID,CommitEvent> GetCommitTable(){
	return CommitTable;
}

public String[] GetFilesRepository(){
	Set<String> set = Fileindex.keySet();
return set.toArray(new String[set.size()]);
}

}