package Jvakt;

/*
 * 2022-08-11 V.1 Michael Ekdal		New plugin to update Ivanti Serrvice Manager with input from the console 
 */

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class PlugIvantiSM {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;

	static String version = "CheckStatus ";
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;

	static boolean swDormant = false;

	static String row = "";
	static FileOutputStream fis;
	static OutputStreamWriter osw;
	static BufferedWriter logg;

	static String id = null;
	static String status = "OK";
	static String body = " ";
	static String agent = " ";
	static String type = "R";  // repeating
	static String prio = "30";  
	static String recid;  
	static boolean swInsert = false;  
	static boolean swDelete = false;  
	static boolean swLogg = true;  
	static boolean swRun = true;  
	static boolean swShow = true;  

	public static void main(String[] args ) throws IOException,
	FileNotFoundException {

		version += getVersion()+".1";

		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("The SolarEdge rest API is used to check the resources.");
			System.out.println("Power and the last reported connection by the site to the solaredge is checked.");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-norun  \tTo avoid update Ivanti SM server side. Used in test situations"+
					"\n-host   \tCheck a single host." +
					"\n-port   \tDefault is 443." +
					"\n-site   \tThe SolarEdge site id" +
					"\n-auth   \tThe SolarEdge API key." +
					"\n-log    \tWrite to specific file. like \"-log c:\\logg.txt\" " +
					"\n-show   \tShow the response from the server."
					);

			System.exit(4);
		}

		String config = null;
		File configF;

		for (int i=0; i<args.length; i++) {
//			System.out.println(args[i]);
			if (args[i].equalsIgnoreCase("-id")) id = args[++i];
			else if (args[i].equalsIgnoreCase("-prio")) prio = args[++i];
			else if (args[i].equalsIgnoreCase("-type")) type = args[++i];
			else if (args[i].equalsIgnoreCase("-sts")) status = args[++i];
			else if (args[i].equalsIgnoreCase("-body")) body = args[++i];
			else if (args[i].equalsIgnoreCase("-agent")) agent = args[++i];
			else if (args[i].equalsIgnoreCase("*INSERT")) swInsert = true;
			else if (args[i].equalsIgnoreCase("*DELETE")) swDelete = true;
			else if (args[i].equalsIgnoreCase("-recid")) recid = args[++i];
			else if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			else if (args[i].equalsIgnoreCase("-norun")) swRun = false;
		}		

		fis = new FileOutputStream("PlugIvantiSM.log", true);
		osw = new OutputStreamWriter(fis, "Cp850");
		logg = new BufferedWriter(osw);

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");

		System.out.println("----------- Jvakt PlugIvantiSM "+new Date()+"  Version: "+version +"  -  config file: "+configF);
		logg.write("----------- Jvakt PlugIvantiSM "+new Date()+"  Version: "+version +"  -  config file: "+configF);
		logg.newLine();

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
			jvhost   = prop.getProperty("jvhost");
			String	mode 	 =  prop.getProperty("mode");
			if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		} catch (IOException ex) {
			 ex.printStackTrace();
		}

		if (swInsert) row += "*INSERT ";
		if (swDelete) row += "*DELETE ";
		row += " -id "+id;
		row += " -prio "+prio;
		row += " -type "+type;
		row += " -sts "+status;
		row += " -body "+body;
		row += " -agent "+agent;
		row += " -recid "+recid;
		row += " -config "+config;

//		System.out.println(row);
//		 logg.write(row);
//		 logg.newLine(); 

		if (swInsert) {
			recid = "*recid-9*";
			try {
				String SQL_UPDATE;
				PreparedStatement StmtUpdate;
				Class.forName("org.postgresql.Driver").newInstance();
				DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
				conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
				conn.setAutoCommit(false);

				SQL_UPDATE ="UPDATE CONSOLE SET RECID=? WHERE ID ilike '"+id+"' AND PRIO="+Integer.parseInt(prio)+" AND TYPE='"+type+"'"+" AND BODY ilike '"+body+"'";
				System.out.println(new Date()+ " > "+SQL_UPDATE);
     			  logg.write(new Date()+" > "+SQL_UPDATE);
				  logg.newLine(); 

				StmtUpdate = conn.prepareStatement(SQL_UPDATE);
				StmtUpdate.setString(1, recid);
//				System.out.println(new Date()+" Updated records > "+StmtUpdate.executeUpdate());
				logg.write(new Date()+" Updated records > "+StmtUpdate.executeUpdate());
				logg.newLine();
				StmtUpdate.close();
				conn.commit();
				conn.close();
			}
			catch (SQLException e) {
				System.out.println(new Date()+" SQLExeption ");
				System.err.println(e);
				System.err.println(e.getMessage());
			}
			catch (Exception e) {
				System.out.println(new Date()+" Exeption");
				System.err.println(e);
				System.err.println(e.getMessage());
			}
		}

		if (swDelete) {
			System.out.println(new Date()+ " *DELETE:  recid = "+recid);
			logg.write(new Date()+ "  *DELETE: recid = "+recid);
			logg.newLine();
		}
		logg.close();

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