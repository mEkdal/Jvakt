package Jvakt;
import java.io.*;
import java.util.*;

//import org.icmp4j.IcmpPingRequest;
//import org.icmp4j.IcmpPingResponse;
//import org.icmp4j.IcmpPingUtil;

import java.net.*;

public class monIpPorts {

	static String state = "a";
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;
	static long ts;
	static boolean swFound;
	static boolean swSingle = false;
	static String host;
	static InetAddress inet;
	static String version = "monIpPorts 1.2 Date 2017-11-096";
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "xz";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static int wport = 80 ;
	static String agent = null;
	static Socket cs = null;

	static String config = null;
	static File configF;

	public static void main(String[] args) throws UnknownHostException, IOException {

		String[] tab = new String [1];
		String tdat;
		String s;
		File[] listf;
		DirFilter df;


		File dir = new File(".");
		String suf = ".csv";
		String pos = "monIpPorts";
		boolean swRun = false;



		//                Socket cs = null;
		//                int port;

		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("File names must contain monHttp and end with .csv. e.g. monHttp-01.csv ");
			System.out.println("Row in the file example: ");
			System.out.println("WSI_PLC_A209;10.100.9.2;80;Vilant truck system Penta");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-run   \tTo actually update the status on the server side."+
					"\n-host  \tCheck a single host."          );

			System.exit(4);
		}

		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
//			if (args[i].equalsIgnoreCase("-dir")) dir = new File(args[++i]);
			if (args[i].equalsIgnoreCase("-port")) wport = Integer.parseInt(args[++i]);
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-host")) { swSingle = true; host = args[++i]; }
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
		}
		if (config != null ) dir = new File(config);
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("Jvakt: "+version);
		System.out.println("-config file: "+configF);
		
		getProps();

		System.setProperty("java.net.preferIPv6Addresses", "false");

		System.out.println("-- Dir : "+dir);
		System.out.println("-- Suf : "+suf);
		System.out.println("-- Pos : "+pos);
		System.out.println("-- Host: "+host);

		if (swSingle) {
			checkIpPort();
		} else {

			if (pos != null) df = new DirFilter(suf, pos);
			else             df = new DirFilter(suf);

			listf = dir.listFiles(df);

			System.out.println("-- Numer of files found:"+ listf.length);

			for (int i = 0; i < listf.length; i++) {

				System.out.println("-- Checking: "+listf[i]);

				BufferedReader in = new BufferedReader(new FileReader(listf[i]));

				while ((s = in.readLine()) != null) {
					if (s.length() == 0) continue; 
					if (s.startsWith("#")) continue; 

					// splittar rad från fil
					tab = s.split(";" , 4);
					t_id = tab[0];
					host = tab[1];
					wport = Integer.parseInt(tab[2]);
					t_desc = tab[3];



					checkIpPort();

					if (swRun)  {
						if (state.equals("OKAY")) 	sendSTS(true);
						else 						sendSTS(false);
					}

				}
				in.close();
			}
			//   	    		if (con != null) try { con.close(); } catch(Exception e) {}
		}

	}

	public static boolean checkIpPort() {
		// connect to port
		state = "OKAY";
		try {
			System.out.println("Connection to: " + host + ":" + wport);
			cs = new Socket();
			cs.connect(new InetSocketAddress(host, wport), 5000);
			BufferedInputStream inFromClient = new BufferedInputStream(cs.getInputStream());
			BufferedOutputStream outToClient = new BufferedOutputStream(cs.getOutputStream());
			//cs = new Socket(host, port);
//			outToClient.write(' ');
//			outToClient.flush();
		} catch (Exception e) { System.out.println("Connection failed:" + e); state = "FAILED";   }

		try { Thread.currentThread(); Thread.sleep(1000); } catch (Exception e) {} ;

		// disconnect from port
		try {
			if (cs != null) {
				cs.close();
			}
		}  catch (Exception e) { System.out.println("Close failed:" + e);   }

		if (state.equals("OKAY")) { System.out.println("Connection succcessful"); return true; }
		else 					  {	System.out.println("Connection failed"); return false; }
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		System.out.println(jm.open());
		jmsg.setId(t_id+"-monIpPort-"+host+":"+wport);
		if (STS) jmsg.setRptsts("OK");
		else jmsg.setRptsts("ERR");
		jmsg.setBody(t_desc);
		jmsg.setType("R");
		jmsg.setAgent(agent);
		jm.sendMsg(jmsg);
		if (jm.close()) System.out.println("-- Rpt Delivered --");
		else            System.out.println("-- Rpt Failed --");

	}

	static void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configF);
			prop.load(input);
			// get the property value and print it out
			jvport   = prop.getProperty("jvport");
			jvhost   = prop.getProperty("jvhost");
			port = Integer.parseInt(jvport);
			System.out.println("getProps jvport:" + jvport + "  jvhost"+jvhost) ;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			inet = InetAddress.getLocalHost();
			System.out.println("-- Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

	}

}
