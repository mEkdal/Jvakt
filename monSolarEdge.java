package Jvakt;
/*
 * 2023-01-10 V.4   Michael Ekdal		Changed default type from T to R
 * 2022-11-15 V.3   Michael Ekdal		Small correction in the text "Currently no power is produced".
 * 2022-08-17 V.1   Michael Ekdal		Created to monitor an solar panel site hosted by SolarEdge.
 */

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;

public class monSolarEdge {

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
	static String version = "monSolarEdge ";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static int wport = 443 ;
	static String agent = null;
	static String webfile = "";
	static String site = null;
	static String webcontent = "400";
	static String auth = "zzz";
	static String fom = "09";
	static String tom = "14";
	static Date now;

	static boolean swRun = false;
	static boolean swBat = false;
	static String config = null;
	static File configF;

	public static void main(String[] args) throws UnknownHostException, IOException, Exception {

		version += getVersion()+".4";
		now = new Date();


		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("The SolarEdge rest API is used to check the resources.");
			System.out.println("Power and the last reported connection by the site to the SolarEdge is checked.");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config  \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-run     \tTo actually update the status on the server side."+
					"\n-host    \tCheck a single host." +
					"\n-port    \tDefault is 443." +
					"\n-show    \tShow extended logging." +
					"\n-site    \tThe SolarEdge site id" +
					"\n-auth    \tThe SolarEdge API key." +
					"\n-from    \tFrom which hour the power is checked. default is 09" +
					"\n-to      \tTo which hour the power is checked.   default is 14" +
					"\n-battery \tCheck the battery." 
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
			if (args[i].equalsIgnoreCase("-site")) site = args[++i];
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-show")) swShow = true;
			if (args[i].equalsIgnoreCase("-battery")) swBat = true;
			if (args[i].equalsIgnoreCase("-host")) { host = args[++i]; }
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-from")) fom = args[++i];
			if (args[i].equalsIgnoreCase("-to"))   tom = args[++i];
			if (args[i].equalsIgnoreCase("-auth")) auth = args[++i];
		}
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");


		System.out.println(now+" *** Jvakt "+version+" ***");
		if (swShow)	System.out.println(" config file: "+configF);

		getProps();

