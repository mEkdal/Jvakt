package Jvakt;

/*
 * 2023-08-02 V.1 Michael Ekdal		New plugin to update a syslog server
 */

import java.io.*;
import java.net.*;
import java.util.*;

//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import org.graylog2.syslog4j.*;
import org.graylog2.syslog4j.util.SyslogUtility;


public class PlugSyslog {

	static String version = "PlugSyslog ";
//	static String database = "jVakt";
//	static String dbuser   = "jVakt";
//	static String dbpassword = "";
//	static String dbhost   = "localhost";
//	static String dbport   = "5433";
//	static String jvhost   = "localhost";
//	static String jvport   = "1956";
	static int syslogport ;

	static String row = "";
	static FileOutputStream fis;
	static OutputStreamWriter osw;
	static BufferedWriter logg;

	static String id = null;
	static String status = "OK";
	static String body = " ";
	static String agent = " ";
	static String type = "R";  // repeating
	static String prio = "30";  
	static String recid;  
	static boolean swInsert = false;  
	static boolean swDelete = false;  
	static boolean swLogg = true;  
	static boolean swRun = true;  
	static boolean swShow = true;  
	static boolean swLogged = false;  
	static boolean swClosed = false;  
	static boolean swExcludeAll = false;  

	static String sysloghost;
	static String prot = "udp";
	static String level = "WARN";

	static String config = null;
	static File configF;
	static File syslogPropF;

	static String[] etab; 
	static String[] ltab; 
	static String[] etabSplit; 
	static String[] ltabSplit; 
	static int ecount = 0;
	static int lcount = 0;

	public static void main(String[] args ) throws UnknownHostException, IOException, Exception, FileNotFoundException {

		version += getVersion()+".1";

		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("A plugin for Jvakt to act as a syslog client.");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n*INSERT \tIndicates it is an insert to the syslog server."+
					"\n*DELETE \tNot used."+
					"\n-prio   \tField from the Jvakt console table."+
					"\n-type   \tField from the Jvakt console table."+
					"\n-sts    \tField from the Jvakt console table." +
					"\n-body   \tField from the Jvakt console table." +
					"\n-agent  \tField from the Jvakt console table." +
					"\n-recid  \tField from the Jvakt console table. Used with the *DELETE function" +
					"\n        \tthe recid field is populated by the *INSERT and used in the *DELETE function" +
					"\n-log    \tWrite to specific file. like \"-log c:\\logg.txt\" " +
					"\n-show   \tShows more in the log file."
					);

