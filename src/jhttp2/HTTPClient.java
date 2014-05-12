package jhttp2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

/**
 * Project: A simple HTTP server Client
 * This HTTP server Client handles an instance of the each client
 * that connects to the server.  It handles the return codes that let the user
 * know the result of thier commands.  
 * @author Tony Knapp, Teagan Atwater, Jake Junda
 * @since April 28, 2014
 * @version 1.0 (05/01/2014)
 */
public class HTTPClient extends Thread {
	Socket controlSoc;
	BufferedReader controlIn;
	DataOutputStream controlOut;
	Date loginTime;
	HTTPServer server;
	String dirName = "/";
	File parentDir;
	boolean running = true;
	boolean dataConnection = false;
	boolean isSending = false;	
	int requestNum;
	ArrayList<String> input = new ArrayList<String>();
	private webftp.Server parentServer;
	private boolean writingPortNum = false;
	private String portNumtoWrite = "00000";
	private boolean writingLogIn = false;
	private boolean logInStatus = false;
	

	
	/**
	 * This constructs a Thread that
	 * handles the client's connections.
	 * @author Tony Knapp
	 * @since Alpha (04/29/2014)
	 * @param Socket, Server
	 */
	public HTTPClient(Socket cSoc, HTTPServer server, File parentFolder, int count, webftp.Server parentServer) throws IOException {
		this.controlSoc = cSoc; // attach to client socket
		this.controlIn = new BufferedReader(new InputStreamReader(controlSoc.getInputStream()));
		this.controlOut = new DataOutputStream(controlSoc.getOutputStream());
		this.server = server;
		this.parentDir = parentFolder;
		this.requestNum = count;
		this.parentServer = parentServer;
		
//		System.out.println(this.requestNum + "New Request");
	}
	
	/**
	 * Runs the thread for each request. 
	 * @author Tony Knapp, Teagan McGillicuddy, The Junda
	 * @since Alpha April 28, 2014
	 */
	public void run() { 
		try {
	  	String tmp = controlIn.readLine();
	  	while (!tmp.isEmpty()){
//	  		System.out.println(this.requestNum + "IN: " + tmp);
	  		this.input.add(tmp);	  		
				tmp = controlIn.readLine();
  		}
	    if (this.input.get(0).startsWith("GET")) { //if tmp equals GET, set to method 1
	      get();
	    }
	    else if (this.input.get(0).startsWith("HEAD")) { //if tmp equals HEAD, set to method 2
	      head();
	    }
	    else { // not supported
      	controlOut.writeBytes(construct_http_header(501, 0));
      	controlOut.close();
        return;
	    }
	    shutThingsDown(0);
		} 
		catch (Exception e3) { //notify user of an error
			e3.printStackTrace();
    }
	}	
	
