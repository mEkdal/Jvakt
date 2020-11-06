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

	@SuppressWarnings("unchecked")
	public static void main(String[] args)  {

		// Displays help
		if (args.length == 0) {
			System.out.println("\n*** DW2Jvakt 2020-10-24 ***" +
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
//		String[] allwords;
		boolean swLoop = false;
		boolean swSyslogOK = false;
		boolean swInteresting = true;
		boolean swPurge = false;
		int sev;
		String sts;
		Enumeration<QueuedMessage> q = null;
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
				QueuedMessage msge = q.nextElement();
				msgkey = msge.getKey();
				msg = msge.getText();
				System.out.println(msg + " Msgkey: " + msgkey);
				words = msg.split(" ",2);
//				allwords = msg.split(" ");
				swInteresting = true;
				swSyslogOK = true;
				swPurge = false;

				if (words[0].startsWith("DWPURGE")) {
					swPurge = true;
					msg = words[1];
//					msg = msg.substring(0, msg.length()-1); // ta bort sista tecknet som är en punkt.
					words = msg.split(" ",2);
				}
				// not interesting
//				if (words[0].startsWith("SYSSTS")) swInteresting = false;
				if (msg.contains(" MonIpPort_"))   swInteresting = false;
				if (msg.contains(" MonHttpText_"))   swInteresting = false;
				if (msg.contains(" MonDBOra"))   swInteresting = false;
				if (msg.contains("Wrn: AZU30"))   swInteresting = false;
				if (msg.contains("CPIEF"))   swInteresting = false;  // IBM Service agent
				if (msg.contains("CPFEF"))   swInteresting = false;  // IBM Service agent
				//				if (msg.contains(" MonIpAddr_"))   swInteresting = false;

				// info  
				sev = 5; 
				sts = "ERR";
				if (swInteresting) {
					System.out.println("-- Interesting...");
					// INFO 30
					if (msg.contains("DAX ")|| msg.contains("ITO0206") || msg.contains("CPI0973") || msg.contains("backup OK") || 
							msg.contains("is mounted") || msg.contains("LOG0010") || msg.contains("SYSSTS:") ||   
							msg.contains("TBM1205") || msg.contains("ITO9806") || msg.contains("CPF1251") || msg.contains("ITO9906") ||
							msg.contains("CPD2706") ||
							msg.contains("Next tape is") || msg.contains("ITO1206") || msg.contains("EDH30") || msg.contains("CPF1393") ||  
							msg.contains("CPF1817")  || msg.contains("CPI0973")    ||
							msg.contains("backup OK") || msg.contains("running okay") || msg.contains("is mounted")) { 
						sev = 30; sts = "INFO"; } 
					// OK 30
//					if (msg.contains("CPF1817")  || msg.contains("CPI0973")    ||
//							msg.contains("backup OK") || msg.contains("running okay") || msg.contains("is mounted") ) {
//						sev = 30; sts = "OK"; } 
					// ERR 30 
					if (msg.contains("Err:") || msg.contains("EDH18") || msg.contains("Wrn:") || 
							msg.contains("not ready.") || msg.contains("RNQ") || msg.contains("CPA4278")    || msg.contains("TBM1202") || msg.contains("TBM1203") ||
							msg.contains("QAIMPS2") || msg.contains("backup ERR")|| msg.contains("BU ERR") || msg.contains("MSGW ") || 
							msg.contains("CPA5305")    || msg.contains("FTP0100")  || msg.contains("APP020") || msg.contains("RPG0121") || msg.contains("RPG0202") ||
							msg.contains("CHKJOBSTS")  || msg.contains("CPF090") || msg.contains("CPA3387") ||  msg.contains("ITO0906") ||
							msg.contains("MONOUTQ01")  || words[0].contains("CHKWTRS1") || msg.contains("CPI59B2") || msg.contains("CPD27CE") || msg.contains("CPD2643") ||
							msg.contains("CPA4072")    || msg.contains("CPA0701") || msg.contains("CPF0909") || msg.contains("CPF0908") || msg.contains("CPFAF98") ||
							msg.contains("CPA57E9") || msg.contains("CPF2697") || msg.contains("CPA0702") ||
							msg.contains("CPIEF07") || msg.contains("CPIEF02") || msg.contains("CPP6307") || msg.contains("CPIEF09") ||  
							 msg.contains("CPIEF03") || msg.contains("TCP8500") || msg.contains("CPFEF") || msg.contains("CPI93B9") ||
							(msg.contains("ITO0206") & msg.toLowerCase().contains("warning")) ||
							(msg.contains("ITO0206") & msg.toLowerCase().contains("error")) 
							) { 
						sev = 30; sts = "ERR"; }
					// ERR 20
					if (msg.contains("ITO0102") || msg.contains("ITO0202")  || msg.contains("SAP0902") ||  msg.contains("ITO0902") || 
							msg.contains("CPI0964") || msg.contains("CPF1816") || msg.contains("CPF1338") || 
							msg.contains("ITO9806 PPSE08 MSGW")
							)	{ 
						sev = 20; sts = "ERR"; } 
					// ERR 10
					if (
						(msg.contains("QSYSCOMM") || msg.contains("QSYSARB") &&
						(!msg.contains("CPF0909") && !msg.contains("CPF0908") && !msg.contains("CPI59B2") && !msg.contains("CPD27CE")))
						|| ( msg.contains("APP0203") && !msg.contains("NOT successfully")) 
						) {
						sev = 10; sts = "ERR"; } 

					// INFO 30 again
					if (msg.contains("Autoanswer:") ||
						msg.contains("ITO9806 PPSE08 MSGW in QINTER jobb MOE202") ||  
						msg.contains("ITO9806 PPSE08 MSGW in GRP100BCH jobb MRU000XJ") ||
						msg.contains("ITO9806 PPSE08 MSGW in GRP280BCH jobb MIU160XJ") ||
						msg.contains("ITO9806 PPSE08 MSGW in GRP280BCH jobb MOR500XJ") 
						)
					{ 
						sev = 30; sts = "INFO"; } 

					try {
						//	 System.out.println(args[0]+" - "+args[1]);
						Message jmsg = new Message();
						SendMsg jm = new SendMsg(jvhost, jvport);
						System.out.println(jm.open());
//						if (swPurge) jmsg.setId("AS400-");
//						else jmsg.setId("AS400-"+words[0]);
						jmsg.setId("AS400-"+words[0]);
						jmsg.setPrio(sev);
						jmsg.setRptsts(sts);
						jmsg.setBody(words[1]);

						if (swPurge) {
							jmsg.setType("D");
							jmsg.setRptsts("OK");
						}
						else jmsg.setType("I");
						
						jmsg.setAgent(agent);
//						jm.sendMsg(jmsg);
						if (jm.sendMsg(jmsg)) { System.out.println("-- Rpt Delivered --"); swSyslogOK = true; }
						else            	  { System.out.println("-- Rpt Failed --");    swSyslogOK = false; }
						//					try { Thread.currentThread().sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
						jm.close();
						System.out.println("-- Read next from queue --");
					}
					catch( Exception e ) { e.printStackTrace(); swSyslogOK = false;}
				}

				if (swSyslogOK) {
					System.out.println("Remove: " + sev + " - " + msg );
					try { queue.remove(msgkey); } catch( Exception e ) { e.printStackTrace();}
				}
			}       

			try { queue.close(); } catch( Exception e ) { e.printStackTrace();}

			// sleep for two second and then do it all over again.
			if (!swLoop) break;
//			try { Thread.currentThread().sleep(2000); } catch (InterruptedException e) { e.printStackTrace();}
			try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace();}
		}
		System.out.println("-- Finished --");
	}
}
