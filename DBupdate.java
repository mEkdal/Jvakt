package Jvakt;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import org.postgresql.util.PSQLException;


class DBupdate {
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

	String database = "jVakt";
	String dbuser   = "jVakt";
	String dbpassword = "xz";
	String dbhost   = "localhost";
	String dbport   = "5433";

	Properties prop = new Properties();

	Process p;
	List<Process> pList = new ArrayList<Process>();

	DBupdate() throws Exception {

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
		int sPrio = 99;

		try {

			if(!swDB) {
				DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
				conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
				conn.setAutoCommit(true);
				swDB = true;
			}

			if ( m.getType().equalsIgnoreCase("Dormant")) {
				if (!swPerm) {
					swDormant = true; System.out.println("Dormant");
				}
			}
			else 
				if ( m.getType().equalsIgnoreCase("Active")) { 
					if (!swPerm) {
						swDormant = false; System.out.println("Active");
					}
				}
				else {

					s = new String("select * from status " + 
							"WHERE id ilike '" + m.getId().toUpperCase() + 
							"';");

					//					stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE);
					stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
					stmt.setFetchSize(500);
					ResultSet rs = stmt.executeQuery(s);
					swHits = false;  // is there already a record?
					while (rs.next() && swLoop) {
						swHits = true;  // A record is already in place
						swPlugin = false;
						sType = rs.getString("type");
						sPrio = rs.getInt("prio");
						System.out.println(LocalDateTime.now()+" #1 " + rs.getString("id") + "  "  + sType + " " + rs.getString("status")+"->"+ m.getRptsts().toUpperCase() );
						//						if ( m.getType().equalsIgnoreCase("P")) {
						//							if (rs.getString("msg").equals("T")) status = "time-out";
						//							else 								 status = rs.getString("status");
						//							swPlugin = true;
						//						}
						rs.updateTimestamp("rptdat", new java.sql.Timestamp(new java.util.Date().getTime())); 
						rs.updateString("status", m.getRptsts().toUpperCase());
						rs.updateString("body", m.getBody());
						if (rs.getString("type").startsWith("D")) {
							rs.updateString("type", m.getType().toUpperCase());
							sType = m.getType().toUpperCase();
							//							System.out.println("sType 2 " + sType );
						}
						if (rs.getString("type").startsWith("I")) rs.updateInt("prio", m.getPrio());

						errors = rs.getInt(10);
						errors++;
						accerr = rs.getInt("accerr");

						// update the errors column
						if (m.getRptsts().toUpperCase().equals("OK") || m.getRptsts().toUpperCase().equals("INFO")) rs.updateInt("errors", 0); 
						else                                          rs.updateInt("errors", errors);

						if ( m.getType().equalsIgnoreCase("D")) {
							rs.updateString("console", " ");
							if (rs.getString("msg").startsWith("M")) rs.updateString("msg", " ");
							else if (rs.getString("msg").startsWith("T")) rs.updateString("msg", " ");
							else if (rs.getString("msg").startsWith("S")) rs.updateString("msg", "R");
							if (rs.getString("type").startsWith("I")) rs.updateString("type", "D");
							rs.updateString("condat", null);					
						}

						if ( rs.getString("type").equalsIgnoreCase("I") && m.getRptsts().toUpperCase().equals("ERR") && errors > accerr) {
							rs.updateString("console", "C");
							rs.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
							if (rs.getString("msg").startsWith(" ")) {
								rs.updateString("msg", "M");
								rs.updateTimestamp("msgdat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
								swPlugin = true;
								status = rs.getString("status");
							}
						}

						// trigger the background process for the plugin.
						if (swPlugin && !swDormant && !rs.getString("type").startsWith("D")) {
							if (!rs.getString("plugin").startsWith(" ")) {
								System.out.println("plugin " + rs.getString("plugin") + " " + rs.getString("id")+ " " + rs.getString("prio")+ " " + status + " \"" + rs.getString("body")+"\"");
								p =  Runtime.getRuntime().exec(rs.getString("plugin") + " " + rs.getString("id")+ " " + rs.getString("prio")+ " " + status + " \"" + rs.getString("body")+"\"");
								pList.add(p);
							}
						}

						try { rs.updateRow(); } catch(NullPointerException npe2) {} 
					}
					rs.close(); 
					stmt.close(); 

					//€newrecord. Not found before, thus create a new record in the status table
					if ( !swHits ) {   

						PreparedStatement st = conn.prepareStatement("INSERT INTO status (state,id,prio,type,status,body,rptdat,chkday,chktim,errors,accerr,msg,msgdat,console,condat,info,plugin,agent) "
								+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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
						if (m.getPrio() < 10 )	st.setTime(9, new java.sql.Time(00 ,00,00)); // chktim
						else 					st.setTime(9, new java.sql.Time(06 ,00,00)); // chktim
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
						int rowsInserted = st.executeUpdate();
						st.close();
					} // €newrecord      


					// remove process used for plugin from list
					Iterator<Process> pIte = pList.iterator();
					while (pIte.hasNext()) {
						try { 
							int exitVal = pIte.next().exitValue(); 
							pIte.remove();
							System.out.println("Process removed ");
						} 
						catch (Exception e) {
							System.out.println("Process exitValue exeption " + e);
						} 
					}


					// €console ** Immediate or delete type cause an update to the console table at once.
					if ( sType.equalsIgnoreCase("I") || m.getType().equalsIgnoreCase("D") ) {   
						System.out.println(">>> Con  m.Type  >>>>: " + m.getType().toUpperCase());
						System.out.println(">>> Con rs.sType >>>>: " + sType);

						// read and remove previous line from the console table and save the count field
						//						if ( sType.equalsIgnoreCase("I") && !m.getType().equalsIgnoreCase("D") && !m.getId().equalsIgnoreCase("SYSSTS") ) {
						if ( sType.equalsIgnoreCase("I") && !m.getType().equalsIgnoreCase("D") ) {
							s = new String("select * from console " + 
									"WHERE id ilike '" + m.getId() + 
									"' and type='" + sType.toUpperCase() +
									"' and body ilike '" + m.getBody() +
									"';");
							System.out.println(s);
						} else {  // delete
							s = new String("select * from console " + 
									"WHERE id ilike '" + m.getId() + 
									"' AND body ilike '" + m.getBody() +
									"' and prio='" + Integer.toString(m.getPrio()) +
									"';");
							System.out.println(s);
						}

						//						stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE);
						stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
						stmt.setFetchSize(500);
						ResultSet rs2 = stmt.executeQuery(s);
						count = 1;
						//						System.out.println(">>> innan rs2.update");
						while (rs2.next()) {
							count = count + rs2.getInt(1);
							//							System.out.println(">>> inuti rs2.update  " + count);
							rs2.updateTimestamp("condat", new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
							rs2.updateInt("count", count);
							rs2.updateString("status", m.getRptsts());

							if (m.getType().equalsIgnoreCase("D")) {
								System.out.println(">>> deleterow");
								addHst(rs2);
								try { rs2.deleteRow(); } catch(NullPointerException npe2) {}
							}
							else { 
								try { rs2.updateRow(); } catch(NullPointerException npe2) {}
							}
						}
						//						System.out.println(">>> innan rs2.close");
						rs2.close(); 
						//						System.out.println(">>> innan stmt.close");
						stmt.close();
						//						System.out.println(">>> efter stmt.close");

						if ( count == 1 && sType.equalsIgnoreCase("I") && !m.getType().equalsIgnoreCase("D")) {
							// insert new line with new timestamp and counter
							System.out.println(">>> Insert new line in console");
							PreparedStatement st = conn.prepareStatement("INSERT INTO Console (count,id,prio,type,condat,credat,status,body,agent) "
									+ "values (?,?,?,?,?,?,?,?,?)");
							//        System.out.println("Prepared insert:");
							st.setInt(1,count); // count
							st.setString(2,m.getId() ); 
							st.setInt(3,m.getPrio()); // prio

							st.setString(4,sType.toUpperCase() ); // type
							st.setTimestamp(5, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime())); // condat
							st.setTimestamp(6, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime())); // credat
							st.setString(7,m.getRptsts().toUpperCase() ); // 
							st.setString(8,m.getBody() ); // 
							st.setString(9,m.getAgent() ); // 
							int rowsInserted = st.executeUpdate();
							//        System.out.println("Executed insert " +rowsInserted);
							st.close();
							//        System.out.println("Closed " + sessnum );
						}
					} //€console    

				}
		}
		catch (SQLException e) {
			System.out.println("DT SQLexeption session " );
			System.err.println(e);
			System.err.println(e.getMessage());
			//Thread.currentThread();
			//Thread.sleep(1000);   
			swDB = false;
		}
		catch (Exception e) {
			//			System.out.println("DT exeptionerror session " );
			//			System.err.println(e);
			//			System.err.println(e.getMessage());
		}
		finally {
			//			System.out.println("DT finally  " ); 
		}

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