package Jvakt;
/*
 * 2023-03-15 V.01  Michael Ekdal		To purge old rows from the console
 */

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

public class PurgeConsoleRows {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	static boolean swShowVer = true;
	static boolean swUpdate;
	static boolean swDelete;
	static boolean swTiming;
	static boolean swPlugin = false;
	static boolean swShDay; // set when the scheduled day is active
	static boolean swDormant = false;
	static boolean swRowDormant = false; 
	static boolean swOKtot = false; 

	static java.sql.Date zDate;
	static java.sql.Timestamp zD;
	static java.sql.Timestamp zTs;
	static java.sql.Time zT;
	static Long lhou, lmin, Lsec, Lrptdat, Lchktim, Lchktimto;
	static int errors = 0;
	static int warnings = 0;
	static int infos = 0;
	static String version = "PurgeConsoleRows ";
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int jvporti ;
	static String agent = null;
	static Calendar cal = Calendar.getInstance();
	static String currentID;
	static private  String cmdPlug1 = null;
	static private  String cmdPlug1prio30 = null;
	static private  String cmdPlug1delete = null;
	static String config = null;
	static InetAddress inet;
	static private  String hours = "72";
	static private  int hoursi = 72;
	static private  int wsec = 72;
	
	//	public static void main(String[] args ) throws IOException, UnknownHostException {
	public static void main(String[] args ) {

		version += getVersion()+".01";
		File configF;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-hours")) hours = args[++i];
			if (args[i].equalsIgnoreCase("-nover")) swShowVer =false;
		}

		hoursi = Integer.valueOf(hours);
//		System.out.println(new Date()+"  hoursi: "+hoursi);
		wsec = hoursi * 3600;
//		System.out.println(new Date()+"  wsec: "+wsec);
		
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");

		if (swShowVer) System.out.println("----------- Jvakt: "+new Date()+"  Version: "+version +"  -  config file: "+configF);

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
			cmdPlug1 = prop.getProperty("cmdPlug1");
			cmdPlug1prio30 = prop.getProperty("cmdPlug1prio30");
			cmdPlug1delete = prop.getProperty("cmdPlug1delete");
			String	mode 	 =  prop.getProperty("mode");
			if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		} catch (IOException ex) {
			// ex.printStackTrace();
		}

		LocalDateTime nu = LocalDateTime.now(); // The current date and time
		LocalDateTime midnatt = LocalDateTime.of(nu.getYear(), nu.getMonthValue(), nu.getDayOfMonth() , 0, 0, 0, 0);
//		Timestamp mi = Timestamp.valueOf(midnatt);
//		DayOfWeek DOW = nu.getDayOfWeek(); 
		Statement stmt = null;
		String s;
		//		boolean swHits;
