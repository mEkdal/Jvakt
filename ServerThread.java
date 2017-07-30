package Jvakt;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

class ServerThread extends Thread {

	List list;
	Socket client;
	DBupdate dt;
	String version = "ServerThread 1.1 Date 2017-07-20";

	ServerThread(Socket client, DBupdate dt) { this.client = client; this.dt = dt; }

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter    ut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
			dt.getStatus();
			ut.println(dt.getStatus() + " " +version ); 
			ut.flush();
			String line;
			Message jm = new Message();
			while((line = in.readLine()) != null ) {
				if (line.length() == 0) break;
				if (line.startsWith("SendMsg")) continue ;
				String[] tab = line.split("<;>");

				jm.setType(tab[0]);
				jm.setId(tab[1]);
				jm.setRptsts(tab[2]);
				jm.setBody(tab[3]);
				jm.setAgent(tab[4]);
				jm.setPrio(Integer.parseInt(tab[5]));

				ut.println("okay " );
				ut.flush();
			}
			ut.println(version+" Disconnecting Session "); 
			ut.flush();
			in.close();
			ut.close();
			client.close();
			dt.dbWrite(jm);  // update DB
//			System.out.println("ServerThread Session:  DBupdate klar");
		}
		catch (IOException e1) { 
//			System.out.println("ServerThread error Session: "); 
//			System.out.println(e1); 
			}
	}        
}