	/**
	 * Returns the requested file to the user.
	 * @throws IOException
	 * @author Tony Knapp, Jake Junda, Teagan Atwater
	 * @since Beta (04/30/2014)
	 */
	private void get() throws IOException {
    String path = this.input.get(0).substring(3, input.get(0).length()-8); //fill in the path
    String version = this.input.get(0).substring(2 + path.length()).trim();
    path = path.trim();
    if (version.startsWith("HTTP")) {
    	String[] versionInt = version.substring(5).split("\\.");
    	if (server.version[0] < Integer.parseInt(versionInt[0])) {
    		//HTTP version is not supported
    	}
    	else if (server.version[0] == Integer.parseInt(versionInt[0]) && server.version[1] < Integer.parseInt(versionInt[1])) {
    		//HTTP version is not supported
    	}
    }
    if (path.equalsIgnoreCase("/")) {
    	path = server.directory.getAbsolutePath() + "/" + "index.html";
    }
    // URL token for ftp config login: "PASS:<username>, <password>"
    else if (path.substring(0, 3).equals("PASS")) {
    	int endUserName = path.indexOf(',');
    	String userName = path.substring(4, endUserName);
    	String passWord = path.substring(1+endUserName);
    	//@tatwater: Admin page needs to return the following
    		//PASS:<username>,<password>
    		//STRT:<portNumbers>
    	//@tatwater: Admin page needs to take the following parameters from us somehow
    		//Login successful
    		//Login failed
    		//FTP started on:___
    		//FTP shut down
    	if (parentServer.ftpLogIn(userName, passWord)) { //parentServer.ftpLogIn needs to rtn boolean
    		//Send config server page
    		this.writingPortNum = true;
    		this.writingLogIn = true; 
    		this.logInStatus = true;
    		this.portNumtoWrite = "00000";
    		path =  server.directory.getAbsolutePath() + System.getProperty("path.separator") + "admin"; //tatwater will get back to us about this 
    	}
    	else {
    		this.writingPortNum = true;
    		this.writingLogIn = true;
    		this.logInStatus = false;

    		path =  server.directory.getAbsolutePath() + System.getProperty("path.separator") + "admin";//TODO set to failed login page
    	}
    }
    // URL token for ftp start: "STRT:<portNumber>"
    else if (path.substring(0, 3).equals("STRT")) {
    	this.portNumtoWrite = parentServer.startFTP(Integer.parseInt(path.substring(5)));
  		this.writingPortNum = true;
  		this.writingLogIn = true; 
  		this.logInStatus = true;
    	path = server.directory.getAbsolutePath() + System.getProperty("path.separator") + "admin"; //TODO set to a page, but we need to somehow set the page to take a portNumber
    }
    else if (path.substring(0, 3).equals("STOP")) {
  		this.writingPortNum = true;
  		this.writingLogIn = true; 
  		this.portNumtoWrite = "00000";
  		this.logInStatus = parentServer.stopFTP();
    	path = server.directory.getAbsolutePath() + System.getProperty("path.separator") + "admin"; //TODO set to a page that 
    }
    else {
    	path = server.directory.getAbsolutePath() + "/" +  path;
    }
    final File sendFile = new File(path);
    if (!sendFile.isFile()) {
    	controlOut.writeBytes(construct_http_header(404, 0));
    }	
    else {
    	int type_is;
    	if (path.endsWith(".zip")) {
    		type_is = 3;
	    }
    	else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
	      type_is = 1;
	    }
    	else if (path.endsWith(".gif")) {
	      type_is = 2;
	    }
    	else {
    		type_is = 0;
    	}
	  	controlOut.writeBytes(construct_http_header(200, type_is));
	  	FileInputStream requestedFile = new FileInputStream(sendFile);
	    int b = requestedFile.read();
	    int counter = 0;
	    while (b != -1) {
	    	counter++;
	    	if (counter == 469 && writingPortNum) {
	    		for (int i = 0 ; i < this.portNumtoWrite.length(); i++){
	    			b = (byte) this.portNumtoWrite.charAt(i);
	    			controlOut.write(b);
	    		}
	    		writingPortNum = false;
	    	}
	    	if (counter == 503 && this.writingLogIn){
	    		if (this.logInStatus){
	    			String toWrite = "True";
	    			for (int i = 0 ; i < toWrite.length(); i++){
		    			b = (byte) toWrite.charAt(i);
		    			controlOut.write(b);
		    		}
	    		}
    			else {
		    			String toWrite = "False";
		    			for (int i = 0 ; i < toWrite.length(); i++){
			    			b = (byte) toWrite.charAt(i);
			    			controlOut.write(b);
			    		}
    			}
    			writingLogIn = false;	    			
	    	}
	      controlOut.write(b);
	      b = requestedFile.read();
      }
	    requestedFile.close();
    }
	}

	/**
	 * Simply returns the header to the client, but no file.
	 * @author Tony Knapp, Jake Junda, Teagan Atwater
	 * @since Alpha April 28, 2014
	 */
	private void head() throws IOException {
    String path = this.input.get(0).substring(2, input.get(0).length()-8).trim(); //fill in the path
    String version = this.input.get(0).substring(2 + path.length()).trim();
    if (version.startsWith("HTTP")) {
    	String[] versionInt = version.substring(4).split(".");
    	if (server.version[0] < Integer.parseInt(versionInt[0])) {
    		//HTTP version is not supported
    	}
    	else if (server.version[0] == Integer.parseInt(versionInt[0]) && server.version[1] < Integer.parseInt(versionInt[1])) {
    		//HTTP version is not supported
    	}
    }
    if (path.equalsIgnoreCase("/")) {
    	path = server.directory.getAbsolutePath() + "index.html";
    }
    else {
    	path = server.directory.getAbsolutePath() + path;
    }
    final File sendFile = new File(path);
    if (!sendFile.isFile()) {
    	controlOut.writeBytes(construct_http_header(404, 0));
    }	
    else {
    	int type_is;
    	if (path.endsWith(".zip")) {
    		type_is = 3;
	    }
    	else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
	      type_is = 1;
	    }
    	else if (path.endsWith(".gif")) {
	      type_is = 2;
	    }
    	else {
    		type_is = 0;
    	}
	  	controlOut.writeBytes(construct_http_header(200, type_is));
    }
	}
  
  
	boolean shutThingsDown(int errorCode) throws IOException{
		if (errorCode==0) {
			
		}
		this.running = false;
		controlOut.close();
		return true;
	}
	
	/**
	 * Description: this method creates the HTTP header for the response
	 * the header tells the browser what the result of the request
	 * @author Tony Knapp, Teagan Atwater, Jake Junda
	 * @since Alpha April 29, 2014 
	 */	
  private String construct_http_header(int return_code, int file_type) {
    String s = "HTTP/1.1 ";
    switch (return_code) {
      case 200:
        s = s + "200 OK";
        break;
      case 400:
        s = s + "400 Bad Request";
        break;
      case 403:
        s = s + "403 Forbidden";
        break;
      case 404:
        s = s + "404 Not Found";
        break;
      case 500:
        s = s + "500 Internal Server Error";
        break;
      case 501:
        s = s + "501 Not Implemented";
        break;
    }

    s = s + "\r\n"; //other header fields,
    s = s + "Connection: close\r\n"; //we can't handle persistent connections
    s = s + "Server: SimpleHTTPtutorial v0\r\n"; //server name

	/**
	 * this switch statement lets the browser know
	 * what kind of file is being sent
	 */	
    switch (file_type) {
      case 0:
        break;
      case 1:
        s = s + "Content-Type: image/jpeg\r\n";
        break;
      case 2:
        s = s + "Content-Type: image/gif\r\n";
      case 3:
        s = s + "Content-Type: application/x-zip-compressed\r\n";
      default:
        s = s + "Content-Type: text/html\r\n";
        break;
    }

    s = s + "\r\n"; //this marks the end of the httpheader
    //and the start of the body
//    System.out.println(this.requestNum + "OUT: "+ s);
    return s;//return the header
  }
}


