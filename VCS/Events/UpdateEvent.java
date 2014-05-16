package VCS.Events;

import java.security.KeyStore.Entry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.Serializable;
import java.util.Set;

public class UpdateEvent extends Command implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String,UUID> Files_to_update;
	String Destsource;
	String Reponame;
	
	public UpdateEvent(HashMap<String, UUID> repofiles, String destination, String reponame){
		Command = "UPDATE";
		Files_to_update =  repofiles;
		Destsource = destination;
		Reponame = reponame;
	}

	public HashMap<String,UUID> Get_Files_To_Update(){
		return Files_to_update;
	}

	public String GetDestination(){
	return Destsource;
	}

	public String GetReponame(){
	return Reponame;
	}

}
