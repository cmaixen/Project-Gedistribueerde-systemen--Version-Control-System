package VCS.API;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 
 */

/**
 * @author Yannick Merckx
 *
 */
public class WorkingDirectory {
	
	private File currentDirectory;
	
	public WorkingDirectory(String path) {
		
		currentDirectory = new File(path);
	}
	
	/**
	 * Current directory path
	 * @return directory path
	 * @throws IOException 
	 */
	public String getWorkingDir() throws IOException {
		
		return currentDirectory.getCanonicalPath();
	}
	
	/**
	 * Change directory
	 * @param newPath new directory path
	 * @return success?
	 * @throws IOException 
	 */
	public boolean changeWorkingDir(String newPath) throws IOException {
		File newDir = new File(getWorkingDir() + "/" + newPath);
		boolean success = newDir.exists();
		if(success) currentDirectory = newDir;
		return success;
	}
	
	public boolean file_exists(String filename) throws IOException{
		File f = new File(getWorkingDir() + "/" + filename);
		return (f.exists() && !f.isDirectory());
	}
	
	public File getFile(String filename) throws IOException{
		File f = new File(getWorkingDir() + "/" + filename);
			return f; 
	}
	
	/**
	 * Content of current directory
	 * @return list of files
	 */
	public String[] list() {
		
		return currentDirectory.list();
	}
	
	public File[] listFiles(){
		return currentDirectory.listFiles();
	}
	
	/**
	 * copy content from "from" file to "to" file
	 * @param from
	 * @param to
	 * @return success?
	 * @throws FileNotFoundException 
	 */
	public boolean copyFile(String from, String to) throws FileNotFoundException {
		
		boolean success = true;
		
		FileInputStream in = new FileInputStream(from);
		FileOutputStream out = new FileOutputStream(to);
		int c;
		try {
			while ((c = in.read()) != -1) {
				out.write(c);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			success = false;
			
		}

		return success;
	}
	
	/**
	 * copy file named "filename" to path "to"
	 * @param filename
	 * @param to
	 * @return
	 * @throws IOException 
	 */
	public boolean getFile(String filename, String to) throws IOException {
		
		return copyFile(getWorkingDir() + "/" + filename, to);
	}
	
	/**
	 * copy file with path "filename" to current directory
	 * @param filename
	 * @return
	 * @throws IOException 
	 */
	public boolean putFile(String filename) throws IOException {
		
		return copyFile(filename, getWorkingDir() + "/" + new File(filename).getName());
	}
	
	/**
	 * Create new directory in current directory
	 * @return success?
	 * @throws IOException 
	 */
	public boolean createDir(String name) throws IOException {
		
		return new File(getWorkingDir() + "/" + name).mkdir();
	}
	
	
	
	/**
	 * Create file in current directory
	 * @param name the file's name
	 * @return success?
	 */
	public boolean createFile(String name) {
		
		try {
			return new File(getWorkingDir() + "/" + name).createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}
	
	public boolean goToWorkingDir(String newPath) throws IOException {
		File newDir = new File("./" + newPath);
		boolean success = newDir.exists();
		if(success) currentDirectory = newDir;
		return success;
	}
	
	//**********************************************
	//**********************************************
	// Methods for echo-client
	public FileInputStream getFileStream(String name) throws FileNotFoundException {
		
		return new FileInputStream(name);
	}
	
	public String getcurrentfolder(){
	return currentDirectory.getName();
	}

}
