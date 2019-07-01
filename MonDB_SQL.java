package Jvakt;

//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
import java.net.InetAddress;
import java.sql.*;

//////////////////////////////////////////////////////////////////////////////////
//
// JDBCQuery example.  This program uses the IBM Toolbox for Java JDBC driver to
// query a table and output its contents.
//
// Command syntax:
//    JDBCQuery system collectionName tableName
//
// For example,
//    JDBCQuery MySystem qiws qcustcdt
//
//////////////////////////////////////////////////////////////////////////////////

//import java.util.Properties;

//import com.ibm.as400.access.AS400;
//import com.ibm.as400.access.AS400Message;
//import com.ibm.as400.access.CommandCall;


public class MonDB_SQL
{

//    static AS400 ppse08    = new AS400("ppse08.perscorp.com","mesenger","notify");
//    static CommandCall cmd = null;
//    static boolean as4opened = false;
    static String system;
    static String instance;
    static String pw;
    static String collectionName;
    static String tableName;
    static String where = ""; 
    static String stmt;
    static String url;
    static boolean state;

    static String id;
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;
	static int antal ;

    
static String jvhost   = "localhost";
static String jvport   = "1956";
static int port ;
static InetAddress inet;
static String version = "MonDB_SQL 1.1 Date 2017-09-08";
static String agent = null;
static boolean swSlut = false;


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



    public static void main (String[] parameters)
    {
        // Check the input parameters.
        if (parameters.length < 3) {
            
	        System.out.println("\n*** MonDB_SQL 1.0 Date 2012-11-13_1 *** \n*** by Michael Ekdal at Perstorp AB (ITOC) Sweden. ***");
            System.out.println("\nUsage:");
            System.out.println("");
//            System.out.println("   Params: host userid password collectionName tableName where-stmt");
            System.out.println("   Params: host instance collectionName tableName where-stmt");
            System.out.println("");
            System.out.println("For example:");
            System.out.println("");
            System.out.println("   ptp491.perscorp.com dafault PCW pcw.TBTCO \"where rpttime='040327'\"");
            System.out.println("");
            System.out.println("   The result is shown in sysout and the status is updated in DW");
            return;
        }
//        getProps();
        System.setProperty("java.net.preferIPv6Addresses", "false");
        system           = parameters[0];
        instance		= parameters[1];
        collectionName   = parameters[2];
        tableName        = parameters[3];
        where        = ""; 
        if (parameters.length >= 4) where        = parameters[4]; 

        Connection connection   = null;
        state = false;
        try {
        	System.out.println("Class loading...");
            // Load the Microsoft for Java JDBC driver.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//        	Class.forName("net.sourceforge.jtds.jdbc.Driver");
        	System.out.println("Class loaded OK");
            

            // Get a connection to the database.  Since we do not
            //String conUrl = "jdbc:sqlserver://" + sysSQL +";databaseName="+ collSQL + ";user=transfer" + ";password=T6aB22Cf" ;
//            url = "jdbc:microsoft:sqlserver://" + system +":1433;databaseName="+ collectionName +", "+"mesenger"+ ", " +"notify12Z" ;
            url = "jdbc:sqlserver://" + system +":1433;integratedSecurity=true;authenticationScheme=NativeAuthentication;databaseName="+ collectionName+";";  
            //url = "jdbc:sqlserver://" + system +":1433;databaseName="+ collectionName + ";user=transfer" + ";password=T6aB22Cf" ;  
//          url = "jdbc:sqlserver://" + system +":1433;databaseName="+ collectionName + ";integratedSecurity=true;" ;
//          jdbc:jtds:sqlserver://localhost/GP8;useNTLMv2=true
//            url = "jdbc:jtds:sqlserver://" + system +":1433;databaseName="+ collectionName + ";useNTLMv2=true;" ;
//            url = "jdbc:jtds:sqlserver://" + system +":1433/"+ collectionName +" ;instance=" + instance;
            System.out.println(url +"\n");
            connection = DriverManager.getConnection(url);
            System.out.println("Connection OK");

//            DatabaseMetaData dmd = connection.getMetaData ();

            // Execute the query.
            Statement select = connection.createStatement ();
//            stmt = ("SELECT * FROM " + collectionName + dmd.getCatalogSeparator() + tableName + " " + where);
            stmt = ("SELECT * FROM " + tableName + " " + where);
            System.out.println(stmt +"\n");
            ResultSet rs = select.executeQuery (stmt);

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

            // Iterate throught the rows in the result set and output
            // the columns for each row.
            antal = 0;
            while (rs.next ()) {
                for (int i = 1; i <= columnCount; ++i) {
                    String value = rs.getString (i);
                    if (rs.wasNull ())
                        value = "<null>";
                    System.out.print (format (value, columnWidths[i-1]));
                    System.out.print (" ");
                    state = true;
                }
                antal++;
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
//            	toJv(state);
            	if (connection != null)
                    connection.close ();
            }
            catch (SQLException e) {
                // Ignore.
            }
//            catch (IOException e) {
//                // Ignore.
//            }
        }

        System.out.println ("-- Antal: "+antal);
        System.exit (antal);
    }

//	// sends status to the server
//	static protected void toJv( boolean STS) throws IOException {
//		System.out.println("--- Connecting to "+jvhost+":"+jvport);
//		Message jmsg = new Message();
//		SendMsg jm = new SendMsg(jvhost, port);
//		System.out.println(jm.open());
////		if (!swSlut) jmsg.setId(id+"-CheckLogs-"+aFile);
////		else		 jmsg.setId(id+"-CheckLogs-"+aFile+"-JV");
//// 	    jmsg.setId(id+"-CheckLogs-"+aFile);
// 	    jmsg.setId(id);
//		if (!STS) jmsg.setRptsts("OK");
//		else jmsg.setRptsts("ERR");
////		if (!swSlut) t_desc =aFile+": "+t_desc;
//		jmsg.setBody(t_desc);
//		jmsg.setType("R");
//		jmsg.setAgent(agent);
//		jm.sendMsg(jmsg);
//		if (jm.close()) System.out.println("--- Rpt Delivered --  " + id + "  --  " + t_desc);
//		else            System.out.println("--- Rpt Failed --");
//
//	}

//	static void getProps() {
//
//		Properties prop = new Properties();
//		InputStream input = null;
//		try {
//			input = new FileInputStream("jVakt.properties");
//			prop.load(input);
//			// get the property value and print it out
//			jvport   = prop.getProperty("jvport");
//			jvhost   = prop.getProperty("jvhost");
//			port = Integer.parseInt(jvport);
//			System.out.println("getProps jvport: " + jvport + "    jvhost: "+jvhost) ;
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//		try {
//			inet = InetAddress.getLocalHost();
//			System.out.println("-- Inet: "+inet);
//			agent = inet.toString();
//		}
//		catch (Exception e) { System.out.println(e);  }
//
//	}



}
