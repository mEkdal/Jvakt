package Jvakt;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
<<<<<<< HEAD
//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.sql.Timestamp;
=======
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
import java.util.*;
import java.time.*;
import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SendMail30 {

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
	static Date now;

	static Set<String>  listTo;
	static List<String> listToS;

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

	static Properties props = new Properties();

	static String toEmail;
	static String fromEmail;	
	static String uname;
	static String pwd;
	static String smtphost;
	static String smtpport;

	static Authenticator auth;

	static String config = null;
	static File configF;


	public static void main(String[] args ) throws IOException, UnknownHostException {

<<<<<<< HEAD
		String version = "SendMail30 (2019-MAY-07)";
=======
		String version = "SendMail30 1.4 (2018-JUL-11)";
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
		String database = "jVakt";
		String dbuser   = "jVakt";
		String dbpassword = "xz";
		String dbhost   = "localhost";
		String dbport   = "5433";
		String jvhost   = "localhost";
		String jvport   = "1956";

<<<<<<< HEAD
		Calendar cal = Calendar.getInstance();
		
=======
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}
 
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version+"  -  config file: "+configF);
		
		//Declare recipient's & sender's e-mail id.
		Properties prop = new Properties();
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
		listTo = new HashSet<String>();

		//		String[] toAddr = toEmailW.split("\\,");
		//		for(int i=0 ; i<toAddr.length;i++) {
		////			System.out.println(toAddr[i]);
		//			listTo.add(toAddr[i]);
		//		}

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
			System.out.println("*** Jvakt in DORMANT mode, SendMail30 exiting *** ");
			System.exit(4);			
		}

		// reads SendMail20*.csv files
		readEmailAdr();

		//create Authenticator object to pass in Session.getInstance argument
		auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, pwd);
			}
		};

		//Set properties and their values
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.host", smtphost);
		props.put("mail.smtp.port", smtpporti);

		LocalDateTime nu = LocalDateTime.now(); // The current date and time
<<<<<<< HEAD
//		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
//		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
		Statement stmt = null;
		String s;
//		boolean swHits;
//		String cause = "";
=======
		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
		Statement stmt = null;
		String s;
		boolean swHits;
		String cause = "";
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()); 

//		System.out.println("**********SendMail30 ********   " + LocalDateTime.now());
		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			//			System.out.println(DBUrl);
			//			System.out.println("dbuser= " + dbuser +"  dbpassword "+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
//			conn.setAutoCommit(true);
			conn.setAutoCommit(false);

			s = new String("select * from status " + 
					"WHERE state='A' " +
					" and (msg='M' or msg='T' or msg='R')" +
					" and prio >= 30" +
					";"); 

			//			System.out.println(s);
			//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
<<<<<<< HEAD
//			swHits = false;  // is there already a record?
			while (rs.next()) {
//				System.out.println("---- main RS: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+ " "+rs.getString("msg"));
				System.out.println("- main RS - State:"+rs.getString("state")+" Id:" + rs.getString("id")+" Type:"+rs.getString("type")+" Prio:"+rs.getString("prio")+" Console:"+rs.getString("console")+" Status:"+rs.getString("status")+ " Msg:"+rs.getString("msg"));
//				swHits = true;  
=======
			swHits = false;  // is there already a record?
			while (rs.next()) {
//				System.out.println("---- main RS: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+ " "+rs.getString("msg"));
				System.out.println("- main RS - State:"+rs.getString("state")+" Id:" + rs.getString("id")+" Type:"+rs.getString("type")+" Prio:"+rs.getString("prio")+" Console:"+rs.getString("console")+" Status:"+rs.getString("status")+ " Msg:"+rs.getString("msg"));
				swHits = true;  
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
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
<<<<<<< HEAD
					cal.setTime(rs.getTime("chktim"));
//					if (nu.getHour() > rs.getTime("chktim").getHours() ) {
					if (nu.getHour() > cal.get(Calendar.HOUR_OF_DAY) ) {
						swShDay = true; System.out.println("Timmen swShDay: "+swShDay);
					}
//					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() > rs.getTime("chktim").getMinutes() ) {
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY) && nu.getMinute() > cal.get(Calendar.MINUTE) ) {
						swShDay = true;	System.out.println("Minuten swShDay: "+swShDay);
					}
