package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import jakarta.mail.*;
import jakarta.mail.search.*;
//import javax.mail.*;
//import javax.mail.search.*;
import Jvakt.Message;
import java.util.*;
import java.io.*;
import java.net.InetAddress;
  
public class GetImap4Msg {

	//	static boolean newMsgId = false;
	static boolean msgFixat = false;
	static boolean swHtml = false;
	static boolean swHelp = false;
	static String msgId;
	static boolean swSunet = false;

	static boolean swSerious = false;

	static boolean swSmq1PXP = false;
	static boolean swSmq2PCP = false;
	static boolean swUnomaly = false;
	static int Sm58PCPerr = 0;

	static String from;
	static String subject;
	static String body;
	static int antal;
	static int detached;
	static String agent = null;
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static String uname;
	static String pwd;
	static String imaphost;
	static String imapport;
	static String imapssl;
	static String imaprw;
	static String imapFolder;
	static String config = null;
	static File configF;

	static InetAddress inet;

	public static void main(String[] args) throws IOException, FileNotFoundException {

		String version = "GetImap4Msg ";
		version += getVersion()+".54";

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-help")) swHelp = true;
			if (args[i].equalsIgnoreCase("-?")) swHelp = true;
		}

		if (swHelp) {
			System.out.println("--- GetImap4Msg --- ");
			System.out.println("by Michael Ekdal Perstorp Sweden.\n");
			System.out.println("Analyses mail directed to a mailbox folder");
			System.exit(4);
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version);
		System.out.println("-config file: "+configF);

		Properties props = new Properties();

		String provider = "imap";  // plain
		int    imapPort = 993; // secure
		//		int    imapPort = 143; // plain


		Address[] adr;
		//		String[] hdr;
		//		String value;
		//		String c;
		//		boolean swFinns;

