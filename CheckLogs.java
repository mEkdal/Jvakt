package Jvakt;

import java.net.InetAddress;
import java.util.Properties;
import java.io.*;
import java.util.*;
import java.text.*;


public class CheckLogs {

	static String state = "a";
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;

	static String id;
	static BufferedReader in;
	static String aFile;

	static String jvhost   = "localhost"; 
	static String jvport   = "1956";
	static String jvtype = "R";
	static int port ;
	static InetAddress inet;
	static String version = "CheckLogs (2021-JUL-11)";
	static String agent = null;
	static boolean swSlut = false;
	static String charset = "UTF8";

	static String config = null;
	static File configF;
	static FileInputStream fis;

	static boolean swJvakt = false;

	static DirFilter df;
	static File dir = null;
	static String suf = null;
	static String pos = ".";
	static File[] listf;
	static String[] etab; 
	static String[] tokay; 
	static String[] tmust;
	static int ecount = 0;
	static int tcount = 0;
	static int mcount = 0;

	public static void main(String[] args) throws IOException {

		//		int j = 0;
		int errors = 0;
		int position=0;
		int posprev = 0;
		String strprev = null;
		String nyttnamn;
		String tdat;
		//		String c;
		String s;
		String prev_s = "";
		boolean swWarn;
		boolean swMust = true;
		File newnamn;
		File oldnamn;
		//		String sys = ".";
		//		String res = ".";
		//		String typ = ".";
		PrintStream ut;
		boolean swRename = false;
		boolean swPsav = false;



		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-dir")) dir = new File(args[++i]);
			if (args[i].equalsIgnoreCase("-suf")) suf = args[++i];
			if (args[i].equalsIgnoreCase("-pos")) pos = args[++i];
			if (args[i].equalsIgnoreCase("-id"))  id  = args[++i];
			if (args[i].equalsIgnoreCase("-ren")) swRename=true;
			if (args[i].equalsIgnoreCase("-psav")) swPsav=true;
			if (args[i].equalsIgnoreCase("-jvakt")) swJvakt=true;
			if (args[i].equalsIgnoreCase("-jvtype")) jvtype  = args[++i];
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-charset")) charset = args[++i];
		}

		System.out.println("--- "+version + " by Michael Ekdal Sweden.\n");
		if (args.length < 1) {

			System.out.println("\nThe parameters and their meaning are:\n"+
					"\n-dir    \tThe directory to scan, like \"-dir c:\\Temp\". UTF-8 is assumed. "+
					"\n-suf    \tThe suffix of the files you want to include in the scan, like \"-suf .log\" "+
					"\n-pos    \tAn optional string that must be contained in the file names." +
					"\n-psav   \tA switch that saves the position of the scanned fil until next scan. No rename. Optional." +
					"\n-ren    \tA switch that makes the scanned file be renamed instead of saving position. Optional." +
					"\n\n--- the following switches is needed if Jvakt is to be used ---" +
					"\n-jvakt  \tA switch to enable report to Jvakt. Default is no connection to Jvakt." +
					"\n-jvtype \tThe type of the Jvakt report. Optional.  The default is \"R\"" +
					"\n-id     \tUsed as identifier in the Jvakt monitoring system." +
					"\n-config \tThe directory where to find the Jvakt.properties file. like \"-config c:\\Temp\". Optional. Default is the current directory." +
					"\n-charset \tDefault is UTF8. It could be UTF-16, UTF-32, ASCII, ISO8859_1...");

			System.out.println("\n\n--- One or more of following files must be present in the current directory or the -config directory ---\n"+
					"\nCheckLogs.csv   \tOne file replacing the three following files."+
					"\n                \tLines starting with E; replaces the srch file."+
					"\n                \tLines starting with O; replaces the okay file."+
					"\n                \tLines starting with M; replaces the must file."+
					"\nCheckLogs.srch  \t(deprecated) Strings considered errors if found in the log file. e.g. ORA-"+
					"\nCheckLogs.okay  \t(deprecated) Strings considered okay even when triggered by the CheckLogs.srch file. e.g. ORA-01013. May be empty." +
					"\nCheckLogs.must  \t(deprecated) Strings mandatory to be found in the log file. May be empty."+
					"\n\nErrorlevel is set the number of errors found, else 0."
					);

			System.exit(4);
		}

