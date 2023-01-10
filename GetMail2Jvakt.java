package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 * 2023-01-10 V.55 Michael Ekdal		Added send of the status to Jvakt server
 */

import jakarta.mail.*;
import jakarta.mail.search.*;
//import Jvakt.Message;
import java.util.*;

import Jvakt.Message;

import java.io.*;
import java.net.InetAddress;

public class GetMail2Jvakt {

	//	static boolean newMsgId = false;
	static boolean msgFixat = false;
	static boolean swHtml = false;
	static boolean swHelp = false;
	static boolean swOkSender = false;
	static boolean swPrio = false;
	static boolean swMailcheck = true;
	static boolean swDormant = false;
	static boolean swId = false;
	static boolean swDelete = false;
	static boolean swOKtot = false;
	static String mimeType;
	static String msgId;
	static String t_id;

	//	static boolean swSunet = false;

	static boolean swSerious = false;
	static boolean swShow = false;
	static boolean swRun = true;

	static String from;
	static String subject;
	static String body;
	static int antal;
	static int detached;
	static String agent = null;
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int jvporti ;
	static String jvrc   = "";
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

		String version = "GetMail2Jvakt ";
		version += getVersion()+".55";

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-help")) swHelp = true;
			if (args[i].equalsIgnoreCase("-?")) swHelp = true;
			if (args[i].equalsIgnoreCase("-norun")) swRun = false;
			if (args[i].equalsIgnoreCase("-nomailcheck")) swMailcheck = false;
			if (args[i].equalsIgnoreCase("-id")) swId = true;
			if (args[i].equalsIgnoreCase("-delete")) swDelete = true;
			if (args[i].equalsIgnoreCase("-show")) swShow = true;

		}

		if (swHelp) {
			System.out.println("--- GetMail2Jvakt --- ");
			System.out.println("by Michael Ekdal Perstorp Sweden.\n");
			System.out.println("Analyses mail directed to the Jvakt mailbox and shows them in the Jvakt console.");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config 		\tThe directory of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-norun  		\tTo turn of the status update in Jvakt." +
					"\n-nomailcheck \tDo not read an analyze the mails. Only log-on the mail server to verify it is possible." +
					"\n-id          \tCreates an ID in the Jvakt server showing the status of the mail server. OK when able to log-on, ERR when not." +
					"\n-delete      \tWhen a mai is found, delete it! ( the default is to mark it as seen )"+ 
					"\n-show   		\tA verbose log showing the response from the server."
					);

			System.exit(4);
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("\n"+new Date()+" ----- Jvakt: Version: "+version);
		System.out.println(new Date()+" -config file: "+configF);

		Properties props = new Properties();
		getProps();  // get Jvakt properties


		if (swRun) {
			SendMsg jm = new SendMsg(jvhost, jvporti);  // Check if JvaktServer is available and if dormant.
			jvrc = jm.open();
			System.out.println(new Date()+" Jvakt server status: "+jvrc);
			if (jvrc.toLowerCase().startsWith("dormant")) swDormant = true;
			if (jvrc.toLowerCase().startsWith("failed")) {
				swRun = false;
				swDormant = true;
				System.out.println(new Date()+" ----- Access to  Jvakt failed! \"run\" set to false !!");
			}
			jm.close();
		}

