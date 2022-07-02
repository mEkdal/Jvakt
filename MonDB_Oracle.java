package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.*;
import java.util.Date;
import java.util.Properties;
import Jvakt.Message;
import Jvakt.SendMsg;

public class MonDB_Oracle
{

	// jvakt
	static String agent = null;
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int jvporti ;
	static InetAddress inet;
	static String config = null;
	static File configF;
	static boolean msgFixat = false;
	// jvakt
	static boolean swShow = false;
	static boolean swRun = false;
	static boolean swList = false;
	static String host;
	static String sid;
	static String port;
	static String user;
	static String pw;
	static String collectionName;
	static String table;
	static String where = ""; 
	static String url;
	static String stmt;
	static String state = "ERR";
	static String version = "monDB_Oracle ";
	static int antal = 0 ;

	// Format a string so that it has the specified width.
	private static String format (String s, int width)
	{
		String formattedString;

		// The string is shorter than specified width,
		// so we need to pad with blanks.
		if (s.length() < width) {
			StringBuffer buffer = new StringBuffer (s);
			for (int i = s.length(); i < width; ++i)
				buffer.append (" ");
			formattedString = buffer.toString();
		}

		// Otherwise, we need to truncate the string.
		else
			formattedString = s.substring (0, width);

		return formattedString;
	}



	public static void main (String[] args)
	{
		version += getVersion()+".54";
		// Check the input parameters.
		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-run    \tTo actually update the status in Jvakt."+
					"\n-host   \tThe host name or IP address of the Oracle server." +
					"\n-port   \tThe port number of the Oracle server." +
					"\n-sid    \tThe sid of the Oracle database." +
					"\n-user   \tThe username in the Oracle database." +
					"\n-pw     \tThe password." +
					"\n-table  \tThe table to query." +
					"\n-where  \tThe where statement of the query." +
					"\n-show   \tShow the response from the server." + 
					"\n-list   \tList the result from the query."  
					);
			System.exit(4);
		}

		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-host")) { host = args[++i]; }
			if (args[i].equalsIgnoreCase("-port")) { port = args[++i]; }
			if (args[i].equalsIgnoreCase("-sid")) { sid = args[++i]; }
			if (args[i].equalsIgnoreCase("-user")) { user = args[++i]; }
			if (args[i].equalsIgnoreCase("-pw")) { pw = args[++i]; }
			if (args[i].equalsIgnoreCase("-table")) { table = args[++i]; }
			if (args[i].equalsIgnoreCase("-where")) { where = args[++i]; }
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-show")) swShow = true;
			if (args[i].equalsIgnoreCase("-list")) swList = true;
		}

		// jvakt
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		if (swShow && swRun) System.out.println("-config file: "+configF);

		//		Properties props = new Properties();
		if (swRun) getProps();  // get Jvakt properties
		// jvakt   	

		Connection connection   = null;

		try {

			// Load the Ora thin JDBS driver.
			//DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
			Class.forName("oracle.jdbc.OracleDriver") ;
			if (swShow) System.out.println("Oracle JDBC driver loaded ok.");


			// Get a connection to the database.  Since we do not
			url = "jdbc:oracle:thin:"+user+"/"+pw+"@//"+host+":"+port+"/"+sid ;
			if (swShow) System.out.println(url +"\n");
			connection = DriverManager.getConnection(url);

//			DatabaseMetaData dmd = connection.getMetaData ();

			// Execute the query.
			Statement select = connection.createStatement ();

			if (swList) {
				if (where.length()>1) stmt = ("SELECT * FROM " + table + " WHERE " + where);
				else                  stmt = ("SELECT * FROM " + table );
			}
			else {
				if (where.length()>1) stmt = ("SELECT count(*) FROM " + table + " WHERE " + where);
				else                  stmt = ("SELECT count(*) FROM " + table );
			}
			state = "OK";

			if (swShow) System.out.println(stmt +"\n");
			ResultSet rs = select.executeQuery (stmt);

			if (swShow || swList) {
				// Get information about the result set.  Set the column
				// width to whichever is longer: the length of the label
				// or the length of the data.
				ResultSetMetaData rsmd = rs.getMetaData ();
				int columnCount = rsmd.getColumnCount ();
				String[] columnLabels = new String[columnCount];
				int[] columnWidths = new int[columnCount];
				for (int i = 1; i <= columnCount; ++i) {
					columnLabels[i-1] = rsmd.getColumnLabel (i);
					columnWidths[i-1] = Math.max (columnLabels[i-1].length(),
							rsmd.getColumnDisplaySize (i));
				}

				// Output the column headings.
				for (int i = 1; i <= columnCount; ++i) {
					System.out.print (format (rsmd.getColumnLabel(i), columnWidths[i-1]));
					System.out.print (" ");
				}
				System.out.println ();

				// Output a dashed line.
				//          StringBuffer dashedLine;
				for (int i = 1; i <= columnCount; ++i) {
					for (int j = 1; j <= columnWidths[i-1]; ++j)
						System.out.print ("-");
					System.out.print (" ");
				}
				System.out.println ();

				// Iterate through the rows in the result set and output
				// the columns for each row.
				antal = 0;
				while (rs.next ()) {
					for (int i = 1; i <= columnCount; ++i) {
						String value = rs.getString (i);
						if (rs.wasNull ())
							value = "<null>";
						System.out.print (format (value, columnWidths[i-1]));
						System.out.print (" ");
						state = "OK";
					}
					antal++;
					System.out.println ();
				}
			}

		}

		catch (Exception e) {
			if (swShow) System.out.println ("ERROR: " + e.getMessage());
			state = "ERR";
		}

		finally {
			if (state.startsWith("OK")) {
				System.out.println(new Date()+" -- Connection succcessful     "+host+":"+port+" "+sid);
			}
			else {
				System.out.println(new Date()+" -- Connection failed          "+host+":"+port+" "+sid);
				antal = 0;
			}
			if (swRun) {
				try {
					sendJv("MonDB_Oracle_"+host+":"+port+"-"+sid , state , "R",  "Checking Oracle connection to: "+host+":"+port+" "+sid);
				}
				catch (IOException e) {
					// Ignore. 
				}
			}
			// Clean up.
			try {
				if (connection != null)
					connection.close ();
			}
			catch (SQLException e) { 
				// Ignore. 
			}
		}

		if (swShow) System.out.println ("-- Antal: "+antal);
		System.exit (antal);
	}

	static protected void sendJv( String ID, String STS, String type, String msg) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, jvporti);
		if (swShow) System.out.println(jm.open());
		else jm.open();
		jmsg.setId(ID);
		jmsg.setRptsts(STS);
		jmsg.setBody(msg);
		jmsg.setType(type);
		jmsg.setAgent(agent);
		jm.sendMsg(jmsg);
		if (jm.close()) {
			if (swShow) System.out.println("-- Rpt Delivered --");
			msgFixat = true;
		}
		else            System.out.println("-- Rpt Failed --");

	}

	static void getProps() {
		// jvakt
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configF);
			prop.load(input);
			// get the property value and print it out
			jvport   = prop.getProperty("jvport");
			jvhost   = prop.getProperty("jvhost");
			jvporti = Integer.parseInt(jvport);

			if (swShow) System.out.println("getProps jvport:" + jvport + "  jvhost:"+jvhost) ;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			inet = InetAddress.getLocalHost();
			if (swShow) System.out.println("-- Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

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
