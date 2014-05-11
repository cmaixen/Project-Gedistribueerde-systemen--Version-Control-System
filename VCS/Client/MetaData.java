package VCS.Client;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.Serializable;


public class MetaData implements Serializable {
	
	private static final long serialVersionUID = 2L;
	
	private ArrayList<String> AddList;
	private HashMap<String,UUID> FileTable;
	
	public MetaData(){
		AddList = new ArrayList<String>();
		FileTable = new HashMap<String,UUID>();
	};
	
	public void add(String filename){
		//Als file al in lijst zit om te commmiten, moet deze niet terug worden toegevoegd
		if (!hasAlreadyFile(filename)){
		//voeg to aan lijst
		AddList.add(filename);
		//update revisienummer in hashtable
		update(filename);
		}
	}
	
	
	//toevoegen van file aan filetable met de gewenste uuid
	public void add(String filename, UUID uuid){
		FileTable.put(filename, uuid);
	}
	
	
	//geeft queue terug met daarin alle files die toegevoegd zijn aan de repository
	public ArrayList<String> ToCommit(){
		return AddList;
	}
	
	//wordt opgeroepen als commit is uitgevoerd
	public void Committed(){
		//maak queue leeg
		AddList.clear();
	}
	
	public void update(String filename){
		//Als file al bestaat wordt het revisienummer gewoon geupdate
			
		//genereer random UUID
		 UUID revisionnumber = UUID.randomUUID(); 
			 
		//update info of voegt file toe
		FileTable.put(filename, revisionnumber);
	}
	
	public HashMap<String,UUID> GetFileTable(){
		return FileTable;
	}
	
	public boolean hasAlreadyFile(String filename){
		    for (String file : AddList) {
		        if (file.equals(filename)) {
		            return true;
		        }
		    }
		    return false;
		}
}
