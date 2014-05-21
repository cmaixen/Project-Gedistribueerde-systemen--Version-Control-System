package VCS.Client;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import VCS.API.AESencrypt;
import VCS.API.ArgumentException;
import VCS.API.WorkingDirectory;
import VCS.Events.CheckoutEvent;
import VCS.Events.Command;
import VCS.Events.CommitEvent;
import VCS.Events.ConflictEvent;
import VCS.Events.DiffEvent;
import VCS.Events.ErrorEvent;
import VCS.Events.ExitEvent;
import VCS.Events.FileEvent;
import VCS.Events.GetCommitsEvent;
import VCS.Events.GetRevisionsEvent;
import VCS.Events.LocalEvent;
import VCS.Events.NewRepositoryEvent;
import VCS.Events.UpdateEvent;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * A Version Control System Client
 * Wanneer geconnecteerd met de server, wordt er een sessie
 * geopend dat elke newline-terminated string van de standaard inputstream
 * verwerkt naar een javaobject en deze stuurt naar  de server
 * bij het invoeren van een lege string beeindigd de sessie.
 * 
 * The connection is established with TCP/IP sockets
 */
public class ClientVCS{
	private WorkingDirectory client_repository = new WorkingDirectory("./") ;

	private static final String PROMPT = "> ";

	//locale plaats waar repositories staan opgeslagen
	private String clientreposfolder = "Clientrepos";
	private String metafile = "MetaVCS";
	private MetaData MetaFile = null;
	private String current_repository = null;
	String force_command = null;
	private boolean force = false;

	//output messages of the server get adjusted by applied methodes that react on original message from server
	Command serverReply;
	String serverMessage;
	private ObjectOutputStream outputStream = null;
	private ObjectInputStream inputStream = null;
	Socket socket = new Socket();

	//Constructor
	public ClientVCS() throws IOException{
		//checks if there is a location for repos, if not initialize this location
		if (!client_repository.changeWorkingDir(clientreposfolder)){
			//create folder
			client_repository.createDir(clientreposfolder);
			//ga naar die homefolder
			client_repository.goToWorkingDir(clientreposfolder);
		}
	}

