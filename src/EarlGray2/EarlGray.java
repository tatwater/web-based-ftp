package EarlGray2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

/*Authors: Tony Knapp, Teagan Atwater, Jake Junda
//Started on: April  3, 2014
//A caffeinated FTP server.
//
//This FTP server allows multiple clients to connect to the server by providing
//a user handle and the server's universal password (Earl Gray). The server uses 
//multi-threading to handle multiple clients connecting at one time

//To login from a local machine: 	ftp 127.0.0.1 [port]
 * [port] is outputed to the console at start of server.

//To view the process, run it, and then: ps -A |grep EarlGray
//To view the resources it is taking up, take the PID from the above, 
//and check it out on some proccess manager
 * 
 */

public class EarlGray extends Thread {

	/**
	 * A very simple FTP server with a caffinated theme!
	 * 
	 * This class creates a FTP server. It allows multiple
	 * clients to connect to the server by providing a user handle and the server's
	 * universal password (EarlGray). The server uses multi-threading to handle
	 * multiple clients connecting at one time. User's Handle (teacup) and login time are
	 * recorded to the log. The class cleanly quits all client instances and itself
	 * via the <code>.stopServer()</code> method.
	 * 
	 * @author Tony Knapp
	 * @version 1.0 (4/24/2014)
	 * @since Alpha (4/03/2014)
	 */
	private static int CNT_FTP_PORT;               		// server port numbe
	private ArrayList<EGClientInst> clientInstList; // store running sessions
	private ServerSocket incoming;                  // socket that the server listens to
	final private String password = "EarlGray";
	private File inFile = new File("log.txt");
	private File directory;
	private PrintWriter out;
	public boolean running = false;
	private static int USER_LIMIT = 10;
	webftp.Server parentServer;
	
	/**
	 * This function takes in the port number
	 *  and directory to be shared
	 *  and creates an instance of
	 * EarlGray on the given port
	 * 
	 * @author Tony Knapp
	 * @param
	 * @since Alpha (04/03/14)
	 * @exception IOException
	 */
	public EarlGray(String directoryPath, webftp.Server parentServer) throws IOException {
		this.clientInstList = new ArrayList<EGClientInst>();
		this.directory=new File(directoryPath);
		this.parentServer = parentServer;
		this.out = new PrintWriter(new BufferedWriter(new FileWriter(inFile, true)));
		if(!this.directory.isDirectory()) {
			this.directory.mkdir();
		}
	}
	
	public void startFTP(int portNumber) {
		try {
			this.incoming = new ServerSocket(portNumber); // create server socket on designated port
			CNT_FTP_PORT = incoming.getLocalPort();
			this.running = true;
		} catch (IOException e) {
			e.printStackTrace();                  // print error stack
		}
		this.start();
	}
	
	/**
	 * This function waits for clients to
	 *  connect to the server, then creates a
	 * socket and stores the new client session
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/03/2014)
	 */
	public void run() {
//		System.out.println("The kettle is hot on: " + CNT_FTP_PORT + "!");
//		System.out.println("Type \"quit\" to exit, or \"port\" to display the Server's port.");
		boolean running = true;
		try {
			while (running) {
				Socket clientSoc = incoming.accept();                            // wait for new connection
				EGClientInst clientInst = new EGClientInst(clientSoc, this, this.directory); // create new session on socket
				if (clientInstList.size() > USER_LIMIT){
					clientInst.shutThingsDown(2);
				}
				else {
					clientInstList.add(clientInst);                                      // store new client session
					clientInst.start();                                              // starts client thread
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();                                             // print error stack
		}
	}
	
	/**
	 * This function takes a string and returns true if the string is the server's
	 * password, false otherwise
	 * 
	 * @author Tony Knapp
	 * @author Jake Junda
	 * @param
	 * @return boolean
	 * @since Alpha (03/04/2014)
	 */
	public boolean checkPassword(String attempt) {
		if (attempt.equals(this.password) && this.running) {
			return true;
		} 
		else {
			return false;
		}
	}
	
	/**
	 * This Function writes a client's details to the log file.
	 * 
	 * @author Tony Knapp
	 * @author Jake Junda
	 * @param
	 * @return true
	 * @since Alpha (04/04/2014)
	 */
	 public boolean logLogIn(String handle, Date date, boolean acceptance) {
		out.print(handle + "\t" + date + "\t" + acceptance +"\n");
		return true;
	}
		
	/**
	 * This Function writes a client's 
	 * file transaction details to a log.
	 * 
	 * @author Tony Knapp
	 * @param
	 * @return true
	 * @since Alpha (04/21/2014)
	 */
	public boolean logTransfer(String handle, Date date, String command) {
		out.print(handle + "\t" + date + "\t" + command +"\n");
		return true;
	}

	/**
	 * This function removes the client from the sessions
	 * 
	 * @author Teagan Atwater
	 * @author Tony Knapp
	 * @author Jake Junda
	 * @param
	 * @return true
	 * @since Alpha
	 */
	boolean terminateSession(EGClientInst egClientInst) {
		clientInstList.remove(egClientInst); // remove the session from the active session list
		return true;
	}
	
	/**
	 * This function closes down all user sessions and the whole server
	 * 
	 * @author Teagan Atwater
	 * @author Jake Junda
	 * @since Alpha
	 * @exception IOException
	 */
	public boolean stopServer() throws IOException {
		if (this.running == true) {
			this.running = false;
			for (EGClientInst client : clientInstList) {
				while (!client.shutThingsDown(1)); // wait for the client to shut down before proceeding
			}
			out.close();
	//		System.out.println("All client sessions have been terminated.\nStopping server.");
			try {
				this.join(100);                    // let the thread die
			} catch (InterruptedException e) {
				e.printStackTrace();               // print error stack
			}
			return true;
		}
		return true;
	}
	
	/**
	 * This function gets the port number
	 * stored in the int CNT_FTP_PORT
	 * 
	 * @author Jake Junda
	 * @param int
	 * @return int
	 * @since Alpha(4/23/2014)
	 */
	public String getPortNum(){
		String rtn = Integer.toString(CNT_FTP_PORT);
		if (rtn.length() < 5) {
			int difference = 5 - rtn.length();
			for (int i = 0; i < difference; i++ ){
				rtn = "0" + rtn;
			}
			return rtn;
		}
			
			return Integer.toString(CNT_FTP_PORT);
	}
	                                                                 
}