		if (swJvakt) {
			if (config == null ) 	configF = new File("Jvakt.properties");
			else 					configF = new File(config,"Jvakt.properties");
			System.out.println("---- Jvakt: "+new Date()+"  Version: "+version);
			System.out.println("-config file: "+configF);
			getProps();
		}

		Date today;
		String pattern = new String("yyyy-MM-dd_HH-mm-ss");
		SimpleDateFormat formatter;

		formatter = new SimpleDateFormat(pattern);
		today = new Date();
		tdat = formatter.format(today);

		if (pos != null) df = new DirFilter(suf, pos);
		else             df = new DirFilter(suf);

		// Importing error strings to search for.
		BufferedReader inokay;
		ecount = 0;
		etab = new String[1000];
		try {
		if (config != null ) {
			configF = new File(config);
			inokay = new BufferedReader(new FileReader(configF.toString()+"/CheckLogs.srch"));
			s = configF.toString()+"/CheckLogs.srch";
		}
		else {
			inokay = new BufferedReader(new FileReader("CheckLogs.srch"));
			s = "CheckLogs.srch";
		}
		System.out.println("--- Importing strings considered error... File: "+s);
		while((s = inokay.readLine())!= null) {
			if (s.length() == 0) continue; 
			if (s.startsWith("#")) continue; 
			etab[ecount++] = s.toUpperCase();
//			System.out.println( etab[ecount - 1]);
		}          
		inokay.close();
		} catch (Exception e) { }

		// Importing strings approved despite hits.
		tcount = 0;
		tokay = new String[1000];
		try {
		if (config != null ) {
			inokay = new BufferedReader(new FileReader(configF.toString()+"/CheckLogs.okay"));
			s = configF.toString()+"/CheckLogs.okay";
		}
		else {
			inokay = new BufferedReader(new FileReader("CheckLogs.okay"));
			s = "CheckLogs.okay";
		}
		System.out.println("--- Importing strings considered okay... File: "+s);
		while((s = inokay.readLine())!= null) {
			if (s.length() == 0) continue; 
			if (s.startsWith("#")) continue; 
			tokay[tcount++] = s.toUpperCase();
//			System.out.println( tokay[tcount - 1]);
		}          
		inokay.close();
		} catch (Exception e) { }

		// Importing strings mandatory present to make the check to be okay.
		mcount = 0;
		tmust = new String[100];
		try {
		if (config != null ) {
			inokay = new BufferedReader(new FileReader(configF.toString()+"/CheckLogs.must"));
			s = configF.toString()+"/CheckLogs.must";
		}
		else {
			inokay = new BufferedReader(new FileReader("CheckLogs.must"));
			s = "CheckLogs.must";
		}
		System.out.println("--- Importing mandatory strings... File: "+s);
		while((s = inokay.readLine())!= null) {
			if (s.length() == 0) continue; 
			if (s.startsWith("#")) continue; 
			tmust[mcount++] = s.toUpperCase();
//			System.out.println( tmust[mcount - 1]);
		}          
		inokay.close();
		} catch (Exception e) { }
		
		getCsv();

		if (mcount == 0 ) swMust = false;
		if (ecount == 0 ) {
			System.out.println("*****  CheckLogs is aborting! **  No error strings imported!  *****");
			System.exit(12);
		}
		

//		for (int i = 0; i < ecount; i++) {
//			System.out.println("--- Ecount: "+etab[i]);
//		}
//		for (int i = 0; i < tcount; i++) {
//			System.out.println("--- Tcount: "+tokay[i]);
//		}
//		for (int i = 0; i < mcount; i++) {
//			System.out.println("--- Mcount: "+tmust[i]);
//		}

