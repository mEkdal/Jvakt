/**
 * This is class receives the Message and updates the DB via the inctance DPUpdate . 
 * It will communicate with the agents and update the DB. 
 * @author 		Michael Ekdal
 * @version 	2021-05-11
 */

package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.io.*;
import java.net.*;

class ServerThread extends Thread {
	/**
	 * The class is started in a new thread by the Server class.
	 * It receives the Message object via the session and sends it to DBupdate to update the DB.
	 */

	Socket client;
	DBupdate dt;
	String version = "ServerThread 2.4.54";
	boolean swData;
	String line;
	Message jm = new Message();

	ServerThread(Socket client, DBupdate dt) { this.client = client; this.dt = dt; }

	public void run() {
		try {
			swData = false;
			client.setSoTimeout(15000);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter    ut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
//			dt.getStatus();
			ut.println(dt.getStatus() + " " +version ); 
			ut.flush();
//			String line;
//			Message jm = new Message();
			while((line = in.readLine()) != null ) {
				swData = false;
				if (line.length() == 0) break;
				if (line.startsWith("SendMsg")) continue ;
				String[] tab = line.split("<;>");
//				System.out.println("ServerThread #1: " + tab.length +"   " + client.getInetAddress() );				
				if (tab.length < 6) break; 
				swData = true;
				
				jm.setType(tab[0]);
				jm.setId(tab[1]);
				jm.setRptsts(tab[2]);
				jm.setBody(tab[3]);
				jm.setAgent(tab[4]);
				jm.setPrio(Integer.parseInt(tab[5]));

//				System.out.println("ServerThread #1: " + client.getInetAddress() + " " + jm.getType() + " " + jm.getId() + " " +jm.getRptsts() + " " + jm.getBody() + " " +jm.getAgent() + " " +jm.getPrio());
//				dt.dbWrite(jm);  // update DB
//				System.out.println("ServerThread #2: After dbWrite");
				
				ut.println("okay " );
				ut.flush();
				break;
			}
//			ut.println(version+" Disconnecting Session "); 
//			ut.flush();
			in.close();
			ut.close();
			client.close();
//			System.out.println("ServerThread #2: " + client.getInetAddress() + " " + jm.getType() + " " + jm.getId() + " " +jm.getRptsts() + " " + jm.getBody() + " " +jm.getAgent() + " " +jm.getPrio());
//			if (swData) dt.dbWrite(jm);  // update DB
		}
		catch (IOException e1) { 
//			System.out.println("ServerThread exception:>> " + client.getInetAddress() + " " + e1 ); 
			}
		if (swData) dt.dbWrite(jm);  // update DB
//		System.out.println("ServerThread Session:  DBupdate done!");
	}        
}