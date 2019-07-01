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
import java.util.*;
//import java.time.*;
//import javax.mail.*;
//import javax.mail.Authenticator;
//import javax.mail.PasswordAuthentication;
=======
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
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee

public class SendSMSSTS {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	static boolean swDelete;
	static boolean swTiming;
	static boolean swFound;
	static boolean swShDay; // set when the scheduled day is active
	static boolean swDormant = false;
	static boolean swDB = true;
	static boolean swServer = true;
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

	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "xz";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int    port   = 1956;

	static String config = null;
	static File configF;

	static String body = "Jvakt: ";
	static int serrors = 0;
<<<<<<< HEAD
	static List<String> listTo;
=======
	static List listTo;
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
	static String toSMSSTSW;

	static String toSMS;
	static String SMShost;
	static String SMSport;
	static int SMSporti;

	static   Socket sock = null;
	static   OutputStreamWriter osw;
	static   InputStreamReader isr;

	static String value = "";


	public static void main(String[] args ) throws IOException, UnknownHostException {

<<<<<<< HEAD
		String version = "SendSMSSTS (2019-APR-29)";
=======
		String version = "SendSMSSTS 1.5 (2018-MAR-09)";
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}
 
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version+"  -  config file: "+configF);
		
		boolean swMail = false;
		getProps();
		
<<<<<<< HEAD
		listTo = new ArrayList<String>();  // All SMS numbers.
=======
		listTo = new ArrayList();  // All SMS numbers.
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee

		String[] toAddr = toSMSSTSW.split("\\,");
		for(int i=0 ; i<toAddr.length;i++) {
			//			System.out.println(toAddr[i]);
			listTo.add(toAddr[i]);
		}
		
		Statement stmt = null;
		String s;
<<<<<<< HEAD
//		boolean swHits;
//		String cause = "";
=======
		boolean swHits;
		String cause = "";
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee

		swServer = true;
		try {
			port = Integer.parseInt(jvport);
			SendMsg jm = new SendMsg(jvhost, port);  // kollar om JvaktServer är tillgänglig.
//			System.out.println(jm.open());
			if (jm.open().startsWith("DORMANT")) 	swDormant = true;
			else 									swDormant = false;
			jm.close();
		} 
		catch (IOException e1) {
			swServer = false;
			System.err.println(e1);
			System.err.println(e1.getMessage());
		}
//		System.out.println("swServer :" + swServer);
		
		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
//			System.out.println(DBUrl);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);

			s = new String("select * from console order by credat desc;"); 


//			System.out.println(s);
			stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
<<<<<<< HEAD
//			swHits = false;  // is there already a record?

			while (rs.next()) {
//				swHits = true;  
=======
			swHits = false;  // is there already a record?

			while (rs.next()) {
				swHits = true;  
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
				swTiming = false;  

				swFound = true;
				//--
				for (int i = 1; i <= 9; i++) {
					if (i==5) continue;  // not interested in showing condat
					if (i==9) continue;  // not interested in showing agent
					value = rs.getString (i);

					if (rs.getInt("prio") < 30 && rs.getString("status").contentEquals("ERR")) {
						errors++;
					}
					else if (rs.getInt("prio") >= 30 && rs.getString("status").contentEquals("ERR")) {
						warnings++;
					}
					else if (rs.getString("status").contentEquals("INFO") || rs.getString("status").contentEquals("OK")) {
						infos++;
					}
					else if (rs.getString("status").startsWith("Tim")) {
						warnings++;
					}
					else if (rs.getString("status").contentEquals("OK"))	{
						infos++;
					}
					else {
						infos++;
					}

				}
			}
			swMail = true;
			rs.close(); 
			stmt.close();

		}
		catch (SQLException e) {
			System.err.println("*** SQLExeption");
			swDB = false;
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			swDB = false;
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		
			if (errors > 0) {
				errors = errors / 7;
				body = body + "Errors: " + errors + "  ";
			}
			if (warnings > 0) {
				warnings = warnings / 7;
				body = body + "Warnings: " + warnings + "  ";
			}
			if (errors == 0 && warnings == 0) {
				body = body + "all is OKAY!  ";
			}
			if (infos > 0) {
				infos = infos / 7;
				body = body + "Infos: " + infos + "  ";
			}
			

			if (!swDB) {
				body = "\n - Jvakt Database not accessible ! -\n"; 
				swMail = true;
			}
			if (!swServer) {
				if (swDB) {
					body = "";
				}
				body = body + "- Jvakt Server not accessible ! "; 
				swMail = true;
			}

//			System.out.println("\n" + body );
			
			if (swMail && !swDormant) {
				sendSMS();
			}

	}        

	static void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
		input = new FileInputStream(configF);
		prop.load(input);
		// get the property value and print it out
		database = prop.getProperty("database");
		dbuser   = prop.getProperty("dbuser");
		dbpassword = prop.getProperty("dbpassword");
		dbhost   = prop.getProperty("dbhost");
		dbport   = prop.getProperty("dbport");
		jvport   = prop.getProperty("jvport");
