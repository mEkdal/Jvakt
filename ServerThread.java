package Jvakt;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

class ServerThread extends Thread {

	List list;
	Socket client;
	DBupdate dt;
	String version = "ServerThread 1.2 Date 2018-01-09";
	boolean swData;
	String line;
	Message jm = new Message();

	ServerThread(Socket client, DBupdate dt) { this.client = client; this.dt = dt; }

	public void run() {
		try {
			swData = false;
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter    ut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
			dt.getStatus();
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
			System.out.println("ServerThread exeption:>> " + client.getInetAddress() + " " + e1 ); 
			}
		if (swData) dt.dbWrite(jm);  // update DB
//		System.out.println("ServerThread Session:  DBupdate klar");
	}        
}