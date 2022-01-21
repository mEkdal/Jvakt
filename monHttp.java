package Jvakt;
import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;

public class monHttp {

	static boolean state = false;
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;
	static long ts;
	static boolean swFound;
	static boolean swSingle = false;
	static boolean swShow = false;
	static String host;
	static String hosturl;
	static String tabbar="                                                                                                 ";
	static InetAddress inet;
	static String version = "monHttp (2022-JAN-20) ";
//	static String database = "jVakt";
//	static String dbuser   = "jVakt";
//	static String dbpassword = "";
//	static String dbhost   = "localhost";
//	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static int wport = 80 ;
	static String agent = null;
	static String webfile = "";
	//	static String webcontent = "400";
	static String webcontent = "";
	static Date now;

	static String config = null;
	static File configF;

	static String stat = null;
	static FileOutputStream statF;
	static boolean swStat = false;
	static OutputStreamWriter osw;
	static BufferedWriter statCsv;

	public static void main(String[] args) throws UnknownHostException, IOException {

		String[] tab = new String [1];
		//		String tdat;
		String s;
//		String[] children;
		File[] listf;
		DirFilter df;
		File dir = new File(".");
		if (config != null ) dir = new File(config);
		String suf = ".csv";
		String pos = "monHttp-";
		boolean swRun = false;
		now = new Date();


		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("File names must contain monHttp- and end with .csv. e.g. monHttp-01.csv ");
			System.out.println("Row in the file example: ");
			System.out.println("wikipedia;sv.wikipedia.org;80;/wiki/Portal:Huvudsida;wikipedia;descriptive text");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-run    \tTo actually update the status on the server side."+
					"\n-host   \tCheck a single host." +      
					"\n-port   \tDefault is 80." +
					"\n-web    \tlike /index.html" +
					"\n-webcontent \tstring in the response to check for." +
					"\n-stat   \tThe dir of the statistics files."+
					"\n-show   \tShow the response from the server."
					);

			System.exit(4);
		}

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
			if (args[i].equalsIgnoreCase("-webcontent")) webcontent = args[++i];

		}
		if (config != null ) dir = new File(config);
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");

		if (stat != null ) {
			swStat = true;
			if (swSingle) {
				try {
					statF = new FileOutputStream(stat+"/monHttp-A-single-check.csv",true); // append
					osw = new OutputStreamWriter(statF, "Cp850");
					statCsv = new BufferedWriter(osw);			
				} catch (Exception ex) {
					System.out.println("-- Exeption when open the statistical file monHttp-A-single-check.csv !");
					ex.printStackTrace();
				}

			}
		}

		System.out.println("\n"+now+" *** Jvakt "+version+" ***\n");


		getProps();

		System.setProperty("java.net.preferIPv6Addresses", "false");

		if (swShow)	{
			System.out.println(" Dir : "+dir);
			System.out.println(" Suf : "+suf);
			System.out.println(" Pos : "+pos);
			System.out.println(" Host: "+host+"\n");
			System.out.println(" config file   : "+configF);
			System.out.println(" stat directory: "+stat+"\n");
		}

		if (swSingle) {
			checkHttp();
		} else {

			//			if (pos != null) df = new DirFilter(suf, pos);
			//			else             df = new DirFilter(suf);

			df = new DirFilter(suf, pos);

			listf = dir.listFiles(df);
//			children = dir.list(df);

			if (swShow) {
			             System.out.println("-- Number of files found:"+ listf.length);
//			             System.out.println("-- Number of files found:"+ children.length);
			}

			for (int i = 0; i < listf.length; i++) {
//			for (int i = 0; i < children.length; i++) {

				if (swShow)	System.out.println("-- Checking 1: "+listf[i]+"\n");
//				if (swShow)	System.out.println("-- Checking: "+dir+"/"+children[i]+"\n");

				BufferedReader in = new BufferedReader(new FileReader(listf[i]));
//				BufferedReader in = new BufferedReader(new FileReader(new File(dir, children[i])));

				while ((s = in.readLine()) != null) {
					if (s.length() == 0) continue; 
					if (s.startsWith("#")) continue; 

					// split the row from the file
					tab = s.split(";" , 6);
					t_id = tab[0];
					host = tab[1];
					wport = Integer.parseInt(tab[2]);
					webfile = tab[3];
					webcontent = tab[4];
					t_desc = tab[5];
					state = false;

					if (swStat) {
						try {
							statF = new FileOutputStream(stat+"/monHttp-"+t_id+".csv",true); // append
							swStat = true;
							osw = new OutputStreamWriter(statF, "Cp850");
							statCsv = new BufferedWriter(osw);
						} catch (Exception ex) {
							System.out.println("-- Exeption when open file monHttp-"+t_id+".csv");
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
		// connect to port
		innan = new Date();
		try {
			hosturl ="http://"+host+":"+wport+webfile;
			if (swShow)	System.out.println("-- URL    : http://"+host+":"+wport+webfile);
			if (swShow)	System.out.println("-- OK text: " +webcontent);
			URL url = new URL("http://"+host+":"+wport+webfile); 
			URLConnection con = url.openConnection();  // new
			con.addRequestProperty("User-Agent", "Mozilla");
			con.setReadTimeout(5000);
			con.setConnectTimeout(5000);
			if (swShow) {
				if (con.getExpiration() > 0) {
					cacheexpiration = new Date(con.getExpiration());
					System.out.println("-- Cache expiration "+cacheexpiration);
				} else System.out.println("-- Cache expiration 0");
			}
			//			BufferedReader httpin = new BufferedReader(
			//					new InputStreamReader(url.openStream()));
			BufferedReader httpin = new BufferedReader(
					new InputStreamReader(con.getInputStream()));

			String inputLine; 

			while ((inputLine = httpin.readLine()) != null  && !state) {
				if (swShow)	System.out.println(inputLine);
				if (inputLine.toLowerCase().indexOf(webcontent.toLowerCase()) >= 0 ) {
					state = true;
					if (swShow)	System.out.println("-- OK text: "+ webcontent + " found! ");
				}
			}
			httpin.close();
			if (!state) { 
				if (swShow)	System.out.println("-- OK text: "+ webcontent + " NOT found! "); 
			}
		} catch (Exception e) { System.out.println(e); state = false;   }
		efter = new Date();
		delay = efter.getTime() - innan.getTime();
		delay++;    // add an extra millisecond to compensate for extremely fast connections  
		if (delay>=5000) delay = 0;   // a response delay over 5000ms is a failure

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
				System.out.println("-- IOexeption when appending statistics file monHttp-"+t_id+".csv !");
				ex.printStackTrace();
			}
			catch (Exception ex) {
				System.out.println("-- cannot append statistics file monHttp-"+t_id+".csv, maybe it is locked by another process?" );
				ex.printStackTrace();
			}
		}


		if (state) {System.out.println(new Date()+" -- Connection succcessful - "+hosturl+t_desc); return true; }
		else 	   {System.out.println(new Date()+" -- Connection failed      - "+hosturl+t_desc); return false; }
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		if (swShow)			System.out.println(jm.open());
		else jm.open();
		jmsg.setId(t_id+"-monHttp-"+host);
		if (STS) jmsg.setRptsts("OK");
		else jmsg.setRptsts("ERR");
		jmsg.setBody(t_desc);
		jmsg.setType("R");
		jmsg.setAgent(agent);
		//		jm.sendMsg(jmsg);
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

}
