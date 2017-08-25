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
	static Connection conn = null;
	static boolean swUpdate;
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
	static String version = "jVakt 2.0 - CheckStatus 1.2 Date 2017-08-22_01";
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "xz";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static String agent = null;

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
		Statement stmt = null;
		String s;
		boolean swHits;
		int accerr;
		int err;
		port = Integer.parseInt(jvport);
		agent = null;
		InetAddress inet;

		try {
			inet = InetAddress.getLocalHost();
//			System.out.println("-- Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()); 

//		System.out.println(DOW + " "+ zDate + " " + zTs);
		System.out.println("******* Nu ****** " + nu + " Klocka " +nu.getHour()+":"+nu.getMinute()+":"+nu.getSecond());
		
		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
//			System.out.println(DBUrl);
//			System.out.println("dbuser= " + dbuser +"  dbpassword "+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);

			s = new String("select * from status " + 
					"WHERE state='A' or state = 'D';"); 


			System.out.println(s);
//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);
			swHits = false;  // is there already a record?
			while (rs.next()) {
				System.out.println("-#1: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status"));
				swHits = true;  
				swTiming = false;  
				swPlugin = false;  
				swUpdate = false;

				if (rs.getString("id").equalsIgnoreCase("syssts")) continue;
//				if (rs.getString("console").equalsIgnoreCase("C") ) {
//					countErr(rs,'+');
//				}

				// Only types R, S or I is acceptable.
				if (!rs.getString("type").equalsIgnoreCase("R") && !rs.getString("type").equalsIgnoreCase("S") && !rs.getString("type").equalsIgnoreCase("I")) continue;

				zD = rs.getTimestamp("rptdat");
				accerr = rs.getInt("accerr");
				err    = rs.getInt("errors");

				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000); 

				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime();
//				System.out.println("Lrptdat " + Lrptdat + "   Lchktim " +Lchktim);
//				System.out.println("Nu " + nu + " Klocka " +nu.getHour()+":"+nu.getMinute()+":"+nu.getSecond() + " Chktim  " +rs.getTime("chktim").getHours());
				
				swShDay = false;
				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").startsWith(DOW.name().substring(0, 2) )) {
					if (nu.getHour() > rs.getTime("chktim").getHours() ) {
					swShDay = true; 
//					System.out.println("Timmen swShDay: "+swShDay);
					}
					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() > rs.getTime("chktim").getMinutes() ) {
						swShDay = true;	
//						System.out.println("Minuten swShDay: "+swShDay);
					}
					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() == rs.getTime("chktim").getMinutes() && nu.getSecond() > rs.getTime("chktim").getSeconds() ) {
						swShDay = true;	
//						System.out.println("Sekunden swShDay: "+swShDay);
					}
				} 
//				System.out.println("swShDay: "+swShDay);
				swDelete = false;

				// Om fel inträffat för S och R varnas till console
				if ((rs.getString("type").equalsIgnoreCase("R") || rs.getString("type").equalsIgnoreCase("S") || rs.getString("type").equalsIgnoreCase("I")) && 
						rs.getString("status").equalsIgnoreCase("ERR") && err > accerr ) {
					System.out.println("ERR #2: " + rs.getString("id")+" "+rs.getString("status")+"  MSG:"+rs.getString("msg"));
//					if (!rs.getString("console").equalsIgnoreCase("C")) {
					if (rs.getString("console").startsWith(" ")) {
//						countErr(rs,'+');
						System.out.println("Set console to C in ERR" + " " + rs.getString("id"));
						rs.updateString("console", "C");
						rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swUpdate=true;
					}
					if (rs.getString("msg").startsWith(" ")) {
						System.out.println("Set msg to M in ERR" + " " + rs.getString("id"));
						rs.updateString("msg", "M");
						rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						rs.updateString("info", "Set msg to M in ERR");  // felsökning 
						swPlugin = true;
						swUpdate=true;
					}
					if (swUpdate) try { rs.updateRow(); } catch(NullPointerException npe2) {}
					updC(rs); // add new line to the console table
					if (swPlugin && !rs.getString("plugin").startsWith(" ") && rs.getString("state").startsWith("A")) {
						trigPlugin(rs.getString("id"), rs.getString("status"), "P", rs.getString("body")); 
					}
				} // Om OK men timeout inträffat för S och R varnas till console
				else	if ( (rs.getString("type").equalsIgnoreCase("R") && rs.getString("status").equalsIgnoreCase("OK") && swShDay && Lsec > 900) ||
							 (rs.getString("type").equalsIgnoreCase("S") && rs.getString("status").equalsIgnoreCase("OK") && swShDay && 
							  rs.getTimestamp("rptdat").getTime() < mi.getTime() )
						) {
					if (!rs.getString("console").equalsIgnoreCase("C")) {
						rs.updateString("console", "C");
						rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
//						countErr(rs,'+');
						swUpdate=true;					}
					swTiming = true;
					System.out.println("timing #3: " + rs.getString("id")+"  MSG:"+rs.getString("msg"));
					if (rs.getString("msg").startsWith(" ")) {
						System.out.println("Set msg to T in timeout " + rs.getString("id"));
						rs.updateString("msg", "T");
						rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swPlugin = true;
						swUpdate=true;
					}
					System.out.println("timing #3.a :  " + rs.getString("id")+"  MSG:"+rs.getString("msg"));
					if (swUpdate) try { rs.updateRow(); } catch(NullPointerException npe2) {}
					updC(rs); // add new line to the console table
					if (swPlugin && !rs.getString("plugin").startsWith(" ")) {
						trigPlugin(rs.getString("id"), rs.getString("status"), "P", rs.getString("body")); 
					}
				} // Om allt bra tas console bort för S och R 
				else	 if ((rs.getString("type").equalsIgnoreCase("R") || rs.getString("type").equalsIgnoreCase("S") || rs.getString("type").equalsIgnoreCase("I")) && 
						rs.getString("console").equalsIgnoreCase("C") && rs.getString("status").equalsIgnoreCase("OK") ) {
					System.out.println("Del #4: " + rs.getString("id")+" "+rs.getString("status")+"  MSG:"+rs.getString("msg"));
					swDelete = true;
					rs.updateString("console", " ");
					rs.updateString("condat", null);
					swUpdate=true;
					if (rs.getString("msg").startsWith("M") || rs.getString("msg").startsWith("T")) {
						System.out.println("Set msg to blank in OK" + " " + rs.getString("id"));
						rs.updateString("msg", " ");
						rs.updateTimestamp("msgdat", null);
					}
					else if (rs.getString("msg").startsWith("S")) {
						System.out.println("Set msg to R in OK" + " " + rs.getString("id"));
						rs.updateString("msg", "R");
						rs.updateTimestamp("msgdat", null);
					}
//					countErr(rs,'-');
					if (swUpdate) try { rs.updateRow(); } catch(NullPointerException npe2) {}
					updC(rs); // update or remove line from the console table
				}
