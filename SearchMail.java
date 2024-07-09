package Jvakt;
/*
 * 2024-07-09 V.57 Michael Ekdal		Added more info when reporting in status to Jvakt server.
 * 2023-11-08 V.56 Michael Ekdal		Removed some System.out.println. They can cause problem in Linux when null.
 * 2023-01-10 V.55 Michael Ekdal		Added send of the status to Jvakt server
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import jakarta.mail.*;
import jakarta.mail.search.*;
import java.util.*;

import Jvakt.Message;

import java.io.*;
import java.net.InetAddress;

public class SearchMail {

	static boolean msgFound = false;
	static boolean msgFixat = false;
	static boolean swHtml = false;
	static boolean swHelp = false;
	static String msgId;
	static boolean swGoodMail = true;
	static boolean swExMail = true;
	static boolean swDormant = false;
	static boolean swOKtot = false;
	static String jvrc   = "";

	static String Sid = null;
	static String Soptional = null;
	static String Stype = null;
	static String Sfrom = null;
	static String Ssubject = null;
	static String Sbody = null;
	static boolean swfrom = false;
	static boolean swsubject = false;
	static boolean swbody = false;
	static boolean swDelete = false;
	static boolean swParfile = false;

	static List<String> listToS;
	//	static String parFile = "SearchMail";
	static String element;
	static char[] parStatus; // Mark with a F when the a parameter line is matched with a mail

	static String from;
	static String subject;
	static String body;
	static int antal;
	static int detached;
	static String agent = null;
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int jvporti ;
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

		String version = "SearchMail  ";
		version += getVersion()+".57";

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-help")) swHelp = true;
			if (args[i].equalsIgnoreCase("-?")) swHelp = true;
		}

		if (swHelp || args.length==0) {
			System.out.println("--- " + version + " ---");
			System.out.println("by Michael Ekdal Perstorp Sweden.\n");
			System.out.println("Search for specific mails identified by the rows in he csv files");

			System.out
			.println("\nThe parameters and their meaning are:\n"
					+ "\n-config   \tThe directory of the input files. Like: \"-dir c:\\Temp\" "
					+ "\n          \t Jvakt.properties and SearchMail-01.csv will be needed."
					);
			System.exit(4);
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("\n"+new Date()+" ----- Jvakt  ----   Version: "+version);
		System.out.println(new Date()+" -config file: "+configF);

		Properties props = new Properties();

		String provider = "imap";  
		int    imapPort = 993; 

		Address[] adr;

		try {

			getProps();  // get Jvakt properties
			readParFile();  // read parameter file

			SendMsg jm = new SendMsg(jvhost, jvporti);  // kollar om JvaktServer är tillgänglig.
			jvrc = jm.open();
			System.out.println(new Date()+" Jvakt server status: "+jvrc);
			//			if (jvrc.toLowerCase().startsWith("dormant")) swDormant = true;
			if (jvrc.toLowerCase().startsWith("failed")) {
				swDormant = true;
				System.out.println(new Date()+" ----- Access to  Jvakt failed!");
			}
			jm.close();

			if (swDormant) {
				//			System.out.println(new Date()+" *** Jvakt in DORMANT mode or unreachable, GetSpecificImap4mail exiting *** ");
				System.out.println(new Date()+" *** Jvakt unreachable, SearchMail exiting *** ");
				System.exit(4);			
			}

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
				System.out.println(new Date()+" Connection to " + imaphost+":"+imapport+"  User "+uname+" failed!");
				System.out.println(new Date()+" "+e);
				System.exit(8);
			}
			System.out.println(new Date()+" Connection to imap4 server established.");

			// Open the folder
			System.out.println(new Date()+" Open folder: " + imapFolder);
			Folder inbox = store.getFolder(imapFolder);
			if (inbox == null) {
				System.out.println(new Date()+" Mailbox folder "+imapFolder+" not found!");
				System.exit(1);
			}

			if (imaprw.startsWith("Y")) inbox.open(Folder.READ_WRITE);  // remove the marked messages from the server
			else 						inbox.open(Folder.READ_ONLY);

			jakarta.mail.Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

			System.out.println(new Date()+" **** Number of rows -> " +listToS.size());
			System.out.println(new Date()+" **** Number of mails-> " +messages.length);

			// Reads all the unseen messages (mails) 
			for (int i = 0; i < messages.length; i++) {
				adr = messages[i].getFrom(); 
				from = null;
				if (adr != null) {
					for (int j = 0; j < adr.length; j++) {
						from = adr[j].toString();
					}
				} else {
					from = "unknown";
				}
				System.out.println("\n"+new Date()+" >>> Mail from From: " + from );

				subject = messages[i].getSubject();
				System.out.println(new Date()+" MimeType> " + messages[i].getContentType());
				System.out.println(new Date()+" Subject: " + subject);

				body = "";
				swHtml = false;
				Object o = null;
				if (messages[i].isMimeType("text/plain") || messages[i].isMimeType("text/html") ) {
					try {
						o = messages[i].getContent();
						body = (String)o;
					} catch (IOException e) {
						body = "";
						System.out.println(new Date()+" The content of the body was not retrieved : " + e);
					}
					if (messages[i].isMimeType("text/html")) swHtml = true;  
				}
				if (messages[i].getContentType().startsWith("multipart")) {
					System.out.println(new Date()+" *** multipart body, will scan the parts... ");
					Multipart mp = (Multipart)messages[i].getContent();
					System.out.println(new Date()+" *** Number of body parts: " + mp.getCount());

					for (int j=0, n=mp.getCount(); j<n; j++) {
						Part part = mp.getBodyPart(j);
//						System.out.println(new Date()+" *** Part "+j+" description : "+part.getDescription());
//						System.out.println(new Date()+" *** Part "+j+" ContentType : "+part.getContentType());
//						System.out.println(new Date()+" *** Part "+j+" Disposition : "+part.getDisposition());
						
						if (part.getContentType().contains("text/")) {
							if (body.length()<=0) 	body  = part.getContent().toString();
							else 					body += part.getContent().toString();
//							System.out.println(new Date()+" *** Body Content : "+body);
						}
//						else if (part.getDisposition().contains("attachment")) {
//							System.out.println(new Date()+" *** File name: "+part.getFileName());
//							body = part.getContent().toString();
//							System.out.println(new Date()+" *** Attachment Content : "+body);
//						}
					}									
				}
				if (body == null) body = "null.";
				//********************************

				System.out.println(new Date()+" From   -> " + from);
				System.out.println(new Date()+" Subject-> " + subject);
//				System.out.println(new Date()+" Body   -> " + body);


				// Checks the mail for a match of all the rows in the parameter file 
				for (int i2 = 0; i2 < listToS.size(); i2++) {
					msgFixat = false;swfrom = false; swsubject = false; swbody = false; 
					swGoodMail = false; swExMail = false; msgFound = false;

					Object object = listToS.get(i2);
					element = (String) object;
					System.out.println("*** ParRow    * " + element);  			// Shows the parameter rows in action
//					System.out.println("*** ParStatus * " + parStatus[i2]); 	// Shows the Status of the line. F indicates a previous match
					String[] tab = null;
					tab = element.split(";");
					Sid = tab[0].trim();
					Soptional = tab[1].toUpperCase().trim();
					if (tab[2].toUpperCase().trim().startsWith("D")) swDelete = true;
					else  swDelete = false;
					Stype = tab[3].toUpperCase().trim();
					Sfrom = tab[4].trim();
					Ssubject = tab[5].trim();
					Sbody = tab[6].trim();
					
					if (Stype.startsWith("G")) swGoodMail = true;
					if (Soptional.startsWith("M")) swExMail = true;

					if (Sfrom==null) swfrom=true;
					if (Ssubject==null) swsubject=true;
					if (Sbody==null) swbody=true;
					//				if (subject == null) subject = "null.";

					if (from.toLowerCase().indexOf(Sfrom.toLowerCase()) >= 0) swfrom=true;
					if (subject.toLowerCase().indexOf(Ssubject.toLowerCase()) >= 0) swsubject=true;
					if (body.toLowerCase().indexOf(Sbody.toLowerCase()) >= 0) swbody=true;

					if (swfrom && swsubject && swbody ) {
						System.out.println(new Date()+" * Mail found!");
						msgFixat = true;
						msgFound = true;
					}


					if (msgFixat && imaprw.startsWith("Y")) { 
						if (swDelete) {
							System.out.println(new Date()+" * Mark as DELETED ");
							messages[i].setFlag(Flags.Flag.DELETED, true);						
						}
						else {
							System.out.println(new Date()+" * Mark as SEEN ");
							messages[i].setFlag(Flags.Flag.SEEN, true); 
						}
					}
					else  {
						System.out.println("* Mail ignored ");
						messages[i].setFlag(Flags.Flag.SEEN, false); // markera mailet som oläst
					}

					if (msgFound ) {
						parStatus[i2]='F'; 
						if (swGoodMail) {
							if (swExMail) {
//								System.out.println(new Date()+" Expected mail from: "+ from +" / "+ subject +" / "+ body );
								System.out.println(new Date()+" Expected mail from: "+ from +" / "+ subject );
								sendJv(Sid , "OK" , "R", "From: "+ from +" / "+ subject +" / "+ body );
							}
							else {
//								System.out.println(new Date()+" Expected mail from: "+ from +" / "+ subject +" / "+ body );
								System.out.println(new Date()+" Expected mail from: "+ from +" / "+ subject);
								sendJv(Sid , "INFO" , "T", "From: "+ from +" / "+ subject +" / "+ body );
							}
						}
						else {
//							System.out.println(new Date()+" From: "+ from +" / "+ subject +" / "+ body );
							System.out.println(new Date()+" From: "+ from +" / "+ subject );
							sendJv(Sid , "ERR" , "T", "From: "+ from +" / "+ subject +" / "+ body );
						}
					}
					if (msgFound ) break;  // read next mail
				}

			} 
			// Close the connection mails are all read
			if (imaprw.startsWith("Y")) inbox.close(true);  // remove the marked messages from the server
			else 						inbox.close(false);
			store.close();
			swOKtot=true;
		}
		catch (Exception ex) {
			swOKtot=false;
			ex.printStackTrace();
		}

		// Loop the parameters an find the ones not matching a mail and report it to Jvakt server.
		System.out.println("\n"+new Date()+" **** Missing mail section ****");
		for (int i2 = 0; i2 < listToS.size(); i2++) {
			Object object = listToS.get(i2);
			element = (String) object;
//			System.out.println("*** ParStatus * " + parStatus[i2]);
			System.out.println("*** ParRow    * " + element);

			if (parStatus[i2]!='F' ) {
				swGoodMail = false; swExMail = false;
				String[] tab = null;
				tab = element.split(";");
				Sid = tab[0].trim();
				Soptional = tab[1].toUpperCase().trim();
				if (tab[2].toUpperCase().trim().startsWith("D")) swDelete = true;
				else  swDelete = false;
				Stype = tab[3].toUpperCase().trim();
				Sfrom = tab[4].trim();
				Ssubject = tab[5].trim();
				Sbody = tab[6].trim();
				
				if (Stype.startsWith("G")) swGoodMail = true;
				if (Soptional.startsWith("M")) swExMail = true;
				
 				if (swGoodMail && swExMail) {
					System.out.println(new Date()+" Expected mail from "+Sfrom+" not found.");
					sendJv(Sid  , "ERR" , "R",  "Expected mail from "+Sfrom+" not found!");
				}
				else {
//					System.out.println(new Date()+" No mail from "+Sfrom+" or "+Ssubject+" or "+Sbody+" found!");
					System.out.println(new Date()+" No mail from "+Sfrom+" found, which is fine!");
					sendJv(Sid , "OK" , "T", "No mail from "+Sfrom+" found, which is fine!");
				}
			}
		}
		
		if (swOKtot ) try {sendSTS(true);}  catch (IOException e) { e.printStackTrace();}
		else 	      try {sendSTS(false);} catch (IOException e) { e.printStackTrace();}


	} // slut på pgm

	// sends status to the Jvakt server
	static protected void sendSTS( boolean STS) throws IOException {
			System.out.println("--- Sending STS to "+jvhost+":"+jvport);
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(jvhost, jvporti);
//			System.out.println(jm.open()); 
			jm.open(); 
			jmsg.setId("Jvakt-SearchMail");
			if (STS) {
				jmsg.setBody("The SearchMail program is working. "+configF.getCanonicalPath());
				jmsg.setRptsts("OK");
			}
			else {
				jmsg.setBody("The SearchMail program is not working! "+configF.getCanonicalPath());
				jmsg.setRptsts("ERR");
			}
			jmsg.setType("T");

			inet = InetAddress.getLocalHost();
//			System.out.println("-- Inet: "+inet);
			agent = inet.toString();

			jmsg.setAgent(agent);
			if (!jm.sendMsg(jmsg)) System.out.println("--- Rpt to Jvakt Failed for SearchMail ---");
			jm.close();
	}

	static protected void sendJv( String ID, String STS, String type, String msg) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, jvporti);
		System.out.println(new Date()+" Jvakt server status: "+jm.open());

		jmsg.setId(ID);
		jmsg.setRptsts(STS);
		jmsg.setBody(msg);
		jmsg.setType(type);
		jmsg.setAgent(agent);
		jmsg.setPrio(30);

		if (jm.sendMsg(jmsg)) {
			System.out.println(new Date()+" -- Rpt to Jvakt server Delivered -- " + ID +" - "+ STS +" - "+ type +" - "+ agent);
			msgFixat = true;
		}
		else  System.out.println(new Date()+"-- Rpt to Jvakt server Failed --");
		jm.close();
	}

//		public static boolean scanFile(String filename, InputStream in)  throws IOException {
//	
//			String s;
//	
//			if (filename == null) {
//				System.out.println("Original filename <null> ");
//				return(false);
//			}
//	
//			if (filename.startsWith("=?")) {
//				System.out.println("Original filename: "+filename);
//				filename = ("noname.pdf");
//			}
//	
//			//    File file = new File("D_"+filename);
//			System.out.println("In file : "+in);
//			BufferedReader in2 = new BufferedReader(new InputStreamReader(in));  
//			antal++;
//			//    if (!filename.toLowerCase().endsWith(".TXT")) { 
//			// 	System.out.println("Kept    : "+filename+"\tFrom: " + from +"\tSubject: " + subject);
//			// 	return(false); }
//	
//			//    byte[] buf = new byte[1024];
//			//   int len;
//			while ((s = in2.readLine()) != null) {
//				if (s.length() == 0) continue; 
//				if (s.indexOf("PXP") >= 0) {
////					swSmq1PXP = true;
//					System.out.println("** Found PXP in input stream!");
//				} 
//				if (s.indexOf("Entries&nbsp;Displayed&#x3a;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp") >= 0) {
//					System.out.println("** Found Entries Displayed: 0 !");
////					swSmq2PCP = true;
//				} 
//				if (s.indexOf("Queues&nbsp;Displayed&#x3a;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp") >= 0) {
//					System.out.println("** Found Queues Displayed: 0 !");
////					swSmq2PCP = true;
//				} 
//				int offset = 0;
//				while (s.indexOf("Transaction&nbsp;executing", offset) >= 0) {
////					Sm58PCPerr++;
//					offset = s.indexOf("Transaction&nbsp;executing", offset);
//					offset++;
////					System.out.println("** Found Transaction executing: " + Sm58PCPerr );
////					swSmq1PXP = false; 
//				}
//			}
//			in2.close();
//			in.close();
//			detached++;
//			return(true);
//		}

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


	static void readParFile() {
		File[] listf;
		DirFilter df;
		String s;
		//		File dir = new File(".");
		File dir;
		String suf = ".csv";
		String pos = "SearchMail";
		int antal = 0;

		if (config == null ) 	dir = new File(".");
		else 					dir = new File(config);



		//		System.out.println("\n*** parameter file: "+" "+dir+ pos+" "+suf);

		listToS = new ArrayList<String>();  // id:mailadress.

		df = new DirFilter(suf, pos);

		listf = dir.listFiles(df);

		System.out.println("\n*** Number of parameter files found: "+ listf.length);
		try {
			BufferedReader in;

			for (int i = 0; i < listf.length; i++) {

				System.out.println("-- Importing parfile: "+listf[i]);
				in = new BufferedReader(new FileReader(listf[i]));

				while ((s = in.readLine()) != null) {
					if (s.length() == 0) continue; 
					if (s.startsWith("#")) continue; 

					listToS.add(s);
					antal++;
					//					System.out.println("-- add: "+s);
				}
				in.close();
			}
		} catch (Exception e) { System.out.println(e);  }
		System.out.println("-- Number of rows imported: "+ antal);
		parStatus = new char[antal];

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
