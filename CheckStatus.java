package Jvakt;
/*
 * 2022-07-02 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 * 2022-07-05 V.55 Michael Ekdal		Added the possibility to use cmdPlug1 during add/delete of lines in console 
 * 2022-08-10 V.56 Michael Ekdal		Added check of cmdPlug1prio30 to trigger messages from 30 up yes or no. Else only below 30. 
 * 2022-08-11 V.57 Michael Ekdal		-config to the cmdPlug1 arguments 
 * 2022-08-11 V.58 Michael Ekdal		-recid to the cmdPlug1 arguments when *DELETE 
 * 2022-08-11 V.59 Michael Ekdal		Added check of cmdPlug1delete to trigger messages deleted from the console.
 * 2023-01-10 V.60 Michael Ekdal		Added send of the status to Jvakt server
 * 2023-03-17 V.61 Michael Ekdal		Hard coded status "TOut" when calling plugin instead when swTimin is true
 * 2023-06-23 V.62 Michael Ekdal		Will send also the Type=I Status=INFO combo to cmdPlug1.
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

public class CheckStatus {

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
	static String version = "CheckStatus ";
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

	//	public static void main(String[] args ) throws IOException, UnknownHostException {
	public static void main(String[] args ) {

		version += getVersion()+".61";
		File configF;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-nover")) swShowVer =false;
		}

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
		Timestamp mi = Timestamp.valueOf(midnatt);
		DayOfWeek DOW = nu.getDayOfWeek(); 
		Statement stmt = null;
		String s;
		//		boolean swHits;
		int accerr;
		int err;
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

		//		System.out.println(DOW + " <> "+ zDate + " <> " + zTs);
		//		System.out.println("******* Nu ****** " + nu + " Klocka " +nu.getHour()+":"+nu.getMinute()+":"+nu.getSecond());

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			//			conn.setAutoCommit(true);
			conn.setAutoCommit(false);

			s = new String("select * from status " + 
					"WHERE state='A' or state = 'D';"); 

			//						System.out.println(s);
			//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_FORWARD_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT ); 
			//			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s); 
			while (rs.next()) {
				//				System.out.println("## 5 " +rs.getString("id"));
				currentID = rs.getString("id");

				swTiming = false;  
				swPlugin = false;  
				swUpdate = false;

				if (rs.getString("state").equalsIgnoreCase("D")) 	swRowDormant = true;
				else 												swRowDormant = false;

				// Only types R, S, T or I is acceptable.
				if (!rs.getString("type").equalsIgnoreCase("R") && !rs.getString("type").equalsIgnoreCase("S") && !rs.getString("type").equalsIgnoreCase("T") && !rs.getString("type").equalsIgnoreCase("I")) continue;

				if (!rs.getString("status").equalsIgnoreCase("OK"))
					System.out.println(new Date()+" - #1: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+" "+rs.getString("errors")+" "+rs.getString("accerr")+" "+rs.getString("chkday")+" "+rs.getTime("chktim")+" "+rs.getTime("chktimto"));

				zD = rs.getTimestamp("rptdat"); 
				accerr = rs.getInt("accerr");
				err    = rs.getInt("errors");

				Lsec = (zTs.getTime() / 1000 - zD.getTime() / 1000);  // Actual time minus the time rptdat in seconds  

				Lchktim = rs.getTime("chktim").getTime();
				Lchktimto = rs.getTime("chktimto").getTime();
				Lrptdat = rs.getTime("rptdat").getTime(); 

				// swShDay will be set to true if the chktim time has passed and chkday is *ALL or the name of day like MON, TUE... 
				swShDay = false;
				if (rs.getString("chkday").startsWith("*ALL") || rs.getString("chkday").indexOf(DOW.name().substring(0, 2)) >= 0 ) {
					// check chktim
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

					// check chktimto
					cal.setTime(rs.getTime("chktimto"));
					if (nu.getHour() > cal.get(Calendar.HOUR_OF_DAY) ) {
						swShDay = false; 
					}
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY)  && nu.getMinute() > cal.get(Calendar.MINUTE) ) {
						swShDay = false;	
					}
					else if (nu.getHour() == cal.get(Calendar.HOUR_OF_DAY)  && nu.getMinute() == cal.get(Calendar.MINUTE) && nu.getSecond() > cal.get(Calendar.SECOND) ) {
						swShDay = false;	
					}

				} 
				swDelete = false;
				PreparedStatement StmtUpdate;
				String SQL_UPDATE;
				// If status is ERR or INFO the row is tagged console, SMS and mail
				if ( (rs.getString("status").equalsIgnoreCase("ERR") && err > accerr) || rs.getString("status").equalsIgnoreCase("INFO") ) { 
					System.out.println(new Date()+" - ERR: #2 "+"  MSG:"+rs.getString("msg")+"  SMS:"+rs.getString("sms")+"  MSG30:"+rs.getString("msg30")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("console")+" "+rs.getString("status")+" "+rs.getString("errors")+" "+rs.getString("accerr"));
					if (rs.getString("console").startsWith(" ")) {
						System.out.println(new Date()+" - ERR: Set console to C" + " " + rs.getString("id"));

						//						rs.updateString("console", "C");
						//						rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));

						SQL_UPDATE ="UPDATE STATUS SET CONSOLE=?, CONDAT=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "C");
						StmtUpdate.setTimestamp(2, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

						swUpdate=true;
					}
					if (rs.getString("msg").startsWith("T") || rs.getString("msg").startsWith(" ")) {
						System.out.println(new Date()+" - ERR: Set msg to M" + " " + rs.getString("id"));
						//						rs.updateString("msg", "M");
						//						rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));

						SQL_UPDATE ="UPDATE STATUS SET MSG=?, MSGDAT=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "M");
						StmtUpdate.setTimestamp(2, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

						swPlugin = true;
						swUpdate=true;
					}
					if ( rs.getString("sms").startsWith("T") || rs.getString("sms").startsWith(" ")) {
						System.out.println(new Date()+" - ERR: Set sms to M" + " " + rs.getString("id"));
						//						rs.updateString("sms", "M");
						//						rs.updateTimestamp("smsdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));

						SQL_UPDATE ="UPDATE STATUS SET SMS=?, SMSDAT=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "M");
						StmtUpdate.setTimestamp(2, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

						swUpdate=true;
					}
					if ( rs.getString("msg30").startsWith("T") || rs.getString("msg30").startsWith(" ")) {
						System.out.println(new Date()+" - ERR: Set msg30 to M" + " " + rs.getString("id"));
						//						rs.updateString("msg30", "M");
						//						rs.updateTimestamp("msgdat30", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));

						SQL_UPDATE ="UPDATE STATUS SET MSG30=?, MSGDAT30=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "M");
						StmtUpdate.setTimestamp(2, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

						swUpdate=true;
					}
					//					if (swUpdate) try { rs.updateRow(); } catch(NullPointerException npe2) {}
					//					conn.commit();
					//					System.out.println("## 1");
					updC(rs); // add or remove line to/from the console table
					//					System.out.println("## 2");
					if (swPlugin && rs.getString("plugin").length() > 4 && rs.getString("state").startsWith("A")) {
						trigPlugin(rs.getString("id"), rs.getString("status"), "P", rs.getString("body")); 
					}
				} 
				// If timeout occurred regarding S and R, console is tagged
				else if ( 
						(rs.getString("type").equalsIgnoreCase("R") && rs.getString("status").equalsIgnoreCase("OK") && Lsec > 1200 && swShDay  ) 
						||
						(rs.getString("type").equalsIgnoreCase("S") && swShDay && 
								rs.getTimestamp("rptdat").getTime() < mi.getTime() )    // mi is the date and time of midnight
						)
				{
					System.out.println(new Date()+" - TOUT: " + " " + rs.getString("id")+"  msg:"+rs.getString("msg"));

					if (!rs.getString("console").equalsIgnoreCase("C")) {
						//						rs.updateString("console", "C");
						//						rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));

						SQL_UPDATE ="UPDATE STATUS SET CONSOLE=?, CONDAT=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "C");
						StmtUpdate.setTimestamp(2, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						//						System.out.println(	StmtUpdate.executeUpdate() );
						StmtUpdate.executeUpdate();

						StmtUpdate.close();

						swUpdate=true;
					}
					swTiming = true;
					System.out.println(new Date()+" - timing #3: " + rs.getString("id")+"  MSG:"+rs.getString("msg"));
					// 2021-03-17
					rs.updateString("status", "TOut");
					rs.updateString("body", "Agent Timed-Out");
					SQL_UPDATE ="UPDATE STATUS SET STATUS=?, BODY=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
					StmtUpdate = conn.prepareStatement(SQL_UPDATE);
					StmtUpdate.setString(1, "TOut");
					StmtUpdate.setString(2, "Agent Timed-Out");
					StmtUpdate.executeUpdate();
					StmtUpdate.close();
					// 2021-03-17

					if (rs.getString("msg").startsWith(" ")) {
						System.out.println(new Date()+" - TOut: Set msg to T " + rs.getString("id"));
						//						rs.updateString("msg", "T");
						//						rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));

						SQL_UPDATE ="UPDATE STATUS SET MSG=?, MSGDAT=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "T");
						StmtUpdate.setTimestamp(2, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						StmtUpdate.executeUpdate();
						StmtUpdate.close();

						swPlugin = true;
						swUpdate=true;
					}
					if (rs.getString("sms").startsWith(" ")) {
						System.out.println(new Date()+" - TOut: Set sms to T " + rs.getString("id"));
						//						rs.updateString("sms", "T");
						//						rs.updateTimestamp("smsdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));

						SQL_UPDATE ="UPDATE STATUS SET SMS=?, SMSDAT=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "T");
						StmtUpdate.setTimestamp(2, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						StmtUpdate.executeUpdate();
						StmtUpdate.close();
					}
					if (rs.getString("msg30").startsWith(" ")) {
						System.out.println(new Date()+" - TOut: Set msg30 to T " + rs.getString("id"));
						rs.updateString("msg30", "T");
						rs.updateTimestamp("msgdat30", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));

						SQL_UPDATE ="UPDATE STATUS SET MSG30=?, MSGDAT30=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
						StmtUpdate = conn.prepareStatement(SQL_UPDATE);
						StmtUpdate.setString(1, "T");
						StmtUpdate.setTimestamp(2, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						StmtUpdate.executeUpdate();
						StmtUpdate.close();
					}
					System.out.println(new Date()+" - timing #3.a :  " + rs.getString("id")+"  MSG:"+rs.getString("msg"));
					//					if (swUpdate) try { rs.updateRow(); } catch(NullPointerException npe2) {}
					//					conn.commit();
					updC(rs); // add new line to the console table
					if (swPlugin && !rs.getString("plugin").startsWith(" ")) {
						trigPlugin(rs.getString("id"), rs.getString("status"), "P", rs.getString("body")); 
					}
				} // If status is OK the console tag will be removed for S,R and T, and if console=C or msg isn't " " or sms isn't " "
				else	 if ((rs.getString("type").equalsIgnoreCase("R") || rs.getString("type").equalsIgnoreCase("S") || rs.getString("type").equalsIgnoreCase("T") || rs.getString("type").equalsIgnoreCase("D")) && 
						rs.getString("status").equalsIgnoreCase("OK") &&  
						(rs.getString("console").equalsIgnoreCase("C") || !rs.getString("msg").startsWith(" ") || !rs.getString("sms").startsWith(" ") || !rs.getString("msg30").startsWith(" ") )
						) {
					System.out.println(new Date()+" - OK: #4 " + rs.getString("id")+" "+rs.getString("status")+"  MSG:"+rs.getString("msg")+"  SMS:"+rs.getString("sms")+"  MSG30:"+rs.getString("msg30") );
					swDelete = true;   // remove lines from console
					//					rs.updateString("console", " ");
					//					rs.updateString("status", "OK");

					SQL_UPDATE ="UPDATE STATUS SET CONSOLE=?, STATUS=? WHERE ID='"+rs.getString("id")+"' AND PRIO="+rs.getString("prio");
					StmtUpdate = conn.prepareStatement(SQL_UPDATE);
					StmtUpdate.setString(1, " ");
					StmtUpdate.setString(2, "OK");
					StmtUpdate.executeUpdate();
					StmtUpdate.close();

					swUpdate=true;
					if (rs.getString("msg").startsWith("M") || rs.getString("msg").startsWith("T")) {
						System.out.println(new Date()+" - OK: Set msg to blank" + " " + rs.getString("id"));
						//						rs.updateString("msg", " ");

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
					updC(rs); // update or remove line from the console table
				}
				conn.commit();
				swOKtot = true;
			}
			rs.close(); 
			stmt.close();
			conn.close();
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

	// trigger plugin
	static protected void trigPlugin(String id, String sts, String type, String body) throws IOException {
		if (swDormant) return;
		System.out.println(new Date()+" - * trigPlugin * " + id +" "+ sts +" "+ type +" "+ body);
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, jvporti);
		System.out.println(jm.open());
		jmsg.setId(id);
		jmsg.setRptsts(sts);
		jmsg.setBody(body);
		jmsg.setType(type);
		jmsg.setAgent(agent);
		//		jm.sendMsg(jmsg);
		if (jm.sendMsg(jmsg)) System.out.println(new Date()+" -  Rpt Delivered --");
		else           	      System.out.println(new Date()+" -  Rpt Failed --");
		jm.close();
	}

	// sends status to the Jvakt server
	static protected void sendSTS( boolean STS) throws IOException {
//			System.out.println("--- Connecting to "+jvhost+":"+jvport);
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(jvhost, jvporti);
//			System.out.println(jm.open()); 
			jm.open(); 
			jmsg.setId("Jvakt-CheckStatus");
			if (STS) {
				jmsg.setBody("The CheckStatus program is working.");
				jmsg.setRptsts("OK");
			}
			else {
				jmsg.setBody("The CheckStatus program is not working!");
				jmsg.setRptsts("ERR");
			}
			jmsg.setType("T");

			inet = InetAddress.getLocalHost();
//			System.out.println("-- Inet: "+inet);
			agent = inet.toString();

			jmsg.setAgent(agent);
			if (!jm.sendMsg(jmsg)) System.out.println("--- Rpt to Jvakt Failed for CheckStatus ---");
			jm.close();
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
					System.out.println(new Date()+" - delete console: " + rs2.getString("id")+ " - "+rs2.getString("prio")+" - "+rs2.getString("body"));
					addHst(rs2);
					try { rs2.deleteRow(); } catch(NullPointerException npe2) {} 
				}
				else { 
					rs2.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
					rs2.updateInt("count", count);
					System.out.println(new Date()+" - update console: row.count:"+count  + " - id:"+ rs2.getString("id")+ " - "+rs2.getString("prio") + " - " + rs2.getString("body") );
					if (swTiming) {
						rs2.updateString("status","TOut");
						//						rs2.updateString("body", rs.getString("body"));
						rs2.updateString("body", "Agent Timed-Out");

					}
					else {
						if (!swShDay) rs2.updateString("status","INFO");    // Force INFO if chkday is out of range
						else rs2.updateString("status",rs.getString("status").toUpperCase() );
						rs2.updateString("body", rs.getString("body"));
					}
					// om dormant
					//					if (swRowDormant) rs2.updateInt("prio", 99);
					try { rs2.updateRow(); } catch(NullPointerException npe2) {}
				}
			}
			rs2.close(); 
			stmt.close();
			//			conn.commit();
			//			}


			if (!swDelete && count == 1 ) {
				// insert new line with new timestamp and counter
				PreparedStatement st = conn.prepareStatement("INSERT INTO Console (count,id,prio,type,condat,credat,status,body,agent) "
						+ "values (?,?,?,?,?,?,?,?,?)");
				//				System.out.println(LocalDateTime.now()+" Prepared insert:" + st);
				System.out.println(new Date()+" - Insert console: "+rs.getString("state")+" " + rs.getString("id")+" "+rs.getString("type")+" "+rs.getString("prio")+" "+rs.getString("body")+" "+rs.getString("status")+" "+rs.getString("errors")+" "+rs.getString("accerr"));
				st.setInt(1,count); // count
				st.setString(2,rs.getString("id") ); 
				// om dormant
				if (swRowDormant) 	st.setInt(3,99); // prio
				else 				st.setInt(3,rs.getInt("prio")); // prio
				st.setString(4,rs.getString("type").toUpperCase() ); // type
				st.setTimestamp(5, zTs); // condat
				st.setTimestamp(6, zTs); // credat
				System.out.println(new Date()+" - swTiming: " + swTiming);
				//				if (swTiming) st.setString(7,"TOut");
				//				else st.setString(7,rs.getString("status").toUpperCase() );// 
				if (swTiming) {
					st.setString(7,"TOut");
					//					st.setString(8,rs.getString("body") );
					st.setString(8,"Agent Timed-Out");
				}
				else {
					if (!swShDay) st.setString(7,"INFO");    // Force INFO if chkday is out of range
					else st.setString(7,rs.getString("status").toUpperCase() );					
					st.setString(8,rs.getString("body") );
				}
				//				st.setString(8,rs.getString("body") ); // 
				st.setString(9,rs.getString("agent") ); // 
				int rowsInserted = st.executeUpdate();
				System.out.println(new Date()+" - Executed insert addC " +rowsInserted);
				st.close();
				//				conn.commit();

				// *** call plugin1 start //
//				if (cmdPlug1 != null  && !swDormant && !swRowDormant && !swTiming) {
				if (cmdPlug1 != null  && !swDormant && !swRowDormant) {
//					if ((cmdPlug1prio30.equalsIgnoreCase("Y") || rs.getString("prio").compareTo("30") < 0) && (!rs.getString("type").startsWith("I") && !rs.getString("status").startsWith("INFO") ) ) {
					if ((cmdPlug1prio30.equalsIgnoreCase("Y") || rs.getString("prio").compareTo("30") < 0)) {
						try {
							String cmd;
							if (swTiming) cmd = cmdPlug1+" *INSERT -id "+rs.getString("id")+" -prio "+rs.getString("prio")+" -type "+rs.getString("type")+" -sts TOut -body \""+rs.getString("body")+"\" -agent \""+rs.getString("agent")+"\""  ;
								else 	  cmd = cmdPlug1+" *INSERT -id "+rs.getString("id")+" -prio "+rs.getString("prio")+" -type "+rs.getString("type")+" -sts "+rs.getString("status")+" -body \""+rs.getString("body")+"\" -agent \""+rs.getString("agent")+"\""  ;
							if (config != null ) cmd = cmd + " -config "+config; 	
							Runtime.getRuntime().exec(cmd);
							System.out.println(new Date()+" - #P1 executed as insert: " +cmd);
						} catch (IOException e1) {
							System.err.println(e1);
							System.err.println(e1.getMessage());
						}
					}
				}
				// *** call plugin1 end //

			}
			//			System.out.println(LocalDateTime.now()+" Closed addC");
		}
		catch (SQLException e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		//		finally { System.out.println(LocalDateTime.now()+" CheckStatus addC finally routine" ); }

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
		finally { System.out.println(new Date()+" - CheckStatus addHst finally routine" ); }


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