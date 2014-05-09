package VCS.Server;
import java.io.Serializable;
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


	private HashMap<UUID,CommitEvent> CommitTable =  new HashMap<UUID,CommitEvent>();


	//deze table bevat de recentste files van de table;
	private HashMap<String ,String> recentfiles = new HashMap<String,String>();

	//houd de relatie filename / UUID bij
	private HashMap<String, UUID> fileUUID =  new HashMap<String, UUID>();

	//toevoegen of veranderen van key/value pair aan Committable table	
	public void AddCommit(UUID uuid, CommitEvent given_event){
		//more to come 
	}

	//toevoegen of veranderen van key/value pair aan fileUUID table
	public void Addfile(String name, UUID uuid){
		//more to come 
	}

	//toevoegen of veranderen van key/value pair aan recentFile table
	public void AddRecentFile(String Filename,String file){

	}



}
