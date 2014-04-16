


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;



/**
 * A simple client to an {@link EchoServer}.
 * 
 * Once connected to the server, opens a session so that any
 * newline-terminated String from the standard input stream
 * is sent to the server. Entering an empty string stops the
 * session and terminates the client.
 * 
 * Illustrates the use of TCP/IP sockets.
 */
public class ClientVCS {
	private WorkingDirectory client_repository = new WorkingDirectory("./") ;
	private static final String PROMPT = "> ";
	//locale plaats waar repositories staan opgeslagen
    private String clientreposfolder = "Clientrepos";
    //output messages of the server get adjusted by applied methodes that react on original message from server
    String serverReply;
	PrintWriter output;
	
	//Constructor
	public ClientVCS() throws IOException{
	//checks if there is a location for repos, if not initialize this location
		if (!client_repository.changeWorkingDir(clientreposfolder)){
		//create folder
		client_repository.createDir(clientreposfolder);
		}
	}

	/**
	 * Connect to server at the given ip:port combination,
	 * send the msg string and await a reply.
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
	 * 
	 * @param ip the server IP address
	 * @param port the server port number
	 * @throws UnknownHostException if the server IP could not be found
	 * @throws IOException if there is an error in setting up or communicating
	 *         with the server.
	 */
	public void connectToServer(InetAddress ip, int port)
		        throws UnknownHostException, IOException {
		
		InetSocketAddress serverAddress = new InetSocketAddress(ip, port);		
		Socket socket = new Socket();
		socket.connect(serverAddress);
		try {
			// get raw input and output streams
			InputStream rawInput = socket.getInputStream();
			OutputStream rawOutput = socket.getOutputStream();
			
			// wrap streams in Readers and Writers to read and write
			// text Strings rather than individual bytes 
			//It's advisable to wrap a BufferedReader around each Reader and Writer to be less costly
			BufferedReader input = new BufferedReader(
					new InputStreamReader(rawInput));
			//PrintWriter converts characters automatically to bytes
			output = new PrintWriter(rawOutput);
			
			BufferedReader consoleInput = new BufferedReader(
					new InputStreamReader(System.in));
			
			String message;
			do {
				
				System.out.print(PROMPT);
				message = consoleInput.readLine();

			    //prepares message before sending it to the server
				prepare(message);
				
				// send message to the server
				output.println(message);
				
				// make sure the output is sent to the server before waiting
				// for a reply
				output.flush();
				
				System.out.println("Client: sent '" + message + "'");
				
				// wait for and read the reply of the server
				serverReply = input.readLine();
				
				//process assignment server
				process(serverReply);
				
				//log the string on the local console
				System.out.println("Client: server replied '" +
				                   serverReply + "'");
				
			} while (true);

		} finally {
			// tear down communication
			socket.close();
		}
	}
	
	
	public void process(String input) throws IOException{
	     Scanner s = new Scanner(input);
	     String command = s.next();
	     
	     if(command.equals("checkout")){
	    	 //more is coming 
	 
	     }
	     else  if(command.equals("add")) {
	    	 //more is coming 
	    	 
	     }
	     else if(command.equals("commit")) {
	    	 //more is coming 
	    	
	     }
	     else if(command.equals("create_repository")) {
	    	 //create a new repository en creeer deze ook bij client
	    	 String name_repo = s.next();
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
	     else if(command.equals("ERROR:")){
	    	 serverReply =  input;
	     }
	     else {
	    	 System.out.println("invalid command '" + input + "'");
	     }
	}

	public void prepare(String input) throws IOException{
	     Scanner s = new Scanner(input);
	     String command = s.next();
	     String name = s.next();
	     String result = null;
	     if(command.equals("checkout")){
	    	 
	 
	     }
	     else  if(command.equals("add")) {
	    	 //prepare stream for an add
	    	 
	     }
	     else if(command.equals("commit")) {
	    	 //more is coming 
	    	
	     }
	     else if(command.equals("create_repository")) {
	    	 //create a new repository en creeer deze ook bij client
	    	 String name_repo = s.next();
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
	     else if(command.equals("ERROR:")){
	    	 serverReply =  input;
	     }
	     else {
	    	 System.out.println("invalid command '" + input + "'");
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
			System.out.println("Client: New repository '" + name + "' succefull locally created");
			serverReply = "New repository '" + name + "' succefully created"; 
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
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: java ClientVCS ip port");
			return;
		}
		
		InetAddress ip = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		
		System.out.println("Client: connecting to server at "+ip+":"+port);
		
		ClientVCS client = new ClientVCS();
		client.connectToServer(ip, port);
		
		System.out.println("Client: terminating");
	}

}
