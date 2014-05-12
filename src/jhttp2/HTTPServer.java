package jhttp2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/*
 * Authors: Tony Knapp, Teagan Atwater, Jake Junda
 * Started: April 28, 2014 (Alpha)
 * Project: A simple HTTP server
 * Description: This HTTP server allows multiple clients to request documents to be sent to them
 *              simultaneously.
 */
public class HTTPServer extends Thread {
	static int PORT;
	private ArrayList<HTTPClient> activeClients = new ArrayList<HTTPClient>();
	private ServerSocket incoming; // Socket that the server listens to
	private File log = new File("log.txt");
	private PrintWriter out;
	private boolean running = true;
	private int USER_LIMIT = 100;
	File directory;
	int requestCnt = 0;
	// version: {major, minor}
	int[] version = {1, 1};
	private webftp.Server parentServer;

	/**
	 * Create new instance of HTTPServer
	 * 
	 * @author Tony Knapp
	 * @author Teagan Atwater
	 * @param newPort (optional, will default to 80)
	 * @param directoryPath (path from computer root to server root directory)
	 * @since Alpha
	 */
	public HTTPServer(int newPort, String directoryPath, webftp.Server parentServer) throws IOException {
		this.activeClients = new ArrayList<HTTPClient>();
		HTTPServer.PORT = newPort;
		this.directory = new File(directoryPath);
		this.parentServer = parentServer;
		if (!this.directory.isDirectory()) {
			this.directory.mkdir();
		}
		try {
			this.out = new PrintWriter(new BufferedWriter(new FileWriter(log, true)));
			this.incoming = new ServerSocket(PORT); // Create server socket on designated port
			PORT = incoming.getLocalPort();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Wait for clients to connect to the server, then creates a socket and
	 * stores the new client session
	 * 
	 * @author Tony Knapp
	 * @author Teagan Atwater
	 * @since Alpha
	 */
	public void run() {
//		System.out.println("Server started on port " + PORT + ".\nType \"QUIT\" to exit. \nType \"PORT\" to print the port.");
		this.running = true;
		try {
			while (running) {
				Socket clientSoc = incoming.accept(); // Wait for new connection
				HTTPClient clientInst = new HTTPClient(clientSoc, this, this.directory, this.requestCnt, this.parentServer); // Create new session on socket
				requestCnt++;
				if (activeClients.size() > USER_LIMIT){
					clientInst.shutThingsDown(0);
				}
				else {
					activeClients.add(clientInst); // Store new client session
					clientInst.start(); // Start client thread
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getPort() {
		return PORT;
	}
	
	
	/**
	 * Closes down all user sessions and stops the server
	 * 
	 * @author Teagan Atwater
	 * @author Jake Junda
	 * @since Alpha
	 */
	public boolean stopServer() throws IOException {
		this.running = false;
		for (HTTPClient client : this.activeClients) {
			while (!client.shutThingsDown(0)); // Wait for the client to shut down before proceeding
		}
		out.close();
//		System.out.println("All client sessions have been shut down. Stopping server.");
		try {
			this.join(100); // Let the thread die -> xp 
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
}