		listf = dir.listFiles(df); 
		System.out.println(tdat+"-- Number of files to scan: "+ listf.length);

		for (int i = 0; i < listf.length; i++) {

			System.out.println(tdat+"-- Checking: "+listf[i]);
			oldnamn = new File(listf[i].toString());
			aFile   = oldnamn.getName();

			if (swPsav) {
				fis = new FileInputStream(oldnamn);
				//				in = new BufferedReader( new FileReader(oldnamn) );

				try{  // read last position if present.
					inokay = new BufferedReader(new FileReader(listf[i]+".position"));
					if ((s = inokay.readLine())!= null) posprev = Integer.parseInt(s);
					else posprev=0;
					if ((s = inokay.readLine())!= null) strprev = s;
					else strprev=null;
					inokay.close();
				} catch (Exception e) {
					posprev = 0;
					strprev=null;
				}
				System.out.println(tdat+"-- posprev: "+ posprev);                    	   
			}
			else if (swRename) { // Create new file name if the file is supposed to be renamed.
				posprev = 0;
				strprev = null;
				nyttnamn = listf[i] + "." + tdat + ".sav";
				newnamn = new File(nyttnamn);
				System.out.println(tdat+"-- New name: "+ nyttnamn);
				if (!oldnamn.renameTo(newnamn)) {
					System.out.println(tdat+"-- Rename failed. Tries "+ oldnamn +" instead...");
					aFile   = oldnamn.getName();
					fis = new FileInputStream(oldnamn);
					//					in = new BufferedReader(new FileReader(oldnamn));
				}
				else {
					aFile   = newnamn.getName();
					fis = new FileInputStream(nyttnamn);
					//					in = new BufferedReader(new FileReader(nyttnamn));
				}
			}
			else { // open the original file name.
				fis = new FileInputStream(oldnamn);
				//				in = new BufferedReader( new FileReader(oldnamn) );
			}
			//			InputStreamReader isr = new InputStreamReader(fis, "UTF8"); 
			InputStreamReader isr = new InputStreamReader(fis, charset); 
			in = new BufferedReader(isr);

			position = 0;
			while ((s = in.readLine()) != null) {
				position++;
				if ( position == 1 && !s.equals(strprev)) { posprev = 0; } // If it's a new logfile start from the beginning 
				if ( position == 1 ) { strprev = s; } // Store first row 
				if ( position <= posprev  ) continue; // read next line if scanned previously. 

				swWarn = false;
				for ( int k = 0; k < ecount ; k++) {  // check if any scan string is present in the line.
					if (s.toUpperCase().indexOf(etab[k]) >= 0) { 
						swWarn = true;
						//						s = s.substring(s.toUpperCase().indexOf(etab[k]));
						etab[k] = "-*dummy-entry*-";
					}
				}

				for ( int k = 0; k < tcount ; k++) {  // reset warning flag if hit is okay.
					if (s.toUpperCase().indexOf(tokay[k]) >= 0) swWarn = false;
				}
//				System.out.println("Mcount :  "+mcount);
				for ( int k = 0; k < mcount ; k++) {  // raise warning if string are missing.
//					System.out.println("Must-check :  "+s+"  "+tmust[k]);
					if (s.toUpperCase().indexOf(tmust[k]) >= 0) swMust = false;
				}

				if (!swWarn) continue;

				//				c = null;
				if (s.length() > 256) s = s.substring(0, 255);
				if (s.compareTo(prev_s) == 0)  continue;
				prev_s = s;
				errors++;
				t_desc = s;
				t_desc =aFile+": "+t_desc;
				sendSTS(swWarn);
			}
			in.close();

			if (swPsav) {
				if ( position < posprev  ) { position=0; System.out.println("Re-seting position!"); } // store position in file
				ut = new PrintStream(new FileOutputStream(new String(listf[i]+".position"), false));
				ut.println(position);
				ut.println(strprev);
				ut.close();
			}
		}
//		System.out.println("swMust:  "+swMust);
//		if (swMust && aFile != null ) {
		if (swMust ) {
			errors++;
			swWarn=true;
			t_desc = "Mandatory text strings in the log file are missing!";
						sendSTS(swWarn);
//			System.out.println("# 01");
		}

