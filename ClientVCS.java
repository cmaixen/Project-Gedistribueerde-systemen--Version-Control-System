


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
	
	private static final String PROMPT = "> ";
	PrintWriter output;
	
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
			BufferedReader input = new BufferedReader(
					new InputStreamReader(rawInput));
			output = new PrintWriter(rawOutput);
		
			
			
			BufferedReader consoleInput = new BufferedReader(
					new InputStreamReader(System.in));
			
			String message;
			do {
				
				System.out.print(PROMPT);
				message = consoleInput.readLine();

				// send message to the server
				output.println(message);
				
				// make sure the output is sent to the server before waiting
				// for a reply
				output.flush();
				
				System.out.println("Client: sent '" + message + "'");
				
				// wait for and read the reply of the server
				String serverReply = input.readLine();
				
				// log the string on the local console
				System.out.println("Client: server replied '" +
				                   serverReply + "'");
				
			} while (true);

		} finally {
			// tear down communication
			socket.close();
		}
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