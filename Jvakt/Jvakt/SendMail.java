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
	//    static ResultSet rs;
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

		LocalDateTime nu = LocalDateTime.now(); // The current date and time
		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
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

		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()); 

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			//"jdbc:postgresql://localhost:5433/Jvakt";
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			System.out.println(DBUrl);
//			conn = DriverManager.getConnection(DBUrl,"Jvakt","xz");
			System.out.println("dbuser= " + dbuser +"  dbpassword "+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);

			s = new String("select * from status " + 
					"WHERE (state='A' or  state='D') " +
					" and (msg='M' or msg='T' or msg='R')" +
					" and prio < 3" +
					";"); 
//			s = new String("select * from status " + 
//					"WHERE (state='A' or  state='D');"); 


			System.out.println(s);
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
			//     System.out.println("Query executed");
			swHits = false;  // is there already a record?
			while (rs.next()) {
				System.out.println("\n\n---- main RS: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+" "+zD);
				swHits = true;  
				swTiming = false;  
				
				if (rs.getString("id").equalsIgnoreCase("syssts")) {
//					System.out.println("Found SYSSTS");
//					subject = rs.getString("body");
					continue;
				}
//				if (rs.getString("msg").startsWith("S") || rs.getString("msg").startsWith(" ")) continue;

				if (!rs.getString("type").equalsIgnoreCase("R") && !rs.getString("type").equalsIgnoreCase("S") && !rs.getString("type").equalsIgnoreCase("I")) continue;
				
				zD = rs.getTimestamp("rptdat");

//				System.out.println("zD : " + zD);
//				System.out.println("zTs : " + zTs);

				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000); 
//				System.out.println("Diff sec : " + Lsec);

				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime();
//				System.out.println("chktim msec : " + Lchktim + " " + rs.getTime("chktim"));
//				System.out.println("rptdat msec : " + Lrptdat + " " + rs.getTime("rptdat") + " "+ rs.getTimestamp("rptdat").getTime());
//				System.out.println("mi msec: " + mi.getTime()+" "+mi);

				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").startsWith(DOW.name())) {
//					System.out.println("chkday : " + rs.getString("chkday") + " " + DOW.name());
					swShDay = true;
				} else swShDay = false;
				
				swDelete = false;

				// Om fel inträffat för S och R varnas till console
//				if (rs.getString("msg").equalsIgnoreCase("M") || rs.getString("msg").equalsIgnoreCase("T") ) {
//					System.out.println("ERR Mail RS: " + rs.getString("id")+" "+rs.getString("status"));
//					System.out.println("Set msg to S");
				if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") < 3 ) { 
					cause = "Problem :\t";
					serrors++;
					sbody = sbody +rowStr+boxStrM+ rs.getString("id")+boxEnd +boxStrM+ rs.getString("body")+boxEnd+rowEnd;
				}
					if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") >= 3 ) { 
						cause = "Problem :\t";
						errors++;
//						ebody = ebody + ebody.format("%-30s %-30s %n", rs.getString("id"),rs.getString("body"));
						ebody = ebody +rowStr+boxStrR+ rs.getString("id")+boxEnd +boxStrR+ rs.getString("body")+boxEnd+rowEnd;
					}
					else if (rs.getString("msg").equalsIgnoreCase("R")) {
						cause = "Resolved:\t";
						resolved++;
//						rbody = rbody + rbody.format("%-30s %-30s %n", rs.getString("id"),rs.getString("body"));
						rbody = rbody +rowStr+boxStrG+ rs.getString("id")+boxEnd +boxStrG+ rs.getString("body")+boxEnd+rowEnd;
					}
							else {
								cause = "Time out:\t";
								warnings++;
//								wbody = wbody + wbody.format("%-30s %-30s %n", rs.getString("id"),rs.getString("body"));
								wbody = wbody +rowStr+boxStrY+ rs.getString("id")+boxEnd +boxStrY+ rs.getString("body")+boxEnd+rowEnd;
							}
//					body = body + "\n" + cause +" "+rs.getString("id")+"\t\t "+rs.getString("body");
					swMail = true;
					if (rs.getString("msg").equalsIgnoreCase("R")) rs.updateString("msg", " ");
					else rs.updateString("msg", "S");
					rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
				try { rs.updateRow(); } catch(NullPointerException npe2) {}
					
//				}  // Om allt bra tas msg bort
//				else	 if (rs.getString("msg").equalsIgnoreCase("S") && 
//						     rs.getString("status").equalsIgnoreCase("OK") ) {
//					System.out.println("OK mail   RS: " + rs.getString("id")+" "+rs.getString("status"));
//						System.out.println("Set msg to blank in OK");
//						rs.updateString("msg", " ");
//						rs.updateTimestamp("msgdat", null);
//					try { rs.updateRow(); } catch(NullPointerException npe2) {}
//				}
//				else { System.out.println("NOTHING RS: " + rs.getString("id")+" "+rs.getString("status")); }
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
//			subject = "Changed STATUS: Errors: " + errors + "  Warnings: " + warnings + "  Resolved: " + resolved ;
			subject = "Changed STATUS: ";
			body = tblStr;

			//			System.out.println("CheckStatus Severe: " + errors + "  Problems: " + warnings + "  Info: " + infos ); 
			System.out.println("\n\nSubject: " + subject );
			if (sbody.length() > 0) {
				subject = subject + "Severes: " + serrors + "  ";
				body = body + rowStr+hdrStrM+"SEVERE" +hdrEnd+hdrStrM+""+hdrEnd+rowEnd+	sbody ;
			}
			if (ebody.length() > 0) {
				subject = subject + "Errors: " + errors + "  ";
//				ebody = rowStr+boxStrR+"none"+boxEnd+boxStrR+"none"+boxEnd+rowEnd;
				body = body + rowStr+hdrStrR+"WARNING" +hdrEnd+hdrStrR+""+hdrEnd+rowEnd+	ebody ;
			}
			if (wbody.length() > 0) {
				subject = subject + "Warnings: " + warnings + "  ";
//				wbody = rowStr+boxStrY+"none"+boxEnd+boxStrY+"none"+boxEnd+rowEnd;
				body = body + rowStr+hdrStrY+"TIME-OUT"+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	wbody ;
			}
			if (rbody.length() > 0) { 
				subject = subject + "Resolved: " + resolved;
//				rbody = rowStr+boxStrG+"none"+boxEnd+boxStrG+"none"+boxEnd+rowEnd;
				body = body + rowStr+hdrStrG+"RESOLVED" +hdrEnd+hdrStrG+""+hdrEnd+rowEnd+	rbody ;
			}
//			body = "--- WARNINGS ---\n"+ ebody +"\n--- TIME-OUTS ---\n"+ wbody+"\n--- RESOLVED ---\n"+ rbody;
//			body = tblStr+
//					rowStr+hdrStrR+"WARNING ID" +hdrEnd+hdrStrR+""+hdrEnd+rowEnd+
//					ebody +
//					rowStr+hdrStrY+"TIME-OUT ID"+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+
//					wbody+
//					rowStr+hdrStrG+"RESOLVED ID" +hdrEnd+hdrStrG+""+hdrEnd+rowEnd+
//					rbody+
//					tblEnd;
			body = body + tblEnd;
			System.out.println( body );
			
			if (swMail && !swDormant) {
			Session session = Session.getInstance(props, auth);
			EmailUtil.sendEmail(session, toEmail,subject, body, fromEmail);
			}
			
		}
	}        

}