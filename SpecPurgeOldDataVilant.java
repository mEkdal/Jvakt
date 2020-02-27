package Jvakt;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.sql.Timestamp;
import java.util.*;
import java.time.*;
import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SpecPurgeOldDataVilant {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	static boolean swRun = false;
	static String lastactivity = "2000-01-01 00:00:00.000";
	static Long lhou, lmin, Lsec, Lrptdat, Lchktim;
	static int count = 0;
	static int count2 = 0;
	static int counttot = 0;
	static Authenticator auth;

	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "SpecPurgeOldDataVilant (2019-AUG-27)";
		String database = "perstorp";
		String dbuser   = "postgres";
		String dbpassword = "postgres";
		String dbhost   = "localhost";
		String dbport   = "5432";
		System.out.println("********** SpecPurgeOldDataVilant Start ********   " + LocalDateTime.now());

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-lastactivity")) lastactivity = args[++i];
			if (args[i].equalsIgnoreCase("-run")) swRun = true; 
		}
		System.out.println("Arguments: -lastactivity="+lastactivity + "  -run="+swRun);

		Statement stmt = null;
		String s;

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			System.out.println(DBUrl);
			System.out.println("dbuser=" + dbuser +"  dbpassword="+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(false);
			Statement st = conn.createStatement();
			stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(100000);

			// ******** attempt table ************
			count=0;
			s = new String("select * from attempt where requesttime <= '"+lastactivity+"'");
			System.out.println(s);
			ResultSet rs = stmt.executeQuery(s);
			ResultSet rs2;

			while (rs.next()) {
				count++; counttot++;
				if (swRun)
					try { rs.deleteRow(); } catch(NullPointerException npe2) { System.out.println("attempt table: "+npe2);}
			}
			System.out.println("- Attempt Count: " + count);
			if (swRun) 	conn.commit();
			else  		conn.rollback();

			// ************* message table ************
			count=0;
			s = new String("select * from message where lastactivity <= '"+lastactivity+"'");
			System.out.println(s);
			rs = stmt.executeQuery(s);

			while (rs.next()) {
				count++; counttot++;
				if (swRun)
					try { rs.deleteRow(); } catch(NullPointerException npe2) { System.out.println("Message table: "+npe2);}
			}
			System.out.println("- Messages Count: " + count);
			if (swRun) 	conn.commit();
			else  		conn.rollback();


			// ************* perstorptag table ************
			count=0;
			s = new String("select * from perstorptag where timestamp <= '"+lastactivity+"'");
			System.out.println(s);
			rs = stmt.executeQuery(s);

			while (rs.next()) {
				count++;  counttot++;
				if (swRun)
					try { rs.deleteRow(); } catch(NullPointerException npe2) { System.out.println("Message table: "+npe2);}
			}
			System.out.println("- perstorptag Count: " + count);
			if (swRun) 	conn.commit();
			else  		conn.rollback();

			// ************* perstorpshipment table ************
			count=0; count2=0;
			s = new String("select * from perstorpshipment where creationtime <= '"+lastactivity+"'");
			System.out.println(s);
			rs = stmt.executeQuery(s);

			while (rs.next()) {
				count++; counttot++;

				if (swRun)	s = new String("delete   from perstorp_parameters where order_id = '"+rs.getString("id")+"';");
				else 		s = new String("select * from perstorp_parameters where order_id = '"+rs.getString("id")+"';");
//				System.out.println(LocalDateTime.now()+"  "+ s);
				if (st.execute(s)) {
					rs2 = st.getResultSet();
					while (rs2.next()) {
						count2++;
					}
				}
				else {
					count2 += st.getUpdateCount();
					//						System.out.println("   - Deleted rows: "+st.getUpdateCount());
				}

				if (swRun)	s = new String("delete   from perstorpshipmentevent where shipment_id = '"+rs.getString("id")+"';");
				else 		s = new String("select * from perstorpshipmentevent where shipment_id = '"+rs.getString("id")+"';");
//				System.out.println(LocalDateTime.now()+"  "+ s);
				if (st.execute(s)) {
					rs2 = st.getResultSet();
					while (rs2.next()) {
						count2++;
					}
				}
				else {
					count2 += st.getUpdateCount();
					//							System.out.println("   - Deleted rows: "+st.getUpdateCount());
				}

				if (rs.getString("latestevent_id")!=null) {
					if (swRun)	s = new String("delete   from perstorpshipmentevent where id = '"+rs.getString("latestevent_id")+"';");
					else 		s = new String("select * from perstorpshipmentevent where id = '"+rs.getString("latestevent_id")+"';");
//					System.out.println(LocalDateTime.now()+"  "+ s);
					if (st.execute(s)) {
						rs2 = st.getResultSet();
						while (rs2.next()) {
							count2++;
						}
					}
					else {
						count2 += st.getUpdateCount();
						//								System.out.println("   - Deleted rows: "+st.getUpdateCount());
					}
				}

				if (swRun)
					try { 
//						System.out.println(LocalDateTime.now()+"  Delete perstorpshipment id="+ rs.getString("id"));
						rs.deleteRow(); 
						} catch(NullPointerException npe2) { System.out.println("Message table: "+npe2);}
			}
			System.out.println("- perstorpshipment Count: " + count +"    perstorp_parameters: "+count2);
			counttot += count2;;
			if (swRun) 	conn.commit();
			else  		conn.rollback();

			// ********** sapdata table **************
			count=0; count2=0;
			s = new String("select * from sapdata where creationtime <= '"+lastactivity+"'");
			System.out.println(s);
			rs = stmt.executeQuery(s);

			while (rs.next()) {
				count++; counttot++;
				if (rs.getString("order_id")!=null) {
				if (swRun) 	s = new String("delete   from perstorp_parameters where order_id = '"+rs.getString("order_id")+"';");
				else  	 	s = new String("select * from perstorp_parameters where order_id = '"+rs.getString("order_id")+"';");
				if (st.execute(s)) {
					rs2 = st.getResultSet();
					while (rs2.next()) {
						count2++;
					}
				}
				else {
					count2 += st.getUpdateCount();
					//						System.out.println("   - Deleted rows: "+st.getUpdateCount());
				}
				}
				if (swRun)	
					try { rs.deleteRow(); } catch(NullPointerException npe2) { System.out.println("sapdata table: "+npe2);}
			}
			System.out.println("- Sapdata Count: " + count +"    perstorp_parameters: "+count2);
			counttot += count2;;
			if (swRun) 	conn.commit();
			else  		conn.rollback();

			// ********** final **************
			rs.close(); 
			stmt.close();
			System.out.println("- Total Count: " + counttot);
			System.out.println("********** SpecPurgeOldDataVilant Done! ********   " + LocalDateTime.now());
		}
		catch (SQLException e) {
			System.err.println("SQLExeption " + e);
			//			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println("Exeption " + e);
			//			System.err.println(e.getMessage());
		}
	}
}