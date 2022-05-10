package Jvakt;
import java.io.*;
import java.util.*;

//import org.icmp4j.IcmpPingRequest;
//import org.icmp4j.IcmpPingResponse;
//import org.icmp4j.IcmpPingUtil;

import java.net.*;
import java.text.SimpleDateFormat;

public class monIpPorts {

	static String state = "a";
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;
	static long ts;
	static boolean swFound;
	static boolean swSingle = false;
	static boolean swShow = false;
	static boolean swNegative = false;
	static String host;
	static String hostport;
	static InetAddress inet;
	static String version = "monIpPorts (2022-MAR-09)";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static int wport = 80 ;
	static String agent = null;
	static Socket cs = null;

	static String config = null;
	static File configF;

	static String 		tabbar = "                                                                                         ";
	static Date now;
	static String pn;

	static String stat = null;
	static FileOutputStream statF;
	static boolean swStat = false;
	static OutputStreamWriter osw;
	static BufferedWriter statCsv;

	public static void main(String[] args) throws UnknownHostException, IOException {

		String[] tab = new String [1];
		//		String tdat;
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
					"\n-run    \tTo actually update the status on the server side."+
					"\n-host   \tCheck a single host."+          
					"\n-port   \tThe port of a single host."+
					"\n-stat   \tThe dir of the statistics files."+
					"\n-show   \tShow the response from the server."
					);

