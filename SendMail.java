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

public class SendMail {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	static boolean swDelete;
	static boolean swTiming;
	static boolean swShDay; // set when the scheduled day is active
	static boolean swDormant = false;
	static java.sql.Date zDate;
	static java.sql.Timestamp zD;
	static java.sql.Timestamp zTs;
	static java.sql.Time zT;
	static Long lhou, lmin, Lsec, Lrptdat, Lchktim;
	static int serrors = 0;
	static int errors = 0;
	static int warnings = 0;
	static int infos = 0;
	static int resolved = 0;

	static String sbody = "";
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
		toEmail  = prop.getProperty("toEmail");
		fromEmail= prop.getProperty("fromEmail");
		uname    = prop.getProperty("smtpuser");
		pwd      = prop.getProperty("smtppwd");
		smtphost = prop.getProperty("smtphost");
		smtpport = prop.getProperty("smtpport");
		int smtpporti = Integer.parseInt(smtpport);
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		input.close();

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

		LocalDateTime nu = LocalDateTime.now(); // The current date and time
		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
		Statement stmt = null;
		String s;
		boolean swHits;
		String cause = "";
		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()); 

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			System.out.println(DBUrl);
			System.out.println("dbuser= " + dbuser +"  dbpassword "+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);

			s = new String("select * from status " + 
					"WHERE (state='A' or  state='D') " +
					" and (msg='M' or msg='T' or msg='R')" +
					" and prio < 3" +
					";"); 

			System.out.println(s);
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
			swHits = false;  // is there already a record?
			while (rs.next()) {
				System.out.println("\n\n---- main RS: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+" "+zD);
				swHits = true;  
				swTiming = false;  

				if (rs.getString("id").equalsIgnoreCase("syssts")) {
					continue;
				}

				if (!rs.getString("type").equalsIgnoreCase("R") && !rs.getString("type").equalsIgnoreCase("S") && !rs.getString("type").equalsIgnoreCase("I")) continue;

				zD = rs.getTimestamp("rptdat");
				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000); 
				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime();

				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").startsWith(DOW.name())) {
					swShDay = true;
				} else swShDay = false;

				swDelete = false;

				// Om fel inträffat för S och R varnas till console
				if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") < 3 ) { 
					cause = "Problem :\t";
					serrors++;
					sbody = sbody +rowStr+boxStrM+ rs.getString("id")+boxEnd +boxStrM+ rs.getString("body")+boxEnd+rowEnd;
				}
				if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") >= 3 ) { 
					cause = "Problem :\t";
					errors++;
					ebody = ebody +rowStr+boxStrR+ rs.getString("id")+boxEnd +boxStrR+ rs.getString("body")+boxEnd+rowEnd;
				}
				else if (rs.getString("msg").equalsIgnoreCase("R")) {
					cause = "Resolved:\t";
					resolved++;
					rbody = rbody +rowStr+boxStrG+ rs.getString("id")+boxEnd +boxStrG+ rs.getString("body")+boxEnd+rowEnd;
				}
				else {
					cause = "Time out:\t";
					warnings++;
					wbody = wbody +rowStr+boxStrY+ rs.getString("id")+boxEnd +boxStrY+ rs.getString("body")+boxEnd+rowEnd;
				}
				swMail = true;
				if (rs.getString("msg").equalsIgnoreCase("R")) rs.updateString("msg", " ");
				else rs.updateString("msg", "S");
				rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
				try { rs.updateRow(); } catch(NullPointerException npe2) {}
			}
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
			subject = "Changed STATUS: ";
			body = tblStr;

			System.out.println("\n\nSubject: " + subject );
			if (sbody.length() > 0) {
				subject = subject + "Severes: " + serrors + "  ";
				body = body + rowStr+hdrStrM+"SEVERE" +hdrEnd+hdrStrM+""+hdrEnd+rowEnd+	sbody ;
			}
			if (ebody.length() > 0) {
				subject = subject + "Errors: " + errors + "  ";
				body = body + rowStr+hdrStrR+"WARNING" +hdrEnd+hdrStrR+""+hdrEnd+rowEnd+	ebody ;
			}
			if (wbody.length() > 0) {
				subject = subject + "Warnings: " + warnings + "  ";
				body = body + rowStr+hdrStrY+"TIME-OUT"+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	wbody ;
			}
			if (rbody.length() > 0) { 
				subject = subject + "Resolved: " + resolved;
				body = body + rowStr+hdrStrG+"RESOLVED" +hdrEnd+hdrStrG+""+hdrEnd+rowEnd+	rbody ;
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