		try {

			getProps();  // get Jvakt properties
			imapPort = Integer.parseInt(imapport);
			// Connect to the imap4 server
			if (imapssl.startsWith("N")) {
				props.put("mail.imap.ssl.enable", "false");  // plain
				provider = "imap"; 
			}
			else {
				props.put("mail.imap.ssl.enable", "true");   // secure
				provider = "imaps"; 
			}

			props.put("mail.imap.ssl.trust", "*");
			props.put("mail.imaps.ssl.trust", "*");

			System.out.println("Initiating connection to imap4 server....");
			System.out.println("Connection... " + imaphost+":"+imapport+"  User: "+uname);
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore(provider);
			//store.connect(host, username, password);
			try {
				store.connect(imaphost, imapPort, uname, pwd);
			} catch (Exception e) {
				System.out.println("Connection to " + imaphost+":"+imapport+"  User "+uname+"  Pass "+pwd+" failed!");
				System.out.println(e);
				System.exit(8);
			}
			System.out.println("Connection to imap4 server established.");

			// Open the folder
			System.out.println("Open folder: " + imapFolder);
			Folder inbox = store.getFolder(imapFolder);
			if (inbox == null) {
				System.out.println("Mailbox folder "+imapFolder+" not found!");
				System.exit(1);
			}

			if (imaprw.startsWith("Y")) inbox.open(Folder.READ_WRITE);  // remove the marked messages from the server
			else 						inbox.open(Folder.READ_ONLY);

			// Get the messages from the server
			// Fetch all messages from inbox folder
			//			javax.mail.Message[] messages = inbox.getMessages();
			// Fetch unseen messages from inbox folder
			jakarta.mail.Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
//			javax.mail.Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

			//	        System.out.println("\n------------ NewMessageCount " + inbox.getNewMessageCount() + " ------------");
			//	        System.out.println("------------ UnreadMessageCount " + inbox.getUnreadMessageCount() + " ------------");

			System.out.println("Number of mails: " +messages.length);

			for (int i = 0; i < messages.length; i++) {
				subject = messages[i].getSubject();
				if (subject == null) subject = "null.";
				adr = messages[i].getFrom(); 
				from = null;
				if (adr != null) {
					System.out.println("adr null ");
					for (int j = 0; j < adr.length; j++) {
						from = adr[j].toString();
					}
				} else {
					from = "unknown";
				}
				System.out.println("From: " + from );
				//	        if (from == null) subject = "null.";  2013-05-23
				if (from == null) from = "null.";
				System.out.println("MimeType> " + messages[i].getContentType());
				System.out.println("Subject: " + subject);
				if (from.contains("postmaster@internal.perscorp.com")) continue;
				body = null;
				swHtml = false;
				Object o = null;
				//				if (messages[i].isMimeType("text/plain") || messages[i].isMimeType("text/html") || messages[i].isMimeType("multipart/alternative")  || messages[i].isMimeType("multipart/mixed")  ) {
				if (messages[i].isMimeType("text/plain") || messages[i].isMimeType("text/html") ) {
					try {
						o = messages[i].getContent();
						body = (String)o;
					} catch (IOException e) {
						body = "";
					}
					if (messages[i].isMimeType("text/html")) swHtml = true;  
				}
				//	        if (body == null) subject = "null."; 2013-05-23
				if (body == null) body = "null.";

				//********************************
				//************** analys start *****

				System.out.println("From-> " + from);
				System.out.println("Subject-> " + subject);
				System.out.println("Body-> " + body);

				msgFixat = false;

				// Msg från Microsoft Defender   
				if (from.indexOf("wdatpntf@microsoft.com") >= 0) {
					sendJv("MAIL_From_MS_Defender" , "ERR" , "I",  subject + " -- Inform Martin S and check servicedesk mailbox for more info!");
					msgFixat = true;
					swSerious = true;
				}

				// Msg från Equallogic  
				if (from.indexOf("PTPGroup") >= 0) {
					//									System.out.println("From> " + from);
					//									System.out.println("Subject> " + subject);
					//						    		System.out.println("Body length> " + body.length());
					//						    		System.out.println("Body> " + body);
					//					if (swHtml) sendJv("MAIL_PTPGroup" , "INFO" , "I",  subject );
					//					else 		sendJv("MAIL_PTPGroup" , "INFO" , "I",  subject + " " + body);
					sendJv("MAIL_From_PTPGroup" , "INFO" , "I",  subject + " " + body);
					msgFixat = true;
				}

				// Msg från Diesel Backup Generator  
				if (from.indexOf("UPS_Notification") >= 0) {
					if (subject.indexOf("DO6 Generator running") >= 0 || 
							subject.indexOf("GCB Closed") >= 0 ||
							subject.indexOf("Engine Speed (rpm) Over") >= 0 ||
							subject.indexOf("Mains failure") >= 0 ||
							subject.indexOf("Emergency stop") >= 0 ||
							subject.indexOf("Motor start") >= 0 ||
							subject.indexOf("Test with load, Baseload") >= 0 ||
							subject.indexOf("Test  with load soft transfer") >= 0 ||
							subject.indexOf("MCB Open") >= 0
							) {
						swSerious = true;
						sendJv("MAIL_From_Diesel_Generator" , "ERR" , "T",  subject + " " + body);
					}
					else if (subject.indexOf("Motos is stopped") >= 0 || 
							subject.indexOf("Auto mode Ready") >= 0
							) {
						swSerious = true;
						sendJv("MAIL_From_Diesel_Generator" , "OK" , "T",  subject + " " + body);
					}
					else sendJv("MAIL_From_Diesel_Generator" , "INFO" , "I",  subject + " " + body);
					msgFixat = true;
				}

				// Msg från UPS huvudkontoret  
				if (from.indexOf("UPS-BY892") >= 0) {
					
					if (messages[i].getContentType().startsWith("multipart")) {
						System.out.println("*** UPS-BY892 multipart ");
						Multipart mp = (Multipart)messages[i].getContent();
						System.out.println("***Count> " + mp.getCount());

						for (int j=0, n=mp.getCount(); j<n; j++) {
							Part part = mp.getBodyPart(j);
							System.out.println("*** Attachment Descript : "+part.getDescription());
							System.out.println("*** Attachment ContentType : "+part.getContentType());
							if (part.getContentType().contains("text/plain")) {
								body = part.getContent().toString();
								System.out.println("*** Attachment Content : "+body);
							}
						}									
					}
				
					
					if (subject.indexOf("Powerfail") >= 0  
							) {
						swSerious = true;
						sendJv("MAIL_From_UPS-BY892" , "ERR" , "T",  subject);
					}
					else if (subject.indexOf("Power restored") >= 0  
							) {
						swSerious = true;
						sendJv("MAIL_From_UPS-BY892" , "OK" , "T",  subject );
					}
					else if (subject.indexOf("Heartbeat") >= 0 || body.indexOf("Heartbeat") >= 0 
							) {
						sendJv("MAIL_From_UPS-BY892_Heartbeat" , "OK" , "T",  subject + " " + body);
						if (imaprw.startsWith("Y")) {
							messages[i].setFlag(Flags.Flag.DELETED, true); // markera mailet för deletion
							System.out.println("* Mark as DELETED ");
						}
					}
					else sendJv("MAIL_From_UPS-BY892_Info" , "INFO" , "I",  subject + " " + body );
					msgFixat = true;
				}

				// Msg från UPS berget  
				if (from.indexOf("UPS-BGT") >= 0) {
					
					if (messages[i].getContentType().startsWith("multipart")) {
						System.out.println("*** UPS-BGT multipart ");
						Multipart mp = (Multipart)messages[i].getContent();
						System.out.println("***Count> " + mp.getCount());

						for (int j=0, n=mp.getCount(); j<n; j++) {
							Part part = mp.getBodyPart(j);
							System.out.println("*** Attachment Descript : "+part.getDescription());
							System.out.println("*** Attachment ContentType : "+part.getContentType());
							if (part.getContentType().contains("text/plain")) {
								body = part.getContent().toString();
								//								if (body.indexOf("00331 FFI")  >=0) swUnomaly = false; // don't send error n this types  
//								if (body.indexOf("significant")>=0)	body = body.substring(body.indexOf("significant"));
								System.out.println("*** Attachment Content : "+body);
							}
						}									
					}
					
					if (subject.indexOf("Powerfail") >= 0  
							) {
						swSerious = true;
						sendJv("MAIL_From_UPS-BGT" , "ERR" , "T",  subject);
					}
					else if (subject.indexOf("Power restored") >= 0  
							) {
						swSerious = true;
						sendJv("MAIL_From_UPS-BGT" , "OK" , "T",  subject );
					}
					else if (subject.indexOf("Heartbeat") >= 0 || body.indexOf("Heartbeat") >= 0 
							) {
						sendJv("MAIL_From_UPS-BGT_Heartbeat" , "OK" , "T",  subject + " " + body);
						if (imaprw.startsWith("Y")) {
							messages[i].setFlag(Flags.Flag.DELETED, true); // markera mailet för deletion
							System.out.println("* Mark as DELETED ");
						}
					}
					else sendJv("MAIL_From_UPS-BGT_Info" , "INFO" , "I",  subject + " " + body );
					msgFixat = true;
				}

				// Msg från IBM bandaren  
				if (from.indexOf("TAP01@perscorp.com") >= 0 || from.indexOf("TAP02@perscorp.com") >= 0) {
					if (body.indexOf("Drive Warn or Crit Tape Alert flag") >= 0) {
						sendJv("MAIL_From_AS400_Tapestation" , "ERR" , "I",  subject + " " + body);
					} 
					msgFixat = true;
				}

				// Msg från Bartender  
				if (from.indexOf("BarTender") >= 0 || from.indexOf("Barender") >= 0) {
					if (body.indexOf("Message Type: Warning") >= 0) {
						sendJv("MAIL_From_Bartender" , "ERR" , "T",  subject + " " + body);
					} 
					if (body.indexOf("Message Type: Error") >= 0) {
						sendJv("MAIL_From_Bartender" , "ERR" , "T",  subject + " " + body);
					} 
					if (body.indexOf("BarTender found the Seagull License Server") >= 0) {
						sendJv("MAIL_From_Bartender" , "OK" , "T",  subject + " " + body);
					} 
					if (body.indexOf("BarTender has successfully connected to Seagull License Server") >= 0) {
						sendJv("MAIL_From_Bartender" , "OK" , "T",  subject + " " + body);
					} 
					msgFixat = true;
				}

				// Msg från id_prove@perstorp.com  
				if (from.indexOf("MAILTODW@perstorp.com") >= 0) {
					if (subject.indexOf("Sanktionslistenimport") >= 0) {
						if (body.indexOf("ERROR") >= 0) {
							sendJv("MAIL_From_ID_PROVE" , "ERR" , "S",  subject + " " + body);
						} 
						else {
							sendJv("MAIL_From_ID_PROVE" , "OK" , "S",  subject + " " + body);
						} 
						msgFixat = true;
						if (imaprw.startsWith("Y")) {
							messages[i].setFlag(Flags.Flag.DELETED, true); // markera mailet för deletion
							System.out.println("* Mark as DELETED ");
						}
					}
				}

				// Msg från UPS_BGT@perstorp.com  
				if (from.indexOf("UPS_BGT@perstorp.com") >= 0) {
					sendJv("MAIL_from_UPS_BGT" , "INFO" , "I",  subject + " " + body);
					msgFixat = true;
				}

				// Msg från SOCOMEC UPS Mail Service  
				if (from.indexOf("SOCOMEC UPS Mail Service") >= 0) {
					sendJv("MAIL_from_UPS_HQ" , "INFO" , "I",  subject + " " + body);
					msgFixat = true;
				}

				//diverse på samma avsändare
				if (from.indexOf("itoc@perstorp.com") >= 0) {
					msgFixat = true;  
					// Problem from EqualLogic
					if (subject.indexOf("SAN HQ Notification Alert") >= 0 || subject.indexOf("BGTGROUP0") >= 0) {
						sendJv("MAIL_From_EqualLogic" , "INFO" , "I", subject + " " + body);
					}
					// Mount request från DP
					if (subject.indexOf("Mount Request Report") >= 0) {
						sendJv("MAIL_from_HP_DP" , "INFO" , "I",  "Data Protector Mount Request!");
					}
					// Mount request från DP
					if (subject.indexOf("Device Error Report") >= 0) {
						sendJv("MAIL_From_HP_DP" , "INFO" , "I",  "Data Protector Device Error Report!");
					}
					// Rapport på fulla tabeller osv. från OeBS
					if (subject.indexOf("Rapport fr�n OeBS p� object d�r antal lediga extents �r l�gt") >= 0) {
						if (body.indexOf("no rows selected") < 0) {
							sendJv("MAIL_From_OeBS" , "ERR" , "I",  "OeBS har l�gt antal lediga extents!");
						}
						if (subject.indexOf("SessionError") >= 0) {
							if (body.indexOf("has errors: 0.") < 0) {
								sendJv("MAIL_from_HP_DP" , "INFO" , "I",  subject + " " + body);
							}
							if (subject.indexOf("Alarm") == 0) {
								if (body.indexOf("Warning") == 0) {
									sendJv("MAIL_From_HP_DP" , "INFO" , "I",  subject + " " + body);
								}
								// Icke intressant
								if (subject.indexOf("LicenseWarning") >= 0 || subject.indexOf("List of Pools") >= 0 || subject.indexOf("PTPGroup") >= 0 || subject.indexOf("StartOfSession") >= 0 || subject.indexOf("End Of Session Report") >= 0 ) {
								}
							}
						}
					}
				}

				if (from.indexOf("echo-reply@sunet.se") >= 0) {
					System.out.println("New mail from Sunet echo found");
					swSunet = true;
					msgFixat = true;  
					if (imaprw.startsWith("Y")) {
						messages[i].setFlag(Flags.Flag.DELETED, true); // markera mailet för deletion
						System.out.println("* Mark as DELETED ");
					}
				}

				if (from.indexOf("sappxp@perstorp.com") >= 0) {
					if (subject.indexOf("PerstorpITOCbase") >= 0) {
						msgFixat = true;  
						//						System.out.println("*-> PerstorpITOCbase found");
						//						System.out.println(body);
						if (body.indexOf("RFC_RECEIVER_STATIC_ERROR") >= 0 && body.indexOf("_VILANT_") >= 0) {
							System.out.println("*-> Problem mail from Vilant found!");
							sendJv("MAIL_SAP_PXP_from_Vilant" , "INFO" , "I",  "SAP-PXP warning in  "+imapFolder+ " (KA1801) \n" + body);
						}
					}
				}

				// Uninteresting
				// Error från DPM
				if (from.indexOf("ptp620") >= 0 || from.indexOf("ptp621") >= 0 || from.indexOf("pcl030") >= 0) {
					msgFixat = true; 
				}

				// Error från Qnap
				if (from.indexOf("QNAP") >= 0 || from.indexOf("PTP292") >= 0 || from.indexOf("ptp267") >= 0 || from.indexOf("ptp257") >= 0 || from.indexOf("ptp258") >= 0 || from.indexOf("ptp259") >= 0 ) {
					sendJv("MAIL_From_Qnap" , "INFO" , "I",  subject + " " + body);
				}

				// Error från Unomaly
				if (from.toLowerCase().indexOf("unomaly") >= 0 || subject.toLowerCase().indexOf("unomaly") >= 0 || body.toLowerCase().indexOf("unomaly") >= 0) {
					msgFixat = true;
					swUnomaly = true;

					if (messages[i].getContentType().startsWith("multipart")) {
						System.out.println("*** Unomaly multipart ");
						Multipart mp = (Multipart)messages[i].getContent();
						System.out.println("***Count> " + mp.getCount());

						for (int j=0, n=mp.getCount(); j<n; j++) {
							Part part = mp.getBodyPart(j);
							System.out.println("*** Attachment Descript : "+part.getDescription());
							System.out.println("*** Attachment ContentType : "+part.getContentType());
							if (part.getContentType().contains("text/plain")) {
								body = part.getContent().toString();
								//								if (body.indexOf("00331 FFI")  >=0) swUnomaly = false; // don't send error n this types  
								if (body.indexOf("significant")>=0)	body = body.substring(body.indexOf("significant"));
								System.out.println("*** Attachment Content : "+body);
							}
						}									
					}

					if (swUnomaly) sendJv("MAIL_From_Unomaly" , "INFO" , "T",  subject + " " + body);
				}

				if (subject.indexOf("Autosvar:") == 0) {
					msgFixat = true;  
				}

				if (subject.startsWith("PCP")) {  // Mail från PCP 
					msgFixat = true;  
					System.out.println("*** PCP> " + body);
					System.out.println("*** SAP System PCP  ");
					if (messages[i].getContentType().startsWith("multipart")) {
						System.out.println("*** multipart ");
						Multipart mp = (Multipart)messages[i].getContent();
						//         			    System.out.println("***Count> " + mp.getCount());

						for (int j=0, n=mp.getCount(); j<n; j++) {
							Part part = mp.getBodyPart(j);

							//							String disposition = part.getDisposition();
							//System.out.println("***Disp> " + disposition);

							//        	        		  if ( (disposition != null) && 
							//        		        		     ( disposition.equals(Part.ATTACHMENT) )) {
							System.out.println("*** Attachment Descript : "+part.getDescription());
							System.out.println("*** Attachment ContentType : "+part.getContentType());
							swSmq1PXP = false;
							scanFile(part.getFileName(), part.getInputStream()); 
							if (swSmq1PXP) {
								sendJv("MAIL_From_SAP_PXP" , "ERR" , "I",  "SAP PXP warning. Check SMQ1 for errors");
							}
						}									
					}
				}

				if (subject.startsWith("Job PTP CHECK SMQ2")) {  // Mail från övervakning av SMQ2 i PCP
					msgFixat = true;  
					if (imaprw.startsWith("Y")) {
						messages[i].setFlag(Flags.Flag.DELETED, true); // markera mailet f�r deletion
						System.out.println("* Mark as DELETED ");
					}
					System.out.println("*** PCP check> " + body);
					System.out.println("*** SAP Monitor SMQ2  ");
					if (messages[i].getContentType().startsWith("multipart")) {
						System.out.println("*** multipart ");
						Multipart mp = (Multipart)messages[i].getContent();
						//         			    System.out.println("***Count> " + mp.getCount());

						for (int j=0, n=mp.getCount(); j<n; j++) {
							Part part = mp.getBodyPart(j);

							//							String disposition = part.getDisposition();
							//System.out.println("***Disp> " + disposition);

							//        	        		  if ( (disposition != null) && 
							//        		        		     ( disposition.equals(Part.ATTACHMENT) )) {
							System.out.println("*** Attachment Descript : "+part.getDescription());
							System.out.println("*** Attachment ContentType : "+part.getContentType());
							swSmq2PCP = false;
							if (scanFile(part.getFileName(), part.getInputStream())) {
								if (swSmq2PCP) {
									sendJv("MAIL_From_SAP_PCP_SMQ2" , "OK" , "R",  "SMQ2 OK");
								}
								else {
									sendJv("MAIL_From_SAP_PCP_SMQ2" , "ERR" , "R",  "SAP PCP warning. Check SMQ2 for errors.");
								}
							}
						}									
					}
				}

				if (subject.startsWith("Job PTP CHECK SM58")) {  // Mail från övervakning av SM58 i PCP
					msgFixat = true;  
					if (imaprw.startsWith("Y")) {
						messages[i].setFlag(Flags.Flag.DELETED, true); // markera mailet för deletion
						System.out.println("* Mark as DELETED ");
					}
					System.out.println("*** PCP check> " + body);
					System.out.println("*** SAP Monitor SM58  ");
					if (messages[i].getContentType().startsWith("multipart")) {
						System.out.println("*** multipart ");
						Multipart mp = (Multipart)messages[i].getContent();
						//         			    System.out.println("***Count> " + mp.getCount());

						for (int j=0, n=mp.getCount(); j<n; j++) {
							Part part = mp.getBodyPart(j);

							//							String disposition = part.getDisposition();
							//System.out.println("***Disp> " + disposition);

							//        	        		  if ( (disposition != null) && 
							//        		        		     ( disposition.equals(Part.ATTACHMENT) )) {
							System.out.println("*** Attachment Descript : "+part.getDescription());
							System.out.println("*** Attachment ContentType : "+part.getContentType());
							Sm58PCPerr = 0;
							if (scanFile(part.getFileName(), part.getInputStream())) {
								if (Sm58PCPerr <= 7) {
									sendJv("MAIL_From_SAP_PCP_SM58" , "OK" , "R",  "SM58 OK");
								}
								else {
									sendJv("MAIL_From_SAP_PCP_SM58" , "ERR" , "R",  "SAP PCP warning. Check SM58 for errors.");
								}
							}
						}									
					}
				}

				if (msgFixat && imaprw.startsWith("Y")) { 
					System.out.println("* Mark as SEEN ");
					messages[i].setFlag(Flags.Flag.SEEN, true); // markera mailet som sett
				}

				if (!msgFixat) {
					System.out.println("* Mail ignored ");
					messages[i].setFlag(Flags.Flag.SEEN, false); // markera mailet som oläst
				}

				//************ analys stopp ******
				//********************************
			}

			// Close the connection
			if (imaprw.startsWith("Y")) inbox.close(true);  // remove the marked messages from the server
			else 						inbox.close(false);
			store.close();

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		// Stängrutiner
		// Sunet echo expected only to be found in the INBOX folder
		if (imapFolder.endsWith("INBOX")) {
			if (!swSunet) {
				System.out.println("No return mail from ping@sunet.se found.");
				sendJv("MAIL_SUNET.SE_Echo" , "ERR" , "R", "No return mail from ping@sunet.se found.");
			}
			else {
				System.out.println("Mail from Sunet echo found.");
				sendJv("MAIL_SUNET.SE_Echo" , "OK" , "R",  "Reply from SUNET.SE arrived");
			}
		}

	} // slut på pgm

