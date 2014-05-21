package VCS.Server;
/**
Copyright (c) 2014, Yannick Merckx, Vrije Universiteit Brussel
All rights reserved.
 */


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import VCS.API.AESencrypt;
import VCS.API.WorkingDirectory;
import VCS.Events.CheckoutEvent;
import VCS.Events.Command;
import VCS.Events.CommitEvent;
import VCS.Events.ConflictEvent;
import VCS.Events.DiffEvent;
import VCS.Events.ErrorEvent;
import VCS.Events.FileEvent;
import VCS.Events.GetCommitsEvent;
import VCS.Events.GetRevisionsEvent;
import VCS.Events.NewRepositoryEvent;
import VCS.Events.UpdateEvent;

public class ServerVCS {
	//  final variable
	private final ServerSocket serverSocket;
	private String metafile = "MetaServerCVS";
	private String servername = "Serverrepos";
	private String serverhomeDirectory = "./" +servername;
	private WorkingDirectory Manager_repository = new WorkingDirectory("./");
	private	HashMap<String, MetaDataServer> MetaDataList = new HashMap<String,MetaDataServer>();

	/**
	 * Construct van een nieuwe ServerVCS dat luistert naar de gegeven poort.
	 * @throws ClassNotFoundException 
	 */
	public ServerVCS(int port) throws IOException, ClassNotFoundException {
		//creates address (ip:port) for the server
		InetSocketAddress serverAddress = new InetSocketAddress(port);
		//bind variable aan socket
		this.serverSocket = new ServerSocket();
		//Serversocket wordt gebonden aan adres
		serverSocket.bind(serverAddress);
		//initialisatie;
		if (!Manager_repository.changeWorkingDir(servername)){
			Manager_repository.createDir(servername);
			Manager_repository.changeWorkingDir(servername);
		}
		initialize_MetaDataList();
		
		
	}
	

