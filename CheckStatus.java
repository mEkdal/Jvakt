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
import java.time.LocalDateTime;

public class CheckStatus {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	static boolean swUpdate;
	static boolean swDelete;
	static boolean swTiming;
	static boolean swPlugin = false;
	static boolean swShDay; // set when the scheduled day is active
	static boolean swDormant = false;
	static boolean swRowDormant = false;
	static java.sql.Date zDate;
	static java.sql.Timestamp zD;
	static java.sql.Timestamp zTs;
	static java.sql.Time zT;
	static Long lhou, lmin, Lsec, Lrptdat, Lchktim;
	static int errors = 0;
	static int warnings = 0;
	static int infos = 0;
	static String version = "CheckStatus (2020-FEB-27)";
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "xz";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static String agent = null;
	static Calendar cal = Calendar.getInstance();
	static String currentID;

	public static void main(String[] args ) throws IOException, UnknownHostException {

		String config = null;
		File configF;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----------- Jvakt: "+new Date()+"  Version: "+version +"  -  config file: "+configF);

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
		//		boolean swHits;
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
		catch (Exception e) { System.out.println(LocalDateTime.now()+"  "+e);  }

		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());     // YYYY-MM-DD
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime());  // YYYY-MM-DD hh:mm.ss.nnn

		//		System.out.println(DOW + " <> "+ zDate + " <> " + zTs);
		//		System.out.println("******* Nu ****** " + nu + " Klocka " +nu.getHour()+":"+nu.getMinute()+":"+nu.getSecond());

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);

			s = new String("select * from status " + 
					"WHERE state='A' or state = 'D';"); 
			
