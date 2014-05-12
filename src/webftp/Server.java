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

//TODO Add Post to JHTTP, make it parses the body for FTP server commands
//TODO Add FTP server commands.
//TODO Make change port function and start FTP server

//TODO Listen to HTTPServer admin page GET and call FTP methods
//TODO Create sub methods for handling FTP configuration commands:
	//TODO Check login credentials for FTP user
	//TODO If logged in, allow start of FTP on port, return port number
	//TODO If logged in, allow quit of FTP, return status

/*
 * Authors: Tony Knapp, Teagan Atwater, Jake Junda
 * Started: May 06, 2014 (Alpha)
 * Project: A simple HTTP and FTP server
 * Description: This HTTP/FTP server serves files via both protocols to different clients
 */
public class Server extends Thread {
	private static HTTPServer httpServer;
	private static EarlGray ftpServer;

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
		Server.httpServer = new HTTPServer(httpPort, directoryPath);
		System.out.println("HTTP initialized.");
		Server.httpServer.start();
		System.out.println("HTTP started.");
//		System.out.print("HTTP started.\nInitializing FTP... ");
//		this.ftpServer = new EarlGray(directoryPath);
//		System.out.println("FTP initialized.\nTo start FTP, use web service: 'localhost/admin/'.");
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
		while (!ftpServer.stopServer());
		while (!httpServer.stopServer());
		in.close();
		System.exit(0);
	}
}