	static protected void sendJv( String ID, String STS, String type, String msg) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		System.out.println(jm.open());
		jmsg.setId(ID);
		jmsg.setRptsts(STS);
		jmsg.setBody(msg);
		jmsg.setType(type);
		jmsg.setAgent(agent);
		if (swSerious) {
			jmsg.setPrio(20);
			swSerious = false;
		} else jmsg.setPrio(30);

		if (jm.sendMsg(jmsg)) {
			//			System.out.println("-- Rpt Delivered -- " + ID +" - "+ STS +" - "+ type +" - "+ msg +" - "+ agent);
			System.out.println("-- Rpt Delivered -- " + ID +" - "+ STS +" - "+ type +" - "+ agent);
			msgFixat = true;
		}
		else            System.out.println("-- Rpt Failed --");
		jm.close();
	}

	public static boolean scanFile(String filename, InputStream in)  throws IOException {

		String s;

		if (filename == null) {
			System.out.println("Original filename <null> ");
			return(false);
		}

		if (filename.startsWith("=?")) {
			System.out.println("Original filename: "+filename);
			filename = ("noname.pdf");
		}

		//    File file = new File("D_"+filename);
		System.out.println("In file : "+in);
		BufferedReader in2 = new BufferedReader(new InputStreamReader(in));  
		antal++;
		//    if (!filename.toLowerCase().endsWith(".TXT")) { 
		// 	System.out.println("Kept    : "+filename+"\tFrom: " + from +"\tSubject: " + subject);
		// 	return(false); }

		//    byte[] buf = new byte[1024];
		//   int len;
		while ((s = in2.readLine()) != null) {
			if (s.length() == 0) continue; 
			if (s.indexOf("PXP") >= 0) {
				swSmq1PXP = true;
				System.out.println("** Found PXP in input stream!");
			} 
			if (s.indexOf("Entries&nbsp;Displayed&#x3a;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp") >= 0) {
				System.out.println("** Found Entries Displayed: 0 !");
				swSmq2PCP = true;
			} 
			if (s.indexOf("Queues&nbsp;Displayed&#x3a;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp") >= 0) {
				System.out.println("** Found Queues Displayed: 0 !");
				swSmq2PCP = true;
			} 
			int offset = 0;
			while (s.indexOf("Transaction&nbsp;executing", offset) >= 0) {
				Sm58PCPerr++;
				offset = s.indexOf("Transaction&nbsp;executing", offset);
				offset++;
				System.out.println("** Found Transaction executing: " + Sm58PCPerr );
				swSmq1PXP = false; 
			}
		}
		in2.close();
		in.close();
		detached++;
		return(true);
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
			uname    = prop.getProperty("smtpuser");
			pwd      = prop.getProperty("smtppwd");
			if (pwd.startsWith("==y")) {
			    byte[] decodedBytes = Base64.getDecoder().decode(pwd.substring(3));
			    String decodedString = new String(decodedBytes);
			    pwd=decodedString;
			}
			imaphost = prop.getProperty("imaphost");
			imapport = prop.getProperty("imapport");
			imapssl = prop.getProperty("imapSSL");
			imaprw = prop.getProperty("imapRW");
			imapFolder = prop.getProperty("imapFolder");
			//			int imapporti = Integer.parseInt(imapport);

			System.out.println("getProps jvport:" + jvport + "  jvhost:"+jvhost) ;
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
