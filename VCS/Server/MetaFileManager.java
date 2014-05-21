package VCS.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import VCS.API.WorkingDirectory;

public class MetaFileManager {
	
	String Servername;
	String Metafile;
	private WorkingDirectory Manager_repository = new WorkingDirectory("./");
	private HashMap<String,Integer> indextable = new HashMap<String, Integer>();
	private ArrayList<MetaDataServer> metafilemanager = new ArrayList<MetaDataServer>();
	int counter = 0;
	
	public MetaFileManager(String servername,String metafile){
		Servername = servername;
		Metafile = metafile;
		try {
			initialize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void initialize() throws IOException, ClassNotFoundException{
		if (!Manager_repository.changeWorkingDir(Servername)){
			Manager_repository.createDir(Servername);
		}
		
		Manager_repository.changeWorkingDir(Servername);
		
	String[] repos = Manager_repository.list();
	ArrayList<String> list =  new ArrayList<String>(Arrays.asList(repos));
	list.remove(".DS_Store");
	repos = list.toArray(new String[list.size()]);

	for(String repo : repos){
	Manager_repository.changeWorkingDir(repo);
	FileInputStream fis = new FileInputStream(Manager_repository.getWorkingDir() + "/" + Metafile);
	ObjectInputStream ois = new ObjectInputStream(fis);
	MetaDataServer Metaobject = (MetaDataServer) ois.readObject();
	ois.close();
	indextable.put(repo,counter);
	metafilemanager.add(Metaobject);
	counter++;
}
	}
	
	public MetaDataServer GetMetafile(String repo){
		int index =indextable.get(repo);
		return metafilemanager.get(index);
	}
	
	public void UpdateMetafile(String repo,MetaDataServer metafile){
		int index =indextable.get(repo);
		metafilemanager.add(index, metafile);
}
	public void AddMetafile(String repo,MetaDataServer metafile){
		indextable.put(repo, counter);
		metafilemanager.add(counter, metafile);
		counter++;
		
	}
}
