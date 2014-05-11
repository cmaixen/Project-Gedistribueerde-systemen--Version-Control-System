package VCS.Server;
/**
Copyright (c) 2014, Yannick Merckx, Vrije Universiteit Brussel
All rights reserved.

 */

import java.awt.List;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.io.ObjectInputStream;

import org.apache.commons.io.IOUtils;

import VCS.API.FileTransfer;
import VCS.API.WorkingDirectory;
import VCS.Events.Acknowledgement;
import VCS.Events.CheckoutEvent;
import VCS.Events.Command;
import VCS.Events.CommitEvent;
import VCS.Events.ErrorEvent;
import VCS.Events.FileEvent;
import VCS.Events.GetEvent;
import VCS.Events.NewRepositoryEvent;


/**
 * A simple echo server.
 * 
 * Once connected to a client, opens a session for that client where
 * strings are echoed back until the client closes the connection.
 * 
 * This is a multi-threaded server: it can serve requests from multiple
 * connected clients concurrently.
 * 
 * This server uses a single thread per client connection. As such,
 * it is not designed to serve thousands of users simultaneously.
 * 
 * This server must be explicitly terminated by the user.
 * 
 * Illustrates the use of TCP/IP sockets.
 */
public class ServerVCS {
	//  final variable
	private final ServerSocket serverSocket;
	private String metafile = "MetaServerCVS";
	private MetaDataServer MetaFile = null;
	private WorkingDirectory server_repository = new WorkingDirectory("./");
	private String servername = "Serverrepos";
	private String serverhomeDirectory = "./" +servername;


	/**
	 * Construct a new ServerVCS that listens on the given port.
	 */
	public ServerVCS(int port) throws IOException {
		//creates address (ip:port) for the server
		InetSocketAddress serverAddress = new InetSocketAddress(port);
		//bind unbound variable "serverSocket" to an unbound server socket
		this.serverSocket = new ServerSocket();
		//Serversocket gets bond to address
		serverSocket.bind(serverAddress);
		if (!server_repository.changeWorkingDir(servername)){
			server_repository.createDir(servername);
		}
	}

	/**
	 * Block and wait until a client arrives.
	 * 
	 * Once a client arrives, start up a new Session Thread
	 * that handles further communication with this client,
	 * and returns immediately.
	 * 
	 * @throws IOException when unable to listen on the specified port.
	 */
	public void acceptClient() throws IOException {
		//gives the client a socket
		Socket clientSocket = serverSocket.accept(); // blocks
		//Creates random number
		int id =  new Random().nextInt();
		Session session = new Session(clientSocket, id);
		System.out.println("Server: client connected");
		// starts a new thread
		session.start();
		// return immediately
	}







	private class Session extends Thread {

		private final Socket clientSocket;
		private int id;
		private ObjectOutputStream outputStream = null;
		private ObjectInputStream inputStream = null;

		public Session(Socket clientSocket, int id) {
			this.id = id;
			this.clientSocket = clientSocket;
		}

		/**
		 * Handles communication with a single client.
		 * 
		 * Listen for client requests and send back echoed replies.
		 * 
		 * Client and server sockets communicate via input and output streams,
		 * as shown schematically below:
		 * 
		 * <pre>
		 *   Client                             Server
		 *    cs = new Socket(addr,port)        ss = socket.accept()
		 *       cs.in <-------------------------- ss.out
		 *       cs.out -------------------------> ss.in
		 * </pre> 
		 */

		public void run() {
			try {
				// get raw input and output streams
				//create Objectstreams
				outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
				inputStream = new ObjectInputStream(clientSocket.getInputStream());
				while (true) {
					Command clientassignment = null;
					// read string from client
					Command clientInput = (Command) inputStream.readObject();
					String clientcommand = clientInput.getCommand();
					// log the string on the local console
					System.out.println("Server: client sent '" +
							clientcommand + "'");
					//process client input
				clientassignment = process(clientInput);
					
				//als clientassignment null is wilt dit zegge dat het een acknowledgment was en dat er niets moet worden weggeschreven
				//en moet je gewoont terug op input wachten.
				  if(clientassignment == null){
						continue;
					}
					
					System.out.println("write");
					// send back the string to the client
					outputStream.writeObject(clientassignment);
					
					// make sure the output is sent to the client before closing
					// the stream
					outputStream.flush();	
					System.out.println("done");
				}

			} catch (IOException e) {

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				// tear down communication
				System.err.println("Server: closing client connection");
				try {
					this.clientSocket.close();
				} catch (IOException e) { /* ignore */ }
			}
		}
	