	private void initialize_MetaDataList() throws IOException, ClassNotFoundException {
		//alle repos oplijsten
		String[] repos = Manager_repository.list();
		for(String repo : repos){
			Manager_repository.changeWorkingDir(repo);
			FileInputStream fis = new FileInputStream(Manager_repository.getWorkingDir() + "/" + metafile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			MetaDataServer Metaobject = (MetaDataServer) ois.readObject();
			ois.close();
			MetaDataList.put(repo, Metaobject);
		}
	}


	/**
	 * Blockt en wacht tot eerste client toekomt
	 * 
	 * Eens de client toekomt, start men een nieuwe Sessie Thread
	 * dat verdere communicatie behandeld
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
		private MetaDataServer MetaFile = null;	
		private WorkingDirectory server_repository = new WorkingDirectory("./");

		public Session(Socket clientSocket, int id) {
			this.id = id;
			this.clientSocket = clientSocket;
		}
			
		/**
		 * Behandelt communicatie met meerdere clients.
		 * 
		 * Luisterd naar een client request en stuurt een event terug
		 * 
		 * Client and server sockets communiceren via input en output streams,
		 * als getoond beneden:
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
				//ga naar server_repo
				server_repository.changeWorkingDir(servername);
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
					System.out.println(clientassignment);
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
			} catch (Exception e) {
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
	public Command process(Command input) throws Exception{
		System.out.println("het print");
		String command = input.getCommand();
		
		if(command.equalsIgnoreCase("CHECKOUT")){
			return Checkout((CheckoutEvent) input); 
		}
		else if(command.equalsIgnoreCase("COMMIT")) {
			return Commit((CommitEvent) input);

		}
		else if(command.equalsIgnoreCase("DIFF")){
			
			return Diff((DiffEvent) input);
		}
		else if (command.equalsIgnoreCase("GETREVISIONS")){
			return GetRevisions((GetRevisionsEvent) input);
		}	
		else if (command.equalsIgnoreCase("LOGS")){
			return GetCommits((GetCommitsEvent) input);	
		}
		else if(command.equalsIgnoreCase("create_repository")) {
	
			//create a new repository en creeer deze ook bij client
			return Create_Repository((NewRepositoryEvent) input);
		}
		else if(command.equalsIgnoreCase("FileEvent")){
			downloadFiles((FileEvent) input);
			return null;
		}
		else if(command.equalsIgnoreCase("UPDATE")) {
			String destrepo = ((UpdateEvent) input).GetReponame();
			//laden van metadate en ga naar die repo
			gotoRepository(destrepo);
			return Update((UpdateEvent) input);
		}
		else {
			System.out.print("Server: ERROR: invalid command '" +  input + "'" );
			return new ErrorEvent("Invalid command!");
		}
	}

	//nodig om diff vlotter te laten werken
	   private LinkedList<String> fileToLines(String path) {
		   
		  String realfilename =  path; 
		   LinkedList<String> lines = new LinkedList<String>();
	        String line = "";
	        try {
	                BufferedReader in = new BufferedReader(new FileReader(realfilename));
	                while ((line = in.readLine()) != null) {
	                	System.out.println("line printer:" + line);
	                        lines.add(line);
	                }
	        } catch (IOException e) {
	                e.printStackTrace();
	        }
	        return lines;
	}

	   //DiffEvent verwerken op de server
	private Command Diff(DiffEvent diffevent) throws IOException, ClassNotFoundException {
		
		String filename = diffevent.getFilename();
		String Repo = diffevent.getRepository();
		String original_file = diffevent.getOriginal_file();
		String revision_file = diffevent.getRevised_file();
	
		//ga naar repo
		gotoRepository(Repo);
		
		//check if file already exist
		if(!(MetaFile.CheckUUID(filename, original_file) && MetaFile.CheckUUID(filename, revision_file))){
			return new ErrorEvent("Given revisions doesn't exist.");
		}
		else{
			
        LinkedList<String> original = fileToLines(server_repository.getWorkingDir() + "/" + original_file + "_" + filename);
        LinkedList<String> revised  = fileToLines(server_repository.getWorkingDir() + "/" + revision_file + "_" + filename);
        
        diffevent.setOriginalResult(original);
        diffevent.setRevisedResult(revised);
        return diffevent;
		}
}

	//het downloaden en opslagen van een file
	public void downloadFiles(FileEvent givenfileEvent) throws Exception {
		
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
			byte[] decryptedoutput = fileEvent.getFileData();
		//byte[] decryptedoutput = AESencrypt.decrypt(fileEvent.getFileData());
			fileOutputStream.write(decryptedoutput);
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

	//Update Command verwerken op de server
	public synchronized Command Update(UpdateEvent event) throws ClassNotFoundException{
		HashMap<String, ArrayList<UUID>> hashmap = MetaFile.getFileindex();
		ArrayList<String> Files_to_update =  new ArrayList<String>();
		String reponame = event.GetReponame();
		String destinationsource = event.GetDestination();

		System.out.println("Before Update:" + hashmap );
		Set<String> set = hashmap.keySet();
		for(String entry : set){
		 		Files_to_update.add(entry);
		 	}
		locateFiles(reponame,Files_to_update,destinationsource);
		 return event;
	}
//Het voorbereiden van de files en dan vervolgens verzenden
public boolean locateFiles(String name, ArrayList<String> Files_to_locate,String sourceDestination) throws ClassNotFoundException {
		try{
			//ga naar repo
			gotoRepository(name);
			int fileCount;
			boolean result = true;
			String sourceDirectory = server_repository.getWorkingDir();
			//file array aanmaken
			ArrayList<File> filelist = new ArrayList<File>();
			for(String filename : Files_to_locate) {
				String realfilename = (MetaFile.GetUUID(filename)).toString() + "_" + filename;
				System.out.println(realfilename);
				filelist.add(server_repository.getFile(realfilename));
			}

			System.out.println(filelist);
			//file versturen
			fileCount = filelist.size();
			int counter = 0;
			for (File file : filelist) {
				System.out.println("Server: Sending " + file.getAbsolutePath());
				String filename = file.getName();
				//files staat in table met de naam voor de gebruiker dus filename ontleden
				//op server files zijn als volgt gevormd: UUID + _ + filename
				String tablename = (filename.split("_"))[1];
				UUID versionnumber = MetaFile.GetUUID(tablename);
				sendFile(file.getAbsolutePath(), fileCount - counter - 1, sourceDirectory, sourceDestination, versionnumber);
				counter++;
			}
			return result;
		} catch(IOException e){
			e.printStackTrace();
			return false;
		}
}

//versturen van een file
	public void sendFile(String fileName, int index, String sourceDirectory, String sourceDestination, UUID versionnumber) {
		FileEvent fileEvent = new FileEvent();
		fileEvent.setDestinationDirectory(sourceDestination);
		fileEvent.setSourceDirectory(sourceDirectory);
		File file = new File(fileName);
		String filename = file.getName();
		String client_filename = (filename.split("_"))[1];
		fileEvent.setFilename(client_filename);
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
			
			//encrypt Data
			//fileBytes = AESencrypt.encrypt(fileBytes);
			fileEvent.setFileData(fileBytes);
			fileEvent.setStatus("Success");
			}
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

	public synchronized void loadMetaFile()  throws FileNotFoundException, IOException, ClassNotFoundException{
		String repo = server_repository.getcurrentfolder();
		MetaFile = MetaDataList.get(repo);
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

	
	//het opslaan van een Metafile
	//syncronised 2 threads kunnen nooit deze tegelijk gebruiken
	public synchronized void saveMetaFile(String reponame) throws ClassNotFoundException{
	
		try{
			//update Metafile in hashtable;
			
			System.out.println("old metafiel: " + MetaDataList.put(reponame, MetaFile));
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
	
public ArrayList<String> Hide_MetaFiles(ArrayList<String> list){
	list.remove(metafile);
	list.remove("DS_Store");
	return list;
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
	public synchronized Command Create_Repository(NewRepositoryEvent newrepo) throws IOException, ClassNotFoundException{
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
			MetaDataList.put(name_repo, MetaFile);
			System.out.println(MetaFile);
			saveMetaFile(name_repo);
			System.out.println("Server: New repository '" + name_repo + "' succefully created");
			return new NewRepositoryEvent(name_repo);
		}
	}

	//Zorgt voor de checkout
	public Command Checkout(CheckoutEvent checkoutevent) throws IOException, ClassNotFoundException{
		String dest = checkoutevent.getDestination();
		String reponame = checkoutevent.getName();
		if(!gotoRepository(reponame)){
			return new ErrorEvent("Source directory is not valid ...");
		}
		else {

			//lijst opstellen met te transferen files zonder de metadata
		ArrayList<String> files_to_transfer = new ArrayList<String>(Arrays.asList(MetaFile.GetFilesRepository()));
		files_to_transfer = Hide_MetaFiles(files_to_transfer);
		
		System.out.println(files_to_transfer);
		//locate files  en hier een commitEvent uit.
		locateFiles(reponame, files_to_transfer,dest);
		return new CheckoutEvent(reponame,dest);	
		}			
	}

	public Command GetRevisions(GetRevisionsEvent revision) throws IOException, ClassNotFoundException{
		String repo = revision.GetRepository();
		//ga naar repo
		gotoRepository(repo);
		//vraag revisies op
		String filename = revision.GetFilename();
		ArrayList<UUID> revisionlist = MetaFile.GetRevisions(filename);
		ArrayList<Timestamp> revisionlist_time = new ArrayList<Timestamp>();


		if (revisionlist == null){
			return new ErrorEvent("File does not exist");
		}
		else {revision.setRevisionlist(revisionlist);
			for(UUID uuid : revisionlist){
				revisionlist_time.add(MetaFile.GetTimestamp(uuid));
				
				revision.setRevisionlist_time(revisionlist_time);
			}

			}
		return revision; }
	


	public Command GetCommits(GetCommitsEvent getcommitevent) throws IOException, ClassNotFoundException{
		String repo = getcommitevent.GetRepository();
		//ga naar repo
		gotoRepository(repo);
		
		HashMap<UUID,CommitEvent> CommitTable =  MetaFile.GetCommitTable();
		System.out.println("Committable" + CommitTable);
		GetCommitsEvent output =  new GetCommitsEvent(repo);
		output.SetCommitTable(CommitTable);
		int number = new Random().nextInt();
		System.out.println(number);
		output.setId(number);
		System.out.println("send Committable" + output.GetCommitTable());
		
		return output;
	}
	
	//voor een commit uit op de server
	//is syncronized dus het gebruik van dezelfde resources wordt hier verhinderd
	public synchronized Command Commit(CommitEvent commitevent) throws IOException, ClassNotFoundException{
		String destname = commitevent.getDestination();
		String comment = commitevent.getComment();
		boolean force = commitevent.getForce();
		
		ArrayList<String> listwfiles = commitevent.getCommitFiles();
		ArrayList<UUID> old_UUIDlist = commitevent.GetOldUUIDList();

		boolean commit_invalid = false;

		//als de commit niet geforceerd moet worden
		if(!force){
			
		String[] listwupdatedfiles = listwfiles.toArray(new String[listwfiles.size()]);
		int index = 0;
		// Controleerd of repo up to date was
		//indien niet, dan wordt de commit als ongeldig verklaard.
		//als de repo nog geen files had moet je kijken of er al files waren op de server indien deze er zijn wilt dit zeggen dat de repo outdated is
		System.out.println(comment);	
		System.out.println("oldlist:"  + old_UUIDlist);
	
		for(UUID uuid : old_UUIDlist){
			String filename = listwupdatedfiles[index];
			//We vragen de previous uuid op, omdat we met filevent al hebben opgeslagen en in de table hebben geupdate
			UUID previous_uuid = MetaFile.GetPreviousUUID(filename);
			System.out.println("Previous UUID :" + previous_uuid);
			if(!(previous_uuid == null || (uuid != null && uuid.equals(previous_uuid)))){
				commit_invalid = true;
			}

		}
	}
		
		//is true als het niet geforced wordt en er een outdated file wordt gecommit
		if(commit_invalid){
			remove_invalid_commit(listwfiles);
			return new ConflictEvent(commitevent, "You're repositiory wasn't up to date. There were some files who are updated by another client. \n Do you like to commit your outdated files as a new revision?(y/n)");
		}else{
		//stop commit met table in committable
		//krijg CommitID
		UUID commitnr = commitevent.getCommitUUID();
		//ga naar repo en laad metafile;
		gotoRepository(destname);
		//toevoegen van commit aan Metafile
		MetaFile.AddCommit(commitnr,commitevent);
		//opslagen van Metafile
		saveMetaFile(destname);
		return new CommitEvent(comment,destname,listwfiles, commitnr, old_UUIDlist, force);
	}

	}
	
	public void remove_invalid_commit(ArrayList<String> listcommitedfiles){
		//verwijderen van opgeslagen files
		//van iedere file de laatste uuid opzoeken
		// file opzoeken door filename op te zoeken door combinatie filename uuid
		for(String filename : listcommitedfiles){
			UUID uuid = MetaFile.GetUUID(filename);
			String realname = uuid.toString() + "_" + filename;
			File file = new File(realname);
			file.delete();
			MetaFile.RemoveVersion(filename,uuid);
		}
		
	}

	}


	/**
	 * Usage: java ServerVCS port
	 * 
	 * port is de poort waar de server naar moet luisteren
	 * 
	 * Example:
	 *   java ServerVCS 6789
	 *   
	 * @throws IOException when unable to setup connection or communicate
	 *         with the client. 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
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

		// ServerSockets zijn automatisch gesloten door de OS
		//wanneer het programma afsluit
	}

}
	