//		if (swDormant) {
//			System.out.println(new Date()+" *** Jvakt in DORMANT mode, GetMail2Jvakt exiting *** ");
//			System.exit(4);			
//		}

		String provider = "imap";  // plain
		int    imapPort = 993; // secure

		Address[] adr;

		try {


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

			System.out.println(new Date()+" Initiating connection to imap4 server....");
			System.out.println(new Date()+" Connection... " + imaphost+":"+imapport+"  User: "+uname);
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore(provider);
			try {
				store.connect(imaphost, imapPort, uname, pwd);
			} catch (Exception e) {
				System.out.println(new Date()+" Connection to " + imaphost+":"+imapport+"  User "+uname+"  Pass "+pwd+" failed!");
				System.out.println(e);
				if (swId) sendJv("GetMail2Jvakt_"+imaphost+"_"+uname , "ERR" , "T", "Connection to imap4 server "+imaphost+" user "+uname+" failed!" );
				System.exit(8);
			}
			System.out.println(new Date()+" Connection to imap4 server established.");
			if (swId) sendJv("GetMail2Jvakt_"+imaphost+"_"+uname , "OK" , "T", "Connection to imap4 server "+imaphost+" user "+uname+" established." );

			if (swRun) {
				// Open the folder
				System.out.println(new Date()+" Open folder: " + imapFolder);
				Folder inbox = store.getFolder(imapFolder);
				if (inbox == null) {
					System.out.println(new Date()+" Mailbox folder "+imapFolder+" not found!");
					System.exit(1);
				}

				if (imaprw.startsWith("Y") && swRun && swMailcheck) {
					System.out.println(new Date()+" Opens "+imapFolder+" READ_WRITE");
					inbox.open(Folder.READ_WRITE);  // remove the marked messages from the server
				}
				else {
					System.out.println(new Date()+" Opens "+imapFolder+" READ_ONLY");
					inbox.open(Folder.READ_ONLY);
				}

				// Get the messages from the server
				// Fetch all messages from inbox folder
				//			javax.mail.Message[] messages = inbox.getMessages();
				// Fetch unseen messages from inbox folder
				jakarta.mail.Message[] messages = null;
				messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

				//	        System.out.println("\n------------ NewMessageCount " + inbox.getNewMessageCount() + " ------------");
				//	        System.out.println("------------ UnreadMessageCount " + inbox.getUnreadMessageCount() + " ------------");

				System.out.println(new Date()+" *** Number of mails-> " +messages.length);

				if (swMailcheck) {
					for (int i = 0; i < messages.length; i++) {
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
						System.out.println("\n"+new Date()+" >>> Mail from From: " + from );
						//	        if (from == null) subject = "null.";  2013-05-23
						if (from == null) from = "null.";
						subject = messages[i].getSubject();
						if (subject == null) subject = "null.";
						mimeType = messages[i].getContentType();
						System.out.println(new Date()+" MimeType> " + mimeType);
						System.out.println(new Date()+" Subject: " + subject);
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
						if (messages[i].getContentType().startsWith("multipart")) {
							System.out.println(new Date()+" *** multipart ");
							Multipart mp = (Multipart)messages[i].getContent();
							System.out.println(new Date()+" ***Count> " + mp.getCount());

							for (int j=0, n=mp.getCount(); j<n; j++) {
								Part part = mp.getBodyPart(j);
								System.out.println(new Date()+" *** Attachment Descript : "+part.getDescription());
								System.out.println(new Date()+" *** Attachment ContentType : "+part.getContentType());
								if (part.getContentType().contains("text/plain")) {
									body = part.getContent().toString();
									System.out.println(new Date()+" *** Attachment Content : "+body);
								}
							}									
						}

						if (body == null) body = "null.";

						//********************************
						//************** analyse start *****

						System.out.println(new Date()+" From-> " + from);
						System.out.println(new Date()+" Subject-> " + subject);
						System.out.println(new Date()+" Body-> " + body);

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
							System.out.println(new Date()+"  - Sender "+from+" is accepted");

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

								if (swRun) {
									if (swPrio) sendJv("MAIL_2_Jvakt_prio" , "INFO" , "I", "From: "+ from +" / "+ subject +" / "+ body );
									else        sendJv("MAIL_2_Jvakt" ,      "INFO" , "I", "From: "+ from +" / "+ subject +" / "+ body );
								}

								//					if (imaprw.startsWith("Y")) {
								//							messages[i].setFlag(Flags.Flag.DELETED, true); // mark the mail for deletion
								//							System.out.println("* Mark as DELETED ");
								//					}
								//					msgFixat = true;
							}
						}

						if (msgFixat && imaprw.startsWith("Y") && swRun) { 
							if (swDelete) {
								System.out.println(new Date()+" * Mark as DELETED ");
								messages[i].setFlag(Flags.Flag.DELETED, true);						
							}
							else {
								System.out.println(new Date()+" * Mark as SEEN ");
								messages[i].setFlag(Flags.Flag.SEEN, true); 
							}
						}

						if (!msgFixat && swRun) {
							System.out.println("* Mail ignored ");
							messages[i].setFlag(Flags.Flag.SEEN, false); 
						}

						//************ analyse stop ******
						//*******************************
					}
				}
				// Close the connection
				if (imaprw.startsWith("Y") && swRun && swMailcheck ) {
					System.out.println(new Date()+" Close Mailbox folder "+imapFolder+" with option true");
					inbox.close(true);  // remove the marked messages from the server
				}
				else {
					System.out.println(new Date()+" Close Mailbox folder "+imapFolder+" with option false");
					inbox.close(false);
				}
			}
			store.close();
			swOKtot = true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			swOKtot = false;
		}

		if (swOKtot ) try {sendSTS(true);}  catch (IOException e) { e.printStackTrace();}
		else 	      try {sendSTS(false);} catch (IOException e) { e.printStackTrace();}

	} 

	// End of program

	static protected void sendJv( String ID, String STS, String type, String msg) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, jvporti);
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
			jvporti  = Integer.parseInt(jvport);
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
			senders = prop.getProperty("senders");

			String[] senderAddr = senders.split("\\,");

			listSenders = new ArrayList<String>();  // Alla mailadresser.
			System.out.println(new Date()+" -- Accepted senders --");
			for(int i=0 ; i<senderAddr.length;i++) {
				System.out.println(senderAddr[i]);
				listSenders.add(senderAddr[i]);
			}

			exSubjects = prop.getProperty("exsubjects");
			String[] exSubjectsStrings = exSubjects.split("\\,"); // All no-wanted subjects

			listExsubjects = new ArrayList<String>();
			System.out.println(new Date()+" -- exempted subjects --");
			for(int i=0 ; i<exSubjectsStrings.length;i++) {
				System.out.println(exSubjectsStrings[i]);
				listExsubjects.add(exSubjectsStrings[i]);
			}


			//			int imapporti = Integer.parseInt(imapport);

			System.out.println(new Date()+" getProps jvport:" + jvport + "  jvhost:"+jvhost) ;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			inet = InetAddress.getLocalHost();
			System.out.println(new Date()+" -- Inet: "+inet);
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

	// sends status to the Jvakt server
	static protected void sendSTS( boolean STS) throws IOException {
			System.out.println("--- Connecting to "+jvhost+":"+jvport);
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(jvhost, jvporti);
//			System.out.println(jm.open()); 
			jm.open(); 
			jmsg.setId("Jvakt-GetMail2Jvakt");
			if (STS) {
				jmsg.setBody("The GetMail2Jvakt program is working.");
				jmsg.setRptsts("OK");
			}
			else {
				jmsg.setBody("The GetMail2Jvakt program is not working!");
				jmsg.setRptsts("ERR");
			}
			jmsg.setType("T");

			inet = InetAddress.getLocalHost();
//			System.out.println("-- Inet: "+inet);
			agent = inet.toString();

			jmsg.setAgent(agent);
			if (!jm.sendMsg(jmsg)) System.out.println("--- Rpt to Jvakt Failed for GetMail2Jvakt ---");
			jm.close();
	}
	

}
