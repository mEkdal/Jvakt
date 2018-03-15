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
	//	static boolean swDelete;
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

	static Authenticator auth;

	static List listTo;
	
	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "SendMail 1.4 (2018-MAR-14)";
		String database = "jVakt";
		String dbuser   = "jVakt";
		String dbpassword = "xz";
		String dbhost   = "localhost";
		String dbport   = "5433";
		String jvhost   = "localhost";
		String jvport   = "1956";

		String config = null;
		File configF;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}
 
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"    Version: "+version+"  -  config file: "+configF);
		
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
		dbhost   = prop.getProperty("dbhost");
		dbport   = prop.getProperty("dbport");
		jvport   = prop.getProperty("jvport");
		int jvporti = Integer.parseInt(jvport);
		jvhost   = prop.getProperty("jvhost");
		toEmailW  = prop.getProperty("toEmail");
		fromEmail= prop.getProperty("fromEmail");
		uname    = prop.getProperty("smtpuser");
		pwd      = prop.getProperty("smtppwd");
		smtphost = prop.getProperty("smtphost");
		smtpport = prop.getProperty("smtpport");
		int smtpporti = Integer.parseInt(smtpport);
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		input.close();

		
		listTo = new ArrayList();  // Alla mailadresser.
		
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
		
		try {
			SendMsg jm = new SendMsg(jvhost, jvporti);  // kollar om JvaktServer är tillgänglig.
//			System.out.println(jm.open());
			if (jm.open().startsWith("DORMANT")) {
				swDormant = true;
			}
			jm.close();
		} 
		catch (IOException e1) {
			System.err.println(e1);
			System.err.println(e1.getMessage());
		}

		if (swDormant) {
			System.out.println("*** Jvakt in DORMANT mode, SendMail exiting *** ");
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
		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
		Statement stmt = null;
		String s;
		boolean swHits;
		String cause = "";
		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()); 
		
//		System.out.println("**********SendMail ********   " + LocalDateTime.now());
		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
//			System.out.println(DBUrl);
//			System.out.println("dbuser= " + dbuser +"  dbpassword "+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
//			conn.setAutoCommit(true);
			conn.setAutoCommit(false);

			//			s = new String("select * from status " + 
			//					"WHERE (state='A' or  state='D') " +
			//					" and (msg='M' or msg='T' or msg='R')" +
			//					" and prio < 3" +
			//					";"); 
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
			swHits = false;  // is there already a record?
			while (rs.next()) {
				System.out.println("- main RS - State:"+rs.getString("state")+" Id:" + rs.getString("id")+" Type:"+rs.getString("type")+" Prio:"+rs.getString("prio")+" Console:"+rs.getString("console")+" Status:"+rs.getString("status")+ " Msg:"+rs.getString("msg"));
				swHits = true;  
				swTiming = false;  

//				if (rs.getString("id").equalsIgnoreCase("syssts")) {
//					continue;
//				}

//				if (!rs.getString("type").equalsIgnoreCase("R") && !rs.getString("type").equalsIgnoreCase("S") && !rs.getString("type").equalsIgnoreCase("I")) continue;

				zD = rs.getTimestamp("rptdat");
				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000); 
				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime();

				//				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").startsWith(DOW.name())) {
				//					swShDay = true;
				//				} else swShDay = false;

				swShDay = false;
				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").startsWith(DOW.name().substring(0, 2) )) {
					if (nu.getHour() > rs.getTime("chktim").getHours() ) {
						swShDay = true; System.out.println("Timmen swShDay: "+swShDay);
					}
					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() > rs.getTime("chktim").getMinutes() ) {
						swShDay = true;	System.out.println("Minuten swShDay: "+swShDay);
					}
					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() == rs.getTime("chktim").getMinutes() && nu.getSecond() > rs.getTime("chktim").getSeconds() ) {
						swShDay = true;	System.out.println("Sekunden swShDay: "+swShDay);
					}
				} 
				if (rs.getInt("prio") <= 10) swShDay = true; // always handle prio 10 and below.
				System.out.println("swShDay: "+swShDay);

				//				swDelete = false;

				if (swShDay) {
					if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") < 30 ) { 
						cause = "Problem :\t";
						serrors++;
//						sbody = sbody +rowStr+boxStrM+ rs.getString("id")+boxEnd +boxStrM+ rs.getString("body")+boxEnd +boxStrM+ rs.getString("agent")+boxEnd+rowEnd;
//						sbody = sbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd +boxStrB+ rs.getString("agent")+boxEnd+rowEnd;
						sbody = sbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd +rowEnd;
					}
					else if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") >= 30 ) { 
						cause = "Problem :\t";
						errors++;