		swSlut = true;
		if (errors == 0 ) {
			swWarn=false;

			if (listf.length == 0 ) {
				if (swMust) {
					errors++;
					swWarn=true;
					t_desc = " No logfiles found to scan! and Missing hits in must file!";
				}
				else t_desc = " No logfiles found to scan!";
			} else t_desc = "No errors found"; 

			sendSTS(swWarn);
//			System.out.println("# 02");
		}
		else      {
			swWarn=true;
//			t_desc = errors + " errors found in the log file(s).";
//			sendSTS(swWarn);
//			System.out.println("# 03");
		}

		System.out.println("-- Number of errors found: "+ errors);

		if (errors == 0) System.exit(0);
		else             System.exit(errors);
	}

	// sends status to the server
	static protected void sendSTS( boolean STS) throws IOException {
		if (!swSlut) { 
			if (jvtype.startsWith("I")) {
				if (t_desc.length() > 200) t_desc = t_desc.substring(0, 200);
				t_desc =aFile+": "+t_desc;
			}
			//			t_desc =t_desc+" : "+aFile;
		}

		if (swJvakt) {
			System.out.println("--- " + id + "  --  " + t_desc);
			System.out.println("--- Connecting to "+jvhost+":"+jvport);
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(jvhost, port);

			System.out.println(jm.open()); 

			jmsg.setId(id);
			if (!STS) {
				jmsg.setRptsts("OK");
			}
			else {
				if (jvtype.startsWith("I")) jmsg.setRptsts("INFO");
				else jmsg.setRptsts("ERR");
			}
			if (swSlut) { 
				if (!STS) jmsg.setRptsts("OK");
				else {
					if (jvtype.startsWith("I")) {
						if (t_desc.length() > 200) t_desc = t_desc.substring(0, 200);
						t_desc =aFile+": "+t_desc;
					}
				}
			} else {
				if (jvtype.startsWith("I")) {
					jmsg.setId(id+"_info");
					jmsg.setRptsts("INFO");
				}
				else {
					jmsg.setRptsts("ERR");
					jmsg.setId(id);
				}
				jmsg.setType(jvtype);
			}
			jmsg.setBody(t_desc);
			jmsg.setAgent(agent);
			if (jm.sendMsg(jmsg)) System.out.println("--- Rpt Delivered --  " + id + "  --  " + t_desc);
			else           		  System.out.println("--- Rpt Failed ---");
			jm.close();
		}
		else {
			System.out.println("---   " + t_desc);
		}
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
			System.out.println("getProps jvport: " + jvport + "    jvhost: "+jvhost) ;
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

	static void getCsv() throws IOException {

		String s;
		String[] tab = new String[1000];

		File dircsv = new File(".");
		if (config != null ) dircsv = new File(config);

		String sufcsv = ".csv";
		String poscsv = "CheckLogs";

		DirFilter dfcsv = new DirFilter(sufcsv, poscsv);

		File[] listfcsv = dircsv.listFiles(dfcsv);

		System.out.println("-- Number of csv files found:"+ listfcsv.length);

		for (int i = 0; i < listfcsv.length; i++) {

			System.out.println("-- Importing: "+listfcsv[i]+"\n");

			BufferedReader in = new BufferedReader(new FileReader(listfcsv[i]));

			while ((s = in.readLine()) != null) {
				if (s.length() == 0) continue; 
				if (s.startsWith("#")) continue; 

//				System.out.println("-- Row: "+s);
				// splittar rad från fil
				tab = s.split(";" , 2); 
				if (tab[0].startsWith("E")) etab[ecount++] = tab[1].toUpperCase();
				if (tab[0].startsWith("O")) tokay[tcount++] = tab[1].toUpperCase();
				if (tab[0].startsWith("M")) tmust[mcount++] = tab[1].toUpperCase();

			}
			in.close();
		}


	}


}