//						s = new String("select * from status " + 
//								"WHERE (state='A' or state = 'I') and id ilike '%Mail_from_ups_by892_%';"); 


			//						System.out.println(s);
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s); 
			//			swHits = false;  // is there already a record?
			while (rs.next()) {
				//				System.out.println("## 5 " +rs.getString("id"));
				currentID = rs.getString("id");

				//				swHits = true;  
				swTiming = false;  
				swPlugin = false;  
				swUpdate = false;

				if (rs.getString("state").equalsIgnoreCase("D")) 	swRowDormant = true;
				else 												swRowDormant = false;

				// Only types R, S, T or I is acceptable.
				if (!rs.getString("type").equalsIgnoreCase("R") && !rs.getString("type").equalsIgnoreCase("S") && !rs.getString("type").equalsIgnoreCase("T") && !rs.getString("type").equalsIgnoreCase("I")) continue;

				if (!rs.getString("status").equalsIgnoreCase("OK"))
					System.out.println(LocalDateTime.now()+" -#1: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+" "+rs.getString("errors")+" "+rs.getString("accerr")+" "+rs.getString("chkday")+" "+rs.getTime("chktim"));

				zD = rs.getTimestamp("rptdat"); 
				accerr = rs.getInt("accerr");
				err    = rs.getInt("errors");
				
//				if (zD == null) {
//					cal.set(1970, 01, 01, 0, 0, 0); // only HH:MM:SS is used
//					zD = new java.sql.Timestamp( cal.getTime().getTime());
//				}
				
				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000);  // Aktuella tiden minus tiden rptdat i sekunder  

				Lchktim = rs.getTime("chktim").getTime();
				Lrptdat = rs.getTime("rptdat").getTime(); 

				// swShDay will be set to true if the chktim time has passed and chkday is *ALL or the name of day like MON, TUE... 
				swShDay = false;
				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").indexOf(DOW.name().substring(0, 2)) >= 0 ) {
					//					System.out.println("Hour:"+rs.getTime("chktim").getHours() +" Min:"+ rs.getTime("chktim").getMinutes()+" Sec:"+rs.getTime("chktim").getSeconds());
					cal.setTime(rs.getTime("chktim"));
					//					System.out.println("Timm "+cal.get(Calendar.HOUR_OF_DAY)+" MIn:"+cal.get(Calendar.MINUTE) + " Sek:"+cal.get(Calendar.SECOND));
					//					if (nu.getHour() > rs.getTime("chktim").getHours() ) {
					if (nu.getHour() > cal.get(Calendar.HOUR_OF_DAY) ) {
						swShDay = true; 
					}
					//					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() > rs.getTime("chktim").getMinutes() ) {
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY)  && nu.getMinute() > cal.get(Calendar.MINUTE) ) {
						swShDay = true;	
					}
					//					else if (nu.getHour() == rs.getTime("chktim").getHours() && nu.getMinute() == rs.getTime("chktim").getMinutes() && nu.getSecond() > rs.getTime("chktim").getSeconds() ) {
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY)  && nu.getMinute() == cal.get(Calendar.MINUTE) && nu.getSecond() > cal.get(Calendar.SECOND) ) {
						swShDay = true;	
					}
				} 
				swDelete = false;
				//				System.out.println("ERR #1,5 swShDay: " + swShDay);

				// If status is ERR or INFO the row is tagged console, SMS and mail
				if ( (rs.getString("status").equalsIgnoreCase("ERR") && err > accerr) || rs.getString("status").equalsIgnoreCase("INFO") ) { 
					//					(rs.getString("type").equalsIgnoreCase("R") || rs.getString("type").equalsIgnoreCase("S") || rs.getString("type").equalsIgnoreCase("I"))) {
					System.out.println(LocalDateTime.now()+" ERR: #2 "+"  MSG:"+rs.getString("msg")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+" "+rs.getString("errors")+" "+rs.getString("accerr"));
					if (rs.getString("console").startsWith(" ")) {
						System.out.println(LocalDateTime.now()+" ERR: Set console to C" + " " + rs.getString("id"));
						rs.updateString("console", "C");
						rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swUpdate=true;
					}
					if (rs.getString("msg").startsWith("T") || rs.getString("msg").startsWith(" ")) {
						System.out.println(LocalDateTime.now()+" ERR: Set msg to M" + " " + rs.getString("id"));
						rs.updateString("msg", "M");
						rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swPlugin = true;
						swUpdate=true;
					}
					if ( rs.getString("sms").startsWith("T") || rs.getString("sms").startsWith(" ")) {
						System.out.println(LocalDateTime.now()+" ERR: Set sms to M" + " " + rs.getString("id"));
						rs.updateString("sms", "M");
						rs.updateTimestamp("smsdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swUpdate=true;
					}
					if ( rs.getString("msg30").startsWith("T") || rs.getString("msg30").startsWith(" ")) {
						System.out.println(LocalDateTime.now()+" ERR: Set msg30 to M" + " " + rs.getString("id"));
						rs.updateString("msg30", "M");
						rs.updateTimestamp("msgdat30", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swUpdate=true;
					}
					if (swUpdate) try { rs.updateRow(); } catch(NullPointerException npe2) {}
					//					System.out.println("## 1");
					updC(rs); // add or remove line to/from the console table
					//					System.out.println("## 2");
					if (swPlugin && rs.getString("plugin").length() > 4 && rs.getString("state").startsWith("A")) {
						trigPlugin(rs.getString("id"), rs.getString("status"), "P", rs.getString("body")); 
					}
				} 
				// If timeout occurred regarding S and R, console is tagged
				if ( 
						(rs.getString("type").equalsIgnoreCase("R") && rs.getString("status").equalsIgnoreCase("OK") && Lsec > 1200 && swShDay  ) 
						||
						(rs.getString("type").equalsIgnoreCase("S") && swShDay && 
								rs.getTimestamp("rptdat").getTime() < mi.getTime() )    // mi is the date and time of midnight
						)
				{
					if (!rs.getString("console").equalsIgnoreCase("C")) {
						rs.updateString("console", "C");
						rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swUpdate=true;
					}
					swTiming = true;
					System.out.println(LocalDateTime.now()+" timing #3: " + rs.getString("id")+"  MSG:"+rs.getString("msg"));
					rs.updateString("status", "TOut");
					if (rs.getString("msg").startsWith(" ")) {
						System.out.println(LocalDateTime.now()+" TOut: Set msg to T " + rs.getString("id"));
						rs.updateString("msg", "T");
						rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						swPlugin = true;
						swUpdate=true;
					}
					if (rs.getString("sms").startsWith(" ")) {
						System.out.println(LocalDateTime.now()+" TOut: Set sms to T " + rs.getString("id"));
						rs.updateString("sms", "T");
						rs.updateTimestamp("smsdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					}
					if (rs.getString("msg30").startsWith(" ")) {
						System.out.println(LocalDateTime.now()+" TOut: Set msg30 to T " + rs.getString("id"));
						rs.updateString("msg30", "T");
						rs.updateTimestamp("msgdat30", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					}
					System.out.println(LocalDateTime.now()+" timing #3.a :  " + rs.getString("id")+"  MSG:"+rs.getString("msg"));
					if (swUpdate) try { rs.updateRow(); } catch(NullPointerException npe2) {}
					updC(rs); // add new line to the console table
					if (swPlugin && !rs.getString("plugin").startsWith(" ")) {
						trigPlugin(rs.getString("id"), rs.getString("status"), "P", rs.getString("body")); 
					}
				} // If status is OK the console tag will be removed for S,R and T, and if console=C or msg isn't " " or sms isn't " "
				else	 if ((rs.getString("type").equalsIgnoreCase("R") || rs.getString("type").equalsIgnoreCase("S") || rs.getString("type").equalsIgnoreCase("T")) && 
						rs.getString("status").equalsIgnoreCase("OK") &&  
						(rs.getString("console").equalsIgnoreCase("C") || !rs.getString("msg").startsWith(" ") || !rs.getString("sms").startsWith(" ") || !rs.getString("msg30").startsWith(" ") )
						) {
					System.out.println(LocalDateTime.now()+" DEL: #4 " + rs.getString("id")+" "+rs.getString("status")+"  MSG:"+rs.getString("msg")+"  SMS:"+rs.getString("sms")+"  MSG30:"+rs.getString("msg30") );
					swDelete = true;   // remove lines from console
					rs.updateString("console", " ");
//					rs.updateString("condat", null);
					rs.updateString("status", "OK");
					swUpdate=true;
					if (rs.getString("msg").startsWith("M") || rs.getString("msg").startsWith("T")) {
						System.out.println(LocalDateTime.now()+" OK: Set msg to blank" + " " + rs.getString("id"));
						rs.updateString("msg", " ");
//						rs.updateTimestamp("msgdat", null);
					}
					else if (rs.getString("msg").startsWith("S")) {
						System.out.println(LocalDateTime.now()+" OK: Set msg to R" + " " + rs.getString("id"));
						rs.updateString("msg", "R");
//						rs.updateTimestamp("msgdat", null);
					}
					if (rs.getString("sms").startsWith("M") || rs.getString("sms").startsWith("T")) {
						System.out.println(LocalDateTime.now()+" OK: Set sms to blank" + " " + rs.getString("id"));
						rs.updateString("sms", " ");
//						rs.updateTimestamp("smsdat", null);
					}
					else if (rs.getString("sms").startsWith("S")) {
						System.out.println(LocalDateTime.now()+" OK: Set sms to R" + " " + rs.getString("id"));
						rs.updateString("sms", "R");
//						rs.updateTimestamp("smsdat", null);
					}
					if (rs.getString("msg30").startsWith("M") || rs.getString("msg30").startsWith("T")) {
						System.out.println(LocalDateTime.now()+" OK: Set msg30 to blank" + " " + rs.getString("id"));
						rs.updateString("msg30", " ");
					}
					else if (rs.getString("msg30").startsWith("S")) {
						System.out.println(LocalDateTime.now()+" OK: Set msg30 to R" + " " + rs.getString("id"));
						rs.updateString("msg30", "R");
					}
					if (swUpdate) try { rs.updateRow(); } catch(NullPointerException npe2) {}
					updC(rs); // update or remove line from the console table
				}
			}
			rs.close(); 
			stmt.close();
			conn.close();
		}
		catch (SQLException e) {
			System.out.println(LocalDateTime.now()+" CurrentID = " + currentID);
			System.err.println(e);
						System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.out.println(LocalDateTime.now()+" CurrentID = " + currentID);
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
		System.out.println(LocalDateTime.now()+" -- trigPlugin --" + id +" "+ sts +" "+ type +" "+ body);
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		System.out.println(jm.open());
		jmsg.setId(id);
		jmsg.setRptsts(sts);
		jmsg.setBody(body);
		jmsg.setType(type);
		jmsg.setAgent(agent);
		//		jm.sendMsg(jmsg);
		if (jm.sendMsg(jmsg)) System.out.println(LocalDateTime.now()+" -- Rpt Delivered --");
		else           	      System.out.println(LocalDateTime.now()+" -- Rpt Failed --");
		jm.close();
	}

	// sends SYSSTS to the server
	static protected void sendSTS() throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		System.out.println(LocalDateTime.now()+" "+jm.open());
		jmsg.setId("SYSSTS");
		jmsg.setRptsts("Info");
		jmsg.setBody("Severe: " + errors + "  Problems: " + warnings + "  Info: " + infos);
		jmsg.setType("I");
		jmsg.setAgent(agent);
		//		jm.sendMsg(jmsg);
		if (jm.sendMsg(jmsg)) System.out.println(LocalDateTime.now()+" -- Rpt Delivered --");
		else           	      System.out.println(LocalDateTime.now()+" -- Rpt Failed --");
		jm.close();
	}

	//----- add or remove line to/from the console table -----
	static protected void updC(ResultSet rs) throws IOException {

		Statement stmt = null;
		int count = 1;

		try {
			System.out.println(LocalDateTime.now()+" updC: swDelete:"+swDelete+" ID:"+rs.getString("id") +" Type:"+rs.getString("type").toUpperCase()+" Prio:"+rs.getString("prio").toUpperCase()+" Body:"+rs.getString("body"));
			// Immediate type cause an update to the console table at once.

			// read and remove previous line from the console table and save the count field
			String s=null;
			String prioS;
			if (swRowDormant)	prioS = "99";
			else 				prioS =  rs.getString("prio");
			System.out.println(LocalDateTime.now()+" Prios: " + prioS);

			if (swDelete) {
				s = new String("select * from console " + 
						"WHERE id ilike '" + rs.getString("id") + 
						"' and prio='" 	 +	prioS +
						"' and type='" 	 +	rs.getString("type").toUpperCase() +
						"';");
			} else {
				s = new String("select * from console " + 
						"WHERE id ilike '" + rs.getString("id") + 
						"' and prio='" 	 +	prioS +
						"' and type='" 	 +	rs.getString("type").toUpperCase() +
						"' and body ilike '" + rs.getString("body") +
						"';");        		
			}

			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(50);
			ResultSet rs2 = stmt.executeQuery(s);
			count = 1;
			while (rs2.next()) {
				count = rs2.getInt(1);
				count++;
				if (count<2) count=2;
				if (swDelete) {
					System.out.println(LocalDateTime.now()+" delete console: " + rs2.getString("id")+ " - "+rs2.getString("prio")+" - "+rs2.getString("body"));
					addHst(rs2);
					try { rs2.deleteRow(); } catch(NullPointerException npe2) {} 
				}
				else { 
					rs2.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					rs2.updateInt("count", count);
					System.out.println(LocalDateTime.now()+" update console: row.count:"+count  + " - id:"+ rs2.getString("id")+ " - "+rs2.getString("prio") + " - " + rs2.getString("body") );
					if (swTiming) {
						rs2.updateString("status","TOut");
						rs2.updateString("body", rs.getString("body"));
					}
					else {
						rs2.updateString("status",rs.getString("status").toUpperCase() );
						rs2.updateString("body", rs.getString("body"));
					}
					// om dormant
					//					if (swRowDormant) rs2.updateInt("prio", 99);
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
//				System.out.println(LocalDateTime.now()+" Prepared insert:" + st);
				System.out.println(LocalDateTime.now()+" Insert console: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("body")+" "+rs.getString("status")+" "+rs.getString("errors")+" "+rs.getString("accerr"));
				st.setInt(1,count); // count
				st.setString(2,rs.getString("id") ); 
				// om dormant
				if (swRowDormant) 	st.setInt(3,99); // prio
				else 				st.setInt(3,rs.getInt("prio")); // prio
				st.setString(4,rs.getString("type").toUpperCase() ); // type
				st.setTimestamp(5, zTs); // condat
				st.setTimestamp(6, zTs); // credat
				System.out.println(LocalDateTime.now()+" swTiming: " + swTiming);
				//				if (swTiming) st.setString(7,"TOut");
				//				else st.setString(7,rs.getString("status").toUpperCase() );// 
				if (swTiming) {
					st.setString(7,"TOut");
					st.setString(8,rs.getString("body") );
				}
				else {
					st.setString(7,rs.getString("status").toUpperCase() );
					st.setString(8,rs.getString("body") );
				}
				//				st.setString(8,rs.getString("body") ); // 
				st.setString(9,rs.getString("agent") ); // 
				int rowsInserted = st.executeUpdate();
				System.out.println(LocalDateTime.now()+" Executed insert addC " +rowsInserted);
				st.close();
			}
			System.out.println(LocalDateTime.now()+" Closed addC");
		}
		catch (SQLException e) {
			System.err.println(LocalDateTime.now()+" "+e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(LocalDateTime.now()+" "+e);
			System.err.println(e.getMessage());
		}
		finally { System.out.println(LocalDateTime.now()+" CheckStatus addC finally routine" ); }
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
			System.out.println(LocalDateTime.now()+" addHst: " + rs.getString("id")+" Type: " + rs.getString("type").toUpperCase());

			// insert new line with new timestamp and counter
			PreparedStatement st = conn.prepareStatement("INSERT INTO ConsoleHst (credat,deldat,count,id,prio,type,status,body,agent) "
					+ "values (?,?,?,?,?,?,?,?,?)");
//			System.out.println(LocalDateTime.now()+" Prepared insert:" + st);
			System.out.println(LocalDateTime.now()+" Insert hist: ID:"+rs.getString("id")+" Prio:"+rs.getInt("prio")+" Type:"+rs.getString("type").toUpperCase()+" Body:"+rs.getString("body")   );
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
			System.out.println(LocalDateTime.now()+" Executed insert addHst " +rowsInserted);
			st.close();
			System.out.println(LocalDateTime.now()+" Closed addHst");
		}
		catch (SQLException e) {
			System.err.println(LocalDateTime.now()+" "+e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(LocalDateTime.now()+" "+e);
			System.err.println(e.getMessage());
		}
		finally { System.out.println(LocalDateTime.now()+" CheckStatus addHst finally routine" ); }
	} 



}