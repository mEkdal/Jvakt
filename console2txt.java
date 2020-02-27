package Jvakt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.*;
import java.util.Date;
public class console2txt {

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
	static String version = "console2txt 2.0 Date 2019-07-09";
	static String database = "Jvakt";
	static String dbuser   = "console";
	static String dbpassword = "Jvakt";
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

    getProps();
    DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
    
	String splitCol  = "<;>";

	boolean swFound = false;
	String query;
	Statement stmt = null;

	try {
		Class.forName("org.postgresql.Driver").newInstance();
		DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
		conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
		conn.setAutoCommit(true);
	    query = new String("select * from console order by credat desc;"); 
		stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT );
		stmt.setFetchSize(1000);
		ResultSet rs = stmt.executeQuery(query);

	    String value = "";
	    while (rs.next ()) {
		    swFound = true;
//	    	System.out.println ("");

		for (int i = 1; i <= 9; i++) {
//			if (i==6) continue;  // not interested in showing credat
		    value = rs.getString (i);
  		       
		    System.out.print (value);
		    System.out.print (splitCol);
		}
    	System.out.println (); 
		
	    }
		if (!swFound) {
	    	System.out.println (); 
		}

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
