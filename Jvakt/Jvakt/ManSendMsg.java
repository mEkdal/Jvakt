package Jvakt;
import java.io.*;
import java.net.*;

public class ManSendMsg {
 public static void main(String[] args ) throws IOException, UnknownHostException {
	 System.out.println(args[0]+" - "+args[1]);
	 	String line = "go";
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        Message jmsg = new Message();
        System.out.println(host+" - "+port);
        SendMsg jm = new SendMsg(host, port);
        System.out.println(jm.open());
//        while( !line.equals("exit") ) {
        System.out.println("type:");
        line = console.readLine();
        jmsg.setType(line);
        System.out.println("id1:");
        line = console.readLine();
        jmsg.setId1(line);
        System.out.println("id2:");
        line = console.readLine();
        jmsg.setId2(line);
        System.out.println("id3:");
        line = console.readLine();
        jmsg.setId3(line);
        System.out.println("status:");
        line = console.readLine();
        jmsg.setRptsts(line);
        System.out.println("body:");
        line = console.readLine();
        jmsg.setBody(line);
        jm.sendMsg(jmsg);
  //              if (!jm.sendMsg(jmsg)) break;
  //      }
        jm.close();
        System.out.println("-- SLUT --");
        
 }        
}