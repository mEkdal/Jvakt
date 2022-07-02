package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

/*
2021-01-13 V1.0 Ekdal: Monitors a msgq and auto response is possible. 
 */

import java.util.*;
import com.ibm.as400.access.*;

public class MonAS400msgq  {
	static Date	now;
	static AS400 as400;
	static String config = null;
	static File configF;


	//	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {

		now = new Date();
		String version = getVersion()+".54";

		// Displays help
		if (args.length == 0) {
			System.out.println("\n*** MonAS400msgq "+version+" ***" +
					"\n*** by Michael Ekdal Perstorp Sweden. ***");
			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-q       \tThe name of the AS400 message queue, like \" /qsys.lib/itoctools.lib/xxx.msgq\" "+
					"\n-ah      \tHostname of the AS400 server."+
					"\n-us      \tThe AS400 user."+
					"\n-pw      \tThe AS400 password."+
					"\n-config  \tThe dir of the input files. Like: \"-config c:\\Temp\" "+
					"\n-jh      \tHostname of the Jvakt server."+
					"\n-jp      \tThe port of the Jvakt server."+
					"\n-dormant \tAll reports will be forced to be 30 or higher. No autoreply will be made.");

			System.exit(12);
		}

		String msgq = null;
		String ashost = null;
		String asuser = null;
		String aspw = null;
		String agent = "";
		InetAddress inet;

		String jvhost = "127.0.0.1";
		String jport = "1956";
		int jvport = 1956;

		String msg;
		String msgL;
		String rpy;
		String jobName;
		String msgId;
		int severity;
		String[] words;
		String s = "";
		boolean swAutoreply = false;
		boolean swDormant = false;
		int sev;
		String sts;
		//		Enumeration<QueuedMessage> q = null;
		byte [] msgkey = null;

		// reads command line arguments
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-q")) msgq = args[++i];
			if (args[i].equalsIgnoreCase("-ah")) ashost = args[++i];
			if (args[i].equalsIgnoreCase("-us")) asuser = args[++i];
			if (args[i].equalsIgnoreCase("-pw")) aspw = args[++i];
			if (args[i].equalsIgnoreCase("-jh")) jvhost = args[++i];
			if (args[i].equalsIgnoreCase("-jp")) jport = args[++i];
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-dormant")) swDormant = true;

		}
		// checks the mandatory parameters
		if (ashost == null) {
			System.out.println("\n\n\t ***  -ah  is missing (host)");
			System.exit(8);
		}
		if (msgq == null ) {
			System.out.println("\n\n\t *** -q is missing (msgq)");
			System.exit(8);
		} 

		// Read filter file
		if (config == null ) {
			configF = new File("MonAS400msgq.csv");
		}
		else {
			//			System.out.println("config is "+config);
			configF = new File(config,"MonAS400msgq.csv");
		}

		System.out.println("--- Jvakt MonAS400msgq ("+version+") --- "+now+" ---");

		// Importing filer file.
		BufferedReader inokay;
		inokay = new BufferedReader(new FileReader(configF));
		int ecount = 0;
		String[] Ftab = new String[1000];
		while((s = inokay.readLine())!= null) {
			if (s.startsWith("#")) continue;
			if (s.length() == 0) continue; 
			Ftab[ecount++] = s.toUpperCase();
			//			System.out.println( Ftab[ecount - 1]);
		}          
		inokay.close();
		System.out.println(" Imported "+ecount+" rows from "+configF);
		if (swDormant) System.out.println(" Dormant mode on");
		//		System.out.println( " count: " + ecount);

		try {
			inet = InetAddress.getLocalHost();
			//			System.out.println(" Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

		jvport = Integer.parseInt(jport);

		if (args.length == 0) System.exit(4);

		// As400
		System.out.println(" AS400 connecting to host " + ashost);
		as400    = new AS400(ashost,asuser,aspw); 
		System.out.println(" Connected to system " + as400.getSystemName());
		//		System.out.println(" CCSID " + as400.getCcsid());

		System.out.println(" Connecting to Msgq " + msgq );
		MessageQueue queue = new MessageQueue(as400, msgq); 
		System.out.println(" Connected  to Msgq " + queue.getPath());

		QueuedMessage msge = null;

		// Starts a never ending loop
		for (;;) {
			//			System.out.println("Queue length: "+queue.getLength());
			if (swAutoreply) {
				try { Thread.sleep(60000); } catch (InterruptedException e) { e.printStackTrace();}
				swAutoreply = false;
			}
			try { msge = queue.receive(null,0,queue.OLD,queue.ANY); } catch( Exception e ) { e.printStackTrace();}
			if (msge == null) {
				try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace();}
				continue;
			}
			if (swDormant) {
				sev = 51;
				sts = "INFO";
			} 
			else {
				sev = 1;
				sts = "ERR";
			}
			rpy = "";
			swAutoreply = false;
			msgkey = msge.getKey();
			msg = msge.getText();
			jobName = msge.getFromJobName();
			severity = msge.getSeverity();
			msgId = msge.getID();
			//			System.out.println("GetType "+msge.getType());
			if (msgId.length()<1)  { System.out.println("msgId empty"); continue; }
			//			System.out.println("* Msge: "+msgId + " "+severity+ " " + jobName + " - " + msg );

			msgL = jobName+" "+msgId+" "+msg;

			for ( int k = 0; k < ecount ; k++) {  // check if any scan string is present in the line.
				words = Ftab[k].split(";",4);

				//			System.out.println("Split length "+ words.length +" "+ words[0] + " "+words[1]+ " " + words[2] + " " + words[3] );

				if (msgL.toUpperCase().indexOf(words[0]) >= 0) { 
					if (words[1].startsWith("X")) {
						sev = -1;
						sts = "X";
					}
					else {
						sev = Integer.parseInt(words[1]);
						sts = words[2];
					}
					if (words.length>3) rpy = words[3];
					else rpy = "";

					System.out.println(new Date()+" - id:" + msgId + " sev:"+sev+" sts:"+sts+ " job:" + jobName + " msg:" + msg + " * String: " + words[0] );

					if (!swDormant && msge.getType() == msge.INQUIRY && rpy.length()>0) {
						try { queue.reply(msgkey, rpy, false); } catch( Exception e ) { e.printStackTrace();}
						msg="Autoreply "+rpy+" -> "+msg;
						swAutoreply = true;
						break;
					}

				}
			}

			if (sev < 0 ) continue; // no Jvakt update. continue with the next message.


			try {
				Message jmsg = new Message();
				SendMsg jm = new SendMsg(jvhost, jvport);
				String openResponse = jm.open();
				System.out.println("Open connection to Jvakt server "+jvhost+":"+jvport+" Response: "+openResponse);
				while (openResponse.startsWith("failed")) {
					System.out.println("Connection to Jvakt server "+jvhost+":"+jvport+" failed");
					try { Thread.sleep(60000); } catch (InterruptedException e) { e.printStackTrace();}
					openResponse = jm.open();
				}

				jmsg.setId(ashost+"-"+jobName+"-"+msgId);
				jmsg.setPrio(sev);
				jmsg.setRptsts(sts);
				jmsg.setBody(msg);

				jmsg.setType("I");

				jmsg.setAgent(agent);
				System.out.println(" Jvakt-> "+jmsg.getType() +" "+ jmsg.getId() +" "+ jmsg.getPrio() +" "+ jmsg.getRptsts() +" - "+ jmsg.getBody() +" "+ jmsg.getAgent() );
				if (jm.sendMsg(jmsg)) { System.out.println("-- Rpt Delivered --"); }
				else            	  { System.out.println("-- Rpt Failed --");    }
				jm.close();
				System.out.println("-- Read next entry in the message queue --");
			}
			catch( Exception e ) { e.printStackTrace(); System.exit(12);}
		}

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

