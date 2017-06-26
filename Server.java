/**
 * This is the core listener. 
 * It will communicate with the agents and update the DB. 
 * @author 		Michael Ekdal
 * @version 	2.0 alpha 1
 */
package Jvakt;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
	/**
	 * @param  		port 	The port the program will listen on.
	 * @throws		exeption
	 */
	public static void main(String[] args ) throws Exception  {

		String version = "Server 1.0 Date 2017-02-14_01";
		String jvport   = "1956";

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("jVakt.properties");
			prop.load(input);
			// get the property value and print it out
			jvport = prop.getProperty("jvport");
		} catch (IOException ex) {
			// ex.printStackTrace();
		}
		input.close();

		int port = Integer.parseInt(jvport);
		ServerSocket ss = new ServerSocket(port);
		DBupdate dt = new DBupdate( );
		while( true ) {
			try {
				Socket client = ss.accept();
				ServerThread t = new ServerThread(client, dt);
				t.start(); 
			}
			catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
