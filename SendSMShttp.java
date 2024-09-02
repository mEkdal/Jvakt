package Jvakt;
/*
 * 2024-09-02 V.03 Michael Ekdal	Increased time out and removed space from the URL string  
 * 2024-07-09 V.02 Michael Ekdal	Added more info when reporting in status to Jvakt server.
 * 2023-08-21 V.01 Michael Ekdal	Created.
 */

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

public class SendSMShttp {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	//	static boolean swDelete;
	static boolean swTiming;
	static boolean swShDay; // set when the scheduled day is active 
	static boolean swDormant = false;
	static boolean swShowVer = true;
	static boolean swOK = false;

	static java.sql.Date zDate;
	static java.sql.Timestamp zD;
	static java.sql.Timestamp zTs;
	static java.sql.Time zT;
	static Long lhou, lmin, Lsec, Lrptdat, Lchktim;
	//	static int serrors = 0;
	static boolean error = false;
	//	static int errors = 0;
	//	static int warnings = 0;
	static boolean warning = false;
	//	static int infos = 0;
	//	static int resolved = 0;
	static boolean resolved = false;

	static String expected = "";
	static String reply = "";

	//	static String subject = "";
	static String body = "";

	static String toSMSW;

	static boolean swMail = false;

	static Properties props;
	static Properties prop;

	static String toSMS;
	static String SMShost;
	static String SMSport;
	static int SMSporti;

	static   Socket sock = null;
	static   OutputStreamWriter osw;
	static   InputStreamReader isr;

	static String agent = null;
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int    port   = 1956;
	static InetAddress inet;

	static String hosturl;
	static String SMSuser   = "jVakt";
	static String SMSpwd = "xxxx";
	static int tout = 5000 ;
	static File configF;
	
	//	static Authenticator auth;

	static List<String> listTo;

	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "SendSMShttp ";
		version += getVersion()+".03";
		String database = "jVakt";
		String dbuser   = "jVakt";
		String dbpassword = "";
		String dbhost   = "localhost";
		String dbport   = "5433";
//		String jvhost   = "localhost";
//		String jvport   = "1956";

		String config = null;

		Calendar cal = Calendar.getInstance();

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-nover")) swShowVer =false;
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");

		if (swShowVer) System.out.println("----- Jvakt: "+new Date()+"    Version: "+version+"  -  config file: "+configF);

		prop = new Properties();
		InputStream input = null;
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
		toSMSW  = prop.getProperty("toSMS");
		SMShost = prop.getProperty("SMShost");
		SMSuser = prop.getProperty("SMSuser");
		SMSpwd  = prop.getProperty("SMSpwd");
		if (SMSpwd.startsWith("==y")) {
		    byte[] decodedBytes = Base64.getDecoder().decode(SMSpwd.substring(3));
		    String decodedString = new String(decodedBytes);
		    SMSpwd=decodedString;
		}

		SMSport = prop.getProperty("SMSport");
		SMSporti = Integer.parseInt(SMSport);
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		input.close();


		listTo = new ArrayList<String>();  // All SMS numbers.