			System.exit(4);
		}

		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			//			if (args[i].equalsIgnoreCase("-dir")) dir = new File(args[++i]);
			if (args[i].equalsIgnoreCase("-port")) wport = Integer.parseInt(args[++i]);
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-host")) { swSingle = true; host = args[++i]; }
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-stat")) stat = args[++i];
			if (args[i].equalsIgnoreCase("-show")) swShow = true;

		}
		if (config != null ) dir = new File(config);
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");

		if (stat != null ) {
			swStat = true;
			if (swSingle) {
				try {
					statF = new FileOutputStream(stat+"/monIpPorts-A-single-check.csv",true); // append
					osw = new OutputStreamWriter(statF, "Cp850");
					statCsv = new BufferedWriter(osw);			
				} catch (Exception ex) {
					System.out.println("-- Exeption when open the statistical file monIpPorts-A-single-check.csv !");
					ex.printStackTrace();
				}

			}
		}		
		
		System.out.println("\n"+new Date()+" *** Jvakt "+version+" ***\n");

		if (swShow) {
			if (swSingle) System.out.println(" *** a Single check is made ***"); 
			System.out.println(" config file: "+configF);
		}

		getProps();

		System.setProperty("java.net.preferIPv6Addresses", "false");

		if (swShow)	{
			System.out.println(" Config: "+config);
			System.out.println(" Dir   : "+dir);
			System.out.println(" Suf   : "+suf);
			System.out.println(" Pos   : "+pos);
			System.out.println(" Host  : "+host);
			System.out.println(" Port  : "+wport);
			System.out.println(" Config file   : "+configF);
			System.out.println(" Stat directory: "+stat+"\n");
		}

		if (swSingle) {
			checkIpPort();
		} else {

			//			if (pos != null) df = new DirFilter(suf, pos);
			//			else             df = new DirFilter(suf);

			df = new DirFilter(suf, pos);

			listf = dir.listFiles(df);

			if (swShow)	System.out.println("-- Numer of files found:"+ listf.length);

			for (int i = 0; i < listf.length; i++) {

				if (swShow)	System.out.println("-- Checking: "+listf[i]+"\n");

				BufferedReader in = new BufferedReader(new FileReader(listf[i]));

				while ((s = in.readLine()) != null) {
					if (s.length() == 0) continue; 
					if (s.startsWith("#")) continue; 

					// splittar rad från fil
					swNegative = false;
					tab = s.split(";" , 5);
					t_id = tab[0];
					host = tab[1];
					wport = Integer.parseInt(tab[2]);
					t_desc = tab[3];
					if (tab.length == 5) {
						if (tab[4].toLowerCase().trim().startsWith("neg")) swNegative = true;
					}
					if (swShow) System.out.println("t_id:"+t_id+" host:"+host+" t_desc:"+t_desc+" swNegative:"+swNegative);


					if (swStat) {
						try {
							statF = new FileOutputStream(stat+"/monIpPorts-"+t_id+".csv",true); // append
							swStat = true;
							osw = new OutputStreamWriter(statF, "Cp850");
							statCsv = new BufferedWriter(osw);
						} catch (Exception ex) {
							System.out.println("-- Exeption when open file monIpPorts-"+t_id+".csv");
							ex.printStackTrace();
						}
					}
					
					checkIpPort();

					if (swStat) {
						try{ statCsv.close(); } catch (Exception ex) {}
					}

					if (swRun)  {
						if (state.equals("OKAY")) 	sendSTS(true);
						else 						sendSTS(false);
					}

				}
				in.close();
			}
			//   	    		if (con != null) try { con.close(); } catch(Exception e) {}
		}
		if (swStat && swSingle) statCsv.close();
	}

	public static boolean checkIpPort() {
		Date innan;
		Date efter;
		long delay;

		// connect to port
		state = "OKAY";
		innan = new Date();
		try {
			//			System.out.print(new Date()+" --- Connection to: " + host + ":" + wport);
			cs = new Socket();
			cs.connect(new InetSocketAddress(host, wport), 5000);
			//			BufferedInputStream inFromClient = new BufferedInputStream(cs.getInputStream());
			//			BufferedOutputStream outToClient = new BufferedOutputStream(cs.getOutputStream());
			//cs = new Socket(host, port);
			//			outToClient.write(' ');
			//			outToClient.flush();
		} catch (Exception e) { System.out.println(new Date()+" *** Exeption - Connection failed:" + e); state = "FAILED";   }

		//		try { Thread.currentThread(); Thread.sleep(1000); } catch (Exception e) {} ;

		// disconnect from port
		try {
			if (cs != null) {
				cs.close();
			}
		}  catch (Exception e) { System.out.println("Close failed:" + e);   }

		efter = new Date();
		delay = efter.getTime() - innan.getTime();
		delay++;    // add an extra millisecond to compensate for extremely fast connections  
		if (delay>=5000 || !state.equals("OKAY")) {
			if (swShow)	System.out.println("-- Delay value set to 0" );
			delay = 0;   // a response delay over 5000ms is a failure
		}
		if (swShow)	System.out.println("-- Response time: "+delay+" ms" );
		if (swStat) {
//			if (swShow)	System.out.println("-- Response time: "+delay+"ms     State:"+state.equals("OKAY")+"  Innan:"+innan.getTime()+"  Efter:"+efter.getTime() );
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

		if (t_desc == null) t_desc = " ";
		hostport = host+":"+wport;
		if (hostport.length()>50) hostport=hostport.substring(0,50);
		hostport = hostport + tabbar.substring(0,50-hostport.length());

		if (swNegative) pn = "N";
		else pn = "P";

		if (state.equals("OKAY")) { System.out.println(new Date()+" ("+pn+") Connection succcessful - "+hostport+t_desc); return true; }
		System.out.println(new Date()+" ("+pn+") Connection failed      - "+hostport+t_desc);
		return false;
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) throws IOException {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		if (swShow)	System.out.println(jm.open());
		else jm.open();
		jmsg.setId(t_id+"-monIpPort-"+host+":"+wport);
		if ((STS && !swNegative) || (!STS && swNegative)) {
			if (swShow) System.out.println("("+pn+") Reported OK to Jvakt server --");
			jmsg.setRptsts("OK"); 
		}
		else {
			if (swShow) System.out.println("("+pn+") Reported ERR to Jvakt server --");
			jmsg.setRptsts("ERR");
		}
		if (swNegative) t_desc="("+pn+") "+t_desc;
		jmsg.setBody(t_desc);
		jmsg.setType("R");
		jmsg.setAgent(agent);
		if (jm.sendMsg(jmsg)) ;
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
			if (swShow)	System.out.println(" Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

	}

}
