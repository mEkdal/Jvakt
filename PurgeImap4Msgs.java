package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import jakarta.mail.*;
import jakarta.mail.search.*;
import jakarta.mail.search.SearchTerm;
import java.util.*;
import java.io.*;
import java.net.InetAddress;

public class PurgeImap4Msgs {

	//	static boolean newMsgId = false;
	static boolean msgFixat = false;
	static boolean swHtml = false;
	static boolean swHelp = false;
	static String msgId;
	static boolean swSunet = false;

	static boolean swSerious = false;

	static String from;
	static String subject;
	static String body;
	static String Pfrom = null;
	static String Psubject = null;
	static String Pbody = null;
	static boolean swPfrom = true;
	static boolean swPsubject = true;
	static boolean swPbody = true;

	static int antal = 0;
	static int antalDmarked = 0;
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
	static String imapcopy;
	static String imapdelete;
	static String imapexpunge = "Y";
	static String imapFolder;
	static String imapFolderc;
	static String config = null;
	static File configF;

	static InetAddress inet;

	public static void main(String[] args) throws IOException, FileNotFoundException {

		String version = "PurgeImap4Msgs ";
		version += getVersion()+".54";

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-help")) swHelp = true;
			if (args[i].equalsIgnoreCase("-?")) swHelp = true;
		}

		if (swHelp) {
			System.out.println("--- PurgeImap4Msgs --- ");
			System.out.println("by Michael Ekdal Perstorp Sweden.\n");
			System.out.println("Analyses mail directed to a mailbox folder");
			System.exit(4);
		}

		if (config == null ) 	configF = new File("PurgeImap4Msgs.properties");
		else 					configF = new File(config,"PurgeImap4Msgs.properties");
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

		//		System.out.println("\n*** Jvakt.PurgeImap4Msgs starting --- " + new Date());

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
				System.out.println("Mailbox folder souce is "+imapFolder+" not found!");
				System.exit(1);
			}
			Folder rensat = null;
			if (imapcopy.startsWith("Y")) {
				rensat = store.getFolder(imapFolderc);
				if (rensat == null) {
					System.out.println("Mailbox folder target is "+imapFolderc+" not found!");
					System.exit(1);
				}
			}
			if (imaprw.startsWith("Y")) inbox.open(Folder.READ_WRITE);  // remove the marked messages from the server
			else 						inbox.open(Folder.READ_ONLY);
			//			if (imaprw.startsWith("Y")) rensat.open(Folder.READ_WRITE);  // remove the marked messages from the server
			//			else 						rensat.open(Folder.READ_ONLY);


			// creates a search criterion
			SearchTerm searchCondition = new SearchTerm() {
				private static final long serialVersionUID = 5298994639594655420L;
				@Override
//				public boolean match(javax.mail.Message message) {
				public boolean match(jakarta.mail.Message message) {
					try {

						if (Pfrom != null) {
							swPfrom = false;
							Address[] adr;
							adr = message.getFrom(); 
							from = null;
							if (adr != null) {
								//        					System.out.println("adr null ");
								for (int j = 0; j < adr.length; j++) {
									from = adr[j].toString();
								}
							} else {
								from = "unknown";
							}
							if (from.contains(Pfrom)) swPfrom = true;
						}

						if (Psubject != null) {
							swPsubject = false;
							if (message.getSubject() != null) {
								if (message.getSubject().contains(Psubject)) swPsubject = true;
							}
						}

						if (Pbody != null) {
							swPbody = false;
							Object o = null;
							if (message.getSubject() != null) {
								if (message.isMimeType("text/plain") || message.isMimeType("text/html")  ) {
									try {
										o = message.getContent();
										body = (String)o;
									} catch (IOException e) {
										body = "";
									}
									if (message.isMimeType("text/html")) swHtml = true;  
								}
								//	        if (body == null) subject = "null."; 2013-05-23
								if (body == null) body = "null.";
								if (body.contains(Pbody)) swPbody=true;
							}
						}

						if (swPfrom && swPsubject && swPbody) {
							return true;
						}

					} catch (MessagingException ex) {
						ex.printStackTrace();
					}
					return false;
				}
			};

			// Get the messages from the server
			// Fetch all messages from inbox folder
			//			javax.mail.Message[] messages = inbox.getMessages();
			// Fetch unseen messages from inbox folder
