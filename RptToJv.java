package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.io.*;
import java.net.*;
import java.util.Properties;

public class RptToJv {
//	public static void main(String[] args ) throws IOException, UnknownHostException {
	public static void main(String[] args )  {

		String version = "RptToJv ";
		version += getVersion()+".54";
		String host = "127.0.0.1";
		int port = 1956; 
		String id = null;
		String status = "OK";
		String body = " ";
		String agent = " ";
		String type = "R";  // repeating
		String prio = "30";  
		String reply = "";  
		InetAddress inet;

		String jvport   = "1956";
		String jvhost   = "127.0.0.1";

		String config = null;
		File configF;
		boolean swTest = false;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("Jvakt: "+version);
		System.out.println("-config file Server: "+configF);

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configF);
			prop.load(input);
			// get the property value and print it out
			jvhost = prop.getProperty("jvhost");
			jvport = prop.getProperty("jvport");
		} catch (IOException ex) {
			System.out.println("Jvakt.properties not found, continues...");
			//			 ex.printStackTrace();
		}
		port = Integer.parseInt(jvport);
		host = jvhost;

		try {
			inet = InetAddress.getLocalHost();
			System.out.println("-- Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-host")) host = args[++i];
			else if (args[i].equalsIgnoreCase("-port")) port = Integer.parseInt(args[++i]);
			else if (args[i].equalsIgnoreCase("-id")) id = args[++i];
			else if (args[i].equalsIgnoreCase("-sts")) status = args[++i];
			else if (args[i].equalsIgnoreCase("-body")) body = args[++i];
			else if (args[i].equalsIgnoreCase("-type")) type = args[++i];
			else if (args[i].equalsIgnoreCase("-ok")) status = "OK";
			else if (args[i].equalsIgnoreCase("-err")) status = "ERR";
			else if (args[i].equalsIgnoreCase("-info")) status = "INFO";
			else if (args[i].equalsIgnoreCase("-prio")) prio = args[++i];
			else if (args[i].equalsIgnoreCase("-test")) swTest = true;
		}

		if (args.length < 1 || (id == null && !swTest)) {
			System.out.println("\n"+version);
			System.out.println("by Michael Ekdal Perstorp Sweden.\n");
			System.out.println("-host \t - default is 127.0.0.1");
			System.out.println("-port \t - default is 1956");
			System.out.println("-id  ");
			System.out.println("-ok   \t -> sts=OK");
			System.out.println("-err  \t -> sts=ERR");
			System.out.println("-info \t -> sts=INFO");
			System.out.println("-sts  \t - default is OK");
			System.out.println("-body \t - Any descriptive text");
			System.out.println("-prio \t - default is 30");
			System.out.println("-type \t - R, S, T, I, D");
			System.out.println("-test \t - test the connection without updating the server side status.");
			System.exit(4);
		}

		if (id == null && !swTest) {
			System.out.println(">>> Failure! The -id switch must contain a value! <<<");
			System.exit(8);
		}
		if (!type.toUpperCase().equals("T") && !type.toUpperCase().equals("R") && !type.toUpperCase().equals("I") && !type.toUpperCase().equals("S") && !type.toUpperCase().equals("D") && !type.toUpperCase().equals("P") && !type.equalsIgnoreCase("Active") && !type.equalsIgnoreCase("Dormant")  ) {
			System.out.println(">>> Failure! The type must be R, I, S, T or D <<<");
			System.exit(8);
		}

		if (status.equals("INFO") && type.toUpperCase().equals("R") ) {
			type = "I";
		}


		//	 System.out.println(args[0]+" - "+args[1]);
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(host, port);
		try {
			reply = jm.open();
			System.out.println("Status: open "+reply);
			if (!reply.equalsIgnoreCase("failed") && !swTest) {
				jmsg.setId(id);
				jmsg.setRptsts(status);
				jmsg.setBody(body);
				jmsg.setType(type);
				jmsg.setAgent(agent);
				jmsg.setPrio( Integer.parseInt(prio) );
				//		jm.sendMsg(jmsg);
				if (jm.sendMsg(jmsg)) System.out.println("-- Rpt Delivered --");
				else            	  System.out.println("-- Rpt Failed --");
				jm.close();
			}
			else {
				if (reply.equalsIgnoreCase("failed")) System.out.println("-- Rpt Failed --");
				else System.out.println("-- Test to Jvakt server succeeded - "+reply);
			}
		}
//		catch (java.net.ConnectException e ) {System.out.println("-- Rpt Failed --" + e); }
		catch (NullPointerException npe2 )   {System.out.println("-- Rpt Failed --" + npe2);}

	}     

	static private String getVersion() {
		String version = "0";
		try { 
			Class<?> c1 = Class.forName("Jvakt.Version",false,ClassLoader.getSystemClassLoader());
			Version ver = new Version();
			version = ver.getVersion();
 		} 
		catch (java.lang.ClassNotFoundException ex) {
			version = "?";
		}
		return version;
	}


}