	/**
	 * Connecteerd de server met een gegeven ip:port combinatie
	 * 
	 * Client en server sockets communiceren via input en output Objectstreams,
	 * zoals beneden schematisch getoond
	 * 
	 * <pre>
	 *   Client                             Server
	 *    cs = new Socket(addr,port)        ss = socket.accept()
	 *       cs.in <-------------------------- ss.out
	 *       cs.out -------------------------> ss.in
	 * </pre>
	 * 
	 * @param ip de server zijn IP address en port is de portnummer
	 * @throws UnknownHostException if the server IP could not be found
	 * @throws IOException if there is an error in setting up or communicating
	 *         with the server.
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 */
	public void connectToServer(InetAddress ip, int port)
			throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {

		InetSocketAddress serverAddress = new InetSocketAddress(ip, port);	
		socket.connect(serverAddress);
		try {

			//create Objectstreams
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());

			//Bufferreader for consoleinput
			BufferedReader consoleInput = new BufferedReader(
					new InputStreamReader(System.in));
		
		
			boolean introduction = true;
			//gebruiker keuze laten maken van repository
			String message = introduction(consoleInput,introduction);
			if (message == null){introduction = false;}
		
			//de motor van de client
			do {
				if (!introduction){

				System.out.print(PROMPT);
				message = consoleInput.readLine();
				}
				//introduction op false zetten zodanig er input wordt gelezen
				introduction = false;

				//prepares and checking message before sending it to the server
				Command commandobject;
				commandobject = prepare(message);

				//check on navigation
				if((commandobject.getCommand().equals("LOCAL"))){
					System.out.println(((LocalEvent) commandobject).getNotification());
					//skip one loop	
					continue;
				}
				//check on valid Command
				if((commandobject.getCommand()).equals("ERROR")){
					System.out.println(((ErrorEvent)(commandobject)).getNotification());
					//skip one loop
					continue;
				}

				//Check on exit
				if((commandobject.getCommand()).equals("EXIT")){
					if(MetaFile != null){
						saveMetaFile();
					}
					System.out.println(((ExitEvent)(commandobject)).getNotification());
					//exit client
					break;
				}

				// send message to the server
				outputStream.writeObject(commandobject);
				// make sure the output is sent to the server before waiting
				// for a reply
				outputStream.flush();

				System.out.println("Client: sent '" + message + "'");

				// wait for and read the reply of the server
				serverReply = (Command) inputStream.readObject();

				//process assignment server
				process(serverReply);

				//log the string on the local console
				if(serverMessage != null){
					System.out.println("Client: server replied '" +
							serverMessage + "'");
				}
			} while (true);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// tear down communication
			socket.close();
		}
	}

	//dwingt de gebruiker om eerste een repo te kiezen voordat hij andere commando's gaat gebruiken.
	//als men dit niet zou doen, zou systeem crashen.
	private String introduction(BufferedReader consoleInput, boolean introduction) throws IOException {
		ArrayList<String> repos = new ArrayList<String>(Arrays.asList(client_repository.list()));
		//remove metafiles
		repos.remove(".DS_Store");
		System.out.println("Please select Your repository: " + repos);
		System.out.println("You can also create/checkout a repository or just exit the program.");
		System.out.print(PROMPT);
		String input = consoleInput.readLine();
		boolean loop = true;
		while(loop){
			if (input.isEmpty()){
	
			}else{
			Scanner s = new Scanner(input);
			String Command = s.next();
			System.out.println(Command);
			if (Command.equalsIgnoreCase("create_repo")){
				try{
					CheckonArguments(1,input);
					loop = false;
	
				}catch(ArgumentException e){
					System.out.println(e.getMessage());
					System.out.print(PROMPT);
					input = consoleInput.readLine();
				}
				 
			}else if (Command.equalsIgnoreCase("checkout")){
				try{
					CheckonArguments(1,input);
					loop = false;
				}catch(ArgumentException e){
					System.out.println(e.getMessage());
					System.out.print(PROMPT);
					input = consoleInput.readLine();
				}
			}
			else if(!client_repository.changeWorkingDir(Command)){
			System.out.println("Please enter a valid repository");
			System.out.print(PROMPT);
			input = consoleInput.readLine();
		}
		else { loop = false;
		input = "change_repo" + " " + Command;
		}
		}
		}
		return input;
		
	}

	//Check on arguments
	public void CheckonArguments(int amount,String input) throws ArgumentException{
		Scanner s = new Scanner(input);
		//remove command;
		s.next();
		int counter = amount;
		while(counter!= 0){
			if(!s.hasNext()){
				throw new ArgumentException();
			}
			s.next();
			--counter;
		}
		if(s.hasNext()){
			throw new ArgumentException();
		}

	}

	public Command prepare(String input) throws IOException, ClassNotFoundException, InterruptedException{
		Scanner s = new Scanner(input);
		//test of de command line niet leeg is
		if (!input.isEmpty()){

			String command = s.next();
			try {
				if(command.equalsIgnoreCase("checkout")){
					CheckonArguments(1, input);
					String name = s.next();
					return Prepare_Checkout(name);

				}
				else  if(command.equalsIgnoreCase("add")) {
					CheckonArguments(1, input);
					String filename = s.next();
					while (s.hasNext()) {
						filename =  filename + " " + s.next();
					}
					//controleren of file wel bestaat
					return Prepare_Add(filename);
				}
				//moet nog nagekeken worden
				else  if(command.equalsIgnoreCase("get_revisions")) {
					CheckonArguments(1, input);
					String filename = s.next();
					return new GetRevisionsEvent(current_repository, filename);
				}
				else if (command.equalsIgnoreCase("logs")){
					return new GetCommitsEvent(current_repository);
				}
				else if(command.equalsIgnoreCase("commit")) {
					String comment = Check_if_Comment(s);
					//Files die gecommit moeten worden
					ArrayList<String> listwithcommitfiles = MetaFile.ToCommit();
					return Prepare_CommitEvent(comment,current_repository,listwithcommitfiles);
				}
				else if(command.equalsIgnoreCase("create_repo")) {
					CheckonArguments(1, input);
					String name = s.next();
					return new NewRepositoryEvent(name);
				}
				else if(command.equalsIgnoreCase("update")) {
					return Prepare_UpdateEvent();
				}
				else if(command.equalsIgnoreCase("status")) {
					return Prepare_StatusEvent();
				}
				else if(command.equalsIgnoreCase("diff")) {
					CheckonArguments(3, input);
					String filename = s.next();
					String original =  s.next();
					String revised = s.next();
					return Prepare_DiffEvent(filename, original , revised);
				}
				else if(command.equalsIgnoreCase("change_repo")){
					CheckonArguments(1, input);
					String repo = s.next();
					return Change_Repo(repo);
				}
				else if(command.equalsIgnoreCase("ls")){
					return new LocalEvent(Arrays.toString(Hide_MetaFiles_ls()));
				}
				else if(command.equalsIgnoreCase("list_repos")){
					WorkingDirectory copydir = new WorkingDirectory(clientreposfolder);
					//go back to Homefolder
					copydir.goToWorkingDir(clientreposfolder);
					//
					ArrayList<String> repos = new ArrayList<String>(Arrays.asList(copydir.list()));
					//remove metafiles
					repos.remove(".DS_Store");
					//list the repositories
					return new LocalEvent(repos.toString()); 
				}
				else if(command.equalsIgnoreCase("current_repo")){
					return new LocalEvent(current_repository);
				}
				else {//invalid command
					return new ErrorEvent("invalid command '" + input + "'");
				}
			}catch(ArgumentException e){
				return new ErrorEvent(e.getErrorMessage());
			}
		}
		//als input leeg is sluit de client af
		else {return new ExitEvent();}

	}
	
	//het verwerken van het antwoord van de server
	public void process(Command input) throws Exception{
		//reset force
		force = false;
		String command = input.getCommand(); 
		if(command.equals("CHECKOUT")){
			serverMessage = "Repository check out succefull!";
		}
		else if(command.equals("COMMIT")) {
			//Filetable Updaten
			ArrayList<String> listwithcommitfiles = ((CommitEvent) input).getCommitFiles();
			UUID uuid_commit = ((CommitEvent) input).getCommitUUID();
			for(String file : listwithcommitfiles){
				MetaFile.add(file, uuid_commit);
			}

			//ToCommitlijst leegmaken
			MetaFile.Committed();
			serverMessage = "Succesfully recieved your commit.";
		}
		else if(command.equals("create_repository")) {
			//create a new repository en creeer deze ook bij client
			NewRepositoryEvent newrepo = (NewRepositoryEvent) input;
			String name_repo = newrepo.getName();
			Create_Repository(name_repo);	 
		}
		else if(command.equals("UPDATE")) {
			serverMessage = "Update was Succesfull";	
		}
		else if(command.equals("DIFF")) {

			DiffEvent diffevent = ((DiffEvent) input);
			LinkedList<String> original = diffevent.getOriginalResult();
			LinkedList<String> revised = diffevent.getRevisedResult();

			System.out.println(original);
			System.out.println(revised);

			// Berekend diff.Je krijgt een Patch object. Patch is een container voor berekende deltas.
			Patch patch = DiffUtils.diff(original, revised);
			for (Delta delta: patch.getDeltas()) {
				System.out.println(delta);
			}
			serverMessage = null;       
		}
		else if (command.equals("GETREVISIONS")){
			GetRevisionsEvent revisionsevent = ((GetRevisionsEvent) input);
			ArrayList<UUID> revisions = revisionsevent.getRevisionlist();
			ArrayList<Timestamp> timestamps = revisionsevent.getRevisionlist_time();
			int counter = 0;
			Timestamp[] arraytimestamp = timestamps.toArray(new Timestamp[timestamps.size()]);
			//printen van revisies
			System.out.println("Revisions of " + revisionsevent.GetFilename());
			System.out.println("");
			for(UUID uuid : revisions){
				System.out.println("# " + arraytimestamp[counter] + "		" + uuid);
				counter++;
			}
			serverMessage = "Revisions succesfully found";		
		}
		else if(command.equals("CONFLICT")){
			ConflictEvent conflictevent = ((ConflictEvent) input);
			String Message = conflictevent.GetMessage();
			System.out.println(Message);
			//Bufferreader for consoleinput
			BufferedReader consoleInput = new BufferedReader(
					new InputStreamReader(System.in));
			boolean loop =  true;
			while(loop){
				System.out.print(PROMPT);
				Message = consoleInput.readLine();
				if(Message.equals("y")){
					//userinput wordt overgeslagen in volgende cyclus  
					force = true;
					//user input is geldig
					loop = false;
					//force commando dat dan zonder input moet worden verzonden.
					force_command = conflictevent.GetTypeEvent();
				}
				else if(Message.equals("n")){
					loop = false;
					serverMessage = "Conflict succesfully resolved";	
				}
				else {
					System.out.println("Invalid answer, please answer with y or n.");
				}
			}
		}

		else if(command.equals("LOGS")){
			GetCommitsEvent event = ((GetCommitsEvent) input);
			System.out.println(event.getId());
			System.out.println("committable" + event.GetCommitTable());
			Set<Entry<UUID, CommitEvent>> commitset = event.GetCommitTable().entrySet();
			System.out.println("committable" + event.GetCommitTable());
			for(Map.Entry<UUID, CommitEvent> entry : commitset){
				UUID uuid = entry.getKey();
				CommitEvent commitevent =  entry.getValue();
				Timestamp timestamp = commitevent.getTimestamp();
				ArrayList<String> commited_files = commitevent.getCommitFiles();
				String comment = commitevent.getComment();
				System.out.println("#	Commit: " + uuid);
				System.out.println("#	Date: " + timestamp);
				System.out.println("#	Commited Files: " + commited_files);
				System.out.println("#");
				System.out.println("#	" + comment);
			}

		}
		else if(command.equals("FileEvent")){
			downloadFiles((FileEvent) input);
		}
		else if(command.equals("ERROR")){
			serverMessage = ((ErrorEvent) input).getNotification();
		}
		else {
			System.out.println("invalid replycommand '" + input + "'");
		}
	}

	/**
	 * Lezen van een Fileevent en deze schrijven naar de hardeschijf
	 * @throws Exception 
	 */
	public void downloadFiles(FileEvent givenfileEvent) throws Exception {
		if (socket.isConnected()) {
			try {
				boolean argument_evaluated = false;
				FileEvent fileEvent;
				File dstFile = null;
				FileOutputStream fileOutputStream = null;
				boolean more = true;
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

					System.out.println("Destination" + fileEvent.getDestinationDirectory() );
					System.out.println(fileEvent.getFilename());
					String outputFile = fileEvent.getDestinationDirectory() + "/" + fileEvent.getFilename();

					dstFile = new File(outputFile);
					fileOutputStream = new FileOutputStream(dstFile);
					System.out.println(fileEvent.getFileData());
						byte[] decryptedoutput = fileEvent.getFileData();
				//	byte[] decryptedoutput = AESencrypt.decrypt(fileEvent.getFileData());
					System.out.println(decryptedoutput);
					fileOutputStream.write(decryptedoutput);
					fileOutputStream.flush();
					fileOutputStream.close();
					//toevoegen van file aan metafile
					MetaFile.add(fileEvent.getFilename(), fileEvent.getVersionNumber());
					System.out.println("Output file : " + outputFile + " is successfully saved ");
					if (fileEvent.getRemainder() == 0) {
						//opslagen van metafile
						saveMetaFile();
						System.out.println("Whole repository is copied...");
						more = false;
					}
				}

			}
			catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}


	//Preparation for Commands
	//Deze methodes zorgen voor de juiste info bij ieder command
	public Command Prepare_Checkout(String name) throws IOException{
		//Ga terug naar homedirectory
		client_repository.goToWorkingDir(clientreposfolder);
		//Kijk of repository al bestaat
		if(client_repository.changeWorkingDir(name)){
			return new ErrorEvent("Cannot Checkout because asked repository already exists");	
		} else {
			//maak de repository aan in de homefolder
			client_repository.createDir(name);
			//ga naar repo
			client_repository.changeWorkingDir(name);
			//zet current_repo op repository
			current_repository = name;
			//nieuwe metafile aanmaken
			MetaFile = new MetaData();
			//metafile opslagen
			saveMetaFile();
			//stuur een CheckoutEvent naar de Server
			return new CheckoutEvent(name, client_repository.getWorkingDir());
		}
	}	


	//voorbereiden van Add event
	public Command Prepare_Add(String filename) throws IOException{
		if (client_repository.file_exists(filename)){
			System.out.println(MetaFile);
			//toevoegen om te comitten
			MetaFile.add(filename);
			return new LocalEvent("File '" + filename + "' succefully added.");
		}
		else 
		{ return new ErrorEvent("File '" + filename + "' doesn't exists!"); }	 
	}


	//Vooorbereiden van CommitEvent
	public Command Prepare_CommitEvent(String comment, String destination,ArrayList<String> listwithcommitfiles) throws IOException{
		System.out.println("printing");
		System.out.println(MetaFile.GetFileTable());
		if (listwithcommitfiles.size() == 0){
			return new ErrorEvent("There is nothing to commit");
		}
		else{//opvragen wat men moet sturen naar de server
			//commit UUID
			UUID uuid_commit = UUID.randomUUID();
			ArrayList<UUID> old_UUIDlist = new ArrayList<UUID>();
System.out.println("listwitfiles:" + listwithcommitfiles);

			for(String file : listwithcommitfiles){
				
				old_UUIDlist.add(MetaFile.GetUUID(file));
			}
			System.out.println(listwithcommitfiles);

			//Files sturen naar de server
			locateFiles(current_repository, listwithcommitfiles , destination,uuid_commit);
			return new CommitEvent(comment, destination,listwithcommitfiles,uuid_commit, old_UUIDlist, force);
		}
	}


	//kijkt of er een comment is, zoja dan geeft het de comment terug en anders de lege string
	public String Check_if_Comment(Scanner s){
		String comment = "";
		if (s.hasNext("-m")){
			//-m verwijderen;
			s.next();
			//er is een comment
			while(s.hasNext()){
				comment = comment + " " + s.next();
			}
			System.out.println("Comment given :" +  comment);	
		}
		return comment;
	}


	//Voorbereiden van UpdateEvent
	public Command Prepare_UpdateEvent() throws IOException{
		//opvragen van al de files met al hun versies
		HashMap<String,UUID> repofiles = MetaFile.GetFileTable();
		//gaat het pad van de reposi
		String Destinationpad = client_repository.getWorkingDir();
		return new UpdateEvent(repofiles, Destinationpad , current_repository);
	}


	//Voorbereiden van StatusEvent
	public Command Prepare_StatusEvent(){
		//printnewline
		System.out.println("#");
		//current_repository
		System.out.println("#current_repository: " + current_repository);
		//The Files to commit
		ArrayList<String> tocommit = MetaFile.ToCommit();
		if (tocommit.isEmpty()){
			//printblanknewline
			System.out.println("#");
			System.out.println("#There are no files to commit");
		}
		else{  System.out.println("#Files to Commit:");
		for (String file : tocommit){
			System.out.println("#-->" + file);
		}
		}
		//printblanknewline
		System.out.println("#");
		System.out.println("# Tracked Files:");
		//printblanknewline
		System.out.println("#");
		HashMap<String,UUID> filetable =  MetaFile.GetFileTable();
		Set<Entry<String, UUID>> set = filetable.entrySet();
		if (set.isEmpty()){
			System.out.println("There are no files tracked!");
		}
		else {
			System.out.println("#	FILENAME			VERSIONNR¡");
			//printblanknewline
			System.out.println("#");

			for (Entry<String,UUID> entry : set){
				System.out.println("#	" + entry.getKey() +  "			" + entry.getValue());
			}
		}
		System.out.println("#");
		System.out.println("#UnTracked Files:");
		System.out.println("#");

		//print untracked files
		ArrayList<String>  UnTrackedFiles = GetUntrackedFiles(filetable.keySet());
		if(UnTrackedFiles.isEmpty()){
			System.out.println("#There are no files untracked!");
		}else{
			for(String file : UnTrackedFiles){
				System.out.println("#	" + file);
			}
		}
		return new LocalEvent("");
	}

	//Voorbereiden van DiffEvent
	public Command Prepare_DiffEvent(String filename, String original_file,String revised_file){
		return new  DiffEvent(filename,original_file, revised_file,current_repository);
	}

	//veranderen van repository
	public Command Change_Repo(String repo) throws FileNotFoundException, IOException, ClassNotFoundException{
		//in het begin is metafile null, indien niet null zit men in een repo
		if(MetaFile != null){
			//opslagen van Metafile in huidige directory
			saveMetaFile();
		}
		//ga naar de Homefolder
		client_repository.goToWorkingDir(clientreposfolder);
		//ga naar gevraagde repo
		if (!client_repository.changeWorkingDir(repo)){
			return new ErrorEvent("Given repository doesn't exist");	
		}
		else {
			loadMetaFile();
			current_repository = repo;
			return new LocalEvent("Sucessfully changed to the repository '" + repo + "'");
		}

	}

	//repository cre‘ren.
	//De verantwoordelijkheid om te kijken of de repository al bestaat ligt bij de server.
	public void Create_Repository(String name) throws IOException{
		client_repository.goToWorkingDir(clientreposfolder);
		client_repository.createDir(name);
		client_repository.changeWorkingDir(name);
		current_repository = name;
		//nieuwe metafile aanmaken
		MetaFile = new MetaData();
		//metafile opslagen
		saveMetaFile();
		System.out.println("Client: New repository '" + name + "' succefull locally created");
		serverMessage = "New repository '" + name + "' succefully created"; 
	}

	//wordt gebruikt om het schrijven naar de server voor te bereiden en dan te schrijven.
	public boolean locateFiles(String name, ArrayList<String> commitlist,String sourceDestination, UUID uuid_commit) {
		try{
			int fileCount;
			boolean result = true;
			String sourceDirectory = client_repository.getWorkingDir();
			ArrayList<File> filelist = new ArrayList<File>();
			for(String filename : commitlist) {
				filelist.add(client_repository.getFile(filename));
			}
			fileCount = filelist.size();
			int counter = 0;

			for (File file : filelist) {
				System.out.println("Client: Sending " + file.getAbsolutePath());
				sendFile(file.getAbsolutePath(), fileCount - counter - 1, sourceDirectory, sourceDestination,uuid_commit);
				counter++;
			}
			return result;
		} catch(IOException e){
			e.printStackTrace();
			return false;
		}
	}

	//het eigelijk versturen van een file
	public void sendFile(String fileName, int index, String sourceDirectory, String sourceDestination, UUID uuid_commit){
		FileEvent fileEvent = new FileEvent();
		fileEvent.setDestinationDirectory(sourceDestination);
		fileEvent.setSourceDirectory(sourceDirectory);
		File file = new File(fileName);
		fileEvent.setFilename(file.getName());
		fileEvent.setRemainder(index);
		fileEvent.setVersionnumber(uuid_commit);
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

	//voor sendFiles nuttig om overbodig info niet mee te sturen
	public File[] Hide_MetaFiles(){
		return Hide_MetaDataClient(Hide_OSX_Files());
	}

	public File[] Hide_OSX_Files(){
		File[] original = client_repository.listFiles();
		ArrayList<File> list= new ArrayList<File>(Arrays.asList(original));
		list.remove(".DS_Store");
		File[] custom = list.toArray(new File[list.size()]);
		return custom;
	}


	//wordt gebruikt voor "ls"
	public String[] Hide_MetaFiles_ls(){
		String[] original = client_repository.list();
		ArrayList<String> list= new ArrayList<String>(Arrays.asList(original));
		list.remove(".DS_Store");
		list.remove(metafile);
		String[] custom = list.toArray(new String[list.size()]);
		return custom;
	}


	public File[] Hide_MetaDataClient(File[] original){
		ArrayList<File> list= new ArrayList<File>(Arrays.asList(original));
		list.remove(metafile);
		File[] custom = list.toArray(new File[list.size()]);
		return custom;

	}

	//het laden van een metafile
	public void loadMetaFile() throws FileNotFoundException, IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream(client_repository.getWorkingDir() + "/" + metafile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		MetaFile = (MetaData) ois.readObject();
		ois.close();

	}
	//het opslaan van een metafile
	public void saveMetaFile() throws FileNotFoundException, IOException{
		FileOutputStream fos = new FileOutputStream(client_repository.getWorkingDir() + "/" + metafile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(MetaFile);
		oos.flush();
		oos.close();
	}

	//het verkrijgen van alle untrackedfiles ib een lijst
	public ArrayList<String> GetUntrackedFiles(Set<String> trackedfiles){
		ArrayList<String> untrackedfiles = new ArrayList<String>();
		String[] all_files = client_repository.list();
		for (String file : all_files){
			if(!trackedfiles.contains(file)){
				untrackedfiles.add(file);
			}
		}
		//verwijder metafile van untrackedfiles
		untrackedfiles.remove(metafile);
		untrackedfiles.remove(".DS_Store");
		return untrackedfiles;
	}

	/**
	 * Usage: java ClientVCS ip port
	 * 
	 * Waar ip het IP address is van de server en port is
	 * het poortnummer
	 * Example:
	 *   java ClientVCS 127.0.0.1 6789 message
	 *   
	 * @throws IOException if there was an error connecting with or
	 *         communicating with the server. 
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		if (args.length != 2) {
			System.out.println("Usage: java ClientVCS ip port");
			return;
		}
		InetAddress ip = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		System.out.println("Client: connecting to server at "+ip+":"+port);
		ClientVCS client = new ClientVCS();
		client.connectToServer(ip, port);
		System.out.println("Client: Connection closed with server at "+ip+":"+ port);
	}

}
