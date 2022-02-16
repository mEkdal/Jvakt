package Jvakt;

import java.net.InetAddress;
import java.util.Properties;
import java.io.*;
import java.util.*;
import java.text.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.codec.binary.Hex;



public class CheckLogs {

	static String state = "a";
	static String t_sys;
	static String t_id;
	static String t_ip;
	static String t_desc;
	static String t_desc_prev="";

	static String id;
	static BufferedReader in;
	static String aFile;

	static String jvhost = "localhost"; 
	static String jvport = "1956";
	static String jvtype = "R";
	static int port ;
	static InetAddress inet;
	static String version = "CheckLogs (2022-FEB-10)";
	static String agent = null;
	static boolean swSlut = false;
	static String charset = "UTF8";

	static String config = null;
	static File configF;
	static FileInputStream fis;

	static boolean swJvakt = false;
	
	static Boolean swUTF8BOM = false;
	static Boolean swUTF16BEBOM = false;
	static Boolean swUTF16LEBOM = false;
	static Boolean swCharset = false;
	static int currI;

	static DirFilter df;
	static File dir = null;
	static String suf = null;
//	static String pos = ".";
	static String pos = null;
	static File[] listf;
	static String[] etab; 
	static String[] tokay; 
	static String[] tmust;
	static String[] etabSplit; 
	static String[] tokaySplit; 
	static String[] tmustSplit;
	static int ecount = 0;
	static int tcount = 0;
	static int mcount = 0;

	static BufferedReader inokay;

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
		//		String prev_s = "";
		boolean swWarn;
		boolean swMust = true;
		boolean swDummy = false;
		File newnamn;
		File oldnamn;
		//		String sys = ".";
		//		String res = ".";
		//		String typ = ".";
		PrintStream ut;
		boolean swRename = false;
		boolean swPsav = false;
		boolean swCsav = false;