		String[] toAddr = toSMSW.split("\\,");
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
			System.out.println(LocalDateTime.now()+" *** Jvakt in DORMANT mode, SendSMS exiting *** ");
			System.exit(4);			
		}

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

		//		System.out.println("**********SendSMS ********   " + LocalDateTime.now());
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
					" and (sms='M' or sms='T' or sms='R')" +
					" and prio < 30" +
					";"); 

			//			System.out.println(s);
			//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
			//			swHits = false;  // is there already a record?
			while (rs.next()) {
				System.out.println("\n"+LocalDateTime.now()+" **** main RS - State:"+rs.getString("state")+" Id:" + rs.getString("id")+" Type:"+rs.getString("type")+" Prio:"+rs.getString("prio")+" Console:"+rs.getString("console")+" Status:"+rs.getString("status")+ " Sms:"+rs.getString("sms"));
				//				swHits = true;  
				swTiming = false;  
				error = false;
				resolved = false;
				warning = false;

				zD = rs.getTimestamp("rptdat");
				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000); 
				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime();

				swShDay = false;
				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").startsWith(DOW.name().substring(0, 2) )) {
					cal.setTime(rs.getTime("chktim"));
					//					if (nu.getHour() > rs.getTime("chktim").getHours() ) {
					if (nu.getHour() > cal.get(Calendar.HOUR_OF_DAY) ) {
						swShDay = true; System.out.println(LocalDateTime.now()+" Timmen swShDay: "+swShDay);
					}
					//					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() > rs.getTime("chktim").getMinutes() ) {
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY) && nu.getMinute() > cal.get(Calendar.MINUTE) ) {
						swShDay = true;	System.out.println(LocalDateTime.now()+" Minuten swShDay: "+swShDay);
					}
					//					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() == rs.getTime("chktim").getMinutes() && nu.getSecond() > rs.getTime("chktim").getSeconds() ) {
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY) && nu.getMinute() == cal.get(Calendar.MINUTE) && nu.getSecond() > cal.get(Calendar.SECOND) ) {
						swShDay = true;	System.out.println(LocalDateTime.now()+" Sekunden swShDay: "+swShDay);
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
				System.out.println(LocalDateTime.now()+" swShDay: "+swShDay);

				if (swShDay) {
					if (rs.getString("sms").equalsIgnoreCase("S")) { 
						System.out.println(LocalDateTime.now()+" -- Already got an S in sms, continues...");
						continue; 
					}
					
					body = rs.getString("id")+" "+rs.getString("body");
					
					if (rs.getString("sms").equalsIgnoreCase("M") ) { 
						System.out.println(LocalDateTime.now()+" Error " + body);
						error = true;
					}
					else if (rs.getString("sms").equalsIgnoreCase("R")) {
						System.out.println(LocalDateTime.now()+" Resolved " + body);
						resolved = true;
					}
					else {
						body = rs.getString("id")+" The Jvakt agent did not report in due time.";
						System.out.println(LocalDateTime.now()+" Timeout " + body);
						warning = true;
					}
					
					if (rs.getString("sms").equalsIgnoreCase("R"))
						 rs.updateString("sms", " ");
					else rs.updateString("sms", "S");
					rs.updateTimestamp("smsdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					try { rs.updateRow(); } catch(NullPointerException npe2) {}

					System.out.println(LocalDateTime.now()+" After update");

					if (sendSMS()) { 
						conn.commit();
						}
					else {
						conn.rollback();
					}
				}
			}


			rs.close(); 
			stmt.close();
			conn.commit();
			conn.close();

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
			if (swOK) try {sendSTS(true);} catch (IOException e) { e.printStackTrace();}
			else      try {sendSTS(false);} catch (IOException e) { e.printStackTrace();}
		}
	}        

	// Connects to the SMS terminal and sends the text
	static boolean sendSMS() {

		swOK = true;
		Date cacheexpiration; 

		System.out.println(LocalDateTime.now()+" --- Sending Jvakt SMS ---\n");

		if (error) {
			body = "ERROR: " + body;
		}
		if (warning) {
			body = "TIME-OUT: "+body ;
		}
		if (resolved) { 
			body = "RESOLVED: "+body ;
		}

		toSMSW = "";
		for(Object object : listTo) { 
			toSMSW = (String) object;
			toSMS = toSMSW;
			//				System.out.println("To:"+toEmailW+"   Subject: " + subject );
			System.out.println(LocalDateTime.now()+" --- Sending SMS to: "+toSMS +"      Body: " + body );

			try {
				if (body.length() > 140 ) body = body.substring(0, 139);
				body = body.replace('_', '-'); // replace _ with - because SMS creates a �
				body = body.replace('Å', 'A'); 
				body = body.replace('Ä', 'A'); 
				body = body.replace('Ö', 'O'); 
				body = body.replace('å', 'a'); 
				body = body.replace('ä', 'a'); 
				body = body.replace('ö', 'o'); 
				body = body.trim();
				body = body.replaceAll("[^a-zA-Z0-9.:-]" , " ");
				body = body.replaceAll("\\s+","%20");

				System.out.println("Sending body: "+body );
				
//				hosturl ="http://www.ekdal.se/";
				hosturl ="http://"+SMShost+":"+SMSport+"/cgi-bin/sms_send?username="+SMSuser+"&password="+SMSpwd+"&number="+toSMS+"&text="+body;
				System.out.println("\n-- URL : "+hosturl);
				URL url = new URL(hosturl); 
				URLConnection con = url.openConnection();  // new
				con.addRequestProperty("User-Agent", "Mozilla");
				con.setReadTimeout(tout);
				con.setConnectTimeout(tout);
					if (con.getExpiration() > 0) {
						cacheexpiration = new Date(con.getExpiration());
						System.out.println("-- Cache expiration "+cacheexpiration);
					} else System.out.println("-- Cache expiration 0");
					
				BufferedReader httpin = new BufferedReader(
						new InputStreamReader(con.getInputStream()));

				String inputLine; 
				System.out.println(" read line" );
				while ((inputLine = httpin.readLine()) != null) {
					System.out.println(inputLine);
					swOK = true;
					if (inputLine.toUpperCase().indexOf("OK") >= 0 ) {
						System.out.println("-- SMS sent!");
						swOK = true;
					}
					else {
						System.out.println("-- SMS failed! ");
						swOK = false;
					}
				}
				httpin.close();
			} 
			catch (Exception e) { System.out.println(e);  }
		}

		if (swOK) {
			System.out.println(LocalDateTime.now()+" Successfully sent SMS message");
			return true;
		}
		else {
			System.out.println(LocalDateTime.now()+" Failure sending SMS message");
			return false;
		}

	}
	
	
	// sends status to the Jvakt server
	static protected void sendSTS( boolean STS) throws IOException {
//			System.out.println("--- Connecting to "+jvhost+":"+jvport);
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(jvhost, port);
//			System.out.println(jm.open()); 
			jm.open(); 
			jmsg.setId("Jvakt-SendSMShttp");
			if (STS) {
				jmsg.setBody("The SendSMShttp program is active. "+configF.getCanonicalPath());
				jmsg.setRptsts("OK");
			}
			else {
				jmsg.setBody("The SendSMShttp program aborted the SMS sending! "+configF.getCanonicalPath());
				jmsg.setRptsts("ERR");
			}
			jmsg.setType("T");

			inet = InetAddress.getLocalHost();
//			System.out.println("-- Inet: "+inet);
			agent = inet.toString();
			jmsg.setAgent(agent);

			if (!jm.sendMsg(jmsg)) System.out.println(LocalDateTime.now()+" --- Rpt to Jvakt Failed for Jvakt-SendSMS ---");
			jm.close();
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