//						ebody = ebody +rowStr+boxStrR+ rs.getString("id")+boxEnd +boxStrR+ rs.getString("body")+boxEnd+boxStrR+ rs.getString("agent")+boxEnd+rowEnd;
//						ebody = ebody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+boxStrB+ rs.getString("agent")+boxEnd+rowEnd;
						ebody = ebody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
					}
					else if (rs.getString("msg").equalsIgnoreCase("R")) {
						cause = "Resolved:\t";
						resolved++;
//						rbody = rbody +rowStr+boxStrG+ rs.getString("id")+boxEnd +boxStrG+ rs.getString("body")+boxEnd+boxStrG+ rs.getString("agent")+boxEnd+rowEnd;
//						rbody = rbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+boxStrB+ rs.getString("agent")+boxEnd+rowEnd;
						rbody = rbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
					}
					else {
						cause = "Time out:\t";
						warnings++;
//						wbody = wbody +rowStr+boxStrY+ rs.getString("id")+boxEnd +boxStrY+ rs.getString("body")+boxEnd+boxStrY+ rs.getString("agent")+boxEnd+rowEnd;
//						wbody = wbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+boxStrB+ rs.getString("agent")+boxEnd+rowEnd;
						wbody = wbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
					}
					swMail = true;
					if (rs.getString("msg").equalsIgnoreCase("R")) rs.updateString("msg", " ");
					else rs.updateString("msg", "S");
					rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					try { rs.updateRow(); } catch(NullPointerException npe2) {}
				}
			}
			
			if (sendMail()) conn.commit();
			else			conn.rollback();

			rs.close(); 
			stmt.close();

		}
		catch (SQLException e) {
			System.err.println("SQLExeption " + e);
//			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println("Exeption " + e);
//			System.err.println(e.getMessage());
		}
		finally { 
//			subject = "Altered status -->   ";
//			body = tblStr;
//
//			if (sbody.length() > 0) {
//				subject = subject + "Errors: " + serrors + "  ";
//				body = body + rowStr+hdrStrM+"ERROR" +hdrEnd+hdrStrM+""+hdrEnd+hdrStrM+""+hdrEnd+rowEnd+	sbody ;
//			}
//			if (ebody.length() > 0) {
//				subject = subject + "Warnings: " + errors + "  ";
//				body = body + rowStr+hdrStrR+"WARNING" +hdrEnd+hdrStrR+""+hdrEnd+hdrStrR+""+hdrEnd+rowEnd+	ebody ;
//			}
//			if (wbody.length() > 0) {
//				subject = subject + "Time-outs: " + warnings + "  ";
//				body = body + rowStr+hdrStrY+"TIME-OUT"+hdrEnd+hdrStrY+""+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	wbody ;
//			}
//			if (rbody.length() > 0) { 
//				subject = subject + "Resolved: " + resolved;
//				body = body + rowStr+hdrStrG+"RESOLVED" +hdrEnd+hdrStrG+""+hdrEnd+hdrStrG+""+hdrEnd+rowEnd+	rbody ;
//			}
//			body = body + tblEnd;
//
//			if (swMail) {
//
////				Iterator iterator = listTo.iterator();
//				toEmailW = "";
//				int n = 0;
//				for(Object object : listTo) { 
//					if (n>0) toEmailW = toEmailW + ",";
//					n++;
//				  String element = (String) object;
//				  System.out.println(object);
//				  toEmailW = toEmailW + (String) object;
//				}
//				
//				toEmail = toEmailW;
////				System.out.println("To:"+toEmailW+"   Subject: " + subject );
//				System.out.println("To:"+toEmail +"   Subject: " + subject );
//				System.out.println( body );
//				Session session = Session.getInstance(props, auth);
//				EmailUtil.sendEmail(session, toEmail,subject, body, fromEmail);
//			}

		}
	}        
	static boolean sendMail() {

		subject = "* NEW status * -->   ";
		body = tblStr;

		if (sbody.length() > 0) {
			subject = subject + "Errors: " + serrors + "  ";
			body = body + rowStr+hdrStrM+"ERROR" +hdrEnd+hdrStrM+""+hdrEnd+hdrStrM+""+hdrEnd+rowEnd+	sbody ;
		}
		if (ebody.length() > 0) {
			subject = subject + "Warnings: " + errors + "  ";
			body = body + rowStr+hdrStrR+"WARNING" +hdrEnd+hdrStrR+""+hdrEnd+hdrStrR+""+hdrEnd+rowEnd+	ebody ;
		}
		if (wbody.length() > 0) {
			subject = subject + "Time-outs: " + warnings + "  ";
			body = body + rowStr+hdrStrY+"TIME-OUT"+hdrEnd+hdrStrY+""+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	wbody ;
		}
		if (rbody.length() > 0) { 
			subject = subject + "Resolved: " + resolved;
			body = body + rowStr+hdrStrG+"RESOLVED" +hdrEnd+hdrStrG+""+hdrEnd+hdrStrG+""+hdrEnd+rowEnd+	rbody ;
		}
		body = body + tblEnd;

		if (swMail) {

			toEmailW = "";
			int n = 0;
			for(Object object : listTo) { 
				if (n>0) toEmailW = toEmailW + ",";
				n++;
				String element = (String) object;
				System.out.println(object);
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
			System.out.println("To:"+toEmail +"   Subject: " + subject );
			System.out.println( body );
			Session session = Session.getInstance(props, auth);
			if (EmailUtil.sendEmail(session, toEmail,subject, body, fromEmail)) {
				System.out.println("return true"); return true; } 
			else { System.out.println("RETURN FALSE"); return false; }
			
		}
//		System.out.println("RETURN true");
		return true;
	}

}