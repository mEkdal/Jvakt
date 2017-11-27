package Jvakt;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.util.*;
import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SendMailSingle {

	//	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	//    static ResultSet rs;
	static Connection conn = null;
	static boolean swDelete;
	static boolean swTiming;
	static boolean swFound;
	static boolean swShDay; // set when the scheduled day is active
	static boolean swDormant = false;
	static java.sql.Date zDate;
	static java.sql.Timestamp zD;
	static java.sql.Timestamp zTs;
	static java.sql.Time zT;
	static Long lhou, lmin, Lsec, Lrptdat, Lchktim;
	static int errors = 0;
	static int warnings = 0;
	static int infos = 0;
	static int resolved = 0;
	static String ebody = "";
	static String wbody = "";
	static String rbody = "";

	static String tblStr = "<TABLE COLS=2 BORDER=4 cellpadding=\"3\" width=\"100%\"  >"; 
	static String tblEnd = "</TABLE>";

	static String hdrStrG = "<TH BGCOLOR=\"#00FF00\"><FONT SIZE=5>"; // Green
	static String hdrStrY = "<TH BGCOLOR=\"#FFFF00\"><FONT SIZE=5>"; // Yellow
	static String hdrStrR = "<TH BGCOLOR=\"#FF6600\"><FONT SIZE=5>"; // Red
	static String hdrStrM = "<TH BGCOLOR=\"#FF00FF\"><FONT SIZE=5>"; // Magenta
	static String hdrEnd = "</TH>";

	static String rowStr = "<TR>"; 
	static String rowEnd = "</TR>";
	static String boxStrG = "<TD BGCOLOR=\"#00FF00\">"; // Green
	static String boxStrY = "<TD BGCOLOR=\"#FFFF00\">"; // Yellow
	static String boxStrR = "<TD BGCOLOR=\"#FF6600\">"; // Red
	static String boxStrM = "<TD BGCOLOR=\"#FF00FF\">"; // Magenta
	static String boxStr  = "<TD>"; 
	static String boxEnd  = "</TD>";



	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "SendMailSingle 1.2 # 2017-11-15";

		//Declare recipient's & sender's e-mail id.
		final String toEmail;
		final String fromEmail;	
		final String uname;
		final String pwd;
		final String smtphost;
		final String smtpport;

		String to = null;
		String subject = null;
		String body = null;
//		String status= "default";
//		String sub = "default";

		String config = null;
		File configF;

		boolean swMail = false;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-to")) to = args[++i];
			else if (args[i].equalsIgnoreCase("-body")) body = args[++i];
			else if (args[i].equalsIgnoreCase("-subject")) subject = args[++i];
			else if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version);
		System.out.println("To:"+to+" Subject:"+subject+" Body:"+body );		    
 
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");

		if (args.length < 1 || to == null || subject == null || body == null  ) {
			System.out.println("\n"+version);
			System.out.println("by Michael Ekdal Perstorp Sweden.\n");
			System.out.println("-to 	 \t - Email address");
			System.out.println("-subject \t - Subject");
			System.out.println("-body	 \t - Body");
			System.out.println("-config	 \t - Folder of config files");
			System.exit(4);
		}

		System.out.println("-config file: "+configF);
		Properties prop = new Properties();
		InputStream input = null;
		//		try {
//		input = new FileInputStream(config + "jVakt.properties");
		input = new FileInputStream(configF);
		prop.load(input);
		// get the property value and print it out
		fromEmail= prop.getProperty("fromEmail");
		uname    = prop.getProperty("smtpuser");
		pwd      = prop.getProperty("smtppwd");
		smtphost = prop.getProperty("smtphost");
		smtpport = prop.getProperty("smtpport");
		
		if (fromEmail == null || uname == null || pwd == null || smtphost == null || smtpport == null  ) {
			System.out.println("==> fromEmail, smtpuser, smtppwd,smtphost and smtpport must be present in the Jvakt.properties file! <==\n");
			System.exit(4);
		}
		
		int smtpporti = Integer.parseInt(smtpport);
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		input.close();

		//create Authenticator object to pass in Session.getInstance argument
		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, pwd);
			}
		};

		//Set properties and their values
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtphost);
		props.put("mail.smtp.port", smtpporti);

		swMail = true;
//		subject = sub;
		toEmail = to;
		//			System.out.println("CheckStatus Severe: " + errors + "  Problems: " + warnings + "  Info: " + infos ); 
		System.out.println("\nTo: " + toEmail );
		System.out.println("From: " + fromEmail );
		System.out.println("Subject: " + subject );
		System.out.println("Body: " + body + "\n");

		if (swMail && !swDormant) {
			Session session = Session.getInstance(props, auth);
			EmailUtil.sendEmail(session, toEmail,subject, body, fromEmail);
		}


	}    

}