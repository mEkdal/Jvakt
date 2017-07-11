package Jvakt;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.postgresql.util.PSQLException;

class DBupdate {
	Connection conn = null;
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

					stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
					stmt.setFetchSize(50);
					ResultSet rs = stmt.executeQuery(s);
					swHits = false;  // is there already a record?
					while (rs.next() && swLoop) {
						swHits = true;  // A record is already in place
						swPlugin = false;
						sType = rs.getString("type");
						System.out.println("sType 1 " + sType );
//						if ( m.getType().equalsIgnoreCase("P")) {
//							if (rs.getString("msg").equals("T")) status = "time-out";
//							else 								 status = rs.getString("status");
//							swPlugin = true;
//						}
						rs.updateTimestamp("rptdat", new java.sql.Timestamp(new java.util.Date().getTime())); 
						rs.updateString("status", m.getRptsts().toUpperCase());
						rs.updateString("body", m.getBody());
						errors = rs.getInt(10);
						errors++;
						accerr = rs.getInt("accerr");
						
						// update the errors column
						if (m.getRptsts().toUpperCase().equals("OK") || m.getRptsts().toUpperCase().equals("INFO")) rs.updateInt("errors", 0); 
						else                                          rs.updateInt("errors", errors);
						
						if ( m.getType().equalsIgnoreCase("D")) {
							rs.updateString("console", " ");
							rs.updateString("msg", " ");
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
						
						// trigger the background process used for the plugin.
						if (swPlugin && !swDormant) {
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
						st.setInt(3,3); // prio
						st.setString(4,m.getType().toUpperCase() ); // type
						sType = m.getType().toUpperCase();
						st.setString(5,m.getRptsts().toUpperCase() ); 
						st.setString(6,m.getBody() ); // 

						st.setTimestamp(7, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime())); // rptdat
						st.setString(8,"*ALL" ); // chkday
						st.setTime(9, new java.sql.Time((new Date(System.currentTimeMillis())).getTime())); // chktim
						if (m.getRptsts().toUpperCase().equals("OK") || m.getRptsts().toUpperCase().equals("INFO")) st.setInt(10,0); // errors 
						else                                          st.setInt(10,1); 
						st.setInt(11,0); // acceptable errors 
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
						System.out.println(">>> Type  >>>>: " + m.getType().toUpperCase());
						System.out.println(">>> sType >>>>: " + sType);

						// read and remove previous line from the console table and save the count field
						if ( sType.equalsIgnoreCase("I") && !m.getType().equalsIgnoreCase("D") && !m.getId().equalsIgnoreCase("SYSSTS") ) {
							s = new String("select * from console " + 
									"WHERE id ilike '" + m.getId() + 
									"' and type='" + sType.toUpperCase() +
									"' and body ilike '" + m.getBody() +
									"';");
						} else {
							s = new String("select * from console " + 
									"WHERE id ilike '" + m.getId() + 
									"';");					
						}

						stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_INSENSITIVE); 
						stmt.setFetchSize(50);
						ResultSet rs2 = stmt.executeQuery(s);
						count = 1;
						while (rs2.next()) {
							count = count + rs2.getInt(1);
							try { rs2.deleteRow(); } catch(NullPointerException npe2) {} 
						}
						rs2.close(); 
						stmt.close();

						if ( sType.equalsIgnoreCase("I") && !m.getType().equalsIgnoreCase("D")) {
							// insert new line with new timestamp and counter
							PreparedStatement st = conn.prepareStatement("INSERT INTO Console (count,id,prio,type,condat,status,body) "
									+ "values (?,?,?,?,?,?,?)");
							//        System.out.println("Prepared insert:");
							st.setInt(1,count); // count
							st.setString(2,m.getId() ); 
							st.setInt(3,3); // prio
							st.setString(4,sType.toUpperCase() ); // type
							st.setTimestamp(5, new java.sql.Timestamp((new Date(System.currentTimeMillis())).getTime()));
							st.setString(6,m.getRptsts().toUpperCase() ); // 
							st.setString(7,m.getBody() ); // 
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
			System.out.println("DT exeptionerror session " );
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		finally { System.out.println("DT finally  " ); }

	}

}