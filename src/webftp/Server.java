package webftp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import EarlGray2.*;
import jhttp2.*;

//TODO add Post to jhttp, make it parse the body for FTP server commands
//TODO add FTP server commands. 
//TODO hardcode directory for HTTP server
//TODO Make change port function and start FTP server


/*
 * Authors: Tony Knapp, Teagan Atwater, Jake Junda
 * Started: May 06, 2014 (Alpha)
 * Project: A simple HTTP and FTP server
 * Description: This HTTP/FTP server serves files via both protocols to different clients
 */
public class Server extends Thread {
	static HTTPServer httpServer;
	static EarlGray ftpServer;

	/**
	 * Create new instance of Server
	 * 
	 * @author Teagan Atwater
	 * @since Alpha
	 */
	public Server() throws IOException {
		int httpPort = 0;
		String directoryPath = "";		
		System.out.print("Initializing HTTP... ");
		this.httpServer = new HTTPServer(httpPort, directoryPath);//TODO modify so that this passes self
		System.out.println("HTTP initialized.");
		this.httpServer.start();
		System.out.print("HTTP started.\nInitializing FTP... ");
		//this.ftpServer = new EarlGray(directoryPath, httpPort, null);//TODO modify so that this passes a port and a self
		//System.out.println("FTP initialized.\nTo start FTP, use web service: 'localhost/admin/'.");
	}
	
	//TODO create a server initializer, starts http and intializes ftp.
	
	//TODO create a server run function, maintains http and ftp servers. Looks for FTP server start and stop commands and  sub methods executes.
	
	//TODO Create sub methods for handeling FTP configuration commands:
		//TODO Check login creditionals for FTP user
		//TODO if Logged in, allow start of FTP on port, return port number
		//TODO if Logged in, allow quit of FTP, return status
	
	/**
	 * Intiate the server
	 * 
	 * @author Teagan Atwater
	 * @since Alpha
	 */
	public static void main(String[] args) throws Exception {
		Scanner in = new Scanner(System.in);
		String text;
		System.out.println("Starting server...");
		Server server = new Server();
		text = in.nextLine();
		while (text != null && !text.trim().equalsIgnoreCase("QUIT")) {
			// Do something useful aka run
			text = in.nextLine();
		}
		while (!ftpServer.stopServer());
		while (!httpServer.stopServer());
		in.close();
		System.exit(0);
	}
}