//		int accerr;
//		int err;
		jvporti = Integer.parseInt(jvport);
		agent = null;
		//		InetAddress inet;

		try {
			inet = InetAddress.getLocalHost();
			//			System.out.println("-- Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(new Date()+" - "+e);  }

		zDate = new java.sql.Date((new Date(System.currentTimeMillis())).getTime());     // YYYY-MM-DD
		zTs = new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime());  // YYYY-MM-DD hh:mm.ss.nnn

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			//			conn.setAutoCommit(true);
			conn.setAutoCommit(false);

			s = new String("select * from status " + 
					"WHERE (state='A' or state = 'D') and (type='T' or type = 'I') and console = 'C' and prio >= 30;"); 

			stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_FORWARD_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s); 
			while (rs.next()) {
				System.out.println("## 5 " +rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("body") );
				currentID = rs.getString("id");

				swTiming = false;  
				swPlugin = false;  
				swUpdate = false;

				if (rs.getString("state").equalsIgnoreCase("D")) 	swRowDormant = true;
				else 												swRowDormant = false;

				// Only types T or I and console C is acceptable.
//				if (!rs.getString("type").equalsIgnoreCase("T") && !rs.getString("type").equalsIgnoreCase("I") ) continue;

				if (!rs.getString("status").equalsIgnoreCase("OK"))
					System.out.println(new Date()+" - #1: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+" "+rs.getString("errors")+" "+rs.getString("accerr")+" "+rs.getString("chkday")+" "+rs.getTime("chktim")+" "+rs.getTime("chktimto"));

				zD = rs.getTimestamp("rptdat"); 
//				accerr = rs.getInt("accerr");
//				err    = rs.getInt("errors");

				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000);  // Actual time minus the time rptdat in seconds  

				Lchktim = rs.getTime("chktim").getTime();
				Lchktimto = rs.getTime("chktimto").getTime();
				Lrptdat = rs.getTime("rptdat").getTime(); 

//				swDelete = false;
				PreparedStatement StmtUpdate;
				String SQL_UPDATE;
				
				// if msg appears on console and the rptdate are older than the parameter -hours 
				if (rs.getString("console").equalsIgnoreCase("C") && Lsec > wsec) {
					
					System.out.println(new Date()+" - OK: #4 " + rs.getString("id")+" "+rs.getString("status")+"  MSG:"+rs.getString("msg")+"  SMS:"+rs.getString("sms")+"  MSG30:"+rs.getString("msg30") );
					swDelete = true;   // remove lines from console

					SQL_UPDATE ="UPDATE STATUS SET CONSOLE=?, STATUS=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
					StmtUpdate = conn.prepareStatement(SQL_UPDATE);
					StmtUpdate.setString(1, " ");
					StmtUpdate.setString(2, "OK");
					StmtUpdate.executeUpdate();
					StmtUpdate.close();

					swUpdate=true;
					if (rs.getString("msg").startsWith("M") || rs.getString("msg").startsWith("T")) {
						System.out.println(new Date()+" - OK: Set msg to blank" + " " + rs.getString("id"));

						SQL_UPDATE ="UPDATE STATUS SET MSG=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, " ");
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

					}
					else if (rs.getString("msg").startsWith("S")) {
						System.out.println(new Date()+" - OK: Set msg to R" + " " + rs.getString("id"));
						//						rs.updateString("msg", "R");

						SQL_UPDATE ="UPDATE STATUS SET MSG=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "R");
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

					}
					if (rs.getString("sms").startsWith("M") || rs.getString("sms").startsWith("T")) {
						System.out.println(new Date()+" - OK: Set sms to blank" + " " + rs.getString("id"));
						//						rs.updateString("sms", " ");

						SQL_UPDATE ="UPDATE STATUS SET SMS=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, " ");
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

					}
					else if (rs.getString("sms").startsWith("S")) {
						System.out.println(new Date()+" - OK: Set sms to R" + " " + rs.getString("id"));
						//						rs.updateString("sms", "R");

						SQL_UPDATE ="UPDATE STATUS SET SMS=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "R");
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

					}
					if (rs.getString("msg30").startsWith("M") || rs.getString("msg30").startsWith("T") || rs.getString("msg30").startsWith("D")) {
						System.out.println(new Date()+" - OK: Set msg30 to blank" + " " + rs.getString("id"));
						//						rs.updateString("msg30", " ");

						SQL_UPDATE ="UPDATE STATUS SET MSG30=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, " ");
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

					}
					else if (rs.getString("msg30").startsWith("S")) {
						System.out.println(new Date()+" - OK: Set msg30 to R" + " " + rs.getString("id"));
						//						rs.updateString("msg30", "R");

						SQL_UPDATE ="UPDATE STATUS SET MSG30=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "R");
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

					}
					//					if (swUpdate) try { rs.updateRow(); } catch(NullPointerException npe2) {}
					//					conn.commit();
//					swDelete = true;
					updC(rs); // remove line from the console table
				}
				conn.commit();
			}
			rs.close(); 
			stmt.close();
			conn.close();
			swOKtot = true;
		}
		catch (SQLException e) {
			System.out.println(new Date()+" SQLExeption - CurrentID = " + currentID);
			System.err.println(e);
			System.err.println(e.getMessage());
			swOKtot = false;
		}
		catch (Exception e) {
			System.out.println(new Date()+" Exeption - CurrentID = " + currentID);
			System.err.println(e);
			System.err.println(e.getMessage());
			swOKtot = false;
		}
		finally { 
			//			System.out.println("CheckStatus Severe: " + errors + "  Problems: " + warnings + "  Info: " + infos ); 
			//		   sendSTS();
		}
		if (swOKtot ) try {sendSTS(true);}  catch (IOException e) { e.printStackTrace();}
		else 	      try {sendSTS(false);} catch (IOException e) { e.printStackTrace();}

	}        

	//----- add or remove line to/from the console table -----
	//	static protected void updC(ResultSet rs) throws IOException {
	static protected void updC(ResultSet rs) {

		Statement stmt = null;
		int count = 1;

		try {
			System.out.println(new Date()+" - updC: swDelete:"+swDelete+" ID:"+rs.getString("id") +" Type:"+rs.getString("type").toUpperCase()+" Prio:"+rs.getString("prio").toUpperCase()+" Body:"+rs.getString("body"));
			// Immediate type cause an update to the console table at once.

			// read and remove previous line from the console table and save the count field
			String s=null;
			String prioS;
			if (swRowDormant)	prioS = "99";
			else 				prioS =  rs.getString("prio");
			System.out.println(new Date()+" - Prios: " + prioS);

				s = new String("select * from console " + 
						"WHERE id ilike '" + rs.getString("id") + 
						"' and prio='" 	 +	prioS +
						"' and type='" 	 +	rs.getString("type").toUpperCase() +
						"';");

			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(50);
			ResultSet rs2 = stmt.executeQuery(s);
			count = 1;
			while (rs2.next()) {
				count = rs2.getInt(1);
				count++;
				if (count<2) count=2;
					System.out.println(new Date()+" - delete console: " + rs2.getString("id")+ " - "+rs2.getString("prio")+" - "+rs2.getString("body"));
					addHst(rs2);
					try { rs2.deleteRow(); } catch(NullPointerException npe2) {} 
			}
			rs2.close(); 
			stmt.close();

		}
		catch (SQLException e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}

	} 

	//----- add new line to the consoleHst table -----
	static protected void addHst(ResultSet rs) throws IOException {

		try {
			System.out.println(new Date()+" - addHst: " + rs.getString("id")+" Type: " + rs.getString("type").toUpperCase());

			// insert new line with new timestamp and counter
			PreparedStatement st = conn.prepareStatement("INSERT INTO ConsoleHst (credat,deldat,count,id,prio,type,status,body,agent) "
					+ "values (?,?,?,?,?,?,?,?,?)");
			//			System.out.println(LocalDateTime.now()+" Prepared insert:" + st);
			System.out.println(new Date()+" - Insert hist: ID:"+rs.getString("id")+" Prio:"+rs.getInt("prio")+" Type:"+rs.getString("type").toUpperCase()+" Body:"+rs.getString("body")   );
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
			System.out.println(new Date()+" - Executed insert addHst " +rowsInserted);
			st.close();
			System.out.println(new Date()+" - Closed addHst");

			// call plugin1 start //
			if (cmdPlug1 != null && !swDormant &&  cmdPlug1delete.equalsIgnoreCase("Y")) {
				if (cmdPlug1prio30.equalsIgnoreCase("Y") || rs.getString("prio").compareTo("30") < 0 ) {
					try {
						String cmd = cmdPlug1+" *DELETE -id "+rs.getString("id")+" -prio "+rs.getString("prio")+" -type "+rs.getString("type")+" -sts "+rs.getString("status")+" -body \""+rs.getString("body")+"\" -agent \""+rs.getString("agent")+"\" -recid \""+rs.getString("recid")+"\""  ;
						if (config != null ) cmd = cmd + " -config "+config; 	
						Runtime.getRuntime().exec(cmd);
						System.out.println(new Date()+" - #P2 executed as delete: " +cmd);

					} catch (IOException e1) {
						System.err.println(e1);
						System.err.println(e1.getMessage());
					}
				}
			}
			// call plugin1 end //
		}
		catch (SQLException e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		finally { System.out.println(new Date()+" - PurgeConsoleRows addHst finally routine" ); }


	} 

	// sends status to the Jvakt server
	static protected void sendSTS( boolean STS) throws IOException {
		System.out.println("--- Connecting to "+jvhost+":"+jvport);
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, jvporti);
		//			System.out.println(jm.open()); 
		jm.open(); 
		jmsg.setId("Jvakt-PurgeConsoleRows");
		if (STS) {
			jmsg.setBody("The PurgeConsoleRows program is working.");
			jmsg.setRptsts("OK");
		}
		else {
			jmsg.setBody("The PurgeConsoleRows program is not working!");
			jmsg.setRptsts("ERR");
		}
		jmsg.setType("T");

		inet = InetAddress.getLocalHost();
		//			System.out.println("-- Inet: "+inet);
		agent = inet.toString();

		jmsg.setAgent(agent);
		if (!jm.sendMsg(jmsg)) System.out.println("--- Rpt to Jvakt Failed for PurgeConsoleRows ---");
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