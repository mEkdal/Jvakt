package Jvakt;


import java.net.InetAddress;

/*
2017-07-18 V1.0 Ekdal: Sends entries in a msgq to Jvakt. 
 */

import java.util.*;
import com.ibm.as400.access.*;

public class DW2Jvakt  {
	static Date	now;
	static AS400 ppse08    = new AS400("ppse08.perscorp.com","mesenger","notify");

	public static void main(String[] args)  {

		// Displays help
		if (args.length == 0) {
			System.out.println("\n*** DW2Jvakt 1.0 Date 2017-07-18_1 ***" +
					"\n*** by Michael Ekdal Perstorp Sweden. ***");
			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-q \tThe name of the AS400 message queue, like \" /qsys.lib/itoctools.lib/xxx.msgq\" "+
					"\n-ah \tHostname of the AS400 server."+
					"\n-ap \tThe port of the AS400 server."+
					"\n-jh \tHostname of the Jvakt server."+
					"\n-jp \tThe port of the Jvakt server.");

			System.exit(12);
		}

		String msgq = null;
		String ashost = null;
		String agent = "PPSE08";
		InetAddress inet;

		//		String asport = null;

		String jvhost = "127.0.0.1";
		String jport = "1956";
		int jvport = 1956;

		String msg;
		String[] words;
		boolean swLoop = false;
		boolean swSyslogOK = false;
		boolean swInteresting = true;
		int sev;
		Enumeration q = null;
		byte [] msgkey = null;
		now = new Date();

		// reads command line arguments
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-q")) msgq = args[++i];
			if (args[i].equalsIgnoreCase("-ah")) ashost = args[++i];
			if (args[i].equalsIgnoreCase("-jh")) jvhost = args[++i];
			if (args[i].equalsIgnoreCase("-jp")) jport = args[++i];
		}
		// checks the mandatory parameters
		if (ashost == null) {
			System.out.println("\n\n\t ***  -ah  is missing (host)");
			System.exit(8);
		}
		//		if (asport == null) {
		//			asport = "514";
		//		}
		if (msgq == null ) {
			System.out.println("\n\n\t *** -q is missing (msgq)");
			System.exit(8);
		} 

		try {
			inet = InetAddress.getLocalHost();
			System.out.println("-- Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

		jvport = Integer.parseInt(jport);

		if (args.length == 0) System.exit(4);

		// As400
		MessageQueue queue = new MessageQueue(ppse08, msgq);

		// Starts an never ending loop
		for (;;) {

			try { q = queue.getMessages(); } catch( Exception e ) { e.printStackTrace();}
			while (q.hasMoreElements()) {
				QueuedMessage msge = (QueuedMessage)q.nextElement();
				msgkey = msge.getKey();
				msg = msge.getText();
				System.out.println(msg +  msgkey);
				words = msg.split(" ",2);
				swInteresting = true;
				swSyslogOK = true;

				// not interesting
				if (words[0].startsWith("SYSSTS")) swInteresting = false;
				if (msg.contains(" MonIpPort_"))   swInteresting = false;
				if (msg.contains(" MonHttpText_"))   swInteresting = false;
//				if (msg.contains(" MonIpAddr_"))   swInteresting = false;

				// info
				sev = 4; 
				if (swInteresting) {
					//error
					if (msg.contains("Err:") || msg.contains("EDH18") || msg.contains("Wrn:")) sev = 3; 
					if (msg.contains("not ready.")) sev = 3; 
					if (msg.contains("MSGW ") || msg.contains("QAIMPS2") || msg.contains("backup ERR")|| msg.contains("BU ERR")) sev = 3; 
					if (msg.contains("CPA5305") || msg.contains("FTP0100")  || msg.contains("CPF090")|| msg.contains("APP020")) sev = 3; 
					if (msg.contains("CHKJOBSTS") || msg.contains("CPF090") || msg.contains("APP020")) sev = 3; 
					if (msg.contains("QSYSCOMM") || msg.contains("QSYSARB")|| msg.contains("APP020")) sev = 1; 
					if (msg.contains("ITO0102") || msg.contains("ITO0202")  || msg.contains("SAP0902") ||  msg.contains("ITO0902") ) sev = 2;
					if (msg.contains("RNQ") || msg.contains("CHK0001") || msg.contains("LOG0010") ) sev = 2;
					if (msg.contains("CPI0964") || msg.contains("CPF1816") ) sev = 2; //UPS problem
					// OK
					if (msg.contains("CPF1817") || msg.contains("CPI0973") ) sev = 5; //UPS better
					if (msg.contains("backup OK") || msg.contains("is mounted") || msg.contains("Next tape is") ) sev = 5; 

					try {
						//	 System.out.println(args[0]+" - "+args[1]);
						Message jmsg = new Message();
						SendMsg jm = new SendMsg(jvhost, jvport);
						System.out.println(jm.open());
						jmsg.setId("AS400-"+words[0]);
						if (sev < 4)       jmsg.setRptsts("ERR");
						else if (sev == 4) jmsg.setRptsts("INFO");
						else 		   jmsg.setRptsts("OK");
						jmsg.setBody(words[1]);
						jmsg.setType("I");
						jmsg.setAgent(agent);
						jm.sendMsg(jmsg);
						if (jm.close()) { System.out.println("-- Rpt Delivered --"); swSyslogOK = true; }
						else            { System.out.println("-- Rpt Failed --");    swSyslogOK = false; }
						//					try { Thread.currentThread().sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
						System.out.println("-- Read next from queue --");
					}
					catch( Exception e ) { e.printStackTrace();}
				}

				if (swSyslogOK) {
					System.out.println(sev + " - " + msg );
					try { queue.remove(msgkey); } catch( Exception e ) { e.printStackTrace();}
				}
			}       

			try { queue.close(); } catch( Exception e ) { e.printStackTrace();}

			// sleep for two second and then do it all over again.
			if (!swLoop) break;
			try { Thread.currentThread().sleep(2000); } catch (InterruptedException e) { e.printStackTrace();}
		}
		System.out.println("-- Finished --");
	}
}