	//gaat gevraagde command ,indien juist, uitvoeren en anders gaat hij de client verwittigen
	public Command process(Command input) throws IOException, ClassNotFoundException{
		String command = input.getCommand();
		
		if(command.equals("CHECKOUT")){
			return Checkout((CheckoutEvent) input); 
		}
		else if(command.equals("COMMIT")) {
			

			return Commit((CommitEvent) input);

		}
		else if(command.equals("create_repository")) {
			//create a new repository en creeer deze ook bij client
			return Create_Repository((NewRepositoryEvent) input);
		}
		else if(command.equals("FileEvent")){
			downloadFiles((FileEvent) input);
			return null;
		}
		else if(command.equals("update")) {
			//more is coming 
			return null;
		}
		else if(command.equals("status")) {
			//more is coming 
			return null;
		}
		else if(command.equals("diff")) {
			//more is coming 
			return null;
		}
		else {
			System.out.print("Server: ERROR: invalid command '" +  input + "'" );
			return new ErrorEvent("Invalid command!");
		}
	}



	public void downloadFiles(FileEvent givenfileEvent) {
		
		FileEvent fileEvent;
		boolean argument_evaluated = false;
		File dstFile = null;
		FileOutputStream fileOutputStream = null;
		boolean more = true;
try {
	String dest = givenfileEvent.getDestinationDirectory();
	//navigeer naar deze gewenste repo
	gotoRepository(dest);
	System.out.println(MetaFile);
		while (more){
			
			//Eerste argument mag niet vergeten worden daarmee voeren we deze kleine test in.
			if (argument_evaluated){
			fileEvent = (FileEvent) inputStream.readObject();
			}
			else{ fileEvent = givenfileEvent;
					argument_evaluated = true;}
			
			if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
				System.out.println("Error occurred ..with  file" + fileEvent.getFilename() + "at sending end ..");
			}
			//Filenumber
			String versionnumber = (fileEvent.getVersionNumber()).toString();
			//filename voor de gebruiker
			String filename = fileEvent.getFilename();
			//filename voor het systeem
			String realfilename = versionnumber + "_" + filename;
			//output voor op hardeschijf
			String outputFile = server_repository.getWorkingDir() + "/" + realfilename;
			dstFile = new File(outputFile);
			fileOutputStream = new FileOutputStream(dstFile);
			fileOutputStream.write(fileEvent.getFileData());
			fileOutputStream.flush();
			fileOutputStream.close();
			System.out.println("Output file : " + outputFile + " is successfully saved ");
			//laatste revisie van file toevoegen aan indexering
			MetaFile.Addfile(filename, fileEvent.getVersionNumber());
			if (fileEvent.getRemainder() == 0) {
				//dest is de gewenste repo.
				saveMetaFile(dest);
				System.out.println("All Files are copied...");
				more = false;
			}
		}
} catch (IOException e) {
	e.printStackTrace();
} catch (ClassNotFoundException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	}

	//moet nog aangepast worden
	public Command locateFiles(String name,String sourceDestination) throws IOException, ClassNotFoundException {

		int fileCount;
		//ga eerst naar de homedir
		goHome();
		//kijk nu of path/map bestaat
		if(!server_repository.changeWorkingDir(name)){
			return  new ErrorEvent("Source directory is not valid ...");

		}
		else {
			String sourceDirectory = server_repository.getWorkingDir();
			File[] files = Hide_MetaFiles();
			fileCount = files.length;
			System.out.println(fileCount);
			if (fileCount == 0) {
				return new CheckoutEvent(name, sourceDestination);
			}
			else	{
				for (int i = 0; i < fileCount; i++) {
					System.out.println("Server: Sending " + files[i].getAbsolutePath());
					String filename =  files[i].getName();
					UUID versionnumberr = MetaFile.GetUUID(filename);
					sendFile(files[i].getAbsolutePath(), fileCount - i - 1, sourceDirectory, sourceDestination, versionnumberr);
				}
				return new CheckoutEvent(name, sourceDestination);
			}

		}
	}

		


