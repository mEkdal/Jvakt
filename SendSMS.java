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

//import ptp.tool.ReceiverThread;

public class SendSMS {

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
	//	static int errors = 0;
	static int warnings = 0;
	static int infos = 0;
	static int resolved = 0;

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


	//	static Authenticator auth;

	static List listTo;

	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "SendSMS 1.0 # 2017-12-07";
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
		System.out.println("----- Jvakt: "+new Date()+"    Version: "+version);
		System.out.println("-config file: "+configF);


		prop = new Properties();
		InputStream input = null;
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
		toSMSW  = prop.getProperty("toSMS");
		SMShost = prop.getProperty("SMShost");
		SMSport = prop.getProperty("SMSport");
		SMSporti = Integer.parseInt(SMSport);
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		input.close();


		listTo = new ArrayList();  // All SMS numbers.

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

		try {
			SendMsg jm = new SendMsg(jvhost, jvporti);  // kollar om JvaktServer �r tillg�nglig.
			//			System.out.println(jm.open());
			if (jm.open().startsWith("DORMANT")) {
				swDormant = true;
			}
		} 
		catch (IOException e1) {
			System.err.println(e1);
			System.err.println(e1.getMessage());
		}

		if (swDormant) {
			System.out.println("*** Jvakt in DORMANT mode, SendSMS exiting *** ");
			System.exit(4);			
		}

		LocalDateTime nu = LocalDateTime.now(); // The current date and time
		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
		//		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
		Statement stmt = null;
		String s;
		//		boolean swHits;
		//		String cause = "";
		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()); 

		System.out.println("**********SendSMS ********   " + LocalDateTime.now());
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
				System.out.println("- main RS - State:"+rs.getString("state")+" Id:" + rs.getString("id")+" Type:"+rs.getString("type")+" Prio:"+rs.getString("prio")+" Console:"+rs.getString("console")+" Status:"+rs.getString("status")+ " Sms:"+rs.getString("sms"));
				//				swHits = true;  
				swTiming = false;  

				zD = rs.getTimestamp("rptdat");
				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000); 
				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime();

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

				if (swShDay) {
					body = rs.getString("id")+" "+rs.getString("body");
					if (rs.getString("sms").equalsIgnoreCase("M") ) { 
						System.out.println("Problem " + body);
						serrors++;
					}
					else if (rs.getString("sms").equalsIgnoreCase("R")) {
						System.out.println("Resolved " + body);
						resolved++;
					}
					else {
						System.out.println("Timeout " + body);
						warnings++;
					}
					if (rs.getString("sms").equalsIgnoreCase("R")) rs.updateString("sms", " ");
					else rs.updateString("sms", "S");
					rs.updateTimestamp("smsdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					try { rs.updateRow(); } catch(NullPointerException npe2) {}

					System.out.println("After update");

					if (sendSMS()) conn.commit();
					else			conn.rollback();
				}
			}


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

	// Connects to the SNS terninal and sents the text
	static boolean sendSMS() {

		boolean swOK = false;
		System.out.println("Sending SMS....");
//		subject = "* NEW status * -->   ";

		if (serrors > 0) {
//			subject = subject + "Errors: " + serrors + "  ";
			body = "ERROR: " + body;
		}
		if (warnings > 0) {
//			subject = subject + "Time-outs: " + warnings + "  ";
			body = "TIME-OUT: "+body ;
		}
		if (resolved > 0) { 
//			subject = subject + "Resolved: " + resolved;
			body = "RESOLVED: "+body ;
		}

		//		toSMSW = "";
//		int n = 0;
		// Loop on all phone numbers
		for(Object object : listTo) { 
			//				if (n>0) toSMSW = toEmailW + ",";
//			n++;
			String element = (String) object;
			//			System.out.println(object);
			toSMS = (String) object;
			//			toSMS = toSMSW;
			//				System.out.println("To:"+toEmailW+"   Subject: " + subject );
			System.out.println("\nSMS to:"+toSMS +"   Body: " + body );

			// Connect to Com-Server
			try {
				System.out.println("Connecting to: "+SMShost +":" + SMSporti );
				sock = new Socket( SMShost, SMSporti );
				sock.setSoTimeout( 200 );  // receive timeout
				osw = new OutputStreamWriter( sock.getOutputStream() );
				isr = new InputStreamReader( sock.getInputStream() );
			} catch( IOException e ) {
				System.out.println("IOExeption while connecting " + e);
				break;
			}
			// Sending 
			try {
				System.out.println("Sending \"AT+CMGF=1\\r\\n\"" );
				osw.write( "AT+CMGF=1\r\n" );
				osw.flush();
				ReceiveText();
				System.out.println("Sending AT+CMGS="+toSMS +"\\r\\n" );
				osw.write( "AT+CMGS="+ toSMS + "\r\n" );
				osw.flush();
				ReceiveText();
				if (body.length() > 152 ) body = body.substring(0, 152);
				body = body.replace('_', '-'); // replace _ with - because SMS creates a �
				body = body.replace('�', 'A'); 
				body = body.replace('�', 'A'); 
				body = body.replace('�', 'O'); 
				body = body.replace('�', 'a'); 
				body = body.replace('�', 'a'); 
				body = body.replace('�', 'o'); 
				body = body.replaceAll("[^a-zA-Z0-9.:-]" , " ");
				System.out.println("Sending "+body +"\\r\\n" );
				osw.write( body + "\r\n" + "\u001A" );
				osw.flush();
				ReceiveText();
				swOK = true;
			} catch( IOException e ) {
				System.out.println("IOExeption while sending " + e);
			}		      
			// closing and disconnecting 
			try {
				osw.close();
				isr.close();
				sock.close();
			} catch( IOException e ) {System.out.println("IOExeption while closing " + e); }

		}

		if (swOK) {
			System.out.println("\nRETURN true");
			return true;
		}
		else {
			System.out.println("\nRETURN false");
			return false;
		}

	}

	static public void ReceiveText() {
		try { Thread.currentThread().sleep(200); } catch (InterruptedException e) { e.printStackTrace();}
		String s;
		int i, len, timeouts;
		char c[] = new char[ 100 ];
		
		timeouts = 0;
		for( ;; ) {
			s = "";
			try {
				len = isr.read( c, 0, 100 );
				if( len < 0 ) {
					return;
				}
				for( i = 0; i < len; i++ ) {
					if( c[ i ] != 0 ) s += c[ i ];
				}
			} catch (InterruptedIOException e) {
				timeouts++;
				if (timeouts>999) break;
				// timeout in input stream
				//	    	  System.out.println("ReceiverText timeout in input stream: " + e);
			} catch (IOException e) {
				System.out.println("ReceiverText IOException: " + e);
				break;
			}

			if( s.length() > 0 ) {
				System.out.println(s);  
				return;
			}	      
		}
	}


}