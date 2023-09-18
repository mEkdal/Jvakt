package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.time.LocalDateTime;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;


public class LogsRowInsert {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	static PreparedStatement st = null; 
	
	static String version = "CheckStatus ";
	
	static String id;
	static String origin;
	static java.sql.Timestamp credat;
	
//	static String database = "Jvakt";
//	static String dbuser   = "Jvakt";
//	static String dbpassword = "xz";
//	static String dbhost   = "localhost";
//	static String dbport   = "5432";

	public static boolean open(String pid, String porigin, java.sql.Timestamp pcredat,String database, String dbuser, String dbpassword, String dbhost, String dbport ) {
		id = pid;
		origin = porigin;
		credat = pcredat;
		try {
			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			//			conn.setAutoCommit(true);
			conn.setAutoCommit(true);
		}
		catch (SQLException e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		return true;
	}

	public static boolean RowIns( String row ) {

		try {
			st = conn.prepareStatement("INSERT INTO Logs (id,origin,credat,row) "
					+ "values (?,?,?,?)");
			//				System.out.println(LocalDateTime.now()+" Prepared insert:" + st);
//			System.out.println(new Date()+" - Insert Logs: "+id+" "+origin+" "+credat+" "+row);
			st.setString(1,id); 
			st.setString(2,origin); 
			st.setTimestamp(3, credat);
			st.setString(4,row ); // 
			int rowsInserted = st.executeUpdate();
//			System.out.println(new Date()+" - Executed insert addC " +rowsInserted);
			st.close();
		}
		catch (SQLException e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		return true;
	}

	public static boolean close() {
		try {
			st.close();
			conn.close();
		}
		catch (SQLException e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println(new Date()+" - "+e);
			System.err.println(e.getMessage());
		}
		return true;
	}
}