			System.exit(4);
		}

		for (int i=0; i<args.length; i++) {
			//			System.out.println(args[i]);
			if (args[i].equalsIgnoreCase("-id")) id = args[++i];
			else if (args[i].equalsIgnoreCase("-prio")) prio = args[++i];
			else if (args[i].equalsIgnoreCase("-type")) type = args[++i];
			else if (args[i].equalsIgnoreCase("-sts")) status = args[++i];
			else if (args[i].equalsIgnoreCase("-body")) body = args[++i];
			else if (args[i].equalsIgnoreCase("-agent")) agent = args[++i];
			else if (args[i].equalsIgnoreCase("*INSERT")) swInsert = true;
			else if (args[i].equalsIgnoreCase("*DELETE")) swDelete = true;
			else if (args[i].equalsIgnoreCase("-recid")) recid = args[++i];
			else if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			else if (args[i].equalsIgnoreCase("-norun")) swRun = false;
		}		

		fis = new FileOutputStream("PlugSyslog.log", true);
		osw = new OutputStreamWriter(fis, "Cp850");
		logg = new BufferedWriter(osw);

		if (config == null ) {
			configF = new File("Jvakt.properties");
			syslogPropF = new File("PlugSyslog.properties");
		}
		else {
			configF = new File(config,"Jvakt.properties");
			syslogPropF = new File(config,"PlugSyslog.properties");
		}

		System.out.println("----------- Jvakt PlugSyslog "+new Date()+"  Version: "+version +"  -  config file: "+configF);
		logg.write("----------- Jvakt PlugSyslog "+new Date()+"  Version: "+version +"  -  config file: "+configF);
		logg.newLine();

		// Read the syslogIvantiSM.properties
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(syslogPropF);
			prop.load(input);
			sysloghost = prop.getProperty("host");
			syslogport = Integer.parseInt(prop.getProperty("port"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		if (swInsert) row += "*INSERT ";
		if (swDelete) row += "*DELETE ";
		row += " -id "+id;
		row += " -prio "+prio;
		row += " -type "+type;
		row += " -sts "+status;
		row += " -body "+body;
		row += " -agent "+agent;
		row += " -recid "+recid;
		row += " -config "+config;

		System.out.println("-- row: "+row);
		logg.write(row);
		logg.newLine(); 
		
		if (!swInsert) {
			System.out.println("-Only rows of type *INSERT is supported, exiting... ");
			logg.write(("-Only rows of type *INSERT is supported, exiting... "));
			logg.newLine(); 
			System.exit(4);
		}

		if (prio.compareTo("30") >= 0) level = "INFO";
		if (prio.compareTo("30") >= 0 && (status.compareToIgnoreCase("ERR")==0 || status.compareToIgnoreCase("TOut")==0 )) level = "WARN";
		if (prio.compareTo("30") <  0 ) level = "ERROR";
		if (prio.compareTo("30") <  0 && (status.compareToIgnoreCase("INFO")==0 || status.compareToIgnoreCase("TOut")==0 )) level = "WARN";
		
		if (getCsv()) {
			System.out.println("-Filter file found!");
			logg.write("-Filter file found!");
			logg.newLine();
		}
		else {
			logg.write("-No Filter file found");
			logg.newLine();
			System.out.println("-No Filter file found"); 
		}

		System.out.println("ecount "+ecount+" lcount "+lcount); 
		logg.write("ecount "+ecount+" lcount "+lcount);
		logg.newLine();
		
		// Checking if ID is to be excluded from creating an incident in Ivanti
		for ( int k = 0; k < ecount ; k++) {  
			etabSplit = etab[k].split("&");
			int eTabWarn= 0;
			for ( int j = 0; j < etabSplit.length ; j++) { 
				if (id.toUpperCase().indexOf(etabSplit[j]) >= 0) eTabWarn++;
			}
			if (eTabWarn == etabSplit.length) {
				logg.write("-- Filter Excluded :"+id);
				logg.newLine();
				System.out.println("-- Filter Excluded: "+id);
				System.exit(4);
			}
		}

		// Checking if incident is to be "Logged" 
		for ( int k = 0; k < lcount ; k++) {  
			ltabSplit = ltab[k].split("&");
			int lTabWarn= 0;
			for ( int j = 0; j < ltabSplit.length ; j++) { 
				if (id.toUpperCase().indexOf(ltabSplit[j]) >= 0) lTabWarn++;
			}
			if (lTabWarn == ltabSplit.length) {
				logg.write("-- Filter set "+id+" to Logged");
				logg.newLine();
				swLogged=true;
				swClosed=false;
				swExcludeAll=false;
				System.out.println("-- Filter set "+id+" to Logged");
			}
		}
		
		
		System.out.println("swInsert "+swInsert+" swExcludeAll "+swExcludeAll);
		if (swInsert && !swExcludeAll) {

			if (post2syslog()) {  

			}
			else {
				System.out.println(new Date()+" post2syslog failed");
			}
		} else {
			if (swExcludeAll) {
				System.out.println(new Date()+ " -  No INSERT done because of the excluding E;* line in the CSV file  ");
				logg.write(new Date()+ " - No INSERT done because of the excluding E;* line in the CSV file  ");
				logg.newLine();		
			}
		}

		logg.close();
	}

	public static boolean post2syslog() {
		// First set the default cookie manager.
		System.out.println("post2syslog start "+" prot: "+prot+"  "+Syslog.getVersion());

		if (!Syslog.exists(prot)) {
        	System.out.println("Protocol "+prot+" not supported");
        }

        SyslogIF syslog = Syslog.getInstance(prot);  
		System.out.println("gotten prot: "+syslog.getProtocol());
		SyslogConfigIF syslogConfig = syslog.getConfig();
		syslogConfig.setHost(sysloghost);
		syslogConfig.setPort(syslogport);
		syslogConfig.setFacility(SyslogUtility.getFacility("USER"));
		syslogConfig.setIdent("Jvakt");
		
//        int leveli = SyslogUtility.getLevel(level);
//        int facility = SyslogUtility.getFacility("USER");
//        System.out.println("level "+leveli);
//        System.out.println("facility "+facility);

        syslog.log(SyslogUtility.getLevel(level),id+" - "+prio+" - "+status+" - "+body);   // send the log string to syslof server 

//		syslog.flush();   // if TCP

		System.out.println("post2syslog end");

		return true; 
	}


	static Boolean getCsv() throws IOException {

		String s;
		String[] tab = new String[1000];
		etab = new String[1000];
		ltab = new String[1000];
		Boolean swFound=false;

		File dircsv = new File(".");
		if (config != null ) dircsv = new File(config);

		String sufcsv = ".csv";
		String poscsv = "PlugSyslog";

		DirFilter dfcsv = new DirFilter(sufcsv, poscsv);

		File[] listfcsv = dircsv.listFiles(dfcsv);

		System.out.println("-- Number of csv files found:"+ listfcsv.length);
		logg.write(new Date()+ "-- Number of csv files found: "+ listfcsv.length);
		logg.newLine();		

		for (int i = 0; i < listfcsv.length; i++) {

			System.out.println("-- Importing: "+listfcsv[i]+"\n");

			BufferedReader in = new BufferedReader(new FileReader(listfcsv[i]));

			while ((s = in.readLine()) != null) {
				swFound=true;  // csv file found
				if (s.length() == 0) continue; 
				if (s.startsWith("#")) continue; 
				//				System.out.println("-- Row: "+s);
				// splittar rad från fil
				tab = s.split(";" , 2); 
				if (tab[0].startsWith("E")) {
					if (tab[1].toUpperCase().startsWith("*")) swExcludeAll = true;
					etab[ecount++] = tab[1].toUpperCase();
				}
				if (tab[0].startsWith("L")) ltab[lcount++] = tab[1].toUpperCase();

			}
			in.close();
		}
		return swFound;
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


//	public static boolean post2syslogX() {
//		// First set the default cookie manager.
//		System.out.println("post2syslogX start ");
//		String[] argsX=new String[8];; 
//		argsX[0]="tcp";
//		argsX[1]="-h";
//		argsX[2]="ubuntu1";
//		argsX[3]="-p";
//		argsX[4]="514";
//		argsX[5]="-l";
//		argsX[6]="warn";
//		argsX[7]="test TCP warn #2";
//		try { 
//			SyslogMain.main(argsX);
//		} 
//		catch (java.lang.Exception ex) {
//			System.out.println("post2syslogX ex "+ex);
//		}
//		System.out.println("post2syslogX end");
//		return state; 
//	}


}