		System.setProperty("java.net.preferIPv6Addresses", "false");
		if (swShow)	{
			System.out.println(" Host: "+host+"\n");
		}
		checkHttpOverview();
		if (swBat) checkHttpBattery();
	}

	public static boolean checkHttpOverview() {
		// First set the default cookie manager.
		java.net.CookieManager cm = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(cm);
		// connect to port
		try {
			hosturl ="https://"+host+":"+wport+"/site/"+site+"/overview.json?api_key="+auth;

			//			if (swShow)	System.out.println("-- URL  : https://"+host+":"+wport+webfile);
			if (swShow)	System.out.println("-- overview URL  : "+hosturl);
			//			if (swShow)	System.out.println("-- Auth : " +auth);
			//			System.setProperty("https.protocols", "SSLv3");
			URL url = new URL(hosturl); 
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			if (swShow)	System.out.println("-- OK, got connection");
			con.setReadTimeout(5000);
			con.setConnectTimeout(5000);
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", "Bearer "+auth);
			con.connect();

			//			if (swShow) System.out.println(new Date()+" -- getURL         : "+con.getURL());
			//			if (swShow) System.out.println(new Date()+" -- Req-Limit-Short: "+con.getHeaderField("Req-Limit-Short"));
			//			if (swShow) System.out.println(new Date()+" -- Req-Limit-Long : "+con.getHeaderField("Req-Limit-Long"));

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
				String parseResult = parseInputlineOverview(inputLine); 
				if (parseResult.startsWith("OK")) {
					System.out.println(new Date()+" -- SolarEdge site "+site+" is working.");
					if (swRun) {
						t_id=site;
						t_desc="SolarEdge site "+site+" is working.";
						sendSTS(true);
					}
				} else {
					System.out.println(new Date()+" -- SolarEdge site "+site+" has an anomality: "+parseResult);
					if (swRun) {
						t_id=site;
						t_desc="SolarEdge site "+site+" has an anomality: "+parseResult;
						sendSTS(false);
					}
				}

			}
			httpin.close();
			con.disconnect();
			if (swShow)	System.out.println("-- End overview check --");
		} 
		catch (Exception e) { System.out.println(e); state = false;   }

		if (!state) {
			if (swShow)	System.out.println(new Date()+" -- Connection failed      - "+hosturl+t_desc);
			if (swRun) {
				t_id=site;
				t_desc = "Connection failed  ";
				sendSTS(false);
			}
			return false; 
		}
		return true; 
	}

	public static boolean checkHttpBattery() {
		// First set the default cookie manager.
		java.net.CookieManager cm = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(cm);
		// connect to port
		try {
			hosturl ="https://"+host+":"+wport+"/site/"+site+"/storageData.json?startTime=2022-09-10%2011:00:00&endTime=2022-09-11%2013:00:00&api_key="+auth;

//			if (swShow)	System.out.println("-- URL  : https://"+host+":"+wport+webfile);
			if (swShow)	System.out.println("\n-- battery URL  : "+hosturl);
			//			if (swShow)	System.out.println("-- Auth : " +auth);
			//			System.setProperty("https.protocols", "SSLv3");
			URL url = new URL(hosturl); 
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			if (swShow)	System.out.println("-- OK, got connection");
			con.setReadTimeout(5000);
			con.setConnectTimeout(5000);
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", "Bearer "+auth);
			con.connect();

			//			if (swShow) System.out.println(new Date()+" -- getURL         : "+con.getURL());
			//			if (swShow) System.out.println(new Date()+" -- Req-Limit-Short: "+con.getHeaderField("Req-Limit-Short"));
			//			if (swShow) System.out.println(new Date()+" -- Req-Limit-Long : "+con.getHeaderField("Req-Limit-Long"));

			if (con.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code: "
						+ con.getResponseCode() +" "+con.getResponseMessage());
			}

			BufferedReader httpin = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			if (swShow)	System.out.println("-- OK, got in-stream");

			String inputLine;
			Boolean batFound = false;
			if (swShow)	System.out.println("-- start read lines");
			while ((inputLine = httpin.readLine()) != null  && !state) {
				state = true;
				if (swShow)	System.out.println(inputLine);
				batFound = true;
//				String parseResult = parseInputlineBattery(inputLine);
//				if (parseResult.startsWith("OK")) {
//					System.out.println(new Date()+" -- SolarEdge site "+site+" is working.");
//					if (swRun) {
//						t_id=site;
//						t_desc="SolarEdge site "+site+" is working.";
//						sendSTS(true);
//					}
//				} else {
//					System.out.println(new Date()+" -- SolarEdge site "+site+" has an anomality: "+parseResult);
//					if (swRun) {
//						t_id=site;
//						t_desc="SolarEdge site "+site+" has an anomality: "+parseResult;
//						sendSTS(false);
//					}
//				}
			}
			if (swShow && !batFound)	System.out.println("No battery found!");

			httpin.close();
			con.disconnect();
			if (swShow)	System.out.println("-- End battry check --");
		} 
		catch (Exception e) { System.out.println(e); state = false;   }

		if (!state) {
			if (swShow)	System.out.println(new Date()+" -- Connection failed      - "+hosturl+t_desc);
			if (swRun) {
				t_id=site;
				t_desc = "Connection failed  ";
				sendSTS(false);
			}
			return false; 
		}
		return true; 
	}

	
	static String parseInputlineOverview(String in) {

		JsonElement jsonElement = JsonParser.parseString(in);

		JsonObject  jsonObject = jsonElement.getAsJsonObject();
		JsonElement overView =  jsonObject.get("overview"); 

		//	    System.out.println("overview: "+ jsonObject.get("overview") );
		//	    System.out.println("lastUpdateTime: "+ overView.getAsJsonObject().get("lastUpdateTime") );
		//	    System.out.println("currentPower:   "+ overView.getAsJsonObject().get("currentPower").getAsJsonObject().get("power") );

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


		Date d1 = null;
		Date d2 = null;
		try {
			d1 = dateFormat.parse("2022-08-08 16:37:41" );
			d1 = new Date() ;
		}
		catch (Exception e) { System.out.println(e);  }
		//	    System.out.println("Date #1: "+ d1 );

		String s = overView.getAsJsonObject().get("lastUpdateTime").getAsString(); 
		//	    System.out.println("s: "+ s );
		try {
			d2 = dateFormat.parse(s);
		}
		catch (Exception e) { System.out.println(e);  }
		//	    System.out.println("Date #2: "+ d2 );

		long diffInMillies = d1.getTime() - d2.getTime();
		
		SimpleDateFormat tod_form;
		int power= overView.getAsJsonObject().get("currentPower").getAsJsonObject().get("power").getAsInt();
		tod_form = new SimpleDateFormat("HH");
		String tod = tod_form.format(new Date());
//		System.out.println("Tod : "+ tod );


		if ( diffInMillies/1000/60 > 20) return "No update from solarpanels to SolarEdge cloud was made in the last 20 minutes";
		else if (power==0 && tod.compareTo(fom)>0 && tod.compareTo(tom)<0 ) return "Currently no power is produced";
		else  return "OK";        

	}

	// sends status to the Jvakt server
	static protected void sendSTS( boolean STS) {
		try {
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(jvhost, port);
			if (swShow)	System.out.println(jm.open());
			else jm.open();
			jmsg.setId("SolarEdge-"+t_id);
			if (STS) jmsg.setRptsts("OK");
			else jmsg.setRptsts("ERR");
			jmsg.setBody(t_desc);
			jmsg.setType("R");
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