//				else { System.out.println("NOTHING RS: " + rs.getString("id")+" "+rs.getString("status")); }

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
		finally { 
//			System.out.println("CheckStatus Severe: " + errors + "  Problems: " + warnings + "  Info: " + infos ); 
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

		Statement stmt = null;
		int count = 1;

		try {
			System.out.println("updC RS: " + rs.getString("id")+" Type: " + rs.getString("type").toUpperCase());
			// Immediate type cause an update to the console table at once.

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

//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(50);
			ResultSet rs2 = stmt.executeQuery(s);
			count = 1;
			while (rs2.next()) {
				count = count + rs2.getInt(1);
				if (swDelete) {
					System.out.println(">>> deleterow");
					addHst(rs2);
					try { rs2.deleteRow(); } catch(NullPointerException npe2) {} 
				}
				else { 
					rs2.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					rs2.updateInt("count", count);
					if (swTiming) rs2.updateString("status","Timed out");
					else rs2.updateString("status",rs.getString("status").toUpperCase() ); 
					rs2.updateString("body", rs.getString("body"));
					try { rs2.updateRow(); } catch(NullPointerException npe2) {}
				}
			}
			rs2.close(); 
			stmt.close();
			//			}


			if (!swDelete && count == 1 ) {
				// insert new line with new timestamp and counter
				PreparedStatement st = conn.prepareStatement("INSERT INTO Console (count,id,prio,type,condat,credat,status,body,agent) "
						+ "values (?,?,?,?,?,?,?,?,?)");
				System.out.println("Prepared insert:" + st);
				st.setInt(1,count); // count
				st.setString(2,rs.getString("id") ); 
				st.setInt(3,rs.getInt("prio")); // prio
				st.setString(4,rs.getString("type").toUpperCase() ); // type
				st.setTimestamp(5, zTs); // condat
				st.setTimestamp(6, zTs); // credat
				System.out.println(">>> swTiming >>>>: " + swTiming);
				if (swTiming) st.setString(7,"Timed out");
				else st.setString(7,rs.getString("status").toUpperCase() );// 
				st.setString(8,rs.getString("body") ); // 
				st.setString(9,rs.getString("agent") ); // 
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

	static protected void countErr(ResultSet rs, char c) throws SQLException {
		if (c=='+')	{	
			if (rs.getString("status").startsWith("INFO") ) infos++; 
			else if (rs.getString("prio").compareTo("30") < 0 ) errors++; 
			else warnings++; 
		}
		else  {
			if (rs.getString("status").startsWith("INFO") ) infos--; 
			else if (rs.getString("prio").compareTo("30") < 0 ) errors--; 
			else warnings--; 
		}
//		System.out.println(" Err: " + errors + "  Warn: " + warnings + "  Info: " + infos ); 

	}
	
	//----- add new line to the consoleHst table -----
	static protected void addHst(ResultSet rs) throws IOException {

		try {
			System.out.println("addHst RS: " + rs.getString("id")+" Type: " + rs.getString("type").toUpperCase());

			// insert new line with new timestamp and counter
			PreparedStatement st = conn.prepareStatement("INSERT INTO ConsoleHst (credat,deldat,count,id,prio,type,status,body,agent) "
					+ "values (?,?,?,?,?,?,?,?,?)");
			System.out.println("Prepared insert:" + st);
			st.setTimestamp(1, rs.getTimestamp("credat")); // credat
			st.setTimestamp(2, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime())); // deldat
			st.setInt(3,rs.getInt("count")); // count
			st.setString(4,rs.getString("id") ); 
			st.setInt(5,rs.getInt("prio")); // prio
			st.setString(6,rs.getString("type").toUpperCase() ); // type
			st.setString(7,rs.getString("status") );// 
			st.setString(8,rs.getString("body") ); // 
			st.setString(9,rs.getString("agent") ); // 
			int rowsInserted = st.executeUpdate();
			System.out.println("Executed insert addHst " +rowsInserted);
			st.close();
			System.out.println("Closed addHst");
		}
		catch (SQLException e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		finally { System.out.println("CheckStatus addHst finally routine" ); }
	} 


	
}