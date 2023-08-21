package Jvakt;
/*
 * 2023-08-21 V.56 Michael Ekdal		Increated sleep times
 * 2023-01-03 V.55 Michael Ekdal		Added send of the status to Jvakt server
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.io.*;
import java.net.InetAddress;
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

public class SendSMS {

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
	
	//	static Authenticator auth;

	static List<String> listTo;

	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "SendSMS ";
		version += getVersion()+".56";
		String database = "jVakt";
		String dbuser   = "jVakt";
		String dbpassword = "";
		String dbhost   = "localhost";
		String dbport   = "5433";
//		String jvhost   = "localhost";
//		String jvport   = "1956";

		String config = null;
		File configF;

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

					if (sendSMS()) conn.commit();
					else			conn.rollback();
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
		}
	}        

	// Connects to the SMS terminal and sends the text
	static boolean sendSMS() {

		swOK = false;
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
		swOK = false;
		//		int n = 0;
		for(Object object : listTo) { 
			//				if (n>0) toSMSW = toEmailW + ",";
			//			n++;
			//			String element = (String) object;
			toSMSW = (String) object;

			toSMS = toSMSW;
			//				System.out.println("To:"+toEmailW+"   Subject: " + subject );
			System.out.println(LocalDateTime.now()+" --- Sending SMS to: "+toSMS +"      Body: " + body );

			// Connect to Com-Server
			try {
				System.out.println(LocalDateTime.now()+" Connecting to: "+SMShost +":" + SMSporti );
//				sock = new Socket( SMShost, SMSporti );
				sock = new Socket();
				sock.connect(new InetSocketAddress(SMShost, SMSporti), 2000);
				sock.setSoTimeout( 2000 );  // receive timeout
				osw = new OutputStreamWriter( sock.getOutputStream() );
				isr = new InputStreamReader( sock.getInputStream() );
			} catch( IOException e ) {
				swOK = false;
				System.out.println(LocalDateTime.now()+" IOExeption while connecting to SMShost" + e);
				break;
			}
			// Sending ( http://chadselph.github.io/smssplit/ )
			try {
				System.out.println(LocalDateTime.now()+" Sending \"AT+CMGF=1\\r\\n\"" );
				osw.write( "AT+CMGF=1\r\n" );   // SMS text mode
				osw.flush();
				ReceiveText();
//				if (reply.indexOf("OK") > 0 ) System.out.println(LocalDateTime.now()+" Received OK  "+reply);
				if (reply.indexOf("O") > 0 ) System.out.println(LocalDateTime.now()+" Received OK  "+reply);
//				if (reply.startsWith("O") )   System.out.println(LocalDateTime.now()+" Received OK  "+reply);
				//  	    	  System.out.println("Sending AT+CMGS="+toSMS +"\\r\\n" );
				System.out.println(LocalDateTime.now()+" Sending AT+CMGS="+toSMS +"\\r" );
				//		        osw.write( "AT+CMGS="+ toSMS + "\r\n" );
				osw.write( "AT+CMGS="+ toSMS + "\r" );
				osw.flush();
				ReceiveText();
				if (reply.indexOf(">") > 0 ) System.out.println(LocalDateTime.now()+" Received >  "+reply);
				if (body.length() > 140 ) body = body.substring(0, 139);
				body = body.replaceAll("_", "-"); // replace _ with - because SMS creates a �
				body = body.replace('Å', 'A'); 
				body = body.replace('Ä', 'A'); 
				body = body.replace('Ö', 'O'); 
				body = body.replace('å', 'a'); 
				body = body.replace('ä', 'a'); 
				body = body.replace('ö', 'o'); 
				body = body.replaceAll("[^a-zA-Z0-9.:-]" , " ");
				//	  	    	  System.out.println("Sending body: "+body +"\\r\\n" );
				System.out.println(LocalDateTime.now()+" Sending body: "+body );
				//			        osw.write( body + "\r\n" + "\u001A" );
				osw.write( body +"\u001A" );
				osw.flush();
				ReceiveText();
//				if (reply.indexOf("+CMGS") > 0 ) {
//				if (reply.startsWith("+")) {
				if (reply.indexOf("+") > 0 ) {
					System.out.println(LocalDateTime.now()+" Received +CMGS  "+reply);
					swOK = true;
				} else {
//					swOK = false;
					System.out.println(LocalDateTime.now()+" Did not receive +CMGS ! "+reply);
					break;					
				}

			} catch( IOException e ) {
//				swOK = false;
				System.out.println(LocalDateTime.now()+" IOExeption while sending SMS " + e);
				break;
			}		      
			// closing and disconnecting 
			try {
				osw.close();
				isr.close();
				sock.close();
			} catch( IOException e ) {}

		}

		if (swOK) {
			System.out.println(LocalDateTime.now()+" RETURN true");
			return true;
		}
		else {
			System.out.println(LocalDateTime.now()+" Failure sending SMS message");
			try {sendSTS(false);} catch (IOException e) { e.printStackTrace();}
			return false;
		}

	}

	static public void ReceiveText() {
		try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace();}
		String s;
		int i, len, timeouts;
		char c[] = new char[ 100 ];

		timeouts = 0;
		for( ;; ) {
			s = "";
			if (timeouts>10) {
				// timeout in input stream
				System.out.println(LocalDateTime.now()+" Aborting because of timeout in isr!");
				try {sendSTS(false);} catch (IOException e) { e.printStackTrace();}
				System.exit(12);
				//				break;
			}
			try {
				if (isr.ready()) {
					try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
					len = isr.read( c, 0, 100 );
				}
				else {
					timeouts++;
					System.out.println(LocalDateTime.now()+" isr is not ready, waiting...");
					try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace();}
					continue;
				}
				if( len < 0 ) {
					timeouts++;
					System.out.println(LocalDateTime.now()+" no reply, waiting...");
					try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace();}
					continue;
//					return;
				}
				for( i = 0; i < len; i++ ) {
					if( c[ i ] != 0 ) s += c[ i ];
				}
				reply = s;
			} catch (InterruptedIOException e) {
				timeouts++;
				System.out.println(LocalDateTime.now()+" ReceiverText InterruptedIOException in input stream: " + e);
			} catch (IOException e) {
				timeouts++;
				System.out.println(LocalDateTime.now()+" ReceiverText IOException: " + e);
				//				break;
			}

			if( s.length() > 0 ) {
				System.out.println(LocalDateTime.now()+" "+s);  
				System.out.flush();
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
				return;
			}	      
		}
	}
	
	// sends status to the Jvakt server
	static protected void sendSTS( boolean STS) throws IOException {
//			System.out.println("--- Connecting to "+jvhost+":"+jvport);
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(jvhost, port);
//			System.out.println(jm.open()); 
			jm.open(); 
			jmsg.setId("Jvakt-SendSMS");
			if (STS) {
				jmsg.setBody("The SendSMS program is active");
				jmsg.setRptsts("OK");
			}
			else {
				jmsg.setBody("The SendSMS program aborted the SMS sending!");
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