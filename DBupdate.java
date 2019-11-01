package Jvakt;
import java.io.*;
//import java.net.*;
import java.util.*;
import java.util.Date;
import java.sql.*;
//import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

//import org.postgresql.util.PSQLException;


class DBupdate {
/* DBupdate ( 2019-JUL-25 ) */
	static Connection conn = null;
	Statement stmt = null;
	PreparedStatement pStmt = null;
	String s;
	String status;
	boolean swHits;
	boolean swPurgeConsole;
	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	int updated;
	int errors;
	int accerr;
	int count;
	boolean swDB = false;
	boolean swPlugin = false;
	boolean swDormant = true;
	boolean swPerm = false;
	boolean swLoop = true;
	static boolean swLogg = false;
    Calendar cal = Calendar.getInstance();


	String database = "jVakt";
	String dbuser   = "jVakt";
	String dbpassword = "xz";
	String dbhost   = "localhost";
	String dbport   = "5433";

	Properties prop = new Properties();

	Process p;
	List<Process> pList = new ArrayList<Process>();

	DBupdate(String[] args) throws Exception {

		String config = null;
		File configF;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-log")) swLogg = true;
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("-config file DBupdate: "+configF);

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
			String	mode 	 =  prop.getProperty("mode");
			if (!mode.equalsIgnoreCase("active")) {  
				swDormant = true;
				swPerm    = true;
			}
		} catch (IOException ex) {
			// ex.printStackTrace();
		}
		input.close();

