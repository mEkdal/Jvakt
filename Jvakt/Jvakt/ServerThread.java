package Jvakt;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

class ServerThread extends Thread {

 List list;
 Socket client;
 int sessnum;
 DBupdate dt;
 String version = "ServerThread 1.0 Date 2017-02-01_01";
 
 ServerThread(Socket client, int sessnum, DBupdate dt) { this.client = client; this.sessnum = sessnum; this.dt = dt; }
 
 public void run() {
  try {
          BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
          PrintWriter    ut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
//          System.out.println(version + " - " + sessnum);
          ut.println(version + " Session: " + sessnum); 
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
                  
                  ut.println("okay " + sessnum);
                  ut.flush();
//                  dt.dbWrite(jm);
          }
//        System.out.println("ServerThread Disconnecting Session: " + sessnum);
          ut.println(version+" Disconnecting Session: " + sessnum); 
          ut.flush();
          in.close();
          ut.close();
          client.close();
//          try {
//        	  System.out.println("DBupdate Session: " + sessnum);
//     DBupdate dt = new DBupdate( sessnum );
        	  dt.dbWrite(jm);  // update DB
        	  System.out.println("ServerThread Session: " + sessnum + " DBupdate klar");
//          }
  //        catch (SQLException e) {
    //    	  System.err.println("ST SQLerror session " + sessnum + "  " + e.getMessage());
      //    }
          
//          java.lang.System.exit(0);
  }
  catch (IOException e1) { System.out.println("ServerThread error Session: " + sessnum); System.out.println(e1); }
//  catch (IOException e1) { System.out.println(e1); java.lang.System.exit(12); }
// catch (SQLException e2) { System.out.println(e2); java.lang.System.exit(12); }
 }        
}