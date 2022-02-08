package Jvakt;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.sql.Timestamp;
import java.util.*;
//import java.time.*;
import jakarta.mail.*;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
//import javax.mail.*;
//import javax.mail.Authenticator;
//import javax.mail.PasswordAuthentication;

public class SendMailSTS {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	static boolean swDelete;
	static boolean swTiming;
	static boolean swFound;
	static boolean swShDay; // set when the scheduled day is active
	static boolean swDormant = false;
	static boolean swDB = true;
	static boolean swServer = true;
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
	static Date now;

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
	static String boxStrB = "<TD BGCOLOR=\"#CCEEFF\">"; // Light blue
	static String boxStr  = "<TD>"; 
	static String boxEnd  = "</TD>";

	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "??";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int    port   = 1956;

	//Declare recipient's & sender's e-mail id.
	static String PtoEmail;
	static String PfromEmail = "";	
	static String Puname = "";
	static String Ppwd = "";
	static String Psmtphost = "";
	static String Psmtpport = "";
	static int smtpporti;
	
	static String config = null;
	static File configF;


	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "SendMailSTS (2022-FEB-08)";

		String subject = "";
		String body = "";
		String value = "";

		//Declare recipient's & sender's e-mail id.
		final String toEmail;
		final String fromEmail;	
//		final String uname;
		final String pwd;
		final String smtphost;
//		final String smtpport;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}
 
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version+"  -  config file: "+configF);
		
		boolean swMail = false;
		getProps();
		toEmail = PtoEmail;
		fromEmail = PfromEmail;	
//		uname = Puname;
		pwd = Ppwd;
		smtphost = Psmtphost;
//		smtpport = Psmtpport;
//		System.out.println(s);
		
		//create Authenticator object to pass in Session.getInstance argument
		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, pwd);
			}
		};

		//Set properties and their values
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.host", smtphost);
		props.put("mail.smtp.port", smtpporti);

		Statement stmt = null;
		String s;
//		boolean swHits;
//		String cause = "";

		swServer = true;
//		try {
			port = Integer.parseInt(jvport);
			SendMsg jm = new SendMsg(jvhost, port);  // kollar om JvaktServer är tillgänglig.
//			System.out.println(jm.open());
			if (jm.open().startsWith("DORMANT")) 	swDormant = true;
			else 									swDormant = false;
			jm.close();
//		} 
//		catch (IOException e1) {
//			swServer = false;
//			System.err.println(e1);
//			System.err.println(e1.getMessage());
//		}
//		System.out.println("swServer :" + swServer);
		
		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
//			System.out.println(DBUrl);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);

			s = new String("select * from console order by credat desc;"); 


//			System.out.println(s);
//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE);
//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
//			swHits = false;  // is there already a record?
			body = tblStr;

			while (rs.next()) {
//				swHits = true;  
				swTiming = false;  

				swFound = true;
				body = body +rowStr;
				//--
				for (int i = 1; i <= 9; i++) {
					if (i==1) continue;  // not interested in showing count
					if (i==5) continue;  // not interested in showing condat
					if (i==9) continue;  // not interested in showing agent
					value = rs.getString (i);

					if (rs.getInt("prio") < 30 && rs.getString("status").contentEquals("ERR")) {
						body = body + boxStrM + value + boxEnd;
						errors++;
					}
					else if (rs.getInt("prio") >= 30 && rs.getString("status").contentEquals("ERR")) {
						body = body + boxStrR + value + boxEnd;
						warnings++;
					}
					else if (rs.getString("status").contentEquals("INFO")) {
						body = body + boxStrB + value + boxEnd;
						infos++;
					}
					else if (rs.getString("status").startsWith("TOut")) {
						body = body + boxStrY + value + boxEnd;
						warnings++;
					}
					else if (rs.getString("status").contentEquals("OK"))	{
						body = body + boxStrG + value + boxEnd;
						infos++;
					}
					else {
						body = body + boxStrB + value + boxEnd;
						infos++;
					}

				}
				body = body + rowEnd; 

				//--
			}
			swMail = true;
			rs.close(); 
			stmt.close();
			conn.close();
		}
		catch (SQLException e) {
			System.err.println("*** SQLExeption");
			swDB = false;
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			swDB = false;
			System.err.println(e);
			System.err.println(e.getMessage());
		}
//		finally { 
//			subject = "Status: ";
//			subject = "";
//		}
		
		subject = "* STATUS * ";
		
			if (errors > 0) {
				errors = errors / 6;
				subject = subject + "Errors: " + errors + "  ";
			}
			if (warnings > 0) {
				warnings = warnings / 6;
				subject = subject + "Warnings: " + warnings + "  ";
			}
			if (errors == 0 && warnings == 0) {
				subject = subject + "Jvakt: all is OKAY!  ";
			}
			if (infos > 0) {
				infos = infos / 6;
				subject = subject + "Infos: " + infos + "  ";
			}
			
			body = body + tblEnd;

			if (!swDB) {
				subject = "\n - Jvakt Database not accessible ! -\n"; 
				body = "<br><p> - Jvakt Database not accessible ! -</p>"; 
				swMail = true;
			}
			if (!swServer) {
				if (swDB) {
					subject = "";
					body = "";
				}
				subject = subject + "\n - Jvakt Server not accessible ! - \n"; 
				body = body + "<br><p> - Jvakt Server not accessible ! -</p>"; 
				swMail = true;
			}
			 
			if (errors == 0 && warnings == 0 && infos == 0 ) body = body + "<br><p> All is OKAY! There is nothing to report!</p>";
			else body = body + "<br><p> - On-call Duty -<br>Only rows with Prio less than 30 is to be handeled out of office hours !</p>"; 
			
			now = new Date();
			subject = subject + " -- " + now;
			System.out.println("-> Mail receivers: " + toEmail );
			System.out.println("-> Mail subject  : " + subject );
			System.out.println("-> Mail body     : " + body );
			
			if (swMail && !swDormant) {
				Session session = Session.getInstance(props, auth);
				if (EmailUtil.sendEmail(session, toEmail,subject, body, fromEmail)) {};
			}

	}        

	static void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
		input = new FileInputStream(configF);
		prop.load(input);
		// get the property value and print it out
		database = prop.getProperty("database");
		dbuser   = prop.getProperty("dbuser");
		dbpassword = prop.getProperty("dbpassword");
		if (dbpassword.startsWith("==y")) {
		    byte[] decodedBytes = Base64.getDecoder().decode(dbpassword.substring(3));
		    String decodedString = new String(decodedBytes);
		    dbpassword=decodedString;
		}
		dbhost   = prop.getProperty("dbhost");
		dbport   = prop.getProperty("dbport");
		jvport   = prop.getProperty("jvport");
		jvhost   = prop.getProperty("jvhost");
		PtoEmail  = prop.getProperty("toSts");
		PfromEmail= prop.getProperty("fromEmail");
		Puname    = prop.getProperty("smtpuser");
		Ppwd      = prop.getProperty("smtppwd");
		if (Ppwd.startsWith("==y")) {
		    byte[] decodedBytes = Base64.getDecoder().decode(Ppwd.substring(3));
		    String decodedString = new String(decodedBytes);
		    Ppwd=decodedString;
		}
		Psmtphost = prop.getProperty("smtphost");
		Psmtpport = prop.getProperty("smtpport");
		smtpporti = Integer.parseInt(Psmtpport);
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		input.close();
		} catch (IOException ex) {
    		// ex.printStackTrace();
    	}  
	}

}