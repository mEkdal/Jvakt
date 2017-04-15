package Jvakt;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.time.*;
import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SendMailSTS {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
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
	static String boxStrB = "<TD BGCOLOR=\"#CCEEFF\">"; // Light blue
	static String boxStr  = "<TD>"; 
	static String boxEnd  = "</TD>";



	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "jVakt 2.0 - SendMail 1.0 Date 2017-02-21_01";
		String database = "jVakt";
		String dbuser   = "jVakt";
		String dbpassword = "xz";
		String dbhost   = "localhost";
		String dbport   = "5433";
		String jvhost   = "localhost";
		String jvport   = "1956";
		
	      //Declare recipient's & sender's e-mail id.
	      final String toEmail;
	      final String fromEmail;	
	      final String uname;
	      final String pwd;
	      final String smtphost;
 		  final String smtpport;
	      
		String subject = "";
		String body = "";
		String value = "";
		
		boolean swMail = false;
		
		Properties prop = new Properties();
		InputStream input = null;
//		try {
		input = new FileInputStream("jVakt.properties");
		prop.load(input);
		// get the property value and print it out
		database = prop.getProperty("database");
		dbuser   = prop.getProperty("dbuser");
		dbpassword = prop.getProperty("dbpassword");
		dbhost   = prop.getProperty("dbhost");
		dbport   = prop.getProperty("dbport");
		jvport   = prop.getProperty("jvport");
		jvhost   = prop.getProperty("jvhost");
		toEmail  = prop.getProperty("toSts");
		fromEmail= prop.getProperty("fromEmail");
		uname    = prop.getProperty("smtpuser");
		pwd      = prop.getProperty("smtppwd");
		smtphost = prop.getProperty("smtphost");
		smtpport = prop.getProperty("smtpport");
		int smtpporti = Integer.parseInt(smtpport);
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		input.close();
//		} catch (IOException ex) {
//			// ex.printStackTrace();
//		}

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
	      props.put("mail.smtp.host", smtphost);
	      props.put("mail.smtp.port", smtpporti);

//		LocalDateTime nu = LocalDateTime.now(); // The current date and time
//		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
//		Timestamp mi = Timestamp.valueOf(midnatt);
//		DayOfWeek DOW = nu.getDayOfWeek(); 
//		Instant instant = mi.toInstant();

/*		System.out.println("Nu = " + nu);
		System.out.println("Midnatt = " + midnatt);
		System.out.println(nu.getYear() + "-" + nu.getMonthValue() + "-"+ nu.getDayOfMonth() );
		System.out.println("Midnatt mi = " + mi);
		System.out.println("DOW = " + DOW.name() +"  Daynum "+ DOW.getValue());
*/

		//	 Connection conn = null;
		Statement stmt = null;
//		PreparedStatement pStmt = null;
		String s;
		boolean swHits;
		String cause = "";
		//PreparedStatement q1;
//		int updated;
//		int sessnum;
//		int accerr;
//		int count;
		//static Date now;

//		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());
//		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()); 

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			//"jdbc:postgresql://localhost:5433/Jvakt";
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			System.out.println(DBUrl);
//			conn = DriverManager.getConnection(DBUrl,"Jvakt","xz");
			System.out.println("dbuser= " + dbuser +"  dbpassword "+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);

			s = new String("select * from console;"); 


			System.out.println(s);
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
			//     System.out.println("Query executed");
			swHits = false;  // is there already a record?
			body = tblStr;

			while (rs.next()) {
//				System.out.println("\n\n---- main RS: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+" "+zD);
				swHits = true;  
				swTiming = false;  
				
			    swFound = true;
			    body = body +rowStr;
//--
			for (int i = 1; i <= 7; i++) {
			    value = rs.getString (i);
	  		       
			    if (rs.getInt("prio") < 3 && rs.getString("status").contentEquals("ERR")) {
			    	body = body + boxStrM + value + boxEnd;
			    	errors++;
			    }
			    else if (rs.getInt("prio") >= 3 && rs.getString("status").contentEquals("ERR")) {
			    	body = body + boxStrR + value + boxEnd;
			    	warnings++;
			    }
			    else if (rs.getString("status").contentEquals("INFO")) {
			    	body = body + boxStrB + value + boxEnd;
			    }
			    else if (rs.getString("status").startsWith("Tim")) {
			    	body = body + boxStrY + value + boxEnd;
			    	warnings++;
			    }
			    else if (rs.getString("status").contentEquals("OK"))	{
			    	body = body + boxStrG + value + boxEnd;
			    }
			    else {
		    		body = body + boxStrB + value + boxEnd;
			    }
			    	 
			}
			body = body + rowEnd; 
			
//--
			}
					swMail = true;
			rs.close(); 
			stmt.close();

		}
		catch (SQLException e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		finally { 
			subject = "SYSSTS: ";

			//			System.out.println("CheckStatus Severe: " + errors + "  Problems: " + warnings + "  Info: " + infos ); 
			System.out.println("\n\nSubject: " + subject );
			if (errors > 0) {
				errors = errors / 7;
				subject = subject + "Errors: " + errors + "  ";
			}
			if (warnings > 0) {
				warnings = warnings / 7;
				subject = subject + "Warnings: " + warnings + "  ";
			}
			body = body + tblEnd;
			System.out.println( body );
			
			if (swMail && !swDormant) {
			Session session = Session.getInstance(props, auth);
			EmailUtil.sendEmail(session, toEmail,subject, body, fromEmail);
			}
			
		}
	}        

}