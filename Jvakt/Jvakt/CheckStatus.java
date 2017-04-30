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

public class CheckStatus {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	//    static ResultSet rs;
	static Connection conn = null;
	static boolean swDelete;
	static boolean swTiming;
	static boolean swPlugin = false;
	static boolean swShDay; // set when the scheduled day is active
	static boolean swDormant = false;
	static java.sql.Date zDate;
	static java.sql.Timestamp zD;
	static java.sql.Timestamp zTs;
	static java.sql.Time zT;
	static Long lhou, lmin, Lsec, Lrptdat, Lchktim;
	static int errors = 0;
	static int warnings = 0;
	static int infos = 0;
	static String version = "jVakt 2.0 - CheckStatus 1.0 Date 2017-02-21_01";
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "xz";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static String agent = null;
//	static ResultSet rs;

	public static void main(String[] args ) throws IOException, UnknownHostException {

		
		Properties prop = new Properties();
		InputStream input = null;
		try {
		input = new FileInputStream("jVakt.properties");
		prop.load(input);
		// get the property value and print it out
		database = prop.getProperty("database");
		dbuser   = prop.getProperty("dbuser");
		dbpassword = prop.getProperty("dbpassword");
		dbhost   = prop.getProperty("dbhost");
		dbport   = prop.getProperty("dbport");
		jvport   = prop.getProperty("jvport");
		jvhost   = prop.getProperty("jvhost");
		String	mode 	 =  prop.getProperty("mode");
		if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		} catch (IOException ex) {
			// ex.printStackTrace();
		}

		LocalDateTime nu = LocalDateTime.now(); // The current date and time
		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
//		Instant instant = mi.toInstant();

/*		System.out.println("Nu = " + nu);
		System.out.println("Midnatt = " + midnatt);
		System.out.println(nu.getYear() + "-" + nu.getMonthValue() + "-"+ nu.getDayOfMonth() );
		System.out.println("Midnatt mi = " + mi);
		System.out.println("DOW = " + DOW.name() +"  Daynum "+ DOW.getValue());
*/

		//	 Connection conn = null;
		Statement stmt = null;
//		PreparedStatement pStmt = null;
		String s;
		boolean swHits;
		//PreparedStatement q1;
//		int updated;
//		int sessnum;
		int accerr;
		int err;
//		int count;
		//static Date now;

//	     String host = "127.0.0.1";
		 port = Integer.parseInt(jvport);
	     agent = null;
	     InetAddress inet;

		   try {
		       inet = InetAddress.getLocalHost();
		       System.out.println("-- Inet: "+inet);
		       agent = inet.toString();
		   }
		   catch (Exception e) { System.out.println(e);  }
		
		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()); 

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			//"jdbc:postgresql://localhost:5433/Jvakt";
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			System.out.println(DBUrl);
//			conn = DriverManager.getConnection(DBUrl,"Jvakt","xz");
			System.out.println("dbuser= " + dbuser +"  dbpassword "+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);

			s = new String("select * from status " + 
					"WHERE state='A' or state = 'D';"); 


			System.out.println(s);
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
			//     System.out.println("Query executed");
			swHits = false;  // is there already a record?
			while (rs.next()) {
				System.out.println("\n\n---- main RS: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+" "+zD);
				swHits = true;  
				swTiming = false;  
				swPlugin = false;  
				
				if (rs.getString("id").equalsIgnoreCase("syssts")) continue;
				if (rs.getString("console").equalsIgnoreCase("C") ) {
					countErr(rs,'+');
				}

				if (!rs.getString("type").equalsIgnoreCase("R") && !rs.getString("type").equalsIgnoreCase("S") && !rs.getString("type").equalsIgnoreCase("I")) continue;
				
				zD = rs.getTimestamp("rptdat");
				accerr = rs.getInt("accerr");
				err    = rs.getInt("errors");

//				System.out.println("zD : " + zD);
//				System.out.println("zTs : " + zTs);

				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000); 
//				System.out.println("Diff sec : " + Lsec);

				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime();
//				System.out.println("chktim msec : " + Lchktim + " " + rs.getTime("chktim"));
//				System.out.println("rptdat msec : " + Lrptdat + " " + rs.getTime("rptdat") + " "+ rs.getTimestamp("rptdat").getTime());
//				System.out.println("mi msec: " + mi.getTime()+" "+mi);

				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").startsWith(DOW.name())) {
//					System.out.println("chkday : " + rs.getString("chkday") + " " + DOW.name());
					swShDay = true;
				} else swShDay = false;
				
