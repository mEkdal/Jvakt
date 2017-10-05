package Jvakt;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;

public class test01 {
	
	public static void main (String args[]) {

	  
	   InetAddress inet;
	   try {
       inet = InetAddress.getLocalHost();
       System.out.println("-- Inet: "+inet);

       System.out.println("-- Start consoleHst ");
       String[] par = new String[] { "One", "Two", "Three" };
       consoleHst.main(par);
       
       
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
       
         System.out.println("-- End console ");

         
   }
   catch (Exception e) { System.out.println(e);  }
   }

}

