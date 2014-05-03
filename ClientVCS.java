


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;



/**
 * A Version Control System Client
 * 
 * Once connected to the server, opens a session that any
 * newline-terminated String from the standard input stream
 * is sent to the server as a java-object.
 * Entering an empty string stops the session and terminates the client.
 * 
 * The connection is established with TCP/IP sockets
 */
public class ClientVCS {
	private WorkingDirectory client_repository = new WorkingDirectory("./") ;

	private static final String PROMPT = "> ";

	//locale plaats waar repositories staan opgeslagen
	private String clientreposfolder = "Clientrepos";
	private String metafile = "MetaVCS";
	private MetaData MetaFile = null;
	private String current_repository = null;

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
		}
	}

	/**
	 * Connect to server at the given ip:port combination
	 * 
	 * Client and server sockets communicate via input and output Objectstreams,
	 * as shown schematically below:
	 * 
	 * <pre>
	 *   Client                             Server
	 *    cs = new Socket(addr,port)        ss = socket.accept()
	 *       cs.in <-------------------------- ss.out
	 *       cs.out -------------------------> ss.in
	 * </pre>
	 * 
	 * @param ip the server IP address
	 * @param port the server port number
	 * @throws UnknownHostException if the server IP could not be found
	 * @throws IOException if there is an error in setting up or communicating
	 *         with the server.
	 * @throws ClassNotFoundException 
	 */
	public void connectToServer(InetAddress ip, int port)
			throws UnknownHostException, IOException, ClassNotFoundException {

		InetSocketAddress serverAddress = new InetSocketAddress(ip, port);	
		socket.connect(serverAddress);
		try {

			//create Objectstreams
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());

			//Bufferreader for consoleinput
			BufferedReader consoleInput = new BufferedReader(
					new InputStreamReader(System.in));

			String message;
			do {

				System.out.print(PROMPT);
				message = consoleInput.readLine();

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
				System.out.println("Client: server replied '" +
						serverMessage + "'");

			} while (true);

		} finally {
			// tear down communication
			socket.close();
		}
	}

	public Command prepare(String input) throws IOException, ClassNotFoundException{
		Scanner s = new Scanner(input);
		Command result = null;

		//test of de command line niet leeg is
		if (!input.isEmpty()){
			String command = s.next();


			if(command.equals("checkout")){
				String name = s.next();
				//Go back to Homedirectory
				client_repository.goToWorkingDir(clientreposfolder);
				//look if asked repository already exists
				if(client_repository.changeWorkingDir(name)){
					result = new ErrorEvent("Cannot Checkout because asked repository already exists");	
				} else {
					//maak de repository aan in de homefolder
					client_repository.createDir(name);
					//stuur een CheckoutEvent naar de Server
					result = new CheckoutEvent(name, client_repository.getWorkingDir() + "/" + name);
				}	

			}
			else  if(command.equals("add")) {
				String filename = s.next();
				//controleren of file wel bestaat
				if (client_repository.file_exists(filename)){
					System.out.println(MetaFile);
					MetaFile.add(filename);
					result = new LocalEvent("File '" + filename + "' succefully added.");
				}
				else 
				{ result = new ErrorEvent("File '" + filename + "' doesn't exists!"); }	 
			}
			else if(command.equals("commit")) {
				String comment = "";
				if (s.hasNext("-m")){
					//-m verwijderen;
					s.next();
					//er is een comment
					while(s.hasNext()){
						comment = comment + " " + s.next();
					}
					System.out.println("Comment given :" +  comment);
					//stuurt een CommitEvent met als Destination de  repo waar de commit moet gebeuren en een comment
				}
				ArrayList<String> listwithcommitfiles = MetaFile.ToCommit();

				result = new CommitEvent(comment, current_repository, listwithcommitfiles);
			}

			else if(command.equals("create_repository")) {
				String name = s.next();
				result = new NewRepositoryEvent(name);
			}
			else if(command.equals("update")) {
				//more is coming 

			}
			else if(command.equals("status")) {
				//more is coming 

			}
			else if(command.equals("diff")) {
				//more is coming
			}
			else if(command.equals("change_repo")){
				String repo = s.next();
				//in het begin is metafile null, indien niet null zit men in een repo
				if(MetaFile != null){
					//opslagen van Metafile in huidige directory
					saveMetaFile();
				}
				//go back to Homefolder
				client_repository.goToWorkingDir(clientreposfolder);
				//go to required repo
				if (!client_repository.changeWorkingDir(repo)){
					result = new ErrorEvent("Given repository doesn't exist");	
				}
				else {
					loadMetaFile();
					current_repository = repo;
					result = new LocalEvent("Sucessfully changed to the repository '" + repo + "'");
				}
			}
			else if(command.equals("ls")){
				result = new LocalEvent(Arrays.toString(client_repository.list()));
			}
			else if(command.equals("list_repos")){
				WorkingDirectory copydir = new WorkingDirectory(clientreposfolder);
				//go back to Homefolder
				copydir.goToWorkingDir(clientreposfolder);
				//list the repositories
				result = new LocalEvent(Arrays.toString(copydir.list())); 
			}

			else if(command.equals("current_repo")){
				result = new LocalEvent(current_repository);
			}
			else {
				//invalid command
				result = new ErrorEvent("invalid command '" + input + "'");
			}
		}
		else {result = new ExitEvent();}

		return result;
	}

	public void process(Command input) throws IOException{
		String command = input.getCommand(); 

		if(command.equals("CHECKOUT")){
			serverMessage = "Repository check out succefull!";
		}
		else  if(command.equals("add")) {
			//more is coming 

		}
		else if(command.equals("COMMIT")) {
			//opvragen van destination op schijf
			String sourcedestination = ((CommitEvent) input).getDestination();
			//opvragen wat men moet sturen naar de server
			ArrayList<String> commitlist =  MetaFile.ToCommit();
			Acknowledgement ACK =  new Acknowledgement("COMMIT_ACK");
			outputStream.writeObject(ACK);
			//Files sturen naar de server
			locateFiles(current_repository, commitlist, sourcedestination);
		}
		else if(command.equals("create_repository")) {
			//create a new repository en creeer deze ook bij client
			NewRepositoryEvent newrepo = (NewRepositoryEvent) input;
			String name_repo = newrepo.getName();
			create_repository(name_repo);	 
		}
		else if(command.equals("update")) {
			//more is coming 

		}
		else if(command.equals("status")) {
			//more is coming 

		}
		else if(command.equals("diff")) {
			//more is coming 

		}

		else if(command.equals("FileEvent")){
			downloadFiles();

		}
		else if(command.equals("ERROR")){
			serverMessage = ((ErrorEvent) input).getNotification();
		}
		else {
			System.out.println("invalid replycommand '" + input + "'");
		}
	}

	/**
	 * Reading the FileEvent object and copying the file to disk.
	 */
	public void downloadFiles() {
		if (socket.isConnected()) {
			try {
				FileEvent fileEvent;
				File dstFile = null;
				FileOutputStream fileOutputStream = null;
				boolean more = true;
				while (more){
					fileEvent = (FileEvent) inputStream.readObject();
					if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
						System.out.println("Error occurred ..with  file" + fileEvent.getFilename() + "at sending end ..");

					}
					String outputFile = fileEvent.getDestinationDirectory() + "/" + fileEvent.getFilename();
					if (!new File(fileEvent.getDestinationDirectory()).exists()) {
						new File(fileEvent.getDestinationDirectory()).mkdirs();
					}
					dstFile = new File(outputFile);
					fileOutputStream = new FileOutputStream(dstFile);
					fileOutputStream.write(fileEvent.getFileData());
					fileOutputStream.flush();
					fileOutputStream.close();
					System.out.println("Output file : " + outputFile + " is successfully saved ");
					if (fileEvent.getRemainder() == 0) {
						System.out.println("Whole repository is copied...");
						more = false;
					}
				}
				Command stop = (Command) inputStream.readObject();
				if(stop.getCommand().equals("CHECKOUT")){
					serverMessage = "Checkout was Succesfull";
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}






	//COMMANNDS OM OP TE STELLEN

	//Laat de client toe om een nieuw bestand aan de repository toe te voegen.

	public void add(String filename){	
	};

	//Dit commando wordt eenmaal uitgevoerd en maakt een working copy aan bij de client.
	public void checkout(String repository){

	};

	//Verstuurt veranderingen naar de repository op de server. Heeft een -m optie om een logbericht toe te voegen aan deze commit.

	public void commit(){	
	};

	//Dit commando wordt gebruikt om de verschillen tussen twee revisies van een bestand te weten te komen.
	public void diff(){	
	};

	//Toont een status van je working copy: welke bestanden je aangepast hebt, revisienummers van de bestanden, etc.	
	public void status(){	
	};

	//Wordt gebruikt om de working copy te synchroniseren met de repository op de server.
	public void update(){	
	};

	//repository cre‘ren.
	//De verantwoordelijkheid om te kijken of de repository al bestaat ligt bij de server.
	public void create_repository(String name) throws IOException{
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

	public void locateFiles(String name, ArrayList<String> commitlist,String sourceDestination) throws IOException {
		int fileCount;

		String sourceDirectory = client_repository.getWorkingDir();
		ArrayList<File> filelist = new ArrayList<File>();
		for(String filename : commitlist) {
			filelist.add(client_repository.getFile(filename));
		}
		fileCount = filelist.size();
		int counter = 0;
	 
		for (File file : filelist) {
			System.out.println("Client: Sending " + file.getAbsolutePath());
			sendFile(file.getAbsolutePath(), fileCount - counter - 1, sourceDirectory, sourceDestination);
			
		}
		
	}

	private void sendFile(String fileName, int index, String sourceDirectory, String sourceDestination) {
		FileEvent fileEvent = new FileEvent();
		fileEvent.setDestinationDirectory(sourceDestination);
		fileEvent.setSourceDirectory(sourceDirectory);
		File file = new File(fileName);
		fileEvent.setFilename(file.getName());
		fileEvent.setRemainder(index);
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
			System.out.println("written on outputstream");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadMetaFile() throws FileNotFoundException, IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream(client_repository.getWorkingDir() + "/" + metafile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		MetaFile = (MetaData) ois.readObject();
		ois.close();

	}

	public void saveMetaFile() throws FileNotFoundException, IOException{
		FileOutputStream fos = new FileOutputStream(client_repository.getWorkingDir() + "/" + metafile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(MetaFile);
		oos.close();
	}

	/**
	 * Usage: java ClientVCS ip port message
	 * 
	 * Where ip is the IP address of the server, port is
	 * the port number, and message is a string to be
	 * sent to the server.
	 * 
	 * Example:
	 *   java ClientVCS 127.0.0.1 6789 message
	 *   
	 * @throws IOException if there was an error connecting with or
	 *         communicating with the server. 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
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
