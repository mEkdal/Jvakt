package Jvakt;
/*
 * 2024-07-09 V.56 Michael Ekdal		Added more info when reporting in status to Jvakt server.
 * 2023-01-09 V.55 Michael Ekdal		Added send of the status to Jvakt server
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import Jvakt.Message;

import java.time.*;
import jakarta.mail.*;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class SendMail {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	//	static boolean swDelete;
	static boolean swTiming;
	static boolean swShDay; // set when the scheduled day is active
	static boolean swDormant = false;
	static boolean swOK = false;
	
	static boolean swShowVer = true;

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
	static Date now;
	
	static InetAddress inet;
	static String agent = null;

	static String sbody = "";
	static String ebody = "";
	static String wbody = "";
	static String rbody = "";

	static String tblStr = "<TABLE COLS=2 BORDER=4 cellpadding=\"3\" width=\"100%\"  >"; 
	static String tblEnd = "</TABLE>";

	static String hdrStrG = "<TH BGCOLOR=\"#00FF00\"><FONT SIZE=3>"; // Green
	static String hdrStrY = "<TH BGCOLOR=\"#FFFF00\"><FONT SIZE=3>"; // Yellow
	static String hdrStrR = "<TH BGCOLOR=\"#FF6600\"><FONT SIZE=3>"; // Red
	static String hdrStrM = "<TH BGCOLOR=\"#FF00FF\"><FONT SIZE=3>"; // Magenta
	static String hdrEnd = "</TH>";

	static String rowStr = "<TR>"; 
	static String rowEnd = "</TR>";
	static String boxStrG = "<TD BGCOLOR=\"#00FF00\">"; // Green
	static String boxStrY = "<TD BGCOLOR=\"#FFFF00\">"; // Yellow
	static String boxStrR = "<TD BGCOLOR=\"#FF6600\">"; // Red
	static String boxStrM = "<TD BGCOLOR=\"#FF00FF\">"; // Magenta
	static String boxStrB = "<TH BGCOLOR=\"#CCEEFF\">"; // Light blue
	static String boxStr  = "<TD>"; 
	static String boxEnd  = "</TD>";

	static String subject = "";
	static String body = "";

	static String toEmailW;

	static boolean swMail = false;

	static Properties props;
	static Properties prop;

	static String toEmail;
	static String fromEmail;	
	static String uname;
	static String pwd;
	static String smtphost;
	static String smtpport;

	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int    jvporti   = 1956;

	static File configF;

	static Authenticator auth;

	static List<String> listTo;
	
	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "SendMail ";
		version += getVersion()+".56";
		String database = "jVakt";
		String dbuser   = "jVakt";
		String dbpassword = "";
		String dbhost   = "localhost";
		String dbport   = "5433";

		String config = null;
//		File configF;
		
//		java.sql.Time sqlt;
		Calendar cal = Calendar.getInstance();

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-nover")) swShowVer =false;
		}
 
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		
		if (swShowVer) System.out.println("----- Jvakt: "+new Date()+"    Version: "+version+"  -  config file: "+configF);
		
		//Declare recipient's & sender's e-mail id.
//		String toEmailW;
//		final String toEmail;
//		final String fromEmail;	
//		final String uname;
//		final String pwd;
//		final String smtphost;
//		final String smtpport;

//		String subject = "";
//		String body = "";
//		boolean swMail = false;

//		Properties prop = new Properties();
		prop = new Properties();
		InputStream input = null;
		//		try {
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
		jvporti = Integer.parseInt(jvport);
		jvhost   = prop.getProperty("jvhost");
		toEmailW  = prop.getProperty("toEmail");
		fromEmail= prop.getProperty("fromEmail");
		uname    = prop.getProperty("smtpuser");
		pwd      = prop.getProperty("smtppwd");
		if (pwd.startsWith("==y")) {
		    byte[] decodedBytes = Base64.getDecoder().decode(pwd.substring(3));
		    String decodedString = new String(decodedBytes);
		    pwd=decodedString;
		}
		smtphost = prop.getProperty("smtphost");
		smtpport = prop.getProperty("smtpport");
		int smtpporti = Integer.parseInt(smtpport);
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		input.close();

		
		listTo = new ArrayList<String>();  // Alla mailadresser.
		
		String[] toAddr = toEmailW.split("\\,");
		for(int i=0 ; i<toAddr.length;i++) {
//			System.out.println(toAddr[i]);
			listTo.add(toAddr[i]);
		}
		
//		Iterator iterator = listTo.iterator();
//		while(iterator.hasNext()) {
////		  String element = (String) iterator.next();
//		  System.out.println(iterator.next());
//		}
		
//		try {
			SendMsg jm = new SendMsg(jvhost, jvporti);  // kollar om JvaktServer är tillgänglig.
//			System.out.println(jm.open());
			if (jm.open().startsWith("DORMANT")) {
				swDormant = true;
			}
			jm.close();
//		} 
//		catch (IOException e1) {
//			System.err.println(e1);
//			System.err.println(e1.getMessage());
//		}

		if (swDormant) {
			System.out.println(new Date()+" *** Jvakt in DORMANT mode, SendMail exiting *** ");
			System.exit(4);			
		}


		//create Authenticator object to pass in Session.getInstance argument
		auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, pwd);
			}
		};

		//Set properties and their values
		props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.host", smtphost);
		props.put("mail.smtp.port", smtpporti);

		LocalDateTime nu = LocalDateTime.now(); // The current date and time
//		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
//		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
		Statement stmt = null;
		String s;
		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()); 
		
//		System.out.println("**********SendMail ********   " + LocalDateTime.now());
		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(false);

			s = new String("select * from status " + 
					"WHERE state='A' " +
					" and (msg='M' or msg='T' or msg='R')" +
					" and prio < 30" +
					";"); 

//			System.out.println(s);
//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
			while (rs.next()) {
				System.out.println(new Date()+" - main RS - State:"+rs.getString("state")+" Id:" + rs.getString("id")+" Type:"+rs.getString("type")+" Prio:"+rs.getString("prio")+" Console:"+rs.getString("console")+" Status:"+rs.getString("status")+ " Msg:"+rs.getString("msg"));
				swTiming = false;  

				zD = rs.getTimestamp("rptdat");
				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000); 
				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime();

				swShDay = false;
				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").startsWith(DOW.name().substring(0, 2) )) {
					cal.setTime(rs.getTime("chktim"));
					if (nu.getHour() > cal.get(Calendar.HOUR_OF_DAY) ) {
						swShDay = true; System.out.println(new Date()+" Timmen swShDay: "+swShDay);
					}
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY) && nu.getMinute() > cal.get(Calendar.MINUTE) ) {
						swShDay = true;	System.out.println(new Date()+" Minuten swShDay: "+swShDay);
					}
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY) && nu.getMinute() == cal.get(Calendar.MINUTE) && nu.getSecond() > cal.get(Calendar.SECOND) ) {
						swShDay = true;	System.out.println(new Date()+" Sekunden swShDay: "+swShDay);
					}
					
					// check chktimto
					cal.setTime(rs.getTime("chktimto"));
					if (nu.getHour() > cal.get(Calendar.HOUR_OF_DAY) ) {
						swShDay = false; 
					}
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY)  && nu.getMinute() > cal.get(Calendar.MINUTE) ) {
						swShDay = false;	
					}
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY)  && nu.getMinute() == cal.get(Calendar.MINUTE) && nu.getSecond() > cal.get(Calendar.SECOND) ) {
						swShDay = false;	
					}
					
				} 
				if (rs.getInt("prio") <= 10) swShDay = true; // always handle prio 10 and below.
				System.out.println(new Date()+" swShDay: "+swShDay);

				//				swDelete = false;

				if (swShDay) {
					if (rs.getString("msg").equalsIgnoreCase("S")) { 
						System.out.println(new Date()+" -- Already got an S in msg, continues...");
						continue; 
					}
					
					if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") < 30 ) { 
						serrors++;
						sbody = sbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd +rowEnd;
					}
					else if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") >= 30 ) { 
						errors++;
						ebody = ebody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
					}
//					else if (rs.getString("msg").equalsIgnoreCase("R") || 
//							(rs.getString("type").equalsIgnoreCase("D")    && rs.getString("msg").equalsIgnoreCase("S")) || 
//							(rs.getString("status").equalsIgnoreCase("OK") && rs.getString("msg").equalsIgnoreCase("S"))  
//							)
					else if (rs.getString("msg").equalsIgnoreCase("R"))	{
						resolved++;
						rbody = rbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
					}
					else {
						warnings++;
						wbody = wbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ "The Jvakt agent did not report in due time."+boxEnd+rowEnd;
					}
					swMail = true;
//					if (rs.getString("msg").equalsIgnoreCase("R") || 
//					   (rs.getString("type").equalsIgnoreCase("D")    && rs.getString("msg").equalsIgnoreCase("S")) || 
//					   (rs.getString("status").equalsIgnoreCase("OK") && rs.getString("msg").equalsIgnoreCase("S"))  
//					   )
					if (rs.getString("msg").equalsIgnoreCase("R"))
						 rs.updateString("msg", " ");
					else rs.updateString("msg", "S");
					rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					try { rs.updateRow(); } catch(NullPointerException npe2) {}
				}
			}
			
			// One mail contains all errors
			if (sendMail()) {
				conn.commit();
				swOK = true;
			}
			else			{
				conn.rollback();
				swOK = false;
			}

			rs.close(); 
			stmt.close();
			conn.commit();
			conn.close();

		}
		catch (SQLException e) {
			System.err.println("SQLExeption " + e);
			swOK = false;
//			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println("Exeption " + e);
			swOK = false;
//			System.err.println(e.getMessage());
		}
//		finally { 
//		}
		if (swOK ) try {sendSTS(true);}  catch (IOException e) { e.printStackTrace();}
		else 	   try {sendSTS(false);} catch (IOException e) { e.printStackTrace();}
	}        
	static boolean sendMail() {

		subject = "* NEW status * -->   ";
		body = tblStr;

		if (sbody.length() > 0) {
			subject = subject + "Errors: " + serrors + "  ";
			body = body + rowStr+hdrStrM+"ERROR" +hdrEnd+hdrStrM+""+hdrEnd+hdrStrM+""+hdrEnd+rowEnd+	sbody ;
			System.out.println(new Date()+" "+ sbody );
		}
		if (ebody.length() > 0) {
			subject = subject + "Warnings: " + errors + "  ";
			body = body + rowStr+hdrStrR+"WARNING" +hdrEnd+hdrStrR+""+hdrEnd+hdrStrR+""+hdrEnd+rowEnd+	ebody ;
			System.out.println(new Date()+" "+ ebody );
		}
		if (wbody.length() > 0) {
			subject = subject + "Time-outs: " + warnings + "  ";
			body = body + rowStr+hdrStrY+"TIME-OUT"+hdrEnd+hdrStrY+""+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	wbody ;
			System.out.println(new Date()+" "+ wbody );
		}
		if (rbody.length() > 0) { 
			subject = subject + "Resolved: " + resolved;
			body = body + rowStr+hdrStrG+"RESOLVED" +hdrEnd+hdrStrG+""+hdrEnd+hdrStrG+""+hdrEnd+rowEnd+	rbody ;
			System.out.println(new Date()+" "+ rbody );
		}
		body = body + tblEnd;

		if (swMail) {

			toEmailW = "";
			int n = 0;
			for(Object object : listTo) { 
				if (n>0) toEmailW = toEmailW + ",";
				n++;
//				String element = (String) object;
//				System.out.println(object);
				toEmailW = toEmailW + (String) object;
			}

			toEmail = toEmailW;
//		    final String LtoEmail = toEmail;
//		    final String LtoEmail="";
//			final String LfromEmail = fromEmail;	
//			final String Luname = uname;
//			final String Lpwd = pwd;
//			final String Lsmtphost = smtphost;
//			final String Lsmtpport = smtpport;
			//				System.out.println("To:"+toEmailW+"   Subject: " + subject );
			now = new Date();
			subject = subject + " -- " + now;

			System.out.println(new Date()+" To:"+toEmail +"   Subject: " + subject );
			System.out.println(new Date()+" "+ body );
			Session session = Session.getInstance(props, auth);
			if (EmailUtil.sendEmail(session, toEmail,subject, body, fromEmail)) {
				System.out.println(new Date()+" return true"); return true; } 
			else { System.out.println(new Date()+" RETURN FALSE"); return false; }
			
		}
//		System.out.println("RETURN true");
		return true;
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
			jmsg.setId("Jvakt-SendMail");
			if (STS) {
				jmsg.setBody("The SendMail program is working. "+configF.getCanonicalPath());
				jmsg.setRptsts("OK");
			}
			else {
				jmsg.setBody("The SendMail program is not working! "+configF.getCanonicalPath());
				jmsg.setRptsts("ERR");
			}
			jmsg.setType("T");

			inet = InetAddress.getLocalHost();
//			System.out.println("-- Inet: "+inet);
			agent = inet.toString();

			jmsg.setAgent(agent);
			if (!jm.sendMsg(jmsg)) System.out.println("--- Rpt to Jvakt Failed for SendMail ---");
			jm.close();
	}

}