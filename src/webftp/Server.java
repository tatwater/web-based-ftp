package webftp;

import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

import jhttp2.HTTPServer;
import EarlGray2.EarlGray;


/*
 * Authors: Tony Knapp, Teagan Atwater, Jake Junda
 * Started: May 06, 2014 (Alpha)
 * Project: A simple HTTP and FTP server
 * Description: This HTTP/FTP server serves files via both protocols to different clients
 */
public class Server extends Thread {
	private HTTPServer httpServer;
	private EarlGray ftpServer;
	private boolean ftpLogin = false;
	private String ftpUser;

	/**
	 * Create new instance of Server
	 * 
	 * @author Teagan Atwater
	 * @since Alpha
	 */
	public Server(String dirPath, int port) throws IOException {
		String directoryPath = dirPath;
		int httpPort = port;
		System.out.print("Initializing HTTP... ");
		this.httpServer = new HTTPServer(httpPort, directoryPath, this);
		System.out.println("HTTP initialized.");
		this.httpServer.start();
		System.out.println("HTTP started.");
//		System.out.print("HTTP started.\nInitializing FTP... ");
		this.ftpServer = new EarlGray(directoryPath, this);
//		System.out.println("FTP initialized.\nTo start FTP, use web service: 'localhost/admin/'.");
	}
	
	public boolean ftpLogIn(String userName, String pass){
		if (ftpServer.checkPassword(pass)) {
			ftpServer.logLogIn(userName, new Date(), true);
			this.ftpLogin = true;
			return true;
			}
		else {
			ftpServer.logLogIn(userName, new Date(), false);
			this.ftpLogin = false;
			return false;
		}
	}
	
	public int startFTP(int portNumber) {
		if (this.ftpLogin) {
			ftpServer.startFTP(portNumber);
			ftpServer.logTransfer(this.ftpUser,  new Date(), ("Started FTP " + ftpServer.getPortNum() +"."));
			return ftpServer.getPortNum();
		}
		else
			return -1;
	}
	
	public boolean stopFTP() throws IOException {
		if (this.ftpLogin) {
			ftpServer.stopServer();
			return true;
		}
		else
			return false;
	}

	boolean shutThingsDown() throws IOException {
		while (!this.ftpServer.stopServer());
		while (!this.httpServer.stopServer());
		return true;
	}
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
		boolean validDir = false;
		String directoryPath = "";
		while(!validDir) {
			System.out.println("Please provide the absolute path to the server root directory:");
			directoryPath = in.nextLine();
			if (directoryPath.startsWith("/") || directoryPath.startsWith("C://")) {
				validDir = true;
			}
			else {
				System.out.println("Error: Must be an absolute path.");
			}
			// TODO: Probably should have other checks, like attempting to follow path to make sure it exists or something
		}
		System.out.println("Path accepted.");
		boolean validPort = false;
		int httpPort = 0;
		while(!validPort) {
			System.out.println("Please provide the HTTP port number or press enter to use default (80):");
			String tempPort = in.nextLine();
			if (tempPort.matches("^([-+] ?)?[0-9]+(,[0-9]+)?$")) {
				if (Integer.parseInt(tempPort) <= 65535) {
					validPort = true;
					httpPort = Integer.parseInt(tempPort);
				}
				else {
					System.out.println("Error: Must be between 0 and 65535.");
				}
			}
		}
		System.out.println("Port accepted.\nInitializing server...");
		Server server = new Server(directoryPath, httpPort);
		System.out.println("Server initialized and running.");
		text = in.nextLine();
		while (text != null && !text.trim().equalsIgnoreCase("QUIT")) {
			// Do something useful / run
			text = in.nextLine();
		}
		while (!server.shutThingsDown());
		in.close();
		System.exit(0);
	}
}