				swDelete = false;

				// Om fel inträffat för S och R varnas till console
				if ((rs.getString("type").equalsIgnoreCase("R") || rs.getString("type").equalsIgnoreCase("S") || rs.getString("type").equalsIgnoreCase("I")) && 
						!rs.getString("status").equalsIgnoreCase("OK") && err > accerr ) {
					System.out.println("ERR RS: " + rs.getString("id")+" "+rs.getString("status"));
					if (!rs.getString("console").equalsIgnoreCase("C")) countErr(rs,'+');
					if (rs.getString("msg").startsWith(" ")) {
						System.out.println("Set msg to M in ERR");
						rs.updateString("msg", "M");
						rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swPlugin = true;
					}
					rs.updateString("console", "C");
					rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					try { rs.updateRow(); } catch(NullPointerException npe2) {}
					updC(rs); // add new line to the console table
 					if (swPlugin && !rs.getString("plugin").startsWith(" ")) {
						trigPlugin(rs.getString("id"), rs.getString("status"), "P", rs.getString("body")); 
					}
				} // Om OK men timeout inträffat för S och R varnas till console
				else	if ( (rs.getString("type").equalsIgnoreCase("R") && rs.getString("status").equalsIgnoreCase("OK") && Lsec > 900) ||
					    	((rs.getString("type").equalsIgnoreCase("S") && rs.getString("status").equalsIgnoreCase("OK") && swShDay &&
//								(Lchktim < Lrptdat || rs.getTimestamp("rptdat").getTime() < mi.getTime())))
								(rs.getTimestamp("rptdat").getTime() < mi.getTime())))
					    	) {
					if (!rs.getString("console").equalsIgnoreCase("C")) countErr(rs,'+');
					swTiming = true;
					System.out.println("timing RS: " + rs.getString("id"));
					if (rs.getString("msg").startsWith(" ")) {
						System.out.println("Set msg to T in timeout");
						rs.updateString("msg", "T");
						rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swPlugin = true;
					}
					rs.updateString("console", "C");
					rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					try { rs.updateRow(); } catch(NullPointerException npe2) {}
					updC(rs); // add new line to the console table
 					if (swPlugin && !rs.getString("plugin").startsWith(" ")) {
						trigPlugin(rs.getString("id"), rs.getString("status"), "P", rs.getString("body")); 
					}
					} // Om allt bra tas console bort för S och R 
				else	 if ((rs.getString("type").equalsIgnoreCase("R") || rs.getString("type").equalsIgnoreCase("S") || rs.getString("type").equalsIgnoreCase("I")) && 
						rs.getString("console").equalsIgnoreCase("C") && rs.getString("status").equalsIgnoreCase("OK") ) {
					System.out.println("Del RS: " + rs.getString("id")+" "+rs.getString("status"));
					swDelete = true;
					rs.updateString("console", " ");
					rs.updateString("condat", null);
					if (rs.getString("msg").startsWith("M") || rs.getString("msg").startsWith("T")) {
						System.out.println("Set msg to blank in OK");
						rs.updateString("msg", " ");
						rs.updateTimestamp("msgdat", null);
					}
					else if (rs.getString("msg").startsWith("S")) {
						System.out.println("Set msg to R in OK");
						rs.updateString("msg", "R");
						rs.updateTimestamp("msgdat", null);
					}
					rs.updateString("console", " ");
					rs.updateString("condat", null);
					countErr(rs,'-');
					try { rs.updateRow(); } catch(NullPointerException npe2) {}
					updC(rs); // remove line from the console table
				}
				else { System.out.println("NOTHING RS: " + rs.getString("id")+" "+rs.getString("status")); }

//				try { rs.updateRow(); } catch(NullPointerException npe2) {} 
			}
			rs.close(); 
			stmt.close();

		}
		catch (SQLException e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		finally { System.out.println("CheckStatus Severe: " + errors + "  Problems: " + warnings + "  Info: " + infos ); 
//		   sendSTS();
		}
	}        

	// trigger plugin
	static protected void trigPlugin(String id, String sts, String type, String body) throws IOException {
		if (swDormant) return;
		System.out.println("-- trigPlugin --" + id +" "+ sts +" "+ type +" "+ body);
        Message jmsg = new Message();
        SendMsg jm = new SendMsg(jvhost, port);
        System.out.println(jm.open());
        jmsg.setId(id);
        jmsg.setRptsts(sts);
        jmsg.setBody(body);
        jmsg.setType(type);
        jmsg.setAgent(agent);
        jm.sendMsg(jmsg);
        if (jm.close()) System.out.println("-- Rpt Delivered --");
        else            System.out.println("-- Rpt Failed --");
		
	}
	
	// sends SYSSTS to the server
	static protected void sendSTS() throws IOException {
        Message jmsg = new Message();
        SendMsg jm = new SendMsg(jvhost, port);
        System.out.println(jm.open());
        jmsg.setId("SYSSTS");
        jmsg.setRptsts("Info");
        jmsg.setBody("Severe: " + errors + "  Problems: " + warnings + "  Info: " + infos);
        jmsg.setType("I");
        jmsg.setAgent(agent);
        jm.sendMsg(jmsg);
        if (jm.close()) System.out.println("-- Rpt Delivered --");
        else            System.out.println("-- Rpt Failed --");
		
	}
	
	//----- add new line to the console table -----
	static protected void updC(ResultSet rs) throws IOException {

		//		 Connection conn = null;
		Statement stmt = null;
//		PreparedStatement pStmt = null;
		int count = 1;

		try {
			System.out.println("updC  RS: " + rs.getString("id"));
			// Immediate type cause an update to the console table at once.
			System.out.println(">>> Type >>>>: " + rs.getString("type").toUpperCase());

//			if (!rs.getString("status").equalsIgnoreCase("INFO")) {

				// read and remove previous line from the console table and save the count field
				String s=null;
				if (swDelete) {
					s = new String("select * from console " + 
							"WHERE id ilike '" + rs.getString("id") + 
							"' and prio='" 	 +	rs.getString("prio") +
							"' and type='" 	 +	rs.getString("type").toUpperCase() +
							"';");
				} else {
					s = new String("select * from console " + 
							"WHERE id ilike '" + rs.getString("id") + 
							"' and prio='" 	 +	rs.getString("prio") +
							"' and type='" 	 +	rs.getString("type").toUpperCase() +
							"' and body ilike '" + rs.getString("body") +
							"';");        		
				}

				stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
				stmt.setFetchSize(50);
				ResultSet rs2 = stmt.executeQuery(s);
				count = 1;
				while (rs2.next()) {
					//System.out.println("rs2.getInt(1) " +rs2.getInt(1));
					count = count + rs2.getInt(1);
					try { rs2.deleteRow(); } catch(NullPointerException npe2) {} 
				}
				rs2.close(); 
				stmt.close();
//			}


			if (!swDelete) {
				// insert new line with new timestamp and counter
				PreparedStatement st = conn.prepareStatement("INSERT INTO Console (count,id,prio,type,condat,status,body) "
						+ "values (?,?,?,?,?,?,?)");
				System.out.println("Prepared insert:" + st);
				st.setInt(1,count); // count
				st.setString(2,rs.getString("id") ); 
				st.setInt(3,rs.getInt("prio")); // prio
				st.setString(4,rs.getString("type").toUpperCase() ); // type
				st.setTimestamp(5, zTs); // condat
				System.out.println(">>> swTiming >>>>: " + swTiming);
				if (swTiming) st.setString(6,"Timed out");
				else st.setString(6,rs.getString("status").toUpperCase() );// 
				st.setString(7,rs.getString("body") ); // 
				int rowsInserted = st.executeUpdate();
				System.out.println("Executed insert addC " +rowsInserted);
				st.close();
			}
			System.out.println("Closed addC");
		}
		catch (SQLException e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		finally { System.out.println("CheckStatus addC finally routine" ); }
	} 

	//----- add new line to the console table -----
	static protected void countErr(ResultSet rs, char c) throws SQLException {
		if (c=='+')	{	
			if (rs.getString("status").startsWith("INFO") ) infos++; 
			else if (rs.getString("prio").compareTo("3") < 0 ) errors++; 
    			 else warnings++; 
		}
		else  {
			if (rs.getString("status").startsWith("INFO") ) infos--; 
			else if (rs.getString("prio").compareTo("3") < 0 ) errors--; 
    			 else warnings--; 
		}
    	System.out.println(" Err: " + errors + "  Warn: " + warnings + "  Info: " + infos ); 

	}
}