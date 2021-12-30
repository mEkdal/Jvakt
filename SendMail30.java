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
import java.time.*;
//import javax.mail.*;
//import javax.mail.Authenticator;
//import javax.mail.PasswordAuthentication;
import jakarta.mail.*;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication; 

public class SendMail30 {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	//	static boolean swDelete;
	static boolean swTiming;
	static boolean swShDay; // set when the scheduled day is active
	static boolean swDormant = false;

	static boolean swOK = false; 
	static boolean swINFO = false; 
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
	static String hdrStrB = "<TH BGCOLOR=\"#CCEEFF\"><FONT SIZE=3>"; // Light blue
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

		String version = "SendMail30 (2021-DEC-30)";
		String database = "jVakt";
		String dbuser   = "jVakt";
		String dbpassword = "";
		String dbhost   = "localhost";
		String dbport   = "5433";
		String jvhost   = "localhost";
		String jvport   = "1956";

		Calendar cal = Calendar.getInstance();

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-nover")) swShowVer =false;
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");

		if (swShowVer) System.out.println("----- Jvakt: "+new Date()+"  Version: "+version+"  -  config file: "+configF);

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
		if (dbpassword.startsWith("==y")) {
		    byte[] decodedBytes = Base64.getDecoder().decode(dbpassword.substring(3));
		    String decodedString = new String(decodedBytes);
		    dbpassword=decodedString;
		}
		dbhost   = prop.getProperty("dbhost");
		dbport   = prop.getProperty("dbport");
		jvport   = prop.getProperty("jvport");
		int jvporti = Integer.parseInt(jvport);
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
		//		listTo = new HashSet<String>();

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
			System.out.println(LocalDateTime.now()+" *** Jvakt in DORMANT mode, SendMail30 exiting *** ");
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
		//		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
		//		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
		Statement stmt = null;
		String s;
		//		boolean swHits;
		//		String cause = "";
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
					" and (msg30='M' or msg30='T' or msg30='R')" +
					";"); 

			//			System.out.println(s);
			//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
			//			swHits = false;  // is there already a record?
			while (rs.next()) {
//				System.out.println(LocalDateTime.now()+" - main RS - State:"+rs.getString("state")+" Id:" + rs.getString("id")+" Type:"+rs.getString("type")+" Prio:"+rs.getString("prio")+" Console:"+rs.getString("console")+" Status:"+rs.getString("status")+ " Msg30:"+rs.getString("msg30"));
				swTiming = false;  

				zD = rs.getTimestamp("rptdat");
				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000); 
				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime();

				swShDay = false;
				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").startsWith(DOW.name().substring(0, 2) )) {
					cal.setTime(rs.getTime("chktim"));
					//					if (nu.getHour() > rs.getTime("chktim").getHours() ) {
					if (nu.getHour() > cal.get(Calendar.HOUR_OF_DAY) ) {
						swShDay = true; // System.out.println("Timmen swShDay: "+swShDay);
					}
					//					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() > rs.getTime("chktim").getMinutes() ) {
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY) && nu.getMinute() > cal.get(Calendar.MINUTE) ) {
						swShDay = true;	// System.out.println("Minuten swShDay: "+swShDay);
					}
					//					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() == rs.getTime("chktim").getMinutes() && nu.getSecond() > rs.getTime("chktim").getSeconds() ) {
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY) && nu.getMinute() == cal.get(Calendar.MINUTE) && nu.getSecond() > cal.get(Calendar.SECOND) ) {
						swShDay = true;	// System.out.println("Sekunden swShDay: "+swShDay);
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
				//				System.out.println("swShDay: "+swShDay);

				//				swDelete = false;

				if (swShDay) {

					if (   checkInterest(rs.getString("id"),rs.getInt("prio"))   ) {

						System.out.println(LocalDateTime.now()+" *** State:"+rs.getString("state")+" Id:" + rs.getString("id")+" Type:"+rs.getString("type")+" Prio:"+rs.getString("prio")+" Console:"+rs.getString("console")+" Status:"+rs.getString("status")+ " Msg30:"+rs.getString("msg30")+" "+rs.getString("body"));

						boolean swPrio = false; 
						boolean swMsg30M = false; 
						boolean swMsg30R = false; 
						boolean swMsg30S = false; 
						boolean swMsg30D = false; 
						boolean swMsg30T = false; 
						boolean swStatusERR = false; 
						boolean swStatusOK = false; 
						boolean swStatusINFO = false; 
						boolean swTypeD = false;
						swOK = false; 
						swINFO = false; 


						if (rs.getInt("prio") < 30) swPrio=true;
						if (rs.getString("msg30").equalsIgnoreCase("M")) swMsg30M=true;
						if (rs.getString("msg30").equalsIgnoreCase("R")) swMsg30R=true;
						if (rs.getString("msg30").equalsIgnoreCase("S")) swMsg30S=true;
						if (rs.getString("msg30").equalsIgnoreCase("D")) swMsg30D=true;
						if (rs.getString("msg30").equalsIgnoreCase("T")) swMsg30T=true;
						if (rs.getString("status").equalsIgnoreCase("ERR")) swStatusERR=true;
						if (rs.getString("status").equalsIgnoreCase("OK")) swStatusOK=true;
						if (rs.getString("status").equalsIgnoreCase("INFO")) swStatusINFO=true;
						if (rs.getString("type").equalsIgnoreCase("D")) swTypeD=true;

						System.out.println(LocalDateTime.now()+" swPrio:"+swPrio+" swMsg30M:"+swMsg30M+" swMsg30R:"+swMsg30R+" swMsg30S:"+swMsg30S+" swMsg30T:"+swMsg30T+" swStatusERR:"+swStatusERR+" swStatusOK:"+swStatusOK+" swStatusINFO:"+swStatusINFO+" swTypeD:"+swTypeD);

						if (swMsg30S) { 
							System.out.println(LocalDateTime.now()+" -- Already got an S in msg30, continues...");
							continue; 
						}

						if (swMsg30M && swPrio && swStatusERR) { 
							serrors++;
							sbody = sbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd +rowEnd;
						}
						else if (swMsg30M && !swPrio && swStatusERR ) { 
							errors++;
							ebody = ebody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
						}
						//						else if ( swMsg30R || 
						//								( swTypeD  && swMsg30S  ) || 
						//								((swStatusOK || swStatusINFO) && (swMsg30S || swMsg30M))
						//								) 
						else if ( swMsg30R || 
								(swStatusINFO && swMsg30M )
								) 
						{
							if (swStatusOK) {
								swOK=true;
								resolved++;
							}
							if (swStatusINFO) {
								swINFO=true;
								infos++;
							}

							rbody = rbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ rs.getString("body")+boxEnd+rowEnd;
						}
						//						else if (swMsg30S) { 
						//							System.out.println("-- Already got an S in msg30, going on...");
						//							continue; 
						//						}
						else {
							warnings++;
							wbody = wbody +rowStr+boxStrB+ rs.getString("id")+boxEnd +boxStrB+ "The Jvakt agent did not report in due time."+boxEnd+rowEnd;
						}

						swMail = true;	

//						if ( swMsg30R ||
//								( swTypeD  && swMsg30S ) || 
//								( swStatusOK && (swMsg30S || swMsg30M))
//								) {
						if ( swMsg30R ) {
							System.out.println(LocalDateTime.now()+" -- Set blank in msg30");
							rs.updateString("msg30", " ");
						}
						else if (swStatusINFO && swMsg30M)
						{
							System.out.println(LocalDateTime.now()+" -- Set D in msg30");
							rs.updateString("msg30", "D");
						}
						else {
							System.out.println(LocalDateTime.now()+" -- Set S in msg30");
							rs.updateString("msg30", "S");
						}

						rs.updateTimestamp("msgdat30", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));

						try { rs.updateRow(); } catch(NullPointerException npe2) {}

						if (sendMail()) {
							System.out.println(LocalDateTime.now()+" -- commit");
							conn.commit();
						}
						else	 {
							System.out.println(LocalDateTime.now()+" -- rollback");
							conn.rollback();
						}

					}

				}
			}

			rs.close(); 
			stmt.close();
			conn.commit();
			conn.close();

		}
		catch (SQLException e) {
			System.err.println(LocalDateTime.now()+" SQLExeption " + e);
			//			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(LocalDateTime.now()+" Exeption " + e);
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


		listToS = new ArrayList<String>();  // id:mailadress.

		df = new DirFilter(suf, pos);

		listf = dir.listFiles(df);

		//		System.out.println("-- Antal filer:"+ listf.length);
		try {
			BufferedReader in;

			for (int i = 0; i < listf.length; i++) {

				//				System.out.println("-- Importing: "+listf[i]);
				in = new BufferedReader(new FileReader(listf[i]));

				while ((s = in.readLine()) != null) {
					if (s.length() == 0) continue; 
					if (s.startsWith("#")) continue; 

					listToS.add(s);
					//					System.out.println("-- add: "+s);
				}
				in.close();
			}
		} catch (Exception e) { System.out.println(LocalDateTime.now()+" "+e);  }
	}

	static boolean checkInterest(String id, int prioi) {
		listTo = new HashSet<String>();
		String[] tab = new String [1];
		boolean ok = false;
		String prio;
		String prioCsv;
		//		int n = 0;
//		System.out.println(LocalDateTime.now()+" -- checking interest of: "+id);
		for(Object object : listToS) { 
			String element = (String) object;
			tab = element.toLowerCase().split(";" , 3 );
			if (tab.length<3) prioCsv = "30";
			else prioCsv = tab[2];
			prio=String.valueOf(prioi);
			//			System.out.println(tab[0] + " <<<>>> "+ id+ " <<<>>> "+ prio +" " + prioCsv);
			//			if (element.toLowerCase().indexOf(id.toLowerCase()) >= 0 ) {
			if ( prio.compareTo(prioCsv) < 0  )	continue; 
			if (id.toLowerCase().indexOf(tab[0].toLowerCase()) >= 0 ) {
				listTo.add(tab[1]);
				//				System.out.println("** YES. Added: "+ tab[1]);
				ok = true;
			}
		}
		if (ok) return true;
		else    return false;

	}

	static boolean sendMail() {

		subject = "* NEW status * -->   ";
		body = tblStr;
		String subjectTxt;

		if (sbody.length() > 0) {
//			subject = subject + "Errors: " + serrors + "  ";
			subject = subject + "Error";
//			body = body + rowStr+hdrStrM+"ERROR" +hdrEnd+hdrStrM+""+hdrEnd+hdrStrM+""+hdrEnd+rowEnd+	sbody ;
			body = body + rowStr+hdrStrM+"ERROR" +hdrEnd+hdrStrM+""+hdrEnd+rowEnd+	sbody ;
		}
		if (ebody.length() > 0) {
//			subject = subject + "Warnings: " + errors + "  ";
			subject = subject + "Warning";
//			body = body + rowStr+hdrStrR+"WARNING" +hdrEnd+hdrStrR+""+hdrEnd+hdrStrR+""+hdrEnd+rowEnd+	ebody ;
			body = body + rowStr+hdrStrR+"WARNING" +hdrEnd+hdrStrR+""+hdrEnd+rowEnd+	ebody ;
		}
		if (wbody.length() > 0) {
//			subject = subject + "Time-outs: " + warnings + "  ";
			subject = subject + "Time-out";
//			body = body + rowStr+hdrStrY+"TIME-OUT"+hdrEnd+hdrStrY+""+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	wbody ;
			body = body + rowStr+hdrStrY+"TIME-OUT"+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	wbody ;
		}
		if (rbody.length() > 0) { 
			
//			if (swOK) subject =  subject + "Resolved: " + resolved+ "  ";
//			if (swINFO) subject = subject + "Info: " + infos+ "  ";
			if (swOK) subject =  subject + "Resolved";
			if (swINFO) subject = subject + " Info";
			
			if (swOK && swINFO ) {
				subjectTxt="RESOLVED & INFO";
//				body = body + rowStr+hdrStrY+subjectTxt+hdrEnd+hdrStrY+""+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	rbody ;
				body = body + rowStr+hdrStrY+subjectTxt+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	rbody ;
			}
			else if (swOK ) {
				subjectTxt="RESOLVED";
//				body = body + rowStr+hdrStrG+subjectTxt+hdrEnd+hdrStrG+""+hdrEnd+hdrStrG+""+hdrEnd+rowEnd+	rbody ;
				body = body + rowStr+hdrStrG+subjectTxt+hdrEnd+hdrStrG+""+hdrEnd+rowEnd+	rbody ;
			}
			else if (swINFO ) {
				subjectTxt="INFO";
//				body = body + rowStr+hdrStrY+subjectTxt+hdrEnd+hdrStrY+""+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	rbody ;
				body = body + rowStr+hdrStrY+subjectTxt+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	rbody ;
			}
			else  {
				subjectTxt="RESOLVED";    // Default. 
//				body = body + rowStr+hdrStrY+subjectTxt+hdrEnd+hdrStrY+""+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	rbody ;
				body = body + rowStr+hdrStrY+subjectTxt+hdrEnd+hdrStrY+""+hdrEnd+rowEnd+	rbody ;
			}
			//			subject = subject + "Resolved: " + resolved;
		}
		body = body + tblEnd;

		if (swMail) {

			toEmailW = "";
			int n = 0;
			for(Object object : listTo) { 
				if (n>0) toEmailW = toEmailW + ",";
				n++;
				//				String element = (String) object;
				System.out.println(LocalDateTime.now()+" "+object);
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

			System.out.println(LocalDateTime.now()+" To:"+toEmail +"   Subject: " + subject );
			System.out.println(LocalDateTime.now()+" "+ body );
			Session session = Session.getInstance(props, auth);
			if (EmailUtil.sendEmail(session, toEmail,subject, body, fromEmail)) {
				System.out.println(LocalDateTime.now()+" return true");
				sbody = ""; ebody = ""; wbody = ""; rbody = "";
				return true; 
			} 
			else { System.out.println(LocalDateTime.now()+" RETURN FALSE"); 
			sbody = ""; ebody = ""; wbody = ""; rbody = "";
			return false; 
			}

		}
		//		System.out.println("RETURN true");
		return true;
	}

}
