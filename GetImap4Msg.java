package Jvakt;


import javax.mail.*;
import javax.mail.search.*;
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

	static boolean swSmq1PXP = false;

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

		String version = "GetImap4Msg 1.2 # 2018-01-04";

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
			javax.mail.Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

			//	        System.out.println("\n------------ NewMessageCount " + inbox.getNewMessageCount() + " ------------");
			//	        System.out.println("------------ UnreadMessageCount " + inbox.getUnreadMessageCount() + " ------------");

			System.out.println("Number of mails: " +messages.length);

			for (int i = 0; i < messages.length; i++) {
				subject = messages[i].getSubject();
				if (subject == null) subject = "null.";
				adr = messages[i].getFrom(); 
				from = null;
				for (int j = 0; j < adr.length; j++) {
					from = adr[j].toString();
					System.out.println("From: " + from );
				}
				//	        if (from == null) subject = "null.";  2013-05-23
				if (from == null) from = "null.";
				System.out.println("MimeType> " + messages[i].getContentType());
				System.out.println("Subject: " + subject);
				if (from.contains("postmaster@internal.perscorp.com")) continue;
				body = null;
				swHtml = false;
				if (messages[i].isMimeType("text/plain") || messages[i].isMimeType("text/html")  ) {
					Object o = messages[i].getContent();
					body = (String)o;
					if (messages[i].isMimeType("text/html")) swHtml = true;  
				}
				//	        if (body == null) subject = "null."; 2013-05-23
				if (body == null) body = "null.";

				//********************************
				//************** analys start *****
				
				//				System.out.println("From> " + from);
				//				System.out.println("Subject> " + subject);
				//    		System.out.println("Body> " + body);
				msgFixat = false;

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
					sendJv("MAIL_From_Diesel_Generator" , "INFO" , "I",  subject + " " + body);
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
						sendJv("MAIL_From_Bartender" , "ERR" , "I",  subject + " " + body);
					} 
					if (body.indexOf("Message Type: Error") >= 0) {
						sendJv("MAIL_From_Bartender" , "ERR" , "I",  subject + " " + body);
					} 
					msgFixat = true;
				}

				//diverse på samma avsändare
				if (from.indexOf("itoc@perstorp.com") >= 0) {
					msgFixat = true;  
					// Problem from EqualLogic
					if (subject.indexOf("SAN HQ Notification Alert") >= 0 || subject.indexOf("BGTGROUP0") >= 0) {
						sendJv("MAIL_From_EqualLogic" , "ERR" , "I", subject + " " + body);
					}
					// Mount request från DP
					if (subject.indexOf("Mount Request Report") >= 0) {
						sendJv("MAIL_from_HP_DP" , "ERR" , "I",  "Data Protector Mount Request!");
					}
					// Mount request från DP
					if (subject.indexOf("Device Error Report") >= 0) {
						sendJv("MAIL_From_HP_DP" , "ERR" , "I",  "Data Protector Device Error Report!");
					}
					// Rapport på fulla tabeller osv. från OeBS
					if (subject.indexOf("Rapport från OeBS på object där antal lediga extents är lågt") >= 0) {
						if (body.indexOf("no rows selected") < 0) {
							sendJv("MAIL_From_OeBS" , "ERR" , "I",  "OeBS har lågt antal lediga extents!");
						}
						if (subject.indexOf("SessionError") >= 0) {
							if (body.indexOf("has errors: 0.") < 0) {
								sendJv("MAIL_from_HP_DP" , "ERR" , "I",  subject + " " + body);
							}
							if (subject.indexOf("Alarm") == 0) {
								if (body.indexOf("Warning") == 0) {
									sendJv("MAIL_From_HP_DP" , "ERR" , "I",  subject + " " + body);
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
					sendJv("MAIL_From_Qnap" , "ERR" , "I",  subject + " " + body);
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

							String disposition = part.getDisposition();
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
		// Sunet echo ecpected only to be fount in the INBOX folder
		if (imapFolder.endsWith("INBOX")) {
			if (!swSunet) {
				System.out.println("No mail from Sunet echo found.");
				sendJv("MAIL_SUNET.SE_Echo" , "ERR" , "R", "No mail from Sunet echo found.");
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
//		jm.sendMsg(jmsg);
		if (jm.sendMsg(jmsg)) {
			System.out.println("-- Rpt Delivered --");
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


}
