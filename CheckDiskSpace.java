package Jvakt;

import java.net.InetAddress;
import java.util.Properties;
import java.io.*;
import java.util.*;

public class CheckDiskSpace {

	static String state = "a";
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;

	static String id;
	static BufferedReader in;
	static String aFile;

	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static String jvtype = "R";
	static int port ;
	static InetAddress inet;
	static String version = "CheckDiskSpace (2020-MAR-31)";
	static String agent = null;

	static String drive[] = new String[20];
	static String pct[] = new String[20];
	static String gig[] = new String[20];


	static String config = null;
	static File configF;
	static FileInputStream fis;

	static boolean swJvakt = false;

	public static void main(String[] args) throws IOException {

		int errors = 0;
		boolean swWarn = false;
		long totalSpace; //total disk space in bytes.
		long usableSpace; ///unallocated / free disk space in bytes.
		long freeSpace; //unallocated / free disk space in bytes.
		long freePct;                          //unallocated / free disk space in %.
		long totalSpaceG; //total disk space in bytes.
		long freeSpaceG; //unallocated / free disk space in bytes.

		if (args.length < 1) {
			System.out.println("\n\n"+version + " by Michael Ekdal Sweden.\n");

			System.out.println("\nThe parameters and their meaning are:\n"+
					"\n-jvakt  \tA switch to enable report to Jvakt. Default is no connection to Jvakt." +
					"\n-id     \tUsed as identifier in the Jvakt monitoring system." +
					"\n-config \tThe directory where to find the Jvakt.properties file. like \"-config c:\\Temp\". Optional. Default is the current directory."+
					"\n-drv    \tThree values is mandatory: drive min-percentage min-gigabyte. e.g -drv D: 10 150 -drv F: 12 120 " +
					"\n\nReturncode is 0 when OK else 12.  "
					);

			System.exit(4);
		}
		
		// reads command line arguments
		int j=0;
		for ( int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-id"))  id  = args[++i];
			if (args[i].equalsIgnoreCase("-jvakt")) swJvakt=true;
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-drv")) {
				drive[j] = args[i+1];
				pct[j] = args[i+2];
				gig[j] = args[i+3];
//				System.out.println("Drive "+drive[j]+" Pct "+pct[j]+" Gig "+gig[j]);
				i++; i++; j++;
			}
		}
		System.out.println("\n---"+ new Date()+" "+version + " by Michael Ekdal Sweden.\n");
//		System.out.println("number of drives found: "+j);

		if (swJvakt) {
						if (config == null ) 	configF = new File("Jvakt.properties");
						else 					configF = new File(config,"Jvakt.properties");
			System.out.println("---- Jvakt: "+new Date()+"  Version: "+version);
			System.out.println("-config file: "+configF);
			getProps();
		}

		for (int i=0; i<drive.length; i++) {
			if (drive[i]==null) break;
			swWarn = false;
			File file = new File(drive[i]);
			totalSpace = file.getTotalSpace(); //total disk space in bytes.
			usableSpace = file.getUsableSpace(); ///unallocated / free disk space in bytes.
			freeSpace = file.getFreeSpace(); //unallocated / free disk space in bytes.

			if (totalSpace==0) continue;      // Non existing drive probably

//			System.out.println(" === bytes "+drive[i]+" ===");
//			System.out.println("Total size : " + totalSpace + " bytes");
//			System.out.println("Space free : " + usableSpace + " bytes");
//			System.out.println("Space free : " + freeSpace + " bytes");

			System.out.println(" === "+drive[i]+" ===");
			totalSpaceG =totalSpace /1024 /1024 /1024;
			System.out.println("Total       size  : " + totalSpaceG + " GB");
//			System.out.println("User  space free  : " + usableSpace /1024 /1024 /1024 + " GB");
			freeSpaceG =freeSpace /1024 /1024 /1024;
			System.out.println("Total space free  : " + freeSpaceG + " GB");

			freePct = freeSpace * 100 / totalSpace ;
			System.out.println("Free space %      : " + freePct + " %");

			if (freePct <  Long.parseLong(pct[i] )) {
				t_desc = "Low on space "+drive[i] + " "+ freePct +" % is less than " + pct[i] + " %";
				System.out.println(t_desc);
				errors++; swWarn = true;
				sendSTS(swWarn);
			}
			if (freeSpaceG <  Long.parseLong(gig[i] )) {
				t_desc = "Low on space "+drive[i] + " "+ freeSpaceG +" GB is less than " + gig[i] + " GB";
				System.out.println(t_desc);
				errors++; swWarn = true;
				sendSTS(swWarn);
			}

		}



		if (!swWarn) {
			t_desc = "Disk space is within limits";
			sendSTS(swWarn);
			System.exit(0);
		}
		else             System.exit(12);
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) throws IOException {

		if (swJvakt) {
			System.out.println("\n--- " + id + "  --  " + t_desc);
			System.out.println("--- Connecting to "+jvhost+":"+jvport);
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(jvhost, port);
			System.out.println(jm.open()); 
			jmsg.setId(id);
			if (!STS) jmsg.setRptsts("OK");
			else jmsg.setRptsts("ERR");
			jmsg.setId(id);
			jmsg.setType("R");
			jmsg.setBody(t_desc);
			jmsg.setAgent(agent);
			if (jm.sendMsg(jmsg)) System.out.println("--- Rpt Delivered --  " + id + "  --  " + t_desc);
			else           		  System.out.println("--- Rpt Failed ---");
			jm.close();
		}
		else {
//			System.out.println("--- " + id + "  --  " + t_desc);
		}
	}

	static void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configF);
			prop.load(input);
			// get the property value and print it out
			jvport   = prop.getProperty("jvport");
			jvhost   = prop.getProperty("jvhost");
			port = Integer.parseInt(jvport);
			System.out.println("getProps jvport: " + jvport + "    jvhost: "+jvhost) ;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			inet = InetAddress.getLocalHost();
			System.out.println("-- Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

	}


}