<<<<<<< HEAD
//		int jvporti = Integer.parseInt(jvport);
=======
		int jvporti = Integer.parseInt(jvport);
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
		jvhost   = prop.getProperty("jvhost");
		toSMSSTSW  = prop.getProperty("toSMSSTS");
		SMShost = prop.getProperty("SMShost");
		SMSport = prop.getProperty("SMSport");
		SMSporti = Integer.parseInt(SMSport);
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		input.close();
		} catch (IOException ex) {
    		// ex.printStackTrace();
    	}  
	}
	
	// Connects to the SMS terminal and sends the text
	static boolean sendSMS() {

		boolean swOK = false;
<<<<<<< HEAD
		System.out.println("--- Sending Jvakt Status SMS ---\n");

		// Loop on all phone numbers
		for(Object object : listTo) { 
//			String element = (String) object;
			toSMS = (String) object;
			System.out.println("--- SMS to:"+toSMS +"      Body: " + body );
=======
		System.out.println("\nSending SMS....");

		// Loop on all phone numbers
		for(Object object : listTo) { 
			String element = (String) object;
			//			System.out.println(object);
			toSMS = (String) object;
			System.out.println("SMS to:"+toSMS +"   Body: " + body );
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee

			// Connect to Com-Server
			try {
				System.out.println("Connecting to: "+SMShost +":" + SMSporti );
				sock = new Socket( SMShost, SMSporti );
<<<<<<< HEAD
				sock.setSoTimeout( 2000 );  // receive timeout
=======
				sock.setSoTimeout( 200 );  // receive timeout
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
				osw = new OutputStreamWriter( sock.getOutputStream() );
				isr = new InputStreamReader( sock.getInputStream() );
			} catch( IOException e ) {
				System.out.println("IOExeption while connecting " + e);
<<<<<<< HEAD
				swOK = false;
=======
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
				break;
			}
			// Sending 
			try {
<<<<<<< HEAD
				System.out.println("Sending \"AT+CMGF=1\\r\\n\"" );
				osw.write( "AT+CMGF=1\r\n" );
				osw.flush();
				ReceiveText();
//				osw.write( "AT+CMGS="+ toSMS + "\r\n" );
				System.out.println("Sending AT+CMGS="+toSMS +"\\r" );
				osw.write( "AT+CMGS="+ toSMS + "\r" );
				osw.flush();
				ReceiveText();
				if (body.length() > 156 ) body = body.substring(0, 155);
=======
//				System.out.println("Sending \"AT+CMGF=1\\r\\n\"" );
				osw.write( "AT+CMGF=1\r\n" );
				osw.flush();
				ReceiveText();
//				System.out.println("Sending AT+CMGS="+toSMS +"\\r\\n" );
				osw.write( "AT+CMGS="+ toSMS + "\r\n" );
				osw.flush();
				ReceiveText();
				if (body.length() > 152 ) body = body.substring(0, 152);
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
				body = body.replace('_', '-'); // replace _ with - because SMS creates a §
				body = body.replace('Å', 'A'); 
				body = body.replace('Å', 'A'); 
				body = body.replace('Ö', 'O'); 
				body = body.replace('å', 'a'); 
				body = body.replace('ä', 'a'); 
				body = body.replace('ö', 'o'); 
				body = body.replaceAll("[^a-zA-Z0-9.:-]" , " ");
<<<<<<< HEAD
//				System.out.println("Sending body: "+body +"\\r\\n" );
				System.out.println("Sending body: "+body );
//				osw.write( body + "\r\n" + "\u001A" );
		        osw.write( body +"\u001A" );
=======
				System.out.println("Sending "+body +"\\r\\n" );
				osw.write( body + "\r\n" + "\u001A" );
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
				osw.flush();
				ReceiveText();
				swOK = true;
			} catch( IOException e ) {
<<<<<<< HEAD
				swOK = false;
				System.out.println("IOExeption while sending " + e);
				break;
=======
				System.out.println("IOExeption while sending " + e);
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
			}		      
			// closing and disconnecting 
			try {
				osw.close();
				isr.close();
				sock.close();
			} catch( IOException e ) {System.out.println("IOExeption while closing " + e); }

		}

		if (swOK) {
			System.out.println("RETURN true");
			return true;
		}
		else {
			System.out.println("RETURN false");
			return false;
		}

	}


	static public void ReceiveText() {
<<<<<<< HEAD
		try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace();}
//		try { Thread.currentThread().sleep(500); } catch (InterruptedException e) { e.printStackTrace();}
=======
		try { Thread.currentThread().sleep(200); } catch (InterruptedException e) { e.printStackTrace();}
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
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
<<<<<<< HEAD
				if (timeouts>999) {
					// timeout in input stream
					System.out.println("ReceiverText timeout in input stream: " + e);
					break;
				}
=======
				if (timeouts>999) break;
				// timeout in input stream
				//	    	  System.out.println("ReceiverText timeout in input stream: " + e);
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
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