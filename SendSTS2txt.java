package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
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

public class SendSTS2txt {

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


	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "SendSTS2txt ";
		version += getVersion()+".54";

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}
 
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
//		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version+"  -  config file: "+configF);
		
		getProps();
		
		
		Statement stmt = null;
		String s;

		swServer = true;
//		try {
			port = Integer.parseInt(jvport);
			SendMsg jm = new SendMsg(jvhost, port);  // Check if the JvaktServer is available.
			if (jm.open().startsWith("DORMANT")) 	swDormant = true;
			else 									swDormant = false;
			jm.close();
//		} 
//		catch (IOException e1) {
//			swServer = false;
//			System.err.println(e1);
//			System.err.println(e1.getMessage());
//		}
		
		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);

			s = new String("select * from console order by credat desc;"); 


			stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);

			while (rs.next()) {
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
			}
			if (!swServer) {
				if (swDB) {
					body = "";
				}
				body = body + "- Jvakt Server not accessible ! "; 
			}

			System.out.println( body );
			

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
		input.close();
		} catch (IOException ex) {
    		// ex.printStackTrace();
    	}  
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