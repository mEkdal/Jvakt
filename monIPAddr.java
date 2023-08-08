package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 * 2023-02-11 V.55 Michael Ekdal		Made the response time calculation better regarding pings
 * 2023-08-02 V.56 Michael Ekdal		Added -tout parameter
 */

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;

public class monIPAddr {

	static String state = "a";
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;
	static Date innan;
	static Date efter;
	static long ts;
	static boolean swFound;
	static boolean swSingle = false;
	static boolean swLoop = false;
	static boolean swEcho = false;
	static boolean swShow = false;
	static boolean swTracert = false;
	static boolean swNegative = false;

	static String host;
	static String host2;
	static String hostport;
	static String tabbar = "                                                                                         ";
	static String status = null;
	static InetAddress inet;
	static String version = "monIPAddr ";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static int tout = 5 ;
	static int toutux;
	static String agent = null;
	static Date now;
	static int sleep = 1000;

	static String config = null;
	static File configF;

	static String cmd;
	static String OS = System.getProperty("os.name").toLowerCase();
	static String pn;
	
	static String stat = null;
	static FileOutputStream statF;
	static boolean swStat = false;
	static OutputStreamWriter osw;
	static BufferedWriter statCsv;

	public static void main(String[] args) throws UnknownHostException, IOException {

		version += getVersion()+".56";
		String[] tab = new String [1];
		//		String tdat;
		String s;
		File[] listf;
		DirFilter df;

		File dir = new File(".");
		String suf = ".csv";
		String pos = "monIPAddr";
		boolean swRun = false;

		if (args.length < 1) {
			System.out.println("\n *** " +version + " *** \n");
			System.out.println("Input file names must contain monIPAddr and end with .csv. e.g. monIPAddr-01.csv ");
			System.out.println("Will try to establish a TCP connection to port 7 (Echo) and will use ICMP (ping) as a fallback.");
			System.out.println("Fields: ID ; host ; description ; wan router ; status ; negative");
			System.out.println("\nwan router: If ip address to WAN router is entered it must respond to ping for the test to be valid.");
			System.out.println("     I.e. the status will be OK if the WAN is down. This to ensure not all addresses behind a WAN is marked failed when the WAN is lost.");
			System.out.println("\nstatus: if the test is of lesser importance, enter the status code INFO in the fifth field, Default valus is ERR when failed test");
			System.out.println("\nnegative: if the host is meant to be unreachable, enter the code NEG sixth field. This will make Jvakt to treat it as OK");
			System.out.println("\nRow in the file example: ");
			System.out.println("either: WSI_PLC_A209;10.100.9.2;Vilant truck system Penta");
			System.out.println("or:     WSI_PLC_A209;10.100.9.2;Vilant truck system Penta;10.4.2.1");
			System.out.println("or:     WSI_PLC_A209;10.100.9.2;Vilant truck system Penta;10.4.2.1;INFO");
			System.out.println("or:     WSI_PLC_A209;10.100.9.2;Vilant truck system Penta;;INFO");
			System.out.println("or:     WSI_PLC_A209;10.100.9.2;Vilant truck system Penta;;;neg");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n-run    \tTo actually update the status on the server side."+
					"\n-loop   \tTo check every second. Use -sleep to change the default intervall."+
					"\n-host   \tCheck a single host." +
					"\n-show   \tShow a verboose log." +
					"\n-tracert\tTo make a traceroute when a check fails." +
					"\n-tout   \tTime out in seconds. Default is 5 seconds."+
					"\n-stat   \tThe dir of the statistics files."+
					"\n-sleep  \tIn seconds. Used with loop to sleep between checks. The default is one second."
					);

			System.exit(4);
		}

		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			//			if (args[i].equalsIgnoreCase("-dir")) dir = new File(args[++i]);
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-host")) { swSingle = true; host = args[++i]; }
			if (args[i].equalsIgnoreCase("-tout")) tout = Integer.parseInt(args[++i]);
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-loop")) swLoop = true;
			if (args[i].equalsIgnoreCase("-show")) swShow = true;
			if (args[i].equalsIgnoreCase("-tracert")) swTracert = true;
			if (args[i].equalsIgnoreCase("-stat")) stat = args[++i];
			if (args[i].equalsIgnoreCase("-sleep")) { sleep = Integer.valueOf(args[++i]); sleep = sleep *1000; }
		}
		now = new Date();
		System.out.println("\n"+now+" *** Jvakt "+version+" ***\n");
		if (config != null ) dir = new File(config);
		if (swRun) {
			if (config == null ) 	configF = new File("Jvakt.properties");
			else 					configF = new File(config,"Jvakt.properties");
			if (swShow)	System.out.println(" config file: "+configF);
			getProps();
		}
		//		System.out.println(OS);

		System.setProperty("java.net.preferIPv6Addresses", "false");
		toutux=tout;
		tout=tout*1000;
		now = new Date();

		if (swShow)	{
			System.out.println(" Config : "+config);
			System.out.println(" Dir    : "+dir);
			System.out.println(" Suf    : "+suf);
			System.out.println(" Pos    : "+pos);
			System.out.println(" Host   : "+host);
			System.out.println(" Run    : "+swRun);
			System.out.println(" Loop   : "+swLoop);
			System.out.println(" Sleep  : "+sleep+" ms");
			System.out.println(" Tout   : "+tout+" ms");
			System.out.println(" stat directory: "+stat+"\n");
		}

		if (stat != null ) {
			swStat = true;
			if (swSingle) {
				try {
					statF = new FileOutputStream(stat+"/monIPAddr-A-single-check.csv",true); // append
					osw = new OutputStreamWriter(statF, "Cp850");
					statCsv = new BufferedWriter(osw);
				} catch (Exception ex) {
					System.out.println("-- Exeption when open the statistical file monIPAddr-A-single-check.csv !");
					ex.printStackTrace();
				}

			}
		}
		
		do {
			if (swSingle) {
				checkIPAddr();
			} else {

				if (pos != null) df = new DirFilter(suf, pos);
				else             df = new DirFilter(suf);

				listf = dir.listFiles(df);

				if (swShow)	System.out.println("-- Antal filer:"+ listf.length);

				for (int i = 0; i < listf.length; i++) {

					if (!swLoop && swShow) System.out.println("-- Checking: "+listf[i]+"\n");

					BufferedReader in = new BufferedReader(new FileReader(listf[i]));

					while ((s = in.readLine()) != null) {
						if (s.length() == 0) continue; 
						if (s.startsWith("#")) continue; 

						// splittar rad från fil
						host2 = null;
						status = null;
						swNegative = false;
						tab = s.split(";" , 6);
						t_id   = tab[0];
						host   = tab[1];
						t_desc = tab[2];
						if (tab.length >= 4) {
							host2  = tab[3];
							host2.trim();
							if (host2.length()<2)   host2 = null;
						}
						if (tab.length >= 5) {
							status = tab[4];
							status.trim();
							if (status.length()<2)  status = null;
						}
						if (tab.length == 6) {
							if (tab[5].toLowerCase().trim().startsWith("neg")) swNegative = true;
						}
						if (swShow) System.out.println("t_id:"+t_id+" host:"+host+" t_desc:"+t_desc+" host2:"+host2+" status:"+status+" swNegative:"+swNegative);

						state = "OKAY";    

						if (swStat) {
							try {
								statF = new FileOutputStream(stat+"/monIPAddr-"+t_id+".csv",true); // append
								swStat = true;
								osw = new OutputStreamWriter(statF, "Cp850");
								statCsv = new BufferedWriter(osw);
							} catch (Exception ex) {
								System.out.println("-- Exeption when open file monIPAddr-"+t_id+".csv");
								ex.printStackTrace();
							}
						}

						checkIPAddr();
						
						if (swStat) {
							try{ statCsv.close(); } catch (Exception ex) {}
						}

						// checks host2 to verify WAN is up. Else host is considered okay
						if (state.equals("FAILED") && host2 != null) { 
							host = host2;
							if (checkIPAddr()) { // checks host2
								state = "FAILED"; 
							}
							else state = "OKAY";  
							host   = tab[1];
						}

						// try { Thread.currentThread(); Thread.sleep(1000); } catch (Exception e) {} ;

						//						System.out.println("-- State: "+state);
						if (swRun)  {
							if (state.equals("OKAY")) 	sendSTS(true);
							else 						sendSTS(false);
						}

					}
					in.close();
				}
			}
			//			if (swLoop) try {Thread.currentThread().sleep(1000);} catch (InterruptedException e) {e.printStackTrace();} // sleep 1 second
			if (swLoop) try {Thread.sleep(sleep);} catch (InterruptedException e) {e.printStackTrace();} 
		} while(swLoop);
		
		if (swStat && swSingle) statCsv.close();
	
	}

	public static boolean checkIPAddr() {
		long delay;

		// connect to host
		swEcho=false; 
		innan = new Date();
		try {
			if (t_id == null) t_id = "";
			if (t_desc==null) t_desc=" ";
			if (swShow)	System.out.println(new Date()+" -- Checking host "+host +" "+t_desc );
			inet = InetAddress.getByName(host); 
			//			if (!swLoop) System.out.println("\n-- Inet: "+inet);
			//System.out.println("-- Inet bool: "+inet.isReachable(5000));
			//  TCP connection on port 7 (Echo) 
			if (!inet.isReachable(tout)) { 
				state = "FAILED"; 
				if (swShow)	System.out.println(new Date()+" -- Echo failed, pinging..." );
			}
			else { 
				state = "OKAY"; 
				swEcho=true;  
			}
		} catch (Exception e) { 
			state = "FAILED"; 
			if (swShow)	System.out.println("-- Echo exception state: "+state);  
		}

		if (state.equals("FAILED")) { // make a second attempt by use of ICMP 
			innan = new Date();
			try {
				state = "OKAY";

				if (OS.indexOf("win") >= 0) cmd = "ping -n 1 -l 8 -w "+tout+" " + host;   // Windows
				else if (OS.indexOf("nux") >= 0 || OS.indexOf("nix") >= 0) {
//					cmd = "ping -c 1 -W 5 " + host;      // Linux or Unix
					cmd = "ping -c 1 -W "+toutux+" "+ host;      // Linux or Unix
				}
				else cmd = "ping " + host;											  // 

				if (swShow)	System.out.println("-- Ping cmd: "+cmd);  
				
				if (!runCMD(cmd)) { 
					state = "FAILED";
				}
			}
			catch (Exception e) { 
				state = "FAILED"; 
				if (swShow)	System.out.println("-- Ping exeption state: "+state);  
				}
		}
		efter = new Date();
		delay = efter.getTime() - innan.getTime();
		delay++;    // add an extra millisecond to compensate for extremely fast connections  
		if (delay>=tout || !state.equals("OKAY")) delay = 0;   // a response delay over 5000ms is a failure or state is failed
		if (swShow)	System.out.println("-- Response time: "+delay+" ms" );
		if (swStat) {
			now = new Date();
//			String dat = new String("yyyy-MM-dd'T'HH:mm:ss");
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

		now = new Date();
		if (t_desc==null) t_desc=" ";
		if (host.length()>50) host=host.substring(0,40);
		host = host + tabbar.substring(0,40-host.length());
		
		if (swNegative) pn = "N";
		else pn = "P";

		if (state.equals("OKAY")) { 
			if (!swLoop) {
				if (swEcho) System.out.println(now+" ("+pn+") Connection successful (echo)      "+host+" "+t_desc );
				else        System.out.println(now+" ("+pn+") Connection successful (ping)      "+host+" "+t_desc );
			}
			return true; 
		}
		
		System.out.println(now+" ("+pn+") Connection failed (echo and ping) "+host+" "+t_desc );
		
		if (swTracert) {
			if (OS.indexOf("win") >= 0) cmd = "tracert -4 -d -h 20 -w 1000 " + host;  // Windows
			else cmd = "traceroute " + host;									  // 
			runCMD(cmd); 
		}
		
		return false; 
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		//		System.out.println(jm.open());
		jm.open();
		jmsg.setId(t_id+"-monIPAddr-"+host);
		//		System.out.println("-- id --"+t_id+"-monIPAddr-"+host);
		if ((STS && !swNegative) || (!STS && swNegative)) {
//			if (STS) jmsg.setRptsts("OK");
			if (swShow) System.out.println("("+pn+") Reported OK to Jvakt server --");
			jmsg.setRptsts("OK"); 
		}
		else {
			if (swShow) System.out.println("("+pn+") Reported ERR to Jvakt server --");
			if (status == null)	jmsg.setRptsts("ERR");
			else jmsg.setRptsts(status);
		}
		if (swNegative) t_desc="("+pn+") "+t_desc;
		jmsg.setBody(t_desc);
		jmsg.setType("R");
		jmsg.setAgent(agent);
		if (jm.sendMsg(jmsg));
		else                  System.out.println("-- Rpt Failed ! --");
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
			if (swShow) System.out.println(" jvport : " + jvport + "\n jvhost : "+jvhost) ;
		} catch (IOException ex) {
			System.out.println(ex);
			//			ex.printStackTrace();
		}
		try {
			inet = InetAddress.getLocalHost();
			if (swShow) System.out.println(" Inet self : "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

	}

	static boolean runCMD(String cmd) {

		boolean swError = false;
		boolean swGoon = false;
		int nuWait = 0;
		int exitVal;
		Process p;

		now = new Date();

		// execute the command if there is one.
		// default command handling
		//						System.out.println(now+" --- runCMD  -> " + cmd );
		try {
			exitVal = 0;
			
			p = Runtime.getRuntime().exec(cmd);
			p.getInputStream();

			InputStreamReader isr;
			isr = new InputStreamReader(p.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			isr = new InputStreamReader(p.getErrorStream());
			BufferedReader bre = new BufferedReader(isr);
			String line=null;

			// waits a number of milliseconds for command to end.
			nuWait = 0;
			swGoon = true;
			innan = new Date();
			while (nuWait < tout && swGoon && !swError) {
				swGoon = false;
				try { exitVal = p.exitValue(); } catch (Exception e) {swGoon = true;} ;
				if (swGoon) {
					Thread.currentThread();
					Thread.sleep(1);
					exitVal = 0;
					nuWait++;
				}
				else {
					if (swShow)	System.out.println("- Got exitval from ping: " + exitVal);
				}
			}
			if (swShow)	System.out.println("- Waited for ping (ms) -- " + nuWait);
			if (nuWait >= tout) {
				System.out.println(new Date() +" ** Timeout --: " + nuWait);
				swError = true;
				p.destroy(); System.out.println(new Date() + " **Destroy process...");
			}

			if (exitVal != 0) { 
				swError = true;
				if (swShow) System.out.println(new Date() +" ** Ping exitVal: " + exitVal +"   -Unsuccessfull cmd: "+ cmd);  
			}

			while ( (line = br.readLine()) != null) {
				if (line.toLowerCase().indexOf("destination host unreachable") >=0 ||
						line.toLowerCase().indexOf("ttl expired in transit") >=0 ||
						line.toLowerCase().indexOf("request timed out") >=0 ||
						line.toLowerCase().indexOf("could not find host") >=0 ||
						line.toLowerCase().indexOf("unknown host") >=0 
						) {
					state= "FAILED";
					swError = true;
					//					System.out.println(new Date() + " ** error in output ");
				}
				if (swShow || (swTracert && state.equals("FAILED"))) System.out.println(new Date() +" OUTPUT> " + line);
			}
			while ( (line = bre.readLine()) != null)
				System.out.println("ERROR> " + line);

			br.close();
			bre.close();

		}
		catch (Exception e) {
			swError = true;
			e.printStackTrace();
			if (swShow)	System.out.println(new Date() +" ** ping exeption (p)  ");
		}

		if (swError) {
			if (swShow) System.out.println(new Date() +" -Unsuccessfull cmd: "+ cmd);  
			return false;
		}
		return true;

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