//			javax.mail.Message[] messages = inbox.search(searchCondition);
			jakarta.mail.Message[] messages = inbox.search(searchCondition);
			//			javax.mail.Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

			//	        System.out.println("\n------------ NewMessageCount " + inbox.getNewMessageCount() + " ------------");
			//	        System.out.println("------------ UnreadMessageCount " + inbox.getUnreadMessageCount() + " ------------");

			System.out.println("--- Number of mails: " +messages.length +" to purge...");

			if (messages.length > 0 ) {

				if (imaprw.startsWith("Y")) {
					if (imapcopy.startsWith("Y")) {
						System.out.println("\n*** Copy messages " + new Date());
						inbox.copyMessages(messages, rensat);
					}
					if (imapdelete.startsWith("Y")) {
						System.out.println("\n*** Delete marking messages... " + new Date());
						//						inbox.setFlags(messages, new Flags(Flags.Flag.DELETED), true); // kan ge timeout i stora mailboxes

						for (int i = 0; i < messages.length; i++) {
							messages[i].setFlag(Flags.Flag.DELETED, true); // markera mailet för deletion
							antal++; antalDmarked++;
							if (antal > 5000 ) {
								//											System.out.println("* expunge...");
//								if (imapexpunge.startsWith("Y")) inbox.expunge();
								inbox.exists();
								antal = 0;
							}
							//										System.out.println("* Mark as DELETED ");
						}
						System.out.println("\n*** Delete-marked messages: "+antalDmarked+" " + new Date());
					}
				}

			}


			// Close the connection
			System.out.println("\n*** Close connection " + new Date());
			if (imaprw.startsWith("Y")) {
				if (imapexpunge.startsWith("Y")) {
					System.out.println("--- Close with expunge..." + new Date());
					inbox.close(true);  // remove the marked messages from the server (expunge)
				}
				else 	inbox.close(false);
			}
			else inbox.close(false);


			store.close();

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("\n*** Jvakt.PurgeImap4Msgs * Finished --- " + new Date());


	} // slut på pgm

	static void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configF);
			prop.load(input);
			// get the property value and print it out
			port = Integer.parseInt(jvport);
			uname    = prop.getProperty("imapuser");
			pwd      = prop.getProperty("imappwd");
			if (pwd.startsWith("==y")) {
			    byte[] decodedBytes = Base64.getDecoder().decode(pwd.substring(3));
			    String decodedString = new String(decodedBytes);
			    pwd=decodedString;
			}
			imaphost = prop.getProperty("imaphost");
			imapport = prop.getProperty("imapport");
			imapssl = prop.getProperty("imapSSL");
			imaprw = prop.getProperty("imapRW");
			imapcopy = prop.getProperty("imapCopy");
			imapdelete = prop.getProperty("imapDelete");
			imapexpunge = prop.getProperty("imapExpunge");
			imapFolder = prop.getProperty("imapFolder");
			imapFolderc = prop.getProperty("imapFolderC");
			Pfrom = prop.getProperty("From");
			Psubject = prop.getProperty("Subject");
			Pbody = prop.getProperty("Body");
			//			int imapporti = Integer.parseInt(imapport);

			System.out.println("Search criteria used during open phase: ") ;
			System.out.println("Pfrom        : "+Pfrom) ;
			System.out.println("Psubject     : "+Psubject) ;
			System.out.println("Pbody        : "+Pbody) ;
			System.out.println("Source Folder: "+imapFolder) ;
			System.out.println("Target Folder: "+imapFolderc) ;
			System.out.println("Copy: "+imapcopy +"   Copy: "+imapdelete +"   Expunge: "+imapexpunge) ;

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		//		try {
		//			inet = InetAddress.getLocalHost();
		//			System.out.println("-- Inet: "+inet);
		//			agent = inet.toString();
		//		}
		//		catch (Exception e) { System.out.println(e);  }

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
