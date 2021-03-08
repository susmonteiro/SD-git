package pt.tecnico.sockets;

import java.io.*;
import java.net.*;
import java.lang.Exception;


public class SocketServer {

	public static void main(String[] args) throws IOException {
		// Check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port%n", SocketServer.class.getName());
			return;
		}

		// Convert port from String to int
		final int port = Integer.parseInt(args[0]);

		// Create server socket
		ServerSocket serverSocket = new ServerSocket(port);
		System.out.printf("Server accepting connections on port %d %n", port);

		// wait for and then accept client connection
		// a socket is created to handle the created connection
		Socket clientSocket = serverSocket.accept();
		final String clientAddress = clientSocket.getInetAddress().getHostAddress();
		final int clientPort = clientSocket.getPort();
		System.out.printf("Connected to client %s on port %d %n", clientAddress, clientPort);

		// Create buffered stream to receive data from client, one line at a time
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		// Receive data until client closes the connection
		String response;
		while (true) {
			// Read a line of text.
			// A line ends with a line feed ('\n').
			response = in.readLine();
			if (response == null) {
				break;
			}
			
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			
			try {
				File f = new File(response);
				BufferedInputStream inF = new BufferedInputStream(new FileInputStream(f));

				long l = f.length();
				out.writeBytes(Long.toString(l));
				out.writeBytes("\n");
				int size;
				byte[] buffer = new byte[512];
				while ((size = inF.read(buffer)) > 0) {
					out.write(buffer, 0, size);
				}
				inF.close();

			}
			catch (FileNotFoundException e) {
				out.writeBytes("NOK\n");
				System.err.println(e);

			}
			
	
			//System.out.printf("Received message with content: '%s'%n", response);
			
		}


		 

		// Close connection to current client
		clientSocket.close();
		System.out.println("Closed connection with client");

		// Close server socket
		serverSocket.close();
		System.out.println("Closed server socket");
	}

}