	public void sendFile(String fileName, int index, String sourceDirectory, String sourceDestination, UUID versionnumber) {
		FileEvent fileEvent = new FileEvent();
		fileEvent.setDestinationDirectory(sourceDestination);
		fileEvent.setSourceDirectory(sourceDirectory);
		File file = new File(fileName);
		fileEvent.setFilename(file.getName());
		fileEvent.setRemainder(index);
		fileEvent.setVersionnumber(versionnumber);
		DataInputStream diStream = null;
		try {
			diStream = new DataInputStream(new FileInputStream(file));
			long len = (int) file.length();
			byte[] fileBytes = new byte[(int) len];
			int read = 0;
			int numRead = 0;
			while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read,
					fileBytes.length - read)) >= 0) {
				read = read + numRead;
			}
			fileEvent.setFileData(fileBytes);
			fileEvent.setStatus("Success");
		} catch (Exception e) {
			e.printStackTrace();
			fileEvent.setStatus("Error");

		}

		try {
			outputStream.writeObject(fileEvent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadMetaFile() throws FileNotFoundException, IOException, ClassNotFoundException{
		try{
		FileInputStream fis = new FileInputStream(server_repository.getWorkingDir() + "/" + metafile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		MetaFile = (MetaDataServer) ois.readObject();
		ois.close();
	}catch(IOException e){
		e.printStackTrace();
	}
	}
	
	//wraps loading repo en metafile in one
	public boolean gotoRepository(String reponame) throws IOException, ClassNotFoundException{
		Boolean succes = server_repository.goToWorkingDir(serverhomeDirectory + "/" + reponame);
		loadMetaFile();
		return succes;
	}

	//shortfunction to go back to homefolder
	public boolean goHome() throws IOException, ClassNotFoundException{
		Boolean succes = server_repository.goToWorkingDir(serverhomeDirectory);
		return succes;
	}

	public void saveMetaFile(String reponame) throws ClassNotFoundException{
	
		try{
			//ga naar gewenste repository
	    server_repository.goToWorkingDir(serverhomeDirectory + "/" + reponame);
		File file = new File(server_repository.getWorkingDir() + "/" + metafile);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(MetaFile);
		oos.flush();
		oos.close();
		}
        catch (FileNotFoundException ex) {
            System.out.println("Error with specified file") ;
            ex.printStackTrace();
        }
        catch (IOException ex) {
            System.out.println("Error with I/O processes") ;
            ex.printStackTrace();
        }             
    }
	
	public File[] Hide_MetaFiles() throws IOException{
		return Hide_MetaDataServer(Hide_OSX_Files());
	}
	
	public File[] Hide_OSX_Files() throws IOException{
	
		File[] original = server_repository.listFiles();
		ArrayList<File> list= new ArrayList<File>(Arrays.asList(original));
		File file = new File(server_repository.getWorkingDir() + "/" + ".DS_Store");
		list.remove(file);
		File[] custom = list.toArray(new File[list.size()]);
		return custom;
		}
	
	public File[] Hide_MetaDataServer(File[] original) throws IOException{
		ArrayList<File> list= new ArrayList<File>(Arrays.asList(original));
		File file = new File(server_repository.getWorkingDir() + "/" + metafile);
		list.remove(file);
		File[] custom = list.toArray(new File[list.size()]);
		return custom;	
	}
	
	//Procces Commands

	//cre‘ren van een nieuwe repository
	public Command Create_Repository(NewRepositoryEvent newrepo) throws IOException, ClassNotFoundException{
		String name_repo = newrepo.getName();
		//reset to homefolder
		goHome();
		//When the creation of a new directory fails and we get the boolean false back, we assume that the directory already exists.
		if (!server_repository.createDir(name_repo)){
			System.out.println("Server: ERROR: Cannot create new repository '" + name_repo + "'It already exists!");
			return new ErrorEvent("repository already exists!");	 
		}
		else{ 
			//create MetaFile and save      
			MetaFile = new MetaDataServer();
			System.out.println(MetaFile);
			saveMetaFile(name_repo);
			System.out.println("Server: New repository '" + name_repo + "' succefully created");
			return newrepo;
		}
	}

	//Zorgt voor de checkout
	public Command Checkout(CheckoutEvent checkoutevent) throws IOException, ClassNotFoundException{
		String dest = checkoutevent.getDestination();
		String reponame = checkoutevent.getName();
		//locate files  en hier een commitEvent uit.
		return locateFiles(reponame, dest);
	
	}


	public Command Commit(CommitEvent commitevent) throws IOException, ClassNotFoundException{
		String destname = commitevent.getDestination();
		String comment = commitevent.getComment();
		System.out.println(comment);
		ArrayList<String> listwfiles = commitevent.getCommitFiles();
		//stop commit met table in committable
		//genereer CommitID
		UUID commitnr = UUID.randomUUID();
		//ga naar repo en laad metafile;
		gotoRepository(destname);
		//toevoegen van commit aan Metafile
		MetaFile.AddCommit(commitnr,commitevent);
		//opslagen van Metafile
		saveMetaFile(destname);
		return new CommitEvent(comment,destname,listwfiles);
	}

	}


	/**
	 * Usage: java ServerVCS port
	 * 
	 * Where port is the port on which the server should listen
	 * for requests.
	 * 
	 * Example:
	 *   java ServerVCS 6789
	 *   
	 * @throws IOException when unable to setup connection or communicate
	 *         with the client. 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: java ServerVCS port");
			return;
		}
		int port = Integer.parseInt(args[0]);

		System.out.println("Server: waiting for clients on port "+port);
		ServerVCS server = new ServerVCS(port);

		while (true) {
			server.acceptClient();
		}

		// ServerSockets are automatically closed for us by OS
		// when the program exits
	}

}
	
