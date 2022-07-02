package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
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
import java.text.SimpleDateFormat;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class monHttps {

	static boolean state = false;
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;
	static long ts;
	static boolean swFound;
	static boolean swSingle = false;
	static boolean swShow = false;
	static boolean swExpire = false;
	static boolean swNegative = false;
	static String expire;
	static String host;
	static String hosturl;
	static String tabbar = "                                                                                               ";
	static InetAddress inet;
	static String version = "monHttps ";
//	static String database = "jVakt";
//	static String dbuser   = "jVakt";
//	static String dbpassword = "xz";
//	static String dbhost   = "localhost";
//	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static int wport = 443 ;
	static String agent = null;
	static String webfile = "";
	//	static String webcontent = "400";
	static String webcontent = "";
	static boolean swWebcontent = false;
	static Date now;
	static long certAgeDays = 10;

	static String config = null;
	static File configF;

	static String stat = null;
	static String pn;
	static FileOutputStream statF;
	static boolean swStat = false;
	static OutputStreamWriter osw;
	static BufferedWriter statCsv;

	public static void main(String[] args) throws UnknownHostException, IOException, Exception {
		
		version += getVersion()+".54";
		String[] tab = new String [1];
		//		String tdat;
		String s;
		File[] listf;
		DirFilter df;
		File dir = new File(".");
		if (config != null ) dir = new File(config);
		String suf = ".csv";
		String pos = "monHttps-";
		boolean swRun = false;
		now = new Date();


		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("File names must contain monHttps- and end with .csv. e.g. monHttps-01.csv ");
			System.out.println("Row in the file example: ");
			System.out.println("wikipedia;sv.wikipedia.org;443;/wiki/Portal:Huvudsida;wikipedia;descriptive text");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-run    \tTo actually update the status on the server side."+
					"\n-host   \tCheck a single host." +
					"\n-port   \tDefault is 443." +
					"\n-web    \tlike /index.html" +
					"\n-webcontent \tstring in the response to check for (optional)(RC 401 is accepted)." +
					"\n-stat   \tThe dir of the statistics files."+
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
			//			if (args[i].equalsIgnoreCase("-dir")) dir = new File(args[++i]);
			if (args[i].equalsIgnoreCase("-port")) wport = Integer.parseInt(args[++i]);
			if (args[i].equalsIgnoreCase("-web")) webfile = args[++i];
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-show")) swShow = true;
			if (args[i].equalsIgnoreCase("-host")) { swSingle = true; host = args[++i]; }
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-stat")) stat = args[++i];
			if (args[i].equalsIgnoreCase("-webcontent")) { 
				webcontent = args[++i];
				swWebcontent = true;
			}
		}
		if (config != null ) dir = new File(config);
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");

		if (stat != null ) {
			swStat = true;
			if (swSingle) {
				try {
					statF = new FileOutputStream(stat+"/monHttps-A-single-check.csv",true); // append
					osw = new OutputStreamWriter(statF, "Cp850");
					statCsv = new BufferedWriter(osw);			
				} catch (Exception ex) {
					System.out.println("-- Exeption when open the statistical file monHttps-A-single-check.csv !");
					ex.printStackTrace();
				}

			}
		}

		System.out.println("\n"+now+" *** Jvakt "+version+" ***\n");
		if (swShow)	System.out.println(" config file: "+configF);

		getProps();

		System.setProperty("java.net.preferIPv6Addresses", "false");
		if (swShow)	{
			System.out.println(" Dir : "+dir);
			System.out.println(" Suf : "+suf);
			System.out.println(" Pos : "+pos);
			System.out.println(" Host: "+host);
			System.out.println(" stat directory: "+stat+"\n");
		}

		if (swSingle) {
			checkHttp();
		} else {

			//			if (pos != null) df = new DirFilter(suf, pos);
			//			else             df = new DirFilter(suf);

			df = new DirFilter(suf, pos);

			listf = dir.listFiles(df);

			if (swShow)	System.out.println("-- Number of files found:"+ listf.length);

			for (int i = 0; i < listf.length; i++) {

				if (swShow)	System.out.println("-- Checking: "+listf[i]+"\n");

				BufferedReader in = new BufferedReader(new FileReader(listf[i]));

				while ((s = in.readLine()) != null) {
					if (s.length() == 0) continue; 
					if (s.startsWith("#")) continue; 
					swNegative = false;

					// splittar rad från fil
					tab = s.split(";" , 7);
					t_id = tab[0];
					host = tab[1];
					wport = Integer.parseInt(tab[2]);
					webfile = tab[3];
					webcontent = tab[4];
					t_desc = tab[5];
					if (tab.length > 6) {
						if (tab[6].toLowerCase().trim().startsWith("neg")) swNegative = true;
					}

					state = false;

					if (webcontent.length()>0) swWebcontent = true;
					else swWebcontent = false;

					if (swStat) {
						try {
							statF = new FileOutputStream(stat+"/monHttps-"+t_id+".csv",true); // append
							swStat = true;
							osw = new OutputStreamWriter(statF, "Cp850");
							statCsv = new BufferedWriter(osw);
						} catch (Exception ex) {
							System.out.println("-- Exeption when open file monHttps-"+t_id+".csv");
							ex.printStackTrace();
						}
					}

					checkHttp();

					if (swStat) {
						try{ statCsv.close(); } catch (Exception ex) {}
					}

					if (swRun)  {
						if (state) 	sendSTS(true);
						else 		sendSTS(false);
					}

				}
				in.close();
			}
		}
		if (swStat && swSingle) statCsv.close();
	}

	public static boolean checkHttp() {
		Date innan;
		Date efter;
		Date cacheexpiration; 
		long delay;

		// First set the default cookie manager.

		java.net.CookieManager cm = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(cm);
		innan = new Date();
		// connect to port
		try {
			hosturl ="https://"+host+":"+wport+webfile;
			if (swShow)	System.out.println("\n-- URL    : https://"+host+":"+wport+webfile);
			if (swShow && swWebcontent)	System.out.println("-- OK text: " +webcontent);
			//			System.setProperty("https.protocols", "SSLv3");
			URL url = new URL("https://"+host+":"+wport+webfile); 
			//			URLConnection con = url.openConnection();  // new
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla");
			if (swShow)	System.out.println("-- OK connection");
			con.setReadTimeout(5000);
			con.setConnectTimeout(5000);
			con.setConnectTimeout(5000);
			if (swShow) {
				if (con.getExpiration() > 0) {
					cacheexpiration = new Date(con.getExpiration());
					System.out.println("-- Cache expiration "+cacheexpiration);
				} else System.out.println("-- Cache expiration 0");
			}
			//			BufferedReader httpin = new BufferedReader(
			//					new InputStreamReader(url.openStream()));

			con.connect();

			swExpire = false;
			Certificate[] certs = con.getServerCertificates();
			for(Certificate c:certs){
				//System.out.println(c.getType());
				//System.out.println(c.toString());
				X509Certificate xc = (X509Certificate)c; // we should really check the type before doing this typecast..
				String dn = xc.getSubjectDN().getName();
				Date expiresOn= xc.getNotAfter();
				Date now = new Date();
				long days = (expiresOn.getTime()-now.getTime())/(1000*60*60*24);
				if (days > 0 && days < certAgeDays) { 
					//					expire = " ** Warning ** Certificate expires in "+days+" days > "+expiresOn+" < "+dn ;
					expire = " ** Warning ** Certificate expire soon > "+expiresOn+" < "+dn ;
					if (swShow)	System.out.println(expire);
					swExpire = true;
				}
				if (days <= 0) { 
					expire = " ** Warning ** Certificate has expired! - "+expiresOn+" - "+dn ;
					if (swShow)	System.out.println(expire);
					swExpire = true;
				}
				if (swShow)	System.out.println(dn+"\n The certificate expires on "+expiresOn+".\n "+days+" days to go");
			}			


			if (swWebcontent) {	
				if (swShow)	System.out.println("-- OK. Trying to get in-stream");
				try {
					BufferedReader httpin = new BufferedReader(
							new InputStreamReader(con.getInputStream()));

					String inputLine;
					if (swShow)	System.out.println("-- start read lines");
					while ((inputLine = httpin.readLine()) != null  && !state) {
						if (swShow)	System.out.println(inputLine);
						if (inputLine.toLowerCase().indexOf(webcontent.toLowerCase()) >= 0) {
							state = true;
							if (swShow)	System.out.println("-- OK text found: "+ webcontent );
						}
					}
					httpin.close();
				} catch (Exception e) { 
					if (swShow) System.out.println(e);
					if (e.toString().indexOf("HTTP response code: 401")>0) {
						state = true; 
						if (swShow) System.out.println(" -- Warning. Not authorized to access server. Still considered OK");
					}
					else state = false;
					t_desc = t_desc+" * "+e.getMessage();
					t_desc.trim();
				}
			} 
			else state = true; 

		} 
		catch (Exception e) { 
			if (swShow) System.out.println(e); 
			state = false; 
			t_desc =e.getMessage();
		}
		efter = new Date();
		delay = efter.getTime() - innan.getTime();
		delay++;    // add an extra millisecond to compensate for extremely fast connections  
		if (delay>=5000) {
			System.out.println("-- Delay over 5000 ms, connection failed!");
			delay = 0;   // a response delay over 5000ms is a failure
		}

		//		catch (UnknownHostException e) { System.out.println(e); state = false;   }
		//		catch (Exception e) { 
		//			System.out.println(e);
		//			if (e.toString().indexOf("403") > 0) state = true;
		//			else state = false;
		//		}

		//		try { Thread.currentThread(); Thread.sleep(1000); } catch (Exception e) {} ;

		if (t_desc == null) t_desc = " ";
		if (hosturl.length()>85) hosturl=hosturl.substring(0,85);
		hosturl = hosturl + tabbar.substring(0,85-hosturl.length());

		if (!state) delay=0;
		if (swShow)	System.out.println("-- Response time: "+delay+" ms" );
		if (swStat) {
			now = new Date();
			String dat = new String("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dat_form;
			dat_form = new SimpleDateFormat(dat);
			String dattime = dat_form.format(now);
			try {
				statCsv.append(dattime+";"+delay );  
				statCsv.newLine();
			} catch (IOException ex) {
				System.out.println("-- IOexeption when appending statistics file monHttps-"+t_id+".csv !");
				ex.printStackTrace();
			}
			catch (Exception ex) {
				System.out.println("-- cannot append statistics file monHttps-"+t_id+".csv, maybe it is locked by another process?" );
				ex.printStackTrace();
			}
		}

		if (swExpire) {     // If the certificate has expired fail the connection
			t_desc = expire +" - "+ t_desc;
			state = false;
		}

		if (swNegative) pn = "N";
		else pn = "P";

		if (state) {System.out.println(new Date()+" ("+pn+") Connection successful - "+hosturl+t_desc); return true; }
		else 	   {System.out.println(new Date()+" ("+pn+") Connection failed     - "+hosturl+t_desc); return false; }

		//		if (state) {System.out.println("Connection successful"); return true; }
		//		else 	   {System.out.println("Connection failed"); return false; }
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		if (swShow)	System.out.println(jm.open());
		else jm.open();
		jmsg.setId(t_id+"-monHttps-"+host);
		if ((STS && !swNegative) || (!STS && swNegative)) {
			jmsg.setRptsts("OK");
			if (swShow) System.out.println("("+pn+") Reported OK to Jvakt server --");
		}
		else {
			jmsg.setRptsts("ERR");
			if (swShow) System.out.println("("+pn+") Reported ERR to Jvakt server --");
		}
		if (swNegative) t_desc="("+pn+") "+t_desc;
		jmsg.setBody(t_desc);
		jmsg.setType("R");
		jmsg.setAgent(agent);
		if (jm.sendMsg(jmsg));
		else            	  System.out.println("-- Rpt Failed --");
		jm.close();
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
			String CertAgeDaysW   = prop.getProperty("CertAgeDays");
			if (CertAgeDaysW != null) certAgeDays = Integer.parseInt(CertAgeDaysW);
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
