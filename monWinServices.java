package Jvakt;
import java.io.*;
import java.util.*;
import java.net.*;

public class monWinServices {

	static String t_id;
	static String t_desc;
	static boolean swShow = false;
	static InetAddress inet;
	static String version = "monWinServices (2021-11-23)";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;
	static String agent = null;
	static Date now;

	static String config = null;
	static File configF;

	static String cmd;
	static String OS = System.getProperty("os.name").toLowerCase();

	static List<String> allList = new ArrayList<String>();
	static List<String> objList = new ArrayList<String>();
	static String[] tab;
	static String service_name;
	static String display_name;
	static String SCstate;
	static String SCtype;

	static boolean swError;
	static boolean swErrorTot;
	static boolean swExclude;
	static boolean swInclude;
	static boolean swDelayed = false;

	static String ex[] = new String[99];
	static String in[] = new String[99];

	public static void main(String[] args)  {

		boolean swRun = false;

		if (args.length < 1) {
			System.out.println("\n *** " +version + " *** \n");
			System.out.println(
					"\nmonWinServices monitor the Running status of Windows services with startup type \"Automatic\""+
					"\n    but not service with \"Delayed Start\" or \"Trigger Start\".\n"+
					"\nThe -ex and -in parameters are to be used if the default values don't apply."+
					"\n\nThe parameters and their meaning are:\n"+
					"\n-config  \tThe directory of the Jvakt.properties input file. Like: \"-dir c:\\Jvakt\" "+
					"\n-run     \tTo actually do the update the status on the Jvakt server side."+
					"\n-id      \tThe id used in Jvakt to managing the status." +
					"\n-in      \tInclude a \"Service Name\" to be monitored (not \"Display Name\"!). A part of the name is accepted." +
					"\n         \tUp to 99 -in is accepted. e.g. -in postgresql -in gp and so on.. " +
					"\n-ex      \tExclude a \"Service Name\" from being monitored (not \"Display Name!\". A part of the name is accepted." +
					"\n         \tUp to 99 -ex is accepted. e.g. -ex postgresql12 -ex gpupdate and so on.. "  
					);

			System.exit(4);
		}

		// reads command line arguments
		int j=0; int k=0;
		for ( int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-id")) t_id = args[++i];
			if (args[i].equalsIgnoreCase("-ex")) ex[j++] = args[++i];
			if (args[i].equalsIgnoreCase("-in")) in[k++] = args[++i];
		}

		now = new Date();
		System.out.println("\n"+now+" *** Jvakt "+version+" ***\n");
		if (swRun) {
			if (config == null ) 	configF = new File("Jvakt.properties");
			else 					configF = new File(config,"Jvakt.properties");
			if (swShow)	System.out.println(" config file: "+configF);
			getProps();
		}

		for ( int i = 0; i <= j-1; i++) {
			System.out.println("exclude service: " +ex[i]);
		}

		for ( int i = 0; i <= k-1; i++) {
			System.out.println("include service: " +in[i]);
		}

		now = new Date();

		// checkin the status of all services and store the result in allList. 
		runCMD("sc query state=all",allList);
		String s1;
		String s2;
		service_name = " ";
		display_name = " ";
		swErrorTot = false;

		// analyze the rows in allList to find names and status of the services   
		Iterator<String> allIte = allList.iterator();
		while (allIte.hasNext()) {
			s1 = allIte.next();

			if (s1.contains("SERVICE_NAME")) {
				swExclude = false;
				swInclude = false;
				tab = s1.split(":" , 2);
				service_name  = tab[1].trim();
				for ( int i = 0; i <= k-1; i++) {
					if (service_name.toLowerCase().contains(in[i].toLowerCase())) swInclude = true;
				}
				for ( int i = 0; i <= j-1; i++) {
					if (service_name.toLowerCase().contains(ex[i].toLowerCase())) {
						swExclude = true;
						swInclude = false;
					}
				}
				continue;
			}
			if (swExclude) continue;

			if (s1.contains("DISPLAY_NAME")) {
				tab = s1.split(":" , 2);
				display_name  = tab[1].trim();
				continue;
			}

			if (s1.contains(" STATE")) {
				tab = s1.split(":" , 2);
				tab = tab[1].trim().split(" " , 2);
				SCstate  = tab[1].trim();
				continue;
			}

			if (s1.contains(" TYPE")) {
				tab = s1.split(":" , 2);
				tab = tab[1].trim().split(" " , 2);
				SCtype  = tab[1].trim();
				continue;
			}

			if (!s1.contains(" WAIT_HINT ")) continue;  // the WAIT_HINT indicates the last info regarding one service.
			if (SCstate.contains("STOPPED")) swError = true;
			else 							 swError = false;

			Iterator<String> objIte;
			// for every service the \"sc qc\" query is executed. If it's a AUTO_START service the status is further investigated
			if (swError && !swInclude) { 
				swError = false;     
				runCMD("sc qc "+service_name,objList);
				objIte = objList.iterator();
				while (objIte.hasNext()) {
					s2=objIte.next(); 
					if ( s2.contains("AUTO_START") && !s2.contains("DELAYED") ) { 
						System.out.println("\n** service_name> "+service_name );
						System.out.println(" display_name> "+display_name );
						System.out.println(" state> "+SCstate );
						//					System.out.println(" type> "+SCtype );
						System.out.println(" sc qc> "+s2 );
						swError = true;      // this service should had been started (if not a trigged service, checked later)
						break;
					}
				}
				objList.clear();
			}

			if (swError && !swInclude) {  // if the service should had been started, we now check if it is a triggered service 
				runCMD("sc qtriggerinfo "+service_name,objList);
				objIte = objList.iterator();
				while (objIte.hasNext()) {
					s2=objIte.next(); 
					if (s2.contains("SERVICE_NAME")) {  // the SERVICE_NAME indicates that it's a triggered service 
						swError=false;                  // thus we turn off the swError switch
						System.out.println(" Trigger Start! Service is ignored!" );
						break;
					}
				}
				objList.clear();
			}

			if (swError) {
				t_desc = "Service \""+service_name+"\" ("+display_name+") is NOT running";
				if (swRun) sendSTS(swError); // sends an ERR to the Jvakt server
				swErrorTot=true;
			}

		}

		if (swRun && !swErrorTot) {
			t_desc = "All elegible services are running";
			sendSTS(swErrorTot);  // sends a OK to the Jvakt server
		}
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) {
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		//		System.out.println(jm.open());
		jm.open();
		jmsg.setId("monWinServices-"+t_id);
		if (STS) jmsg.setRptsts("ERR");
		else jmsg.setRptsts("OK");

		jmsg.setBody(t_desc);
		jmsg.setType("R");
		jmsg.setAgent(agent);
		System.out.println("To Jvakt>  monWinServices-"+t_id+"  "+jmsg.getRptsts()+" "+jmsg.getBody());
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

	static boolean runCMD(String cmd, List<String> list) {

		boolean swError = false;
		Process p;

		now = new Date();

		// execute the command if there is one.
		// default command handling
		//		System.out.println(now+" --- runCMD  -> " + cmd );
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.getInputStream();
			//			System.out.println(now+" Efter runCMD  -> " + cmd );

			InputStreamReader isr;
			isr = new InputStreamReader(p.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			isr = new InputStreamReader(p.getErrorStream());
			BufferedReader bre = new BufferedReader(isr);
			String line=null;

			while ( (line = br.readLine()) != null) {
				list.add(line);
			}

			while ( (line = bre.readLine()) != null) {
				System.out.println("ERROR> " + line);
			}

			br.close();
			bre.close();

		}
		catch (Exception e) {
			swError = true;
			e.printStackTrace();
			System.out.println(new Date() +" ** exeption (p)  ");
		}

		if (swError) {
			if (swShow) System.out.println(new Date() +" -Unsuccessfull cmd: "+ cmd);  
			return false;
		}
		return true;

	}

}
