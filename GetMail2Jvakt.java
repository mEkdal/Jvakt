package Jvakt;


import jakarta.mail.*;
import jakarta.mail.search.*;
//import Jvakt.Message;
import java.util.*;
import java.io.*;
import java.net.InetAddress;

public class GetMail2Jvakt {
 
	//	static boolean newMsgId = false;
	static boolean msgFixat = false;
	static boolean swHtml = false;
	static boolean swHelp = false;
	static boolean swOkSender = false;
	static boolean swPrio = false;
	static String mimeType;
	static String msgId;
	//	static boolean swSunet = false;

	static boolean swSerious = false;

	//	static boolean swSmq1PXP = false;
	//	static boolean swSmq2PCP = false;
	//	static boolean swUnomaly = false;
	//	static int Sm58PCPerr = 0;

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
	static String senders;
	static String sender;
	static List<String> listSenders;
	static String exSubjects;
	static String exSubject;
	static List<String> listExsubjects;

	static String config = null;
	static File configF;

	static InetAddress inet;

	public static void main(String[] args) {

		String version = "GetMail2Jvakt ( 2021-09-08 )";

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-help")) swHelp = true;
			if (args[i].equalsIgnoreCase("-?")) swHelp = true;
		}

		if (swHelp) {
			System.out.println("--- GetMail2Jvakt --- ");
			System.out.println("by Michael Ekdal Perstorp Sweden.\n");
			System.out.println("Analyses mail directed to the Jvakt mailbox");
			System.exit(4);
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version);
		System.out.println("-config file: "+configF);

		Properties props = new Properties();

		String provider = "imap";  // plain
		int    imapPort = 993; // secure

		Address[] adr;

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

			//	        System.out.println("\n------------ NewMessageCount " + inbox.getNewMessageCount() + " ------------");
			//	        System.out.println("------------ UnreadMessageCount " + inbox.getUnreadMessageCount() + " ------------");

			System.out.println("Number of mails: " +messages.length);

			for (int i = 0; i < messages.length; i++) {
				subject = messages[i].getSubject();
				if (subject == null) subject = "null.";
				adr = messages[i].getFrom(); 
				from = null;
				if (adr != null) {
//					System.out.println("adr null ");
					for (int j = 0; j < adr.length; j++) {
						from = adr[j].toString();
					}
				} else {
					from = "unknown";
				}
				System.out.println("From: " + from );
				//	        if (from == null) subject = "null.";  2013-05-23
				if (from == null) from = "null.";
				mimeType = messages[i].getContentType();
				System.out.println("MimeType> " + mimeType);
				System.out.println("Subject: " + subject);
				//				if (from.contains("postmaster@internal.perscorp.com")) continue;
				body = null;
				swHtml = false;
				Object o = null;
				//				if (messages[i].isMimeType("text/plain") || messages[i].isMimeType("text/html") || messages[i].isMimeType("multipart/alternative")  || messages[i].isMimeType("multipart/mixed")  ) {
				//				if (messages[i].isMimeType("text/plain") || messages[i].isMimeType("text/html") ) {
				if (mimeType.toLowerCase().indexOf("text/plain")>=0 || mimeType.toLowerCase().indexOf("text/html")>=0) {
					try {
						o = messages[i].getContent();
						body = (String)o;
					} catch (IOException e) {
						body = "";
					}
					if (messages[i].isMimeType("text/html")) swHtml = true;  
				}
				if (body == null) body = "null.";

				//********************************
				//************** analys start *****

				System.out.println("From-> " + from);
				System.out.println("Subject-> " + subject);
				System.out.println("Body-> " + body);

				msgFixat = false;
				swOkSender = false;
				swPrio = false;

				for(Object object : listSenders) { 
					sender = (String)object;
					sender = sender.trim().toLowerCase();
					if (from.toLowerCase().indexOf(sender) >= 0) swOkSender = true;
					if (sender.compareTo("*")==0) swOkSender = true;
				}


				if (swOkSender) {
					System.out.println(" - Sender "+from+" is accepted");

					if (messages[i].getContentType().toLowerCase().startsWith("multipart")) {
//						System.out.println("*** multipart ");
						Multipart mp = (Multipart)messages[i].getContent();
//						System.out.println("*** Count> " + mp.getCount());

						for (int j=0, n=mp.getCount(); j<n; j++) {
							Part part = mp.getBodyPart(j);
//							System.out.println("*** Attachment Descript : "+part.getDescription());
//							System.out.println("*** Attachment ContentType : "+part.getContentType());
							if (part.getContentType().toLowerCase().contains("text/plain")) {
								body = part.getContent().toString();
//								System.out.println("*** Attachment Content : \n"+body);
							}
						}									
					}

					for(Object object : listExsubjects) { 
						exSubject = (String)object;
						exSubject = exSubject.trim().toLowerCase();
						if (subject.toLowerCase().indexOf(exSubject) >= 0) {
							swOkSender = false;
							System.out.println(" - but subject '"+exSubject+"' is exempted!");
						}
						if (body.toLowerCase().indexOf(exSubject) >= 0) {
							swOkSender = false;
							System.out.println(" - but body '"+exSubject+"' is exempted!");
						}
					}

					if (swOkSender) {
						if (subject.toLowerCase().indexOf("sms:") >= 0) {
							subject = subject.substring(5);
							swPrio = true;
						}

						if (swPrio) sendJv("MAIL_2_Jvakt_prio" , "INFO" , "I", "From: "+ from +" / "+ subject +" / "+ body );
						else        sendJv("MAIL_2_Jvakt" ,      "INFO" , "I", "From: "+ from +" / "+ subject +" / "+ body );
						//					if (imaprw.startsWith("Y")) {
						//							messages[i].setFlag(Flags.Flag.DELETED, true); // mark the mail for deletion
						//							System.out.println("* Mark as DELETED ");
						//					}
						//					msgFixat = true;
					}
				}

				if (msgFixat && imaprw.startsWith("Y")) { 
					System.out.println("* Mark as SEEN ");
					messages[i].setFlag(Flags.Flag.SEEN, true); // markera mailet som sett
				}

				if (!msgFixat) {
					System.out.println("* Mail ignored ");
					messages[i].setFlag(Flags.Flag.SEEN, true); 
				}

				//************ analys stop ******
				//*******************************
			}

			// Close the connection
			if (imaprw.startsWith("Y")) inbox.close(true);  // remove the marked messages from the server
			else 						inbox.close(false);
			store.close();

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	} 

	// End of program

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
			System.out.println("-- Rpt Delivered -- " + ID +" - "+ STS +" - "+ type +" - "+ agent);
			msgFixat = true;
		}
		else     {
			System.out.println("-- Rpt Failed --");
			msgFixat = false;
		}
		jm.close();
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
			senders = prop.getProperty("senders");

			String[] senderAddr = senders.split("\\,");

			listSenders = new ArrayList<String>();  // Alla mailadresser.
			System.out.println("-- Accepted senders --");
			for(int i=0 ; i<senderAddr.length;i++) {
				System.out.println(senderAddr[i]);
				listSenders.add(senderAddr[i]);
			}

			exSubjects = prop.getProperty("exsubjects");
			String[] exSubjectsStrings = exSubjects.split("\\,"); // All no-wanted subjects

			listExsubjects = new ArrayList<String>();
			System.out.println("-- exempted subjects --");
			for(int i=0 ; i<exSubjectsStrings.length;i++) {
				System.out.println(exSubjectsStrings[i]);
				listExsubjects.add(exSubjectsStrings[i]);
			}


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
