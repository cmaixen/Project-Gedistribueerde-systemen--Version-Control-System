/*
Copyright (c) 2013, Tom Van Cutsem, Vrije Universiteit Brussel
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Vrije Universiteit Brussel nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/


import java.io.BufferedReader;
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
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.io.ObjectInputStream;

import org.apache.commons.io.IOUtils;


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
public class ServerVCS  {
	//  final variable
	private final ServerSocket serverSocket;
	
	private WorkingDirectory server_repository = new WorkingDirectory("./");
	private String servername = "Serverrepos";
	Command clientassignment;
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
	
	//gaat gevraagde command ,indien juist, uitvoeren en anders gaat hij de client verwittigen
	public Command process(Command input) throws IOException{
	    String command = input.getCommand();
	    Command respons = null;
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
	    	 NewRepositoryEvent newrepo = (NewRepositoryEvent) input;
	    	 String name_repo = newrepo.getName();
	    	 respons = newrepo;
	    	 //When the creation of a new directory fails and we get the boolean false back, we assume that the directory already exists.
	    	 if (!create_repository(name_repo)){
	    		 System.out.println("Server: ERROR: Cannot create new repository '" + name_repo + "'! It already exists!");
	    		 respons = new ErrorEvent("repository already exists!");
	    		 
	    	 }
	    	 else{ 
	    		 System.out.println("Server: New repository '" + name_repo + "' succefully created");
	    	 }
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
	     else {
	    	 System.out.print("Server: ERROR: invalid command '" +  input + "'" );
	    	 respons = new ErrorEvent("Invalid command!");
	     }
		return respons;
	}

	//<<<<<<COMMANDS>>>>>>>
	
	public Boolean create_repository(String name) throws IOException{
	    return server_repository.createDir(name);
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
					// read string from client
					Command clientInput = (Command) inputStream.readObject();
					
					String clientcommand = clientInput.getCommand();
					
					// log the string on the local console
					System.out.println("Server: client sent '" +
					                   clientcommand + "'");
					
					//process client input
					clientassignment = process(clientInput);
					
					// send back the string to the client
					outputStream.writeObject(clientassignment);
					
					// make sure the output is sent to the client before closing
					// the stream
					outputStream.flush();				
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
