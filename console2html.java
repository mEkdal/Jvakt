package Jvakt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.*;
import java.util.Date;
public class console2html {

//    static Vector map = new Vector(100,10);
    static Date now;
	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	//    static ResultSet rs;
	static Connection conn = null;
	static boolean swDelete;
	static boolean swTiming;
	static boolean swShDay; // set when the scheduled day is active
	static java.sql.Date zDate;
	static java.sql.Timestamp zD;
	static java.sql.Timestamp zTs;
	static java.sql.Time zT;
	static Long lhou, lmin, Lsec, Lrptdat, Lchktim;
	static int errors = 0;
	static int warnings = 0;
	static int infos = 0;
	static String version = "CheckStatus ( 2021-12-30 )";
	static String database = "Jvakt";
	static String dbuser   = "console";
	static String dbpassword = "";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static String agent = null;
    static String config = null;
	static File configF;

    public static void main (String[] args) throws IOException, UnknownHostException
    {

    now = new Date();


	for (int i=0; i<args.length; i++) {
		if (args[i].equalsIgnoreCase("-config")) config = args[++i];
	}

	if (config == null ) 	configF = new File("console.properties");
	else 					configF = new File(config,"console.properties");
//	System.out.println("----- Jvakt: "+now+"   version: "+version);
//	System.out.println("-config file: "+configF);

    getProps();
    DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
    
	String head   = "<html><head>  <TITLE>jVakt - CONSOLE - System Status</TITLE> <meta http-equiv=\"refresh\" content=\"60\">  </head> <body bgcolor=\"silver\">";
	String foot   = "</body></html>";
//	String tblStr = "<TABLE COLS=7 BORDER=8 cellpadding=\"5\" width=\"100%\"  >"; 
	String tblStr = "<TABLE COLS=6 BORDER=8 cellpadding=\"5\" width=\"100%\"  >"; 
	String tblEnd = "</TABLE>";
//	String tblHdr = "<TH>Count</TH> <TH>Id</TH> <TH>Prio</TH> <TH>Type</TH> <TH>Condat</TH>  <TH>Status</TH>  <TH>Body - "+ now +" </TH> <TH>Agent</TH>"; 
	String tblHdr = "<TH>Id</TH> <TH>Prio</TH> <TH>Type</TH> <TH>Credat</TH>  <TH>Status</TH>  <TH>Body - "+ now +" </TH> <TH>Agent</TH>"; 
	String hdrStrG = "<TH BGCOLOR=\"#00FF00\"><FONT SIZE=5>"; // Green
	String hdrStrY = "<TH BGCOLOR=\"#FFFF00\"><FONT SIZE=5>"; // Yellow
	String hdrStrR = "<TH BGCOLOR=\"#FF6600\"><FONT SIZE=5>"; // Red
	String hdrStrM = "<TH BGCOLOR=\"#FF00FF\"><FONT SIZE=5>"; // Magenta
	String hdrStrB = "<TH BGCOLOR=\"#CCEEFF\"><FONT SIZE=5>"; // Light blue
	String hdrEnd = "</TH>";
	String rowStr = "<TR>"; 
	String rowEnd = "</TR>";
	String boxStrG = "<TD BGCOLOR=\"#3CB371\"><FONT SIZE=5>"; // MediumSeaGreen
//	String boxStrG = "<TD BGCOLOR=\"#00FF00\"><FONT SIZE=5>"; // Green
	String boxStrY = "<TD BGCOLOR=\"#FAFAD2\"><FONT SIZE=5>"; // LightGoldenRodYellow
//	String boxStrY = "<TD BGCOLOR=\"#FFFF00\"><FONT SIZE=5>"; // Yellow
	String boxStrR = "<TD BGCOLOR=\"#CD5C5C\"><FONT SIZE=5>"; // IndianRed 
//	String boxStrR = "<TD BGCOLOR=\"#FF6600\"><FONT SIZE=5>"; // Red
	String boxStrM = "<TD BGCOLOR=\"#FF00FF\"><FONT SIZE=5>"; // Magenta
	String boxStrB = "<TD BGCOLOR=\"#CCEEFF\"><FONT SIZE=5>"; // Light blue
	String boxStrO = "<TD BGCOLOR=\"#FF8C00\"><FONT SIZE=5>"; // DarkOrange
	String boxStr  = "<TD><FONT SIZE=5>"; 
	String boxEnd  = "</FONT></TD>";

	System.out.println (head);
	System.out.println (tblStr);
	System.out.println (tblHdr);
	boolean swFound = false;
	String query;
	Statement stmt = null;

	try {
		Class.forName("org.postgresql.Driver").newInstance();
		DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
		conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
		conn.setAutoCommit(true);
	    query = new String("select * from console order by credat desc;"); 
//		stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE);
		stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT );
		stmt.setFetchSize(1000);
		ResultSet rs = stmt.executeQuery(query);

	    String value = "";
	    while (rs.next ()) {
		    swFound = true;
	    	System.out.println (rowStr);

		for (int i = 1; i <= 9; i++) {
			if (i==1) continue;  // not interested in showing count
			if (i==5) continue;  // not interested in showing condat
//			if (i==6) continue;  // not interested in showing credat
		    value = rs.getString (i);
  		       
		    if (rs.getInt("prio") < 30 && rs.getString("status").contentEquals("ERR")) System.out.println (boxStrM);
		    else if (rs.getInt("prio") >= 30 && rs.getString("status").contentEquals("ERR")) System.out.println (boxStrR);
		    else if (rs.getString("status").contentEquals("INFO")) System.out.println (boxStrY);
		    else if (rs.getString("status").startsWith("TOut")) System.out.println (boxStrO);
		    else if (rs.getString("status").contentEquals("OK"))		System.out.println (boxStrG); 
		    else System.out.println (boxStrB);
		    	 
		    System.out.print (value);
		    System.out.println (boxEnd);
		}
    	System.out.println (rowEnd); 
		
	    }
//		if (!swFound) {
//			System.out.println (rowStr);
//			System.out.println (boxStrG);
//			System.out.println ("&nbsp;");
//			System.out.println (boxEnd);
//			System.out.println (boxStrG);
//			System.out.println ("&nbsp;");
//			System.out.println (boxEnd);
//			System.out.println (boxStrG);
//			System.out.println ("&nbsp;");
//			System.out.println (boxEnd);
//			System.out.println (boxStrG);
//			System.out.println ("&nbsp;");
//			System.out.println (boxEnd);
//			System.out.println (boxStrG);
//			System.out.println ("&nbsp;");
//			System.out.println (boxEnd);
//			System.out.println (boxStrG);
//			System.out.println ("&nbsp;");
//			System.out.println (boxEnd);
//			System.out.println (boxStrG);
//			System.out.println ("&nbsp;");
//			System.out.println (boxEnd);
//			System.out.println (boxStrG);
//			System.out.println ("&nbsp;");
//			System.out.println (boxEnd);
//			System.out.println (rowEnd);
//		}

	}
	catch (Exception e) {
	    System.out.println ();
	    System.out.println ("ERROR: " + e.getMessage());
	}

	finally {

	    // Clean up.
	    try {
		if (conn != null)
		    conn.close ();
	    }
	    catch (SQLException e) {
		// Ignore.
	    }
	    System.out.println (tblEnd);
		System.out.println (foot);

	}

	System.exit (0);
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
    	jvhost   = prop.getProperty("jvhost");
    	input.close();
    	} catch (IOException ex) {
    		// ex.printStackTrace();
    	}
    	
	}
}
