package Jvakt;
/*
 * 2023-01-03 V.55 Michael Ekdal		Added send of the status to Jvakt server
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.io.*;
import java.net.InetAddress;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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

	static String expected = "";
	static String reply = "";

	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int    port   = 1956;

	static String config = null;
	static File configF;

	static String body = "Jvakt: ";
	static int serrors = 0;
	static List<String> listTo;
	static String toSMSSTSW;

	static String toSMS;
	static String SMShost;
	static String SMSport;
	static int SMSporti;

	static   Socket sock = null;
	static   OutputStreamWriter osw;
	static   InputStreamReader isr;

	static String value = "";

	static InetAddress inet;
	static String agent = null;

//	public static void main(String[] args ) throws IOException, UnknownHostException {
	public static void main(String[] args ) {

		String version = "SendSMSSTS ";
		version += getVersion()+".55";

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version+"  -  config file: "+configF);

		boolean swMail = false;
		getProps();

		listTo = new ArrayList<String>();  // All SMS numbers.

		String[] toAddr = toSMSSTSW.split("\\,");
		for(int i=0 ; i<toAddr.length;i++) {
			//			System.out.println(toAddr[i]);
			listTo.add(toAddr[i]);
		}

		Statement stmt = null;
		String s;
		//		boolean swHits;
		//		String cause = "";

		swServer = true;
//		try {
			port = Integer.parseInt(jvport);
			SendMsg jm = new SendMsg(jvhost, port);  // kollar om JvaktServer �r tillg�nglig.
			//			System.out.println(jm.open());
			if (jm.open().startsWith("DORMANT")) 	swDormant = true;
			else 									swDormant = false;
			jm.close();
//		} 
//		catch (IOException e1) {
//			swServer = false;
//			System.err.println(e1);
//			System.err.println(e1.getMessage());
//		}
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
			//			swHits = false;  // is there already a record?

			while (rs.next()) {
				//				swHits = true;  
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
					else if (rs.getString("status").startsWith("TOut")) {
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
			conn.close();
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
			sendSMS();  // sendSMS will exit the program with no return here.
		}
		try {sendSTS(true);} catch (IOException e) { e.printStackTrace();}

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
			if (dbpassword.startsWith("==y")) {
			    byte[] decodedBytes = Base64.getDecoder().decode(dbpassword.substring(3));
			    String decodedString = new String(decodedBytes);
			    dbpassword=decodedString;
			}
			dbhost   = prop.getProperty("dbhost");
			dbport   = prop.getProperty("dbport");
			jvport   = prop.getProperty("jvport");
			//		int jvporti = Integer.parseInt(jvport);
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
		System.out.println("--- Sending Jvakt Status SMS ---\n");

		// Loop on all phone numbers
		for(Object object : listTo) { 
			//			String element = (String) object;
			swOK = false;
			toSMS = (String) object;
			System.out.println("--- SMS to:"+toSMS +"      Body: " + body );

			// Connect to Com-Server
			try {
				System.out.println("Connecting to SMShost: "+SMShost +":" + SMSporti +"\n" );
				//				sock = new Socket( SMShost, SMSporti );
				sock = new Socket();
				sock.connect(new InetSocketAddress(SMShost, SMSporti), 2000);
				sock.setSoTimeout( 2000 );  // receive timeout
				osw = new OutputStreamWriter( sock.getOutputStream() );
				isr = new InputStreamReader( sock.getInputStream() );
			} catch( IOException e ) {
				System.out.println("IOExeption while connecting to SMShost" + e);
				swOK = false;
				break;
			}
			// Sending 
			try {
				System.out.println("Sending \"AT+CMGF=1\\r\\n\"" );
				osw.write( "AT+CMGF=1\r\n" );
				osw.flush();
				ReceiveText();
				if (reply.indexOf("OK") > 0 ) System.out.println("Received OK  "+reply);
				//				osw.write( "AT+CMGS="+ toSMS + "\r\n" );
				System.out.println("Sending AT+CMGS="+toSMS +"\\r" );
				osw.write( "AT+CMGS="+ toSMS + "\r" );
				osw.flush();
				ReceiveText();
				if (reply.indexOf(">") > 0 ) System.out.println("Received >  "+reply);
				if (body.length() > 140 ) body = body.substring(0, 139);
				body = body.replace('_', '-'); // replace _ with - because SMS creates a �
				body = body.replace('Å', 'A'); 
				body = body.replace('Ä', 'A'); 
				body = body.replace('Ö', 'O'); 
				body = body.replace('å', 'a'); 
				body = body.replace('ä', 'a'); 
				body = body.replace('ö', 'o'); 
				body = body.replaceAll("[^a-zA-Z0-9.:-]" , " ");
				//				System.out.println("Sending body: "+body +"\\r\\n" );
				System.out.println("Sending body: "+body );
				//				osw.write( body + "\r\n" + "\u001A" );
				osw.write( body +"\u001A" );
				osw.flush();
				ReceiveText();
				if (reply.indexOf("+CMGS") > 0 ) {
					System.out.println("Received +CMGS  "+reply);
					swOK = true;
				} else {
					swOK = false;
					System.out.println("Did not receive +CMGS ! "+reply);
//					break;					
				}
					
			} catch( IOException e ) {
				swOK = false;
				System.out.println("IOExeption while sending SMS " + e);
				break;
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
			try {sendSTS(true);} catch (IOException e) { e.printStackTrace();}
			System.exit(0);
			return true;
		}
		else {
			System.out.println("Failure sending SMS message");
			try {sendSTS(false);} catch (IOException e) { e.printStackTrace();}
			System.exit(12);
			return false;
		}

	}


	static public void ReceiveText() {
		try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
		String s;
		int i, len, timeouts;
		char c[] = new char[ 100 ];

		timeouts = 0;
		for( ;; ) {
			s = ""; reply = "";
			if (timeouts>10) {
				// timeout in input stream
				System.out.println("Aborting because of timeout in isr!");
				try {sendSTS(false);} catch (IOException e) { e.printStackTrace();}
				System.exit(12);
				//				break;
			}
			try {
				if (isr.ready()) {
					try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace();}
					len = isr.read( c, 0, 100 );
				}
				else {
					timeouts++;
					System.out.println("isr is not ready, waiting...");
					try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
					continue;
				}
				if( len < 0 ) {
					timeouts++;
					System.out.println("no reply, waiting...");
					try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
					continue;
//					return;
				}
				for( i = 0; i < len; i++ ) {
					if( c[ i ] != 0 ) s += c[ i ];
				}
				reply = s;
			} catch (InterruptedIOException e) {
				timeouts++;
				System.out.println("ReceiverText InterruptedIOException in input stream: " + e);
			} catch (IOException e) {
				timeouts++;
				System.out.println("ReceiverText IOException: " + e);
				//				break;
			}

			if( s.length() > 0 ) {
				System.out.println(s);  
				System.out.flush();
				try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace();}
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
			jmsg.setId("Jvakt-SendSMSSTS");
			if (STS) {
				jmsg.setBody("The SendSMSSTS program is working.");
				jmsg.setRptsts("OK");
			}
			else {
				jmsg.setBody("The SendSMSSTS program is not working!");
				jmsg.setRptsts("ERR");
			}
			jmsg.setType("T");

			inet = InetAddress.getLocalHost();
//			System.out.println("-- Inet: "+inet);
			agent = inet.toString();

			jmsg.setAgent(agent);
			if (!jm.sendMsg(jmsg)) System.out.println("--- Rpt to Jvakt Failed for Jvakt-SendSMSSTS ---");
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