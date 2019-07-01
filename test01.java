package Jvakt;

import java.io.IOException;
import java.net.InetAddress;
<<<<<<< HEAD
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.time.*;


public class test01 {
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "xz";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static String agent = null;
	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;

=======
import java.sql.*;

public class test01 {
	
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
	public static void main (String args[]) {

	  
	   InetAddress inet;
<<<<<<< HEAD
	   Statement stmt = null;
	   
=======
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
	   try {
       inet = InetAddress.getLocalHost();
       System.out.println("-- Inet: "+inet);

       System.out.println("-- Start consoleHst ");
<<<<<<< HEAD
       
//       String[] par = new String[] { "One", "Two", "Three" };
//       consoleHst.main(par);
//       
=======
       String[] par = new String[] { "One", "Two", "Three" };
       consoleHst.main(par);
       
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
       
//       new Thread() {
//           public void run(){
//        	   String[] par = new String[] { "One", "Two", "Three" };
//        	   try {
//        	   consoleHst.main(par);
//        	   } 		catch (IOException e1) {
//    			System.err.println(e1);
//    			System.err.println(e1.getMessage());
//    		}
// 
//           }
//         }.start();
<<<<<<< HEAD

//       Class.forName("org.postgresql.Driver").newInstance();
//		DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
//		conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
//		conn.setAutoCommit(true);
//
//		String s = new String("select * from status " + 
//				"WHERE state='A' or state = 'D';"); 


		//			System.out.println(s);
//		stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
//		stmt.setFetchSize(1000);
//		ResultSet rs = stmt.executeQuery(s);
//		ResultSet rs;
		
//		LocalDateTime nu = LocalDateTime.now(); // The current date and time

//       Date nux = new Date(rs.getTime("chktim").getTime()  );
       Date nux = new Date(222112000);
       
       Calendar cal = Calendar.getInstance();
//       cal.setTime(rs.getTime("chktim"));
       cal.setTime(nux);
       cal.set(1970, 01, 01, 8, 0, 0);
       System.out.println("-- nux "+nux);
       System.out.println("-- Timme "+cal.get(Calendar.HOUR_OF_DAY)+"    -- MInut "+cal.get(Calendar.MINUTE) + "-- Sekund "+cal.get(Calendar.SECOND));
       
       cal.set(1970, 01, 01, 0, 0, 0);
       java.sql.Time sqlt =  new java.sql.Time( cal.getTime().getTime());
       System.out.println("-- sqlt "+sqlt + "   "+cal.getTime().getTime());
       cal.set(1970, 01, 01, 6, 0, 0);
       sqlt =  new java.sql.Time( cal.getTime().getTime());
       System.out.println("-- sqlt "+sqlt + "   "+cal.getTime().getTime());
       cal.set(1970, 01, 01, 8, 0, 0);
       sqlt =  new java.sql.Time( cal.getTime().getTime());
       System.out.println("-- sqlt "+sqlt + "   "+cal.getTime().getTime());
       
       cal.setTimeInMillis(2696400000L);
       sqlt =  new java.sql.Time( cal.getTime().getTime());
       System.out.println("-- sqlt "+sqlt + "   "+cal.getTime().getTime());

       LocalDateTime nu = LocalDateTime.now(); // The current date and time
       
		cal.set(1970, 01, 01, 6, 0, 0); // only HH:MM:SS is used
//		st.setTime(9, new java.sql.Time( cal.getTime().getTime())); // chktim 06:00:00

//       if (nu.getHour() > nux.getHours() ) {};
//       if (nu.getHour() > rs.getTime("chktim").getHours() ) {};
       
//       Runtime.getRuntime().exec("java -cp \"C:\\Users\\septpadm\\OneDrive - Perstorp Group\\JavaSrc;C:\\Users\\septpadm\\OneDrive - Perstorp Group\\JavaSrc\\postgresql-42.1.3.jar\" Jvakt.consoleHst");
       
=======
>>>>>>> 36f55cebd265b234fca790644580636fd16c20ee
       
         System.out.println("-- End console ");

         
   }
   catch (Exception e) { System.out.println(e);  }
   }

}

