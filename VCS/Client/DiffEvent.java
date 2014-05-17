package VCS.Client;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.UUID;

import difflib.Patch;
import VCS.Events.Command;

public class DiffEvent extends Command implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String filename;
	String Original_file;
	String Revised_file;
	String Repository;
	LinkedList<String> OriginalResult;


	LinkedList<String> RevisedResult;


	public DiffEvent(String givenfile, String original_file , String revised_file, String repo){
		filename = givenfile;
		Original_file = original_file;
		Revised_file = revised_file;
		Command = "DIFF";
		Repository = repo;
		
	}
	public String getFilename() {
		return filename;
	}
	public String getOriginal_file() {
		return Original_file;
	}
	public String getRevised_file() {
		return Revised_file;
	}
	
	public String getRepository() {
		return Repository;
	}
	public LinkedList<String> getOriginalResult() {
		return OriginalResult;
	}
	public void setOriginalResult(LinkedList<String> originalResult) {
		OriginalResult = originalResult;
	}
	public LinkedList<String> getRevisedResult() {
		return RevisedResult;
	}
	public void setRevisedResult(LinkedList<String> revisedResult) {
		RevisedResult = revisedResult;
	}
}