		System.out.println("--- "+version + " by Michael Ekdal Sweden.\n");
		
		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-dir")) dir = new File(args[++i]);
			if (args[i].equalsIgnoreCase("-suf")) suf = args[++i];
			if (args[i].equalsIgnoreCase("-pos")) pos = args[++i];
			if (args[i].equalsIgnoreCase("-id"))  id  = args[++i];
			if (args[i].equalsIgnoreCase("-ren")) swRename=true;
			if (args[i].equalsIgnoreCase("-psav")) swPsav=true;
			if (args[i].equalsIgnoreCase("-csav")) swCsav=true;
			if (args[i].equalsIgnoreCase("-jvakt")) swJvakt=true;
			if (args[i].equalsIgnoreCase("-jvtype")) jvtype  = args[++i];
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-charset")) {
				charset = args[++i];
				System.out.println("-- Will use charset "+charset);
				swCharset = true;
			}
		}

		if (args.length < 1) {
			System.out.println("\nThe parameters and their meaning are:\n"+
					"\n-dir    \tThe directory to scan, like \"-dir c:\\Temp\". Charset UTF8 is assumed. "+
					"\n-suf    \tThe suffix of the files you want to include in the scan, like \"-suf .log\" "+
					"\n-pos    \tAn optional string that must be contained in the file names. Optional." +
					"\n-csav   \tSaves the position of the scanned fil in the -config directory. Optional." +
					"\n-psav   \tSaves the position of the scanned fil in the logfiles directory. Optional." +
					"\n-ren    \tThe scanned files be renamed. Optional." +
					"\n\n--- the following switches is needed if Jvakt is to be used. Optional. ---" +
					"\n-jvakt  \tA switch to enable report to Jvakt. Default is no connection to Jvakt." +
					"\n-jvtype \tThe type of the Jvakt report. Optional.  The default is \"R\"" +
					"\n-id     \tUsed as identifier in the Jvakt monitoring system." +
					"\n-config \tThe directory where to find the Jvakt.properties file. like \"-config c:\\Temp\". Optional. Default is the current directory." +
					"\n-charset \tDefault is UTF8. It could be UTF-16, UTF-32, ASCII, ISO8859_1...");

			System.out.println("\n\n--- One or more of following files must be present in the current directory or the -config directory ---\n"+
					"\nCheckLogs.csv   \tOne file replacing the three following files."+
					"\n                \tLines starting with E; replaces the srch file."+
					"\n                \tLines starting with O; replaces the okay file."+
					"\n                \tLines starting with M; replaces the must file.\n"+
					"\nCheckLogs.srch  \t(deprecated) Strings considered errors if found in the log file. e.g. ORA-"+
					"\nCheckLogs.okay  \t(deprecated) Strings considered okay even when triggered by the CheckLogs.srch file. e.g. ORA-01013. May be empty." +
					"\nCheckLogs.must  \t(deprecated) Strings mandatory to be found in the log file. May be empty.\n"+
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

		getSetup();
		getCsv();

		if (mcount == 0 ) swMust = false;
		if (ecount == 0 ) {
			System.out.println("*****  CheckLogs is aborting! **  No error strings imported!  *****");
			System.exit(12);
		}
		if (swCsav && config == null ) {
			System.out.println("*****  CheckLogs is aborting! **  No -config provided!  *****");
			System.exit(12);
		}

		if (suf != null && pos != null) df = new DirFilter(suf, pos);
		else if (suf != null)           df = new DirFilter(suf);
		else if (pos != null)           df = new DirFilter(null, pos);

		
		if (suf != null || pos != null) listf = dir.listFiles(df);
		else listf = dir.listFiles();
		
		System.out.println(tdat+"-- Number of files to scan: "+ listf.length);

		for (int i = 0; i < listf.length; i++) {

			if (listf[i].isDirectory()) continue;
			
			if (swDummy) {
				getSetup();
				getCsv();
				swDummy = false;
			}

			if (!swCharset) {
				currI = i;
				checkFileForBOM();
			}
			
			//			System.out.println("\n  -- ecount: "+ecount);

			System.out.println("\n"+tdat+"-- Checking: "+listf[i]);
			oldnamn = new File(listf[i].toString());
			aFile   = oldnamn.getName();
			if (mcount > 0 ) swMust = true;

			if (swPsav || swCsav) {
				fis = new FileInputStream(oldnamn);
				//				in = new BufferedReader( new FileReader(oldnamn) );

				try{  // read last position if present.
					if (swPsav) inokay = new BufferedReader(new FileReader(listf[i]+".position"));
					else        inokay = new BufferedReader(new FileReader(configF+"/"+aFile+".position"));

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
				
				if (swPsav || swCsav) {
					position++;
					if ( position == 1 && !s.equals(strprev)) { posprev = 0; } // If it's a new logfile start from the beginning 
					if ( position == 1 ) { strprev = s; } // Store first row 
					if ( position <= posprev  ) continue; // read next line if scanned previously. 
				}

				// check if any scan string is present in the line.
				swWarn = false;
				for ( int k = 0; k < ecount ; k++) {
//					System.out.println("etab[k] " +etab[k]);
					if (etab[k].contains("-*dummy-entry*-")) break;
					etabSplit = etab[k].split("&");
//					System.out.println("etabSplit.length " +etabSplit.length);
					int eTabWarn= 0;
					for ( int j = 0; j < etabSplit.length ; j++) { 
//						System.out.println("etabSplit[j] "+j+" "+etabSplit[j]+ "  s "+s);
						if (s.toUpperCase().indexOf(etabSplit[j]) >= 0) { 
							eTabWarn++;
						}
					}
//					System.out.println("swWarn " +swWarn+"  eTabWarn " +eTabWarn+" etabSplit.length " +etabSplit.length);
					if (eTabWarn == etabSplit.length) {
						etab[k] = "-*dummy-entry*-";     // only warn on first hit
						swDummy = true;
						swWarn = true;
					}
				}

				 // reset warning flag if hit is okay.
				for ( int k = 0; k < tcount ; k++) { 
					tokaySplit = tokay[k].split("&");
					int tokayWarn= 0;
					for ( int j = 0; j < tokaySplit.length ; j++) { 
			        	if (s.toUpperCase().indexOf(tokaySplit[j]) >= 0) {
			        		tokayWarn++;
			    		}
					}
					if (tokayWarn == tokaySplit.length) {
						swWarn = false;
					}
				}
				
//								System.out.println("Mcount :  "+mcount);
				// raise warning if string are missing.
				for ( int k = 0; k < mcount ; k++) {  
//					System.out.println("Must-check :  "+s+"  "+tmust[k]);
					tmustSplit = tmust[k].split("&");
					int tmustWarn= 0;
//					System.out.println("tmustSplit.length " +tmustSplit.length+" tmustWarn:"+tmustWarn);
					for ( int j = 0; j < tmustSplit.length ; j++) { 
						if (s.toUpperCase().indexOf(tmustSplit[j]) >= 0) tmustWarn++;
					}
					if (tmustWarn == tmustSplit.length) {
						swMust = false;
					}
				}

				//				System.out.println(" swWarn = "+swWarn);
				if (!swWarn) continue;

				//				c = null;
				if (s.length() > 256) s = s.substring(0, 255);
				//				if (s.compareTo(prev_s) == 0)  continue;
				//				prev_s = s;
				errors++;
				t_desc = s;
				t_desc =aFile+": "+t_desc;
				if (t_desc.compareTo(t_desc_prev) !=0) {    // Lessen number of duplicate messages in Jvakt
					sendSTS(swWarn); 
					t_desc_prev = t_desc;
				} 
				else {
					System.out.println(" Message ignored:  "+t_desc);
				}

			}
			in.close();

			if (swPsav || swCsav) {
				if ( position < posprev  ) { position=0; System.out.println("Re-seting position!"); } // store position in file
				//				ut = new PrintStream(new FileOutputStream(new String(listf[i]+".position"), false));
				if (swPsav) ut = new PrintStream(new FileOutputStream(new String(listf[i]+".position"), false));
				else        ut = new PrintStream(new FileOutputStream(new String(configF+"/"+aFile+".position"), false));

				ut.println(position);
				ut.println(strprev);
				ut.close();
			}

			if (swMust ) {
				errors++;
				swWarn=true;
				t_desc = "Mandatory text strings are missing from the log file "+aFile+"!";
				sendSTS(swWarn);
			}
		}

		swSlut = true;
		if (errors == 0 ) {
			swWarn=false;

			if (listf.length == 0 ) {
				if (swMust) {
					errors++;
					swWarn=true;
					t_desc = " No logfiles found to scan! and missing must hits!";
				}
				else t_desc = " No logfiles found to scan!";
			} else t_desc = "No errors found"; 

			sendSTS(swWarn);
			//			System.out.println("# 02");
		}
		else      {
			swWarn=true;
			t_desc = errors + " errors found in the log file(s).";
			sendSTS(swWarn);
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
				jmsg.setId(id+"_info");
				if (!STS) {
					jmsg.setRptsts("INFO");
				}
				else {
					jmsg.setRptsts("ERR");
					//					jmsg.setId(id);
				}
				//				jmsg.setType(jvtype);
				jmsg.setType("I");
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

	static void getSetup()  {

		String s;
		// Importing error strings to search for.
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
		} catch (Exception e) {	
			System.out.println("\n-- Not found: CheckLogs.srch"); 
		}

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
		} catch (Exception e) {
			System.out.println("-- Not found: CheckLogs.okay"); 
		}

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
		} catch (Exception e) { 
			System.out.println("-- Not found: CheckLogs.must");
		}

	}

	static void checkFileForBOM() {
		
		byte[] bom = new byte[3];

		Path path = Paths.get(listf[currI].getPath());

		try {
			InputStream is = new FileInputStream(path.toFile()); 

			is.read(bom);
			
	          String content = new String(Hex.encodeHex(bom));
//	          System.out.println("content "+content);
	          if ("00".equalsIgnoreCase(content.substring(0, 2))) {
	        	  System.out.println("\n** Found out the input file probably is UCS-2 BE without BOM. Trying -charset UTF-16");
	        	  swUTF16BEBOM = true;
	        	  charset = "UTF-16";
	          }
	          if ("feff".equalsIgnoreCase(content.substring(0, 4))) {
	        	  System.out.println("\n** Found the input file to be UCS-2 BE BOM. Using -charset UTF-16");
	        	  swUTF16BEBOM = true;
	        	  charset = "UTF-16";
	          }
	          if ("00".equalsIgnoreCase(content.substring(2, 4))) {
	        	  System.out.println("\n** Found out the input file probably is UCS-2 LE without BOM. Trying -charset UTF-16");
	        	  swUTF16LEBOM = true;
	        	  charset = "UTF-16";
	          }
	          if ("fffe".equalsIgnoreCase(content.substring(0, 4))) {
	        	  System.out.println("\n** Found the input file to be UCS-2 LE BOM. Using -charset UTF-16");
	        	  swUTF16LEBOM = true;
	        	  charset = "UTF-16";
	          }
	          if ("efbbbf".equalsIgnoreCase(content.substring(0, 6))) {
	        	  System.out.println("\n** Found the input file to be UCS8 BOM. Using -charset UTF8");
	        	  swUTF8BOM = true;
	        	  charset = "UTF8";
	          }
			
			
			is.close();
		} catch (FileNotFoundException fnfe) {
			System.out.println("File not found "+configF.getPath()+"\n"+fnfe);
		}
		catch (IOException ioe) {
			System.out.println("IO error "+configF.getPath()+"\n"+ioe);
		}
	}

}