		Class.forName("org.postgresql.Driver").newInstance();

	}

	String getStatus() {
		if (swDormant) return "DORMANT";
		else 		   return "ACTIVE";

	}

	public synchronized void dbWrite(Message m)  {

		String sType = "";
		String sId = "";		
		String sBody = "";
//		int sPrio = 99;

		// Ignore empty conenctions. Most often from console checking the status.
		if ( m.getId().isEmpty() ) {
			if (swLogg) System.out.println(LocalDateTime.now()+"dbWrite #A: " + m.getType() + " " + m.getId() + " " +m.getRptsts() + " " + m.getBody() + " " +m.getAgent() + " " +m.getPrio());
			return;
		}

		try {

			if(!swDB) {
				DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
				conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
				conn.setAutoCommit(true);
				swDB = true;
			}

			if ( m.getType().equalsIgnoreCase("Dormant")) {
				if (!swPerm) {
					swDormant = true; System.out.println(LocalDateTime.now()+" -> Dormant");
				}
			}
			else 
				if ( m.getType().equalsIgnoreCase("Active")) { 
					if (!swPerm) {
						swDormant = false; System.out.println(LocalDateTime.now()+" -> Active");
					}
					swPerm = false;    // swPerm denies change to Active only once. 
				}
				else {

					s = new String("select * from status " + 
							"WHERE id ilike '" + m.getId().toUpperCase() + 
							"';");
//					if (swLogg) System.out.println("s: " + s );

					//					stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE);
					stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
					stmt.setFetchSize(500);
					ResultSet rs = stmt.executeQuery(s);
					swHits = false;  // is there already a record?
					while (rs.next() && swLoop) {
						swHits = true;  // A record is already in place
						swPlugin = false;
						sType = rs.getString("type");
						sId = rs.getString("id");
						sBody = rs.getString("body");
//						sPrio = rs.getInt("prio");
						if (swLogg) {
							if (!rs.getString("status").equals(m.getRptsts().toUpperCase()))
								System.out.println(LocalDateTime.now()+" #1 " + rs.getString("id") + "  "  + sType + " " + rs.getString("status")+"->"+ m.getRptsts().toUpperCase() );
						}
						// trigger plugin
						if ( m.getType().equalsIgnoreCase("P")) {
							if (rs.getString("msg").equals("T")) status = "TOut";
							else 								 status = rs.getString("status");
							swPlugin = true;
						}

						rs.updateTimestamp("rptdat", new java.sql.Timestamp(new java.util.Date().getTime())); 
						rs.updateString("status", m.getRptsts().toUpperCase());
						rs.updateString("body", m.getBody());
						if (!m.getAgent().equalsIgnoreCase("GUI")) rs.updateString("agent", m.getAgent());
						if (rs.getString("type").startsWith("D")) {
							rs.updateString("type", m.getType().toUpperCase());
							sType = m.getType().toUpperCase();
							if (m.getType().toUpperCase().startsWith("I")) rs.updateInt("prio", m.getPrio());
							//							System.out.println("sType 2 " + sType );
						}
						if (rs.getString("type").startsWith("I")) rs.updateInt("prio", m.getPrio());

						errors = rs.getInt(10);
						errors++;
						accerr = rs.getInt("accerr");

						// update the errors column
//						if (m.getRptsts().toUpperCase().equals("OK") || m.getRptsts().toUpperCase().equals("INFO")) rs.updateInt("errors", 0); 
//						else                                          rs.updateInt("errors", errors);
						if (m.getRptsts().toUpperCase().equals("OK") ) errors= 0; 
						rs.updateInt("errors", errors);

						// If delete request in message, clean up of "console" and reset "msg"
						if ( m.getType().equalsIgnoreCase("D")) {
							rs.updateString("console", " ");

							if (rs.getString("msg").startsWith("M")) rs.updateString("msg", " ");
							else if (rs.getString("msg").startsWith("T")) rs.updateString("msg", " ");
							else if (rs.getString("msg").startsWith("S")) rs.updateString("msg", "R");

							if (rs.getString("sms").startsWith("M")) rs.updateString("sms", " ");
							else if (rs.getString("sms").startsWith("T")) rs.updateString("sms", " ");
							else if (rs.getString("sms").startsWith("S")) rs.updateString("sms", "R");

							if (rs.getString("type").startsWith("I")) rs.updateString("type", "D");
							rs.updateString("condat", null);					
						}

						// If Immediate and not OK, set "console" to C and set "msg" and "sms" to M
//						if ( rs.getString("type").equalsIgnoreCase("I") && !m.getRptsts().toUpperCase().equals("OK") && errors > accerr) {
						if ( (rs.getString("type").equalsIgnoreCase("I") || m.getType().equalsIgnoreCase("I")) && !m.getRptsts().toUpperCase().equals("OK") && errors > accerr) {
							rs.updateString("console", "C");
							rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
							if (rs.getString("msg").startsWith(" ")) {
								rs.updateString("msg", "M");
								rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
								swPlugin = true;
								status = rs.getString("status");
							}
							if (rs.getString("sms").startsWith(" ")) {
								rs.updateString("sms", "M");
								rs.updateTimestamp("smsdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
							}
						}

						// trigger the background process for the plugin.
						if (swPlugin && !swDormant && !rs.getString("type").startsWith("D")) {
							if (!rs.getString("plugin").startsWith(" ")) {
								System.out.println(LocalDateTime.now()+" #4 plugin " + rs.getString("plugin") + " " + rs.getString("id")+ " " + rs.getString("prio")+ " " + status + " \"" + m.getBody() +"\"");
								p =  Runtime.getRuntime().exec(rs.getString("plugin") + " " + rs.getString("id")+ " " + rs.getString("prio")+ " " + status + " \"" + m.getBody() +"\"");
								pList.add(p);
							}
						}

						try { rs.updateRow(); } catch(NullPointerException npe2) {} 
					}
					rs.close(); 
					stmt.close(); 

					// newrecord. Not found before, thus create a new record in the status table
					if ( !swHits ) {   

						PreparedStatement st = conn.prepareStatement("INSERT INTO status (state,id,prio,type,status,body,rptdat,chkday,chktim,errors,accerr,msg,msgdat,console,condat,info,plugin,agent,sms,smsdat) "
								+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
						st.setString(1,"A"); // sts
						st.setString(2,m.getId() ); 
						st.setInt(3,m.getPrio()); // prio
						st.setString(4,m.getType().toUpperCase() ); // type
						sType = m.getType().toUpperCase();
						st.setString(5,m.getRptsts().toUpperCase() ); 
						st.setString(6,m.getBody() ); // 

						st.setTimestamp(7, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime())); // rptdat
						st.setString(8,"*ALL" ); // chkday
						//						st.setTime(9, new java.sql.Time((new Date(System.currentTimeMillis())).getTime())); // chktim
						if (m.getPrio() <= 10 ) {
							cal.set(1970, 01, 01, 0, 0, 0); // only HH:MM:SS is used
							st.setTime(9, new java.sql.Time( cal.getTime().getTime())); // chktim 00:00:00
						}
						else { 
//							if (sType.toUpperCase().equals("S")) st.setTime(9, new java.sql.Time(8,0,0)); 
//							else st.setTime(9, new java.sql.Time(6,0,0)); 
							
							if (sType.toUpperCase().equals("S")) {
								cal.set(1970, 01, 01, 8, 0, 0); // only HH:MM:SS is used
								st.setTime(9, new java.sql.Time( cal.getTime().getTime())); // chktim 08:00:00 
							}
							else {
								cal.set(1970, 01, 01, 6, 0, 0); // only HH:MM:SS is used
								st.setTime(9, new java.sql.Time( cal.getTime().getTime())); // chktim 06:00:00
							}
						}
						if (m.getRptsts().toUpperCase().equals("OK") || m.getRptsts().toUpperCase().equals("INFO")) st.setInt(10,0); // errors 
						else                                          st.setInt(10,1); 
						if (sType.toUpperCase().equals("R")) st.setInt(11,1); // acceptable errors 
						else 								 st.setInt(11,0); // acceptable errors 
						st.setString(12," "); // msg 
						st.setTime(13,null ); // msgdat 
						if (m.getType().equalsIgnoreCase("I"))   {
							st.setString(14,"C" ); // console
							st.setTimestamp(15, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
						}
						else { 
							st.setString(14," " ); // console
							st.setTime(15,null ); // condat
						}
						st.setString(16," "); // info
						st.setString(17," "); // plugin
						st.setString(18,m.getAgent() ); 
						st.setString(19," "); // sms 
						st.setTime(20,null ); // smsdat 
						@SuppressWarnings("unused")
						int rowsInserted = st.executeUpdate();
						st.close();
					} // �newrecord      


					// remove process used for plugin from list
					Iterator<Process> pIte = pList.iterator();
					while (pIte.hasNext()) {
						try { 
							int exitVal = pIte.next().exitValue(); 
							pIte.remove();
							System.out.println(LocalDateTime.now()+" #5 " +" Plugin Process finished, exitValue: "+exitVal);
						} 
						catch (Exception e) {
//							System.out.println(" Plugin Process exitValue exeption " + e);
						} 
					}


					// console ** Immediate or delete type cause an update to the console table at once.
					if ( sType.equalsIgnoreCase("I") || m.getType().equalsIgnoreCase("I") || m.getType().equalsIgnoreCase("D") ) {   
						if (swLogg) {
							System.out.println(LocalDateTime.now()+" #6 Con  m.Type  : " + m.getType().toUpperCase() +" - "+  m.getId()+" - "+ m.getBody());
							System.out.println(LocalDateTime.now()+" #7 Con rs.sType : " + sType +" - "+sId+"- -"+ sBody);
						}

						// read and remove previous line from the console table and save the count field
						//						if ( sType.equalsIgnoreCase("I") && !m.getType().equalsIgnoreCase("D") && !m.getId().equalsIgnoreCase("SYSSTS") ) {
						if (( sType.equalsIgnoreCase("I") || m.getType().equalsIgnoreCase("I")) && !m.getType().equalsIgnoreCase("D") ) {
							s = new String("select * from console " + 
									"WHERE id ilike '" + m.getId() + 
									"' and type='" + sType.toUpperCase() +
									"' and body ilike '" + m.getBody() +
									"';");
							if (swLogg)
								System.out.println(LocalDateTime.now()+" #8 s for update: "+s);
						} else {  // delete 
							s = new String("select * from console " + 
									"WHERE id ilike '" + m.getId() + 
									"' AND body ilike '" + m.getBody() +
									"' and prio='" + Integer.toString(m.getPrio()) +
									"';");
							if (swLogg)
								System.out.println(LocalDateTime.now()+" #9 s for delete: "+s);
						}

						//						stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE);
						stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
						stmt.setFetchSize(500);
						ResultSet rs2 = stmt.executeQuery(s);
						count = 1;
						//						System.out.println(">>> innan rs2.update");
						while (rs2.next()) {
							count = count + rs2.getInt(1);
							if (swLogg)	System.out.println(LocalDateTime.now()+" #10 Console update. rs2.Count=" + count);

							if (m.getType().equalsIgnoreCase("D")) {
								addHst(rs2);
								if (swLogg)
									System.out.println(LocalDateTime.now()+" #11 delete row in console " + rs2.getString("id")+" - "+rs2.getString("body"));
								try { rs2.deleteRow(); } catch(NullPointerException npe2) {System.out.println(LocalDateTime.now()+" #12 delete row npe " + npe2 );}
							}
							else { 
								if (swLogg) System.out.println(LocalDateTime.now()+" #13  update row in console" + rs2.getString("id")+" - "+rs2.getString("body"));
								rs2.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
								rs2.updateInt("count", count);
								rs2.updateString("status", m.getRptsts());
								try { rs2.updateRow(); } catch(NullPointerException npe2) {System.out.println(LocalDateTime.now()+" #14  updaterow npe " + npe2 );}
							}
						}
						//						System.out.println(">>> innan rs2.close");
						rs2.close(); 
						//						System.out.println(">>> innan stmt.close");
						stmt.close();
						//						System.out.println(">>> efter stmt.close");

						if ( count == 1 && (sType.equalsIgnoreCase("I") || m.getType().equalsIgnoreCase("I")) && !m.getType().equalsIgnoreCase("D")) {
							// insert new line with new timestamp and counter
							PreparedStatement st = conn.prepareStatement("INSERT INTO Console (count,id,prio,type,condat,credat,status,body,agent) "
									+ "values (?,?,?,?,?,?,?,?,?)");
							if (swLogg)
								System.out.println(LocalDateTime.now()+" #15 Insert new line in console " + st );
							st.setInt(1,count); // count
							st.setString(2,m.getId() ); 
							st.setInt(3,m.getPrio()); // prio

							st.setString(4,sType.toUpperCase() ); // type
							st.setTimestamp(5, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime())); // condat
							st.setTimestamp(6, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime())); // credat
							st.setString(7,m.getRptsts().toUpperCase() ); // 
							st.setString(8,m.getBody() ); // 
							st.setString(9,m.getAgent() ); // 
							@SuppressWarnings("unused")
							int rowsInserted = st.executeUpdate();
							//        System.out.println("Executed insert " +rowsInserted);
							st.close();
							//        System.out.println("Closed " + sessnum );
							//  Sleep one millisecond to be sure next timestamp is unique.
							try { Thread.sleep(1); } catch (InterruptedException e) { e.printStackTrace();}
						}
					} //�console    

				}
		}
		catch (SQLException e) {
			System.out.println(LocalDateTime.now()+" #E1 DT SQL exeption session " );
			System.err.println(e);
			System.err.println(e.getMessage());
			//Thread.currentThread();
			//Thread.sleep(1000);   
			swDB = false;
		}
		catch (Exception e) {
			System.out.println(LocalDateTime.now()+" #E2 DT exeption error session " );
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		finally {
			//			System.out.println("DT finally  " ); 
		}

	}

	//----- add new line to the consoleHst table -----
	static protected void addHst(ResultSet rs) throws IOException {

		try {
			if (swLogg)
				System.out.println(LocalDateTime.now()+" #15 addHst RS: " + rs.getString("id")+" Type: " + rs.getString("type").toUpperCase()+" Body: " + rs.getString("body"));

			// insert new line with new timestamp and counter
			PreparedStatement st = conn.prepareStatement("INSERT INTO ConsoleHst (credat,deldat,count,id,prio,type,status,body,agent) "
					+ "values (?,?,?,?,?,?,?,?,?)");
			if (swLogg)
				System.out.println(LocalDateTime.now()+" #19 Prepared insert:" + st);
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
			if (swLogg)
				System.out.println(LocalDateTime.now()+" #16 Executed insert addHst " +rowsInserted);
			st.close();
			if (swLogg)
				System.out.println(LocalDateTime.now()+" #17 Closed addHst");
		}
		catch (SQLException e) {
			System.out.println(LocalDateTime.now()+" #E3 SQL exeption error session " );
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.out.println(LocalDateTime.now()+" #E4 exeption error session " );
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		finally { if (swLogg) System.out.println(LocalDateTime.now()+" #18 CheckStatus addHst finally routine" ); }
	} 


}