package VCS.Server;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
	private static final long serialVersionUID = 1L;


	private HashMap<UUID,CommitEvent> CommitTable = new HashMap<UUID,CommitEvent>();


	//deze table bevat de recentste files van de table;
	private HashMap<String , ArrayList<UUID>> Fileindex = new HashMap<String,ArrayList<UUID>>();
		
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

}