//					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() == rs.getTime("chktim").getMinutes() && nu.getSecond() > rs.getTime("chktim").getSeconds() ) {
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY) && nu.getMinute() == cal.get(Calendar.MINUTE) && nu.getSecond() > cal.get(Calendar.SECOND) ) {
=======
					if (nu.getHour() > rs.getTime("chktim").getHours() ) {
						swShDay = true; System.out.println("Timmen swShDay: "+swShDay);
					}
					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() > rs.getTime("chktim").getMinutes() ) {
						swShDay = true;	System.out.println("Minuten swShDay: "+swShDay);
					}
					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() == rs.getTime("chktim").getMinutes() && nu.getSecond() > rs.getTime("chktim").getSeconds() ) {
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
						swShDay = true;	System.out.println("Sekunden swShDay: "+swShDay);
					}
				} 
				if (rs.getInt("prio") <= 10) swShDay = true; // always handle prio 10 and below.
				System.out.println("swShDay: "+swShDay);

				//				swDelete = false;

				if (swShDay) {

					if (checkInterest(rs.getString("id"))) {

						if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") < 30 ) { 
<<<<<<< HEAD
//							cause = "Problem :\t";
=======
							cause = "Problem :\t";
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
							serrors++;
							//						sbody = sbody +rowStr+boxStrM+ rs.getString("id")+boxEnd +boxStrM+ rs.getString("body")+boxEnd +boxStrM+ rs.getString("agent")+boxEnd+rowEnd;
//							sbody = sbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd +boxStrB+ rs.getString("agent")+boxEnd+rowEnd;
							sbody = sbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd +rowEnd;
						}
						else if (rs.getString("msg").equalsIgnoreCase("M") && rs.getInt("prio") >= 30 ) { 
<<<<<<< HEAD
//							cause = "Problem :\t";
=======
							cause = "Problem :\t";
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
							errors++;
							//						ebody = ebody +rowStr+boxStrR+ rs.getString("id")+boxEnd +boxStrR+ rs.getString("body")+boxEnd+boxStrR+ rs.getString("agent")+boxEnd+rowEnd;
//							ebody = ebody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+boxStrB+ rs.getString("agent")+boxEnd+rowEnd;
							ebody = ebody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
						}
						else if (rs.getString("msg").equalsIgnoreCase("R")) {
<<<<<<< HEAD
//							cause = "Resolved:\t";
=======
							cause = "Resolved:\t";
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
							resolved++;
							//						rbody = rbody +rowStr+boxStrG+ rs.getString("id")+boxEnd +boxStrG+ rs.getString("body")+boxEnd+boxStrG+ rs.getString("agent")+boxEnd+rowEnd;
//							rbody = rbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+boxStrB+ rs.getString("agent")+boxEnd+rowEnd;
							rbody = rbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
						}
						else {
<<<<<<< HEAD
//							cause = "Time out:\t";
							warnings++;
							//						wbody = wbody +rowStr+boxStrY+ rs.getString("id")+boxEnd +boxStrY+ rs.getString("body")+boxEnd+boxStrY+ rs.getString("agent")+boxEnd+rowEnd;
//							wbody = wbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+boxStrB+ rs.getString("agent")+boxEnd+rowEnd;
							wbody = wbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ "The Jvakt agent did not report in set time."+boxEnd+rowEnd;
=======
							cause = "Time out:\t";
							warnings++;
							//						wbody = wbody +rowStr+boxStrY+ rs.getString("id")+boxEnd +boxStrY+ rs.getString("body")+boxEnd+boxStrY+ rs.getString("agent")+boxEnd+rowEnd;
//							wbody = wbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+boxStrB+ rs.getString("agent")+boxEnd+rowEnd;
							wbody = wbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
						}
						swMail = true;	
						if (rs.getString("msg").equalsIgnoreCase("R")) rs.updateString("msg", " ");
						else rs.updateString("msg", "S");
						rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						try { rs.updateRow(); } catch(NullPointerException npe2) {}
					}

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
		}
	}        

	static void readEmailAdr() {
		File[] listf;
		DirFilter df;
		String s;
		File dir = new File(".");
		String suf = ".csv";
		String pos = "SendMail30";

		if (config != null ) dir = new File(config);

		
<<<<<<< HEAD
		listToS = new ArrayList<String>();  // id:mailadress.
=======
		listToS = new ArrayList();  // id:mailadress.
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee

		df = new DirFilter(suf, pos);

		listf = dir.listFiles(df);

//		System.out.println("-- Antal filer:"+ listf.length);
		try {
			BufferedReader in;

			for (int i = 0; i < listf.length; i++) {

				System.out.println("-- Importing: "+listf[i]);
				in = new BufferedReader(new FileReader(listf[i]));

				while ((s = in.readLine()) != null) {
					if (s.length() == 0) continue; 
					if (s.startsWith("#")) continue; 

					listToS.add(s);
//					System.out.println("-- add: "+s);
				}
				in.close();
			}
		} catch (Exception e) { System.out.println(e);  }
	}

	static boolean checkInterest(String id) {
		String[] tab = new String [1];
		boolean ok = false;
		//		int n = 0;
		System.out.println("-- checking interest of: "+id);
		for(Object object : listToS) { 
			String element = (String) object;
			tab = element.toLowerCase().split(";" , 2 );
			System.out.println(tab[0] + " <<<>>> "+ id);
			//			if (element.toLowerCase().indexOf(id.toLowerCase()) >= 0 ) {
			if (id.toLowerCase().indexOf(tab[0].toLowerCase()) >= 0 ) {
				listTo.add(tab[1]);
				System.out.println("** YES. Added: "+ tab[1]);
				ok = true;
			}
		}
		if (ok) return true;
		else    return false;

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
<<<<<<< HEAD
//				String element = (String) object;
=======
				String element = (String) object;
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
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
			now = new Date();
			subject = subject + " -- " + now;

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
