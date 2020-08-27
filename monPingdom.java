package Jvakt;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class monPingdom {

	static boolean state = false;
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;
	static long ts;
	static boolean swFound;
	static boolean swShow = false;
	static String host;
	static String hosturl;
	static String tabbar = "                                                                                               ";
	static InetAddress inet;
	static String version = "monPingdom (2020-08-27)";
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "xz";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static int wport = 443 ;
	static String agent = null;
	static String webfile = "";
	static String webcontent = "400";
	static String auth = "zzz";
	static Date now;

	static boolean swRun = false;
	static String config = null;
	static File configF;

	public static void main(String[] args) throws UnknownHostException, IOException, Exception {

		//		String[] tab = new String [1];
		//		String tdat;
		//		String s;
		//		File[] listf;
		//		DirFilter df;
//		File dir = new File(".");
//		if (config != null ) dir = new File(config);
//		String suf = ".csv";
//		String pos = "monHttps-";

		now = new Date();


		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("The Pingdom 3.1 API is used to check the resources.");
			System.out.println("Every resource is reported to Jvakt server.");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-run    \tTo actually update the status on the server side."+
					"\n-host   \tCheck a single host." +
					"\n-port   \tDefault is 443." +
					"\n-web    \tlike /index.html" +
					"\n-auth   \tThe Pingdom API Token." +
					"\n-show   \tShow the response from the server."
					);

			System.exit(4);
		}

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}
		};

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-port")) wport = Integer.parseInt(args[++i]);
			if (args[i].equalsIgnoreCase("-web")) webfile = args[++i];
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-show")) swShow = true;
			if (args[i].equalsIgnoreCase("-host")) { host = args[++i]; }
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-auth")) auth = args[++i];
		}
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");


		System.out.println("\n"+now+" *** Jvakt "+version+" ***\n");
		if (swShow)	System.out.println(" config file: "+configF);

		getProps();

		System.setProperty("java.net.preferIPv6Addresses", "false");
		if (swShow)	{
			System.out.println(" Host: "+host+"\n");
		}
		checkHttp();
	}

	public static boolean checkHttp() {
		// First set the default cookie manager.
		java.net.CookieManager cm = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(cm);
		// connect to port
		try {
			hosturl ="https://"+host+":"+wport+webfile;
			if (swShow)	System.out.println("-- URL  : https://"+host+":"+wport+webfile);
			if (swShow)	System.out.println("-- Auth : " +auth);
			//			System.setProperty("https.protocols", "SSLv3");
			URL url = new URL("https://"+host+":"+wport+webfile); 
			//			URLConnection con = url.openConnection();  // new
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			if (swShow)	System.out.println("-- OK, got connection");
			con.setReadTimeout(5000);
			con.setConnectTimeout(5000);
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			//			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer "+auth);

			if (con.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code: "
						+ con.getResponseCode() +" "+con.getResponseMessage());
			}

			BufferedReader httpin = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			if (swShow)	System.out.println("-- OK, got in-stream");

			String inputLine;
			if (swShow)	System.out.println("-- start read lines");
			while ((inputLine = httpin.readLine()) != null  && !state) {
				state = true;
				if (swShow)	System.out.println(inputLine);
				parseInputline(inputLine);
			}
			httpin.close();
			con.disconnect();
		} 
		catch (Exception e) { System.out.println(e); state = false;   }

		if (state) {
			if (swShow)	System.out.println(new Date()+" -- Connection succcessful - "+hosturl+t_desc);
			if (swRun) {
				t_id="Monitor";
				t_desc="Connection succcessful";
				sendSTS(true);
			}
			return true; 
		}
		else 	   {
			if (swShow)	System.out.println(new Date()+" -- Connection failed      - "+hosturl+t_desc);
			if (swRun) {
				t_id="Monitor";
				t_desc = "Connection failed  ";
				sendSTS(false);
			}
			return false; 
		}
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) {
		try {
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(jvhost, port);
			if (swShow)	System.out.println(jm.open());
			else jm.open();
			jmsg.setId("monPingdom-"+t_id);
			if (STS) jmsg.setRptsts("OK");
			else jmsg.setRptsts("ERR");
			jmsg.setBody(t_desc);
			jmsg.setType("T");
			jmsg.setAgent(agent);
			if (jm.sendMsg(jmsg));
			else            	  System.out.println("-- Rpt Failed --");
			jm.close();
		} 
		catch (Exception e) { System.out.println("sendSTS: "+e); state = false;   }
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
			if (swShow)	System.out.println(" jvport : " + jvport + "\n jvhost : "+jvhost) ;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			inet = InetAddress.getLocalHost();
			if (swShow)	System.out.println(" Inet : "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

	}

	static void parseInputline(String in) {

		int fom=0;
		int tom;
		String na;
		String hn;
		String sts;

		while (  in.indexOf("\"name\":",fom)>=0 ) {
			fom = in.indexOf("\"name\":",fom); 
			fom = fom + 8;
			tom = in.indexOf("\"", fom);
			//		System.out.println("Fom: " +fom+ "  Tom: " +tom );
			na = in.substring(fom, tom);
			fom = in.indexOf("\"hostname\":",fom); 
			fom = fom + 12;
			tom = in.indexOf("\"", fom);
			//		System.out.println("Fom: " +fom+ "  Tom: " +tom );
			hn = in.substring(fom, tom);
			//		System.out.println(hn);
			fom = in.indexOf("\"status\":",fom);
			fom = fom + 10;
			tom = in.indexOf("\"", fom);
			//		System.out.println("Fom: " +fom+ "  Tom: " +tom );
			sts = in.substring(fom, tom);
			t_id=na;
			t_desc="Name: "+na+"   Hostname: "+hn+"   Status: "+sts;
			if (swShow)	System.out.println("Name: "+na+"   Hostname: "+hn+"   Status: "+sts);
			if (swRun) {
				if (sts.startsWith("up"))	sendSTS(true);
				else sendSTS(false);
			}